package org.examplee.plague.pale;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.ChunkPos;
import org.examplee.plague.PlagueConfig;
import org.examplee.plague.leper.LeperData;
import org.examplee.plague.util.PosPack;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Spread budgets, chunk indexing, zone effects, great wards, admin purge. */
public class PaleTick {
    private static final Map<ServerLevel, Double> CARRY = new HashMap<>();
    private static final Map<ServerLevel, ArrayDeque<ChunkPos>> INDEX_QUEUE = new HashMap<>();
    private static final Map<UUID, ArrayDeque<ChunkPos>> PURGES = new HashMap<>();
    private static final Map<UUID, Long> PURGE_CLEANED = new HashMap<>();

    public static long totalAttempts, totalInfected, totalProtected, totalCleansed;
    private static final int[] PER_SEC = new int[60];
    private static int secIdx = 0;
    private static long lastSec = -1;

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(PaleTick::tick);
        ServerChunkEvents.CHUNK_LOAD.register((level, chunk) -> {
            if (PlagueConfig.INSTANCE.pale.running) {
                INDEX_QUEUE.computeIfAbsent(level, k -> new ArrayDeque<>()).add(chunk.getPos());
            }
        });
    }

    public static void enqueueAllLoaded(ServerLevel level) {
        // Indexing happens lazily via chunk events + spread; nothing needed here.
    }

    public static void startPurge(ServerPlayer p) {
        ServerLevel level = p.serverLevel();
        int ccx = p.blockPosition().getX() >> 4, ccz = p.blockPosition().getZ() >> 4;
        int r = PlagueConfig.INSTANCE.pale.purgeRadiusChunks;
        ArrayDeque<ChunkPos> q = new ArrayDeque<>();
        for (int dz = -r; dz <= r; dz++) {
            for (int dx = -r; dx <= r; dx++) q.add(new ChunkPos(ccx + dx, ccz + dz));
        }
        PURGES.put(p.getUUID(), q);
        PURGE_CLEANED.put(p.getUUID(), 0L);
    }

    public static boolean hasPurge(UUID id) { return PURGES.containsKey(id); }

    public static int infectedLastMinute() {
        int sum = 0;
        for (int v : PER_SEC) sum += v;
        return sum;
    }

    private static void rollSecond() {
        long sec = System.currentTimeMillis() / 1000L;
        if (lastSec == -1) { lastSec = sec; return; }
        long diff = sec - lastSec;
        if (diff > 0) {
            int steps = (int) Math.min(diff, 60L);
            for (int i = 0; i < steps; i++) {
                secIdx = (secIdx + 1) % 60;
                PER_SEC[secIdx] = 0;
            }
            lastSec = sec;
        }
    }

    public static void addCleansed(int n) { totalCleansed += Math.max(0, n); }
    public static void addInfected(int n) {
        if (n > 0) { totalInfected += n; PER_SEC[secIdx] += n; }
    }

    private static void tick(ServerLevel level) {
        rollSecond();
        var c = PlagueConfig.INSTANCE.pale;
        long time = level.getGameTime();

        if (c.running) {
            spreadTick(level, c);
            indexTick(level, c);
            purgeTick(level, c);
        }
        if (time % Math.max(1, c.effectsPeriodTicks) == 0) effectsTick(level, c);
        if (time % Math.max(1, c.stepPeriodTicks) == 0) stepTick(level, c);
        long wardPeriod = Math.max(1, c.greatWardCleanIntervalSec) * 20L;
        if (time % wardPeriod == 0) greatWardTick(level, c);
    }

    private static void spreadTick(ServerLevel level, PlagueConfig.Pale c) {
        int loaded = level.getChunkSource().getLoadedChunksCount();
        if (loaded <= 0) return;
        PaleWorldData data = PaleWorldData.get(level);
        if (data.sourceCount() == 0) return;
        double want = loaded * (PlagueConfig.effectiveRatePerSecPerChunk() / 20.0) + CARRY.getOrDefault(level, 0.0);
        int attempts = (int) want;
        CARRY.put(level, want - attempts);
        if (attempts <= 0) return;

        double t = (Math.max(1, Math.min(5000, c.speedPerChunk)) - 1.0) / 4999.0;
        double turbo = Math.pow(t, 8.0);
        int cap = (int) Math.round(c.maxAttemptsPerTickPerWorld + (c.turboMaxPerWorld - c.maxAttemptsPerTickPerWorld) * turbo);
        if (data.sourceCount() > 5000 && c.speedPerChunk > 2500) attempts = (int) Math.ceil(attempts * 1.2);
        attempts = Math.min(attempts, cap);
        if (attempts <= 0) return;

        RandomSource rnd = level.random;
        PaleEngine engine = PaleEngine.get();
        for (int i = 0; i < attempts; i++) {
            totalAttempts++;
            BlockPos src = data.randomLiveSource(level, rnd);
            if (src == null) break;
            if (engine.isWardProtected(level, src.getX(), src.getY(), src.getZ())) {
                totalProtected++;
                continue;
            }
            if (engine.trySpreadFromSource(level, src)) addInfected(1);
        }
        if (rnd.nextInt(200) == 0) data.compact();
    }

    private static void indexTick(ServerLevel level, PlagueConfig.Pale c) {
        if (c.indexChunksPerTickPerWorld <= 0) return;
        ArrayDeque<ChunkPos> q = INDEX_QUEUE.get(level);
        if (q == null || q.isEmpty()) return;
        int n = 0;
        while (n < c.indexChunksPerTickPerWorld && !q.isEmpty()) {
            ChunkPos pos = q.pollFirst();
            if (pos == null) break;
            if (level.hasChunk(pos.x, pos.z)) {
                PaleEngine.get().indexChunk(level, pos.x, pos.z);
                n++;
            }
        }
    }

    private static void purgeTick(ServerLevel level, PlagueConfig.Pale c) {
        if (PURGES.isEmpty()) return;
        var it = PURGES.entrySet().iterator();
        while (it.hasNext()) {
            var e = it.next();
            int n = 0;
            while (n < c.purgeChunksPerTick && !e.getValue().isEmpty()) {
                ChunkPos pos = e.getValue().pollFirst();
                if (pos == null) break;
                if (!level.hasChunk(pos.x, pos.z)) {
                    if (c.purgeOnlyLoaded) continue;
                    level.getChunk(pos.x, pos.z);
                }
                int cleaned = PaleEngine.get().purgeChunk(level, pos.x, pos.z, c.purgeDepth);
                if (cleaned > 0) {
                    PURGE_CLEANED.put(e.getKey(), PURGE_CLEANED.get(e.getKey()) + cleaned);
                    addCleansed(cleaned);
                }
                n++;
            }
            if (e.getValue().isEmpty()) {
                it.remove();
                ServerPlayer p = level.getServer().getPlayerList().getPlayer(e.getKey());
                if (p != null) {
                    p.displayClientMessage(net.minecraft.network.chat.Component.literal(
                            "[Pale] Purge done. Removed: " + PURGE_CLEANED.getOrDefault(e.getKey(), 0L)), false);
                }
                PURGE_CLEANED.remove(e.getKey());
            }
        }
    }

    private static void effectsTick(ServerLevel level, PlagueConfig.Pale c) {
        if (!c.effectsEnabled || !c.stagesEnabled) return;
        int dur = c.effectsPeriodTicks + 40;
        for (ServerPlayer p : level.players()) {
            if (p.hasPermissions(2) || LeperData.get(p).leper()) continue;
            int stage = PaleEngine.get().getStage(level, p.blockPosition().getX() >> 4, p.blockPosition().getZ() >> 4);
            if (stage < c.effectsMinStage) continue;
            if (stage >= 2) p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, dur, c.slowAmp2, true, false, true));
            if (stage >= 3) p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, dur, c.weakAmp3, true, false, true));
            if (stage >= 4) p.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, dur, c.fatigueAmp4, true, false, true));
            if (stage >= 5 && c.darknessStage5) p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, dur, 0, true, false, true));
        }
    }

    private static void stepTick(ServerLevel level, PlagueConfig.Pale c) {
        for (ServerPlayer p : level.players()) {
            if (p.isCreative() || p.isSpectator()) continue;
            if (!PaleEngine.isInfectedBlock(level.getBlockState(p.blockPosition().below()))) continue;
            int dur = c.stepDurationTicks;
            if (LeperData.get(p).leper()) {
                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, dur, 0, true, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, dur, 0, true, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, dur, 0, true, false, true));
            } else if (!p.hasPermissions(2)) {
                p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, dur, 0, true, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, dur, 0, true, false, true));
                p.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, dur, 0, true, false, true));
            }
        }
    }

    private static void greatWardTick(ServerLevel level, PlagueConfig.Pale c) {
        PaleWorldData data = PaleWorldData.get(level);
        if (data.greatWards().isEmpty()) return;
        boolean dirty = false;
        for (Map.Entry<Long, Integer> e : new HashMap<>(data.greatWards()).entrySet()) {
            int charge = e.getValue();
            if (charge <= 0) continue;
            BlockPos ward = PosPack.unpack(e.getKey());
            if (!level.hasChunk(ward.getX() >> 4, ward.getZ() >> 4)) continue;
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
                    ward.getX() + 0.5, ward.getY() + 1.2, ward.getZ() + 0.5, 5, 0.3, 0.4, 0.3, 0.01);
            BlockPos target = findInfected(level, ward, c.greatWardRadius);
            if (target != null) {
                int cleaned = PaleEngine.get().cleanse(level, target, 0);
                if (cleaned > 0) {
                    int next = Math.max(0, charge - c.greatWardCleanIntervalSec);
                    data.greatWards().put(e.getKey(), next);
                    dirty = true;
                    addCleansed(cleaned);
                    level.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,
                            target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5, 10, 0.3, 0.3, 0.3, 0.02);
                    if (next == 0) {
                        level.playSound(null, ward, net.minecraft.sounds.SoundEvents.BEACON_DEACTIVATE,
                                net.minecraft.sounds.SoundSource.BLOCKS, 0.8f, 0.9f);
                    }
                }
            }
        }
        if (dirty) data.setDirty();
    }

    private static BlockPos findInfected(ServerLevel level, BlockPos ward, int radius) {
        RandomSource rnd = level.random;
        int yMin = Math.max(level.getMinY(), ward.getY() - 24);
        int yMax = Math.min(level.getMaxY() - 1, ward.getY() + 24);
        for (int i = 0; i < 30; i++) {
            double angle = rnd.nextDouble() * Math.PI * 2.0;
            double dist = Math.sqrt(rnd.nextDouble()) * radius;
            int tx = ward.getX() + (int) Math.round(Math.cos(angle) * dist);
            int tz = ward.getZ() + (int) Math.round(Math.sin(angle) * dist);
            if (!level.hasChunk(tx >> 4, tz >> 4)) continue;
            for (int y = yMax; y >= yMin; y--) {
                BlockPos p = new BlockPos(tx, y, tz);
                if (PaleEngine.isInfectedBlock(level.getBlockState(p))) return p;
            }
        }
        return null;
    }
}
