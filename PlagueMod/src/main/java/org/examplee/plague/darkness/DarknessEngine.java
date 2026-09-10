package org.examplee.plague.darkness;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.examplee.plague.PlagueConfig;
import org.examplee.plague.leper.LeperData;
import org.examplee.plague.pale.PaleEngine;
import org.examplee.plague.registry.ModBlocks;
import org.examplee.plague.util.PosPack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Black blight spread + branches + water drinking + step effects. Singleton via {@link #get()}. */
public class DarknessEngine {
    private static final DarknessEngine INSTANCE = new DarknessEngine();
    public static DarknessEngine get() { return INSTANCE; }

    private static final int BRANCH_MAX = 30;
    private static final int BRANCH_ARMS_FROM = 10;
    private static final Set<Block> UNINFECTABLE = Set.of(
            Blocks.OBSIDIAN, Blocks.BEDROCK, Blocks.CRYING_OBSIDIAN,
            Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.BARREL, Blocks.ENDER_CHEST,
            Blocks.SHULKER_BOX, Blocks.WHITE_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.MAGENTA_SHULKER_BOX,
            Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX, Blocks.LIME_SHULKER_BOX, Blocks.PINK_SHULKER_BOX,
            Blocks.GRAY_SHULKER_BOX, Blocks.LIGHT_GRAY_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX,
            Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.RED_SHULKER_BOX,
            Blocks.BLACK_SHULKER_BOX, Blocks.HOPPER, Blocks.DISPENSER, Blocks.DROPPER,
            Blocks.FURNACE, Blocks.BLAST_FURNACE, Blocks.SMOKER, Blocks.BREWING_STAND,
            Blocks.REDSTONE_WIRE, Blocks.REDSTONE_TORCH, Blocks.REDSTONE_WALL_TORCH, Blocks.REDSTONE_BLOCK,
            Blocks.REDSTONE_LAMP, Blocks.REPEATER, Blocks.COMPARATOR,
            Blocks.PISTON, Blocks.STICKY_PISTON, Blocks.OBSERVER);

    private double infectAcc = 0.0;
    private double growthAcc = 0.0;
    private final Map<UUID, Long> damageCd = new HashMap<>();
    private final Map<UUID, Long> pickupCd = new HashMap<>();

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(DarknessEngine.get()::tick);
    }

    public boolean isDarknessKin(ServerPlayer p) {
        LeperData d = LeperData.get(p);
        return d.leper() || d.stage() >= 3;
    }

    public int totalBlocks(MinecraftServer server) {
        int n = 0;
        for (ServerLevel level : server.getAllLevels()) n += DarknessWorldData.get(level).size();
        return n;
    }

    // ---- rates ----
    private static double speedMult(int s) {
        double m = Math.pow(1.072, Math.min(s, 100));
        if (s > 100) m *= Math.pow(Math.min(s, 1000) / 100.0, 2.0);
        return m;
    }

    private double infectRate(MinecraftServer server) {
        double mult = speedMult(PlagueConfig.INSTANCE.dark.infectSpeed);
        return 0.4 * mult / 20.0 + Math.sqrt(totalBlocks(server)) * mult / 40.0;
    }

    private double growthRate(MinecraftServer server) {
        double mult = speedMult(PlagueConfig.INSTANCE.dark.growthSpeed);
        return 0.08 * mult / 20.0 + Math.sqrt(totalBlocks(server)) * mult / 600.0;
    }

    private void tick(ServerLevel level) {
        if (!PlagueConfig.INSTANCE.dark.enabled) return;
        // Spread once per tick from the overworld tick to keep a single budget.
        if (level != level.getServer().overworld()) {
            if (level.getGameTime() % 10 == 0) stepTick(level);
            return;
        }
        MinecraftServer server = level.getServer();
        infectAcc += infectRate(server);
        int attempts = (int) Math.min(20000.0, Math.floor(infectAcc));
        infectAcc -= attempts;
        if (infectAcc > 20000.0) infectAcc = 20000.0;
        for (int i = 0; i < attempts; i++) trySpreadOnce(server);

        growthAcc += growthRate(server);
        int growth = (int) Math.min(2000.0, Math.floor(growthAcc));
        growthAcc -= growth;
        if (growthAcc > 2000.0) growthAcc = 2000.0;
        for (int i = 0; i < growth; i++) tryGrowBranchOnce(server);

        if (level.getGameTime() % 10 == 0) {
            for (ServerLevel sl : server.getAllLevels()) stepTick(sl);
        }
        if (level.getGameTime() % 20 == 0) {
            for (ServerLevel sl : server.getAllLevels()) pickupTick(sl);
        }
    }

    private ServerLevel randomLevel(MinecraftServer server, RandomSource rnd) {
        List<ServerLevel> levels = new ArrayList<>();
        server.getAllLevels().forEach(levels::add);
        return levels.isEmpty() ? null : levels.get(rnd.nextInt(levels.size()));
    }

    private void trySpreadOnce(MinecraftServer server) {
        ServerLevel level = randomLevel(server, server.overworld().random);
        if (level == null) return;
        RandomSource rnd = level.random;
        BlockPos src = DarknessWorldData.get(level).pickRandom(level, rnd);
        if (src == null) return;

        int dx, dy, dz;
        if (rnd.nextInt(10) < 3) {
            dx = rnd.nextInt(7) - 3; dz = rnd.nextInt(7) - 3; dy = rnd.nextInt(5) - 2;
        } else {
            dx = rnd.nextInt(3) - 1; dz = rnd.nextInt(3) - 1; dy = rnd.nextInt(3) - 1;
            if (dx == 0 && dy == 0 && dz == 0) dx = 1;
        }
        BlockPos target = src.offset(dx, dy, dz);
        for (int i = 0; i < 4 && level.getBlockState(target).isAir(); i++) target = target.below();

        BlockState ts = level.getBlockState(target);
        if (ts.is(Blocks.WATER) || ts.is(Blocks.KELP) || ts.is(Blocks.KELP_PLANT)
                || ts.is(Blocks.SEAGRASS) || ts.is(Blocks.TALL_SEAGRASS)) {
            if (rnd.nextInt(4) == 0) {
                level.setBlock(target, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                if (rnd.nextInt(6) == 0) {
                    level.sendParticles(ParticleTypes.SMOKE, target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5, 4, 0.25, 0.25, 0.25, 0.01);
                    level.playSound(null, target, SoundEvents.SPONGE_ABSORB, SoundSource.BLOCKS, 0.4f, 0.6f);
                }
            }
            return;
        }
        // Wards only slow darkness x10, never stop it.
        if (PaleEngine.get().isWardProtected(level, target.getX(), target.getY(), target.getZ()) && rnd.nextInt(10) != 0) return;
        tryInfectTarget(level, target);
    }

    private boolean tryInfectTarget(ServerLevel level, BlockPos pos) {
        if (!level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) return false;
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.is(ModBlocks.DARKNESS_BLOCK)) return false;
        if (UNINFECTABLE.contains(state.getBlock())) return false;
        String name = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        boolean infectable;
        if (PlagueConfig.INSTANCE.dark.infectAll) {
            if (state.is(Blocks.WATER) || state.is(Blocks.LAVA)) return false;
            infectable = true;
        } else {
            infectable = state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT)
                    || state.is(Blocks.ROOTED_DIRT) || state.is(Blocks.PODZOL) || state.is(Blocks.MYCELIUM)
                    || state.is(Blocks.MUD) || state.is(Blocks.MOSS_BLOCK) || state.is(Blocks.DIRT_PATH)
                    || name.contains("log") || name.contains("wood") || name.contains("leaves")
                    || PaleEngine.isInfectedBlock(state);
        }
        if (!infectable) return false;
        boolean tree = name.contains("log") || name.contains("wood");
        level.setBlock(pos, ModBlocks.DARKNESS_BLOCK.defaultBlockState(), Block.UPDATE_CLIENTS);
        DarknessWorldData.get(level).register(pos);
        if (tree) {
            BlockPos cur = pos.above();
            for (int i = 0; i < 12; i++) {
                BlockState s = level.getBlockState(cur);
                String n = BuiltInRegistries.BLOCK.getKey(s.getBlock()).getPath();
                if (s.is(ModBlocks.DARKNESS_BLOCK) || (!n.contains("log") && !n.contains("wood") && !n.contains("leaves"))) break;
                level.setBlock(cur, ModBlocks.DARKNESS_BLOCK.defaultBlockState(), Block.UPDATE_CLIENTS);
                DarknessWorldData.get(level).register(cur);
                cur = cur.above();
            }
        }
        if (level.random.nextInt(20) == 0) {
            level.sendParticles(ParticleTypes.SCULK_SOUL, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 3, 0.3, 0.2, 0.3, 0.01);
            level.playSound(null, pos, SoundEvents.SCULK_SPREAD, SoundSource.BLOCKS, 0.4f, 0.7f);
        }
        return true;
    }

    private void tryGrowBranchOnce(MinecraftServer server) {
        ServerLevel level = randomLevel(server, server.overworld().random);
        if (level == null) return;
        BlockPos base = DarknessWorldData.get(level).pickRandom(level, level.random);
        if (base == null) return;
        int h = base.getX() * 73856093 ^ base.getZ() * 19349663;
        if (Math.floorMod(h, 25) != 0) return;

        int height = 0;
        BlockPos below = base.below();
        DarknessWorldData data = DarknessWorldData.get(level);
        while (height < BRANCH_MAX + 2 && data.isDarkness(below)) {
            height++;
            below = below.below();
        }
        if (height >= BRANCH_MAX) return;
        BlockPos above = base.above();
        if (!level.getBlockState(above).isAir()) return;
        level.setBlock(above, ModBlocks.DARKNESS_BLOCK.defaultBlockState(), Block.UPDATE_CLIENTS);
        data.register(above);

        int idx = height + 2;
        if (idx >= BRANCH_ARMS_FROM && (idx - BRANCH_ARMS_FROM) % 2 == 0) {
            Direction[] sides = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
            BlockPos arm = above.relative(sides[level.random.nextInt(4)]);
            if (level.getBlockState(arm).isAir()) {
                level.setBlock(arm, ModBlocks.DARKNESS_BLOCK.defaultBlockState(), Block.UPDATE_CLIENTS);
                data.register(arm);
            }
        }
        level.sendParticles(ParticleTypes.SCULK_CHARGE_POP, above.getX() + 0.5, above.getY() + 0.5, above.getZ() + 0.5, 5, 0.2, 0.3, 0.2, 0.02);
        level.playSound(null, above, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.BLOCKS, 0.5f, 0.6f);
    }

    // ---- cleanse ----
    public int cleanse(ServerLevel level, BlockPos center, int radius) {
        DarknessWorldData data = DarknessWorldData.get(level);
        if (data.size() == 0) return 0;
        int cleaned = 0;
        int rSq = radius * radius;
        for (int x = center.getX() - radius; x <= center.getX() + radius; x++) {
            for (int y = Math.max(level.getMinY(), center.getY() - radius); y <= Math.min(level.getMaxY() - 1, center.getY() + radius); y++) {
                for (int z = center.getZ() - radius; z <= center.getZ() + radius; z++) {
                    int ddx = x - center.getX(), ddy = y - center.getY(), ddz = z - center.getZ();
                    if (ddx * ddx + ddy * ddy + ddz * ddz > rSq) continue;
                    BlockPos p = new BlockPos(x, y, z);
                    if (!data.isDarkness(p)) continue;
                    restore(level, p, false);
                    data.unregister(p);
                    cleaned++;
                }
            }
        }
        if (cleaned > 0) {
            level.sendParticles(ParticleTypes.END_ROD, center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5,
                    20, radius * 0.5, 1.0, radius * 0.5, 0.02);
        }
        return cleaned;
    }

    public int cleanseChunks(ServerLevel level, int ccx, int ccz, int radiusChunks) {
        DarknessWorldData data = DarknessWorldData.get(level);
        if (data.size() == 0) return 0;
        List<Long> targets = new ArrayList<>();
        for (long k : data.all()) {
            int bx = PosPack.x(k), bz = PosPack.z(k);
            if (Math.abs((bx >> 4) - ccx) <= radiusChunks && Math.abs((bz >> 4) - ccz) <= radiusChunks) targets.add(k);
        }
        if (targets.isEmpty()) return 0;
        Set<Long> targetSet = new HashSet<>(targets);
        targets.sort((a, b) -> Integer.compare(PosPack.y(b), PosPack.y(a)));
        int cleaned = 0;
        for (long k : targets) {
            BlockPos p = PosPack.unpack(k);
            boolean belowDark = targetSet.contains(PosPack.pack(p.getX(), p.getY() - 1, p.getZ()));
            restore(level, p, belowDark);
            data.unregister(p);
            cleaned++;
        }
        return cleaned;
    }

    private void restore(ServerLevel level, BlockPos p, boolean forceAir) {
        if (!level.getBlockState(p).is(ModBlocks.DARKNESS_BLOCK)) return;
        boolean solidBelow = level.getBlockState(p.below()).isSolid();
        if (forceAir || !solidBelow) {
            level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        } else {
            boolean exposed = level.getBlockState(p.above()).isAir();
            level.setBlock(p, (exposed ? Blocks.GRASS_BLOCK : Blocks.DIRT).defaultBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    // ---- effects ----
    private void stepTick(ServerLevel level) {
        long now = System.currentTimeMillis();
        for (ServerPlayer p : level.players()) {
            if (p.isDeadOrDying()) continue;
            BlockPos feet = p.blockPosition();
            BlockPos under = feet.below();
            DarknessWorldData data = DarknessWorldData.get(level);
            boolean standing = (level.getBlockState(under).is(ModBlocks.DARKNESS_BLOCK) && data.isDarkness(under))
                    || (level.getBlockState(feet).is(ModBlocks.DARKNESS_BLOCK) && data.isDarkness(feet));
            if (!standing) continue;
            if (isDarknessKin(p)) {
                p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, 0, true, false, true));
                continue;
            }
            if (p.hasPermissions(2)) continue;
            Long last = damageCd.get(p.getUUID());
            if (last == null || now - last >= 1000L) {
                damageCd.put(p.getUUID(), now);
                p.hurtServer(level, level.damageSources().wither(), 1.6f);
            }
            p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0, true, false, true));
            p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 600, 0, true, false, true));
            p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 1, true, false, true));
            p.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 0, true, false, true));
            p.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 300, 0, true, false, true));
        }
    }

    private void pickupTick(ServerLevel level) {
        long now = System.currentTimeMillis();
        for (ServerPlayer p : level.players()) {
            boolean holds = false;
            for (ItemStack stack : p.getInventory().items) {
                if (isForeignDark(stack, p)) { holds = true; break; }
            }
            if (!holds && isForeignDark(p.getOffhandItem(), p)) holds = true;
            if (!holds) continue;
            if (isDarknessKin(p) || p.hasPermissions(2)) continue;
            Long last = pickupCd.get(p.getUUID());
            if (last != null && now - last < 15000L) continue;
            pickupCd.put(p.getUUID(), now);
            p.addEffect(new MobEffectInstance(MobEffects.POISON, 160, 0, true, false, true));
            p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 300, 0, true, false, true));
            p.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 300, 0, true, false, true));
            p.displayClientMessage(Component.translatable("msg.plague.dark_burns").withStyle(ChatFormatting.DARK_GRAY), true);
        }
    }

    private boolean isForeignDark(ItemStack stack, ServerPlayer p) {
        if (stack.isEmpty() || !(stack.getItem() instanceof DarkBlockItem)) return false;
        String owner = DarkBlockItem.ownerOf(stack);
        return !owner.isEmpty() && !owner.equals(p.getUUID().toString());
    }

    public record MapResult(byte[] levels, int max, int total) {}

    public MapResult mapLevels(ServerLevel level, int ccx, int ccz, int radius) {
        DarknessWorldData data = DarknessWorldData.get(level);
        int size = radius * 2 + 1;
        Map<Long, Integer> counts = new HashMap<>();
        for (long k : data.all()) {
            int chx = PosPack.x(k) >> 4, chz = PosPack.z(k) >> 4;
            if (Math.abs(chx - ccx) > radius || Math.abs(chz - ccz) > radius) continue;
            long ck = PosPack.packChunk(chx, chz);
            counts.put(ck, counts.getOrDefault(ck, 0) + 1);
        }
        int max = 0;
        for (int v : counts.values()) max = Math.max(max, v);
        byte[] levels = new byte[size * size];
        for (int dz = -radius; dz <= radius; dz++) {
            for (int dx = -radius; dx <= radius; dx++) {
                int n = counts.getOrDefault(PosPack.packChunk(ccx + dx, ccz + dz), 0);
                levels[(dz + radius) * size + (dx + radius)] =
                        (byte) (max <= 0 ? 0 : Math.max(0, Math.min(9, Math.round(n / (double) max * 9.0))));
            }
        }
        return new MapResult(levels, max, data.size());
    }
}
