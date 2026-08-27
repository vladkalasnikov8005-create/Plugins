package org.examplee.palePlugin.tasks;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.examplee.palePlugin.PalePlugin;
import org.examplee.palePlugin.store.SourceStore;

public final class SpreadController {
    private final org.examplee.palePlugin.PalePlugin plugin;
    private boolean running;
    private org.bukkit.scheduler.BukkitTask spreadTask;
    private org.bukkit.scheduler.BukkitTask indexTask;
    private org.bukkit.scheduler.BukkitTask statsSecondTask;
    private org.bukkit.scheduler.BukkitTask playerEffectsTask;
    private org.bukkit.scheduler.BukkitTask stepEffectsTask;
    private final java.util.Map carryAttemptsByWorld;
    private final java.util.Map indexQueueByWorld;
    private final java.util.Map loadedChunkCountByWorld;
    private long totalAttempts;
    private long totalInfected;
    private long totalSkippedProtected;
    private long totalCleansed;
    private final int[] infectedPerSec;
    private int infectedSecIndex;
    private long lastEpochSecond;
    private final java.util.Random rnd;
    private static java.lang.String PERM_ADMIN;
    private static java.lang.String PERM_EFFECT_IMMUNE;
    private final org.bukkit.NamespacedKey KEY_LEPER_CLASS;

    public SpreadController(org.examplee.palePlugin.PalePlugin plugin) {
        super();
        this.running = 0;
        this.carryAttemptsByWorld = new java.util.HashMap();
        this.indexQueueByWorld = new java.util.HashMap();
        this.loadedChunkCountByWorld = new java.util.HashMap();
        this.totalAttempts = 0L;
        this.totalInfected = 0L;
        this.totalSkippedProtected = 0L;
        this.totalCleansed = 0L;
        this.infectedPerSec = new int[60];
        this.infectedSecIndex = 0;
        this.lastEpochSecond = -1L;
        this.rnd = new java.util.Random();
        this.KEY_LEPER_CLASS = org.bukkit.NamespacedKey.fromString("leperclass:class_leper");
        this.plugin = plugin;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean val) {
        if (val) {
            startRunning();
        } else {
            stopRunning();
        }
    }

    public void startAlwaysOnTasks() {
        startStatsSecondTask();
        startPlayerEffectsTask();
        startStepEffectsTask();
    }

    public void stopAllTasks() {
        stopRunning();
        stopStatsSecondTask();
        stopPlayerEffectsTask();
        stopStepEffectsTask();
    }

    public void startRunning() {
        if (running) {
            return;
        }
        this.running = 1;
        refreshLoadedChunkCounts();
        enqueueLoadedChunksForIndex();
        startIndexTask();
        startSpreadTask();
    }

    public void stopRunning() {
        this.running = 0;
        stopSpreadTask();
        stopIndexTask();
    }

    public void addCleansed(int n) {
        this.totalCleansed = totalCleansed + (long) java.lang.Math.max(0, n);
    }

    public void addInfected(int n) {
        if (n <= 0) {
            return;
        }
        this.totalInfected = totalInfected + (long) n;
        infectedPerSec[infectedSecIndex] = infectedPerSec[infectedSecIndex] + n;
    }

    public void refreshLoadedChunkCounts() {
        loadedChunkCountByWorld.clear();
        java.util.Iterator local1 = org.bukkit.Bukkit.getWorlds().iterator();
        if (local1.hasNext()) {
            org.bukkit.World w = (org.bukkit.World) local1.next();
            loadedChunkCountByWorld.put(w.getUID(), java.lang.Integer.valueOf(w.getLoadedChunks().length));
            /* continue */
        }
    }

    public void onChunkLoad(org.bukkit.World w, int cx, int cz) {
        java.util.UUID wid = w.getUID();
        loadedChunkCountByWorld.put(wid, java.lang.Integer.valueOf(((java.lang.Integer) loadedChunkCountByWorld.getOrDefault(wid, java.lang.Integer.valueOf(0))).intValue() + 1));
        if (running) {
            ((java.util.ArrayDeque) indexQueueByWorld.computeIfAbsent(wid, (java.util.UUID p0) -> org.examplee.palePlugin.tasks.SpreadController.lambda_onChunkLoad_0(p0))).addLast(new org.examplee.palePlugin.tasks.SpreadController$ChunkPos(cx, cz));
        }
    }

    public void onChunkUnload(org.bukkit.World w) {
        java.util.UUID wid = w.getUID();
        loadedChunkCountByWorld.put(wid, java.lang.Integer.valueOf(java.lang.Math.max(0, ((java.lang.Integer) loadedChunkCountByWorld.getOrDefault(wid, java.lang.Integer.valueOf(0))).intValue() - 1)));
    }

    private void enqueueLoadedChunksForIndex() {
        java.util.Iterator local1 = org.bukkit.Bukkit.getWorlds().iterator();
        if (local1.hasNext()) {
            org.bukkit.World world = (org.bukkit.World) local1.next();
            java.util.UUID wid = world.getUID();
            java.util.ArrayDeque q = (java.util.ArrayDeque) indexQueueByWorld.computeIfAbsent(wid, (java.util.UUID p0) -> org.examplee.palePlugin.tasks.SpreadController.lambda_enqueueLoadedChunksForIndex_1(p0));
            org.bukkit.Chunk[] local5 = world.getLoadedChunks();
            int local6 = local5.length;
            int local7 = 0;
            if (local7 < local6) {
                org.bukkit.Chunk ch = local5[local7];
                q.addLast(new org.examplee.palePlugin.tasks.SpreadController$ChunkPos(ch.getX(), ch.getZ()));
                local7++;
                /* continue */
            }
            /* continue */
        }
    }

    private void startSpreadTask() {
        stopSpreadTask();
        this.spreadTask = org.bukkit.Bukkit.getScheduler().runTaskTimer(plugin, () -> lambda_startSpreadTask_2(), 1L, 1L);
    }

    private void stopSpreadTask() {
        if (spreadTask != null) {
            spreadTask.cancel();
            this.spreadTask = null;
        }
    }

    private void startIndexTask() {
        stopIndexTask();
        if (plugin.cfg.indexChunksPerTickPerWorld <= 0) {
            return;
        }
        this.indexTask = org.bukkit.Bukkit.getScheduler().runTaskTimer(plugin, () -> lambda_startIndexTask_3(), 1L, 1L);
    }

    private void stopIndexTask() {
        if (indexTask != null) {
            indexTask.cancel();
            this.indexTask = null;
        }
    }

    private void startStatsSecondTask() {
        stopStatsSecondTask();
        this.statsSecondTask = org.bukkit.Bukkit.getScheduler().runTaskTimer(plugin, this::rollStatsSecond, 20L, 20L);
    }

    private void stopStatsSecondTask() {
        if (statsSecondTask != null) {
            statsSecondTask.cancel();
            this.statsSecondTask = null;
        }
    }

    private void rollStatsSecond() {
        long sec = java.lang.System.currentTimeMillis() / 1000L;
        if (Long.compare(lastEpochSecond, -1L) != 0) {
            long diff = sec - lastEpochSecond;
            if (Long.compare(diff, 0L) <= 0) {
                return;
            }
        }
        this.lastEpochSecond = sec;
        diff = sec - lastEpochSecond;
        if (Long.compare(diff, 0L) <= 0) {
            return;
        }
        int steps = (int) java.lang.Math.min(diff, 60L);
        int i = 0;
        if (i < steps) {
            this.infectedSecIndex = (infectedSecIndex + 1) % 60;
            infectedPerSec[infectedSecIndex] = 0;
            i++;
            /* continue */
        }
        this.lastEpochSecond = sec;
    }

    private int infectedLastMinute() {
        int sum = 0;
        int[] local2 = infectedPerSec;
        int local3 = local2.length;
        int local4 = 0;
        if (local4 < local3) {
            int v = local2[local4];
            sum = sum + v;
            local4++;
            /* continue */
        }
        return sum;
    }

    private void startPlayerEffectsTask() {
        stopPlayerEffectsTask();
        this.playerEffectsTask = org.bukkit.Bukkit.getScheduler().runTaskTimer(plugin, () -> lambda_startPlayerEffectsTask_4(), 20L, (long) plugin.cfg.effectsCheckPeriodTicks);
    }

    private void stopPlayerEffectsTask() {
        if (playerEffectsTask != null) {
            playerEffectsTask.cancel();
            this.playerEffectsTask = null;
        }
    }

    private void startStepEffectsTask() {
        stopStepEffectsTask();
        this.stepEffectsTask = org.bukkit.Bukkit.getScheduler().runTaskTimer(plugin, () -> lambda_startStepEffectsTask_5(), 10L, (long) plugin.cfg.stepEffectsCheckPeriodTicks);
    }

    private void stopStepEffectsTask() {
        if (stepEffectsTask != null) {
            stepEffectsTask.cancel();
            this.stepEffectsTask = null;
        }
    }

    private boolean isLeper(org.bukkit.entity.Player p) {
        if (KEY_LEPER_CLASS == null) {
            return false;
        }
        java.lang.Byte v = (java.lang.Byte) p.getPersistentDataContainer().get(KEY_LEPER_CLASS, org.bukkit.persistence.PersistentDataType.BYTE);
        if (v != null) {
            if (v.byteValue() == 1) {
            } else {
            }
        } else {
        }
        return false;
    }

    public void sendInfo(org.bukkit.command.CommandSender sender) {
        java.lang.Object[] tmp1 = new java.lang.Object[1];
        tmp1[0] = java.lang.Double.valueOf(plugin.cfg.effectiveAttemptsPerSecondPerChunk());
        sender.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.GRAY) + "[Pale] running=" + running + " speed=" + plugin.cfg.speedPerChunk + " rate≈" + java.lang.String.format(java.util.Locale.US, "%.6f", tmp1));
        sender.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.GRAY) + "[Pale] infected total=" + totalInfected + " attempts total=" + totalAttempts + " skippedProtected=" + totalSkippedProtected + " cleansed total=" + totalCleansed + " infected/min=" + infectedLastMinute());
        java.util.Iterator local2 = org.bukkit.Bukkit.getWorlds().iterator();
        if (local2.hasNext()) {
            org.bukkit.World w = (org.bukkit.World) local2.next();
            java.util.UUID wid = w.getUID();
            int loaded = ((java.lang.Integer) loadedChunkCountByWorld.getOrDefault(wid, java.lang.Integer.valueOf(0))).intValue();
            int sources = plugin.engine.sources(w).size();
            int wards = plugin.engine.wards(w).size();
            int biomeCells = plugin.engine.biomes(w).size();
            sender.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.DARK_GRAY) + "world=" + w.getName() + " loaded=" + loaded + " sources=" + sources + " wards=" + wards + " biomeCells=" + biomeCells);
            /* continue */
        }
    }

    private void lambda_startStepEffectsTask_5() {
        java.util.Iterator local1 = org.bukkit.Bukkit.getOnlinePlayers().iterator();
        if (local1.hasNext()) {
            org.bukkit.entity.Player p = (org.bukkit.entity.Player) local1.next();
            org.bukkit.GameMode gm = p.getGameMode();
            if (gm == org.bukkit.GameMode.SPECTATOR) { /* continue */ }
            if (gm == org.bukkit.GameMode.CREATIVE) {
                /* continue */
            }
            org.bukkit.block.Block under = p.getLocation().getBlock().getRelative(org.bukkit.block.BlockFace.DOWN);
            if (!(plugin.engine.infectedTypes().contains(under.getType()))) {
                /* continue */
            }
            int dur = plugin.cfg.stepEffectsDurationTicks;
            if (isLeper(p)) {
                p.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SPEED, dur, 0, 1, 0, 1));
                p.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.REGENERATION, dur, 0, 1, 0, 1));
                p.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.RESISTANCE, dur, 0, 1, 0, 1));
                /* continue */
            }
            if (p.hasPermission("pale.admin")) { /* continue */ }
            if (p.hasPermission("pale.effect.immune")) {
                /* continue */
            }
            p.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SLOWNESS, dur, 0, 1, 0, 1));
            p.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.WEAKNESS, dur, 0, 1, 0, 1));
            p.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.MINING_FATIGUE, dur, 0, 1, 0, 1));
            /* continue */
        }
    }

    private void lambda_startPlayerEffectsTask_4() {
        if (!(plugin.cfg.effectsEnabled)) {
            return;
        }
        if (!(plugin.cfg.stagesEnabled)) {
            return;
        }
        java.util.Iterator local1 = org.bukkit.Bukkit.getOnlinePlayers().iterator();
        if (local1.hasNext()) {
            org.bukkit.entity.Player p = (org.bukkit.entity.Player) local1.next();
            if (p.hasPermission("pale.effect.immune")) /* continue */
            if (p.hasPermission("pale.admin")) { /* continue */ }
            if (isLeper(p)) {
                /* continue */
            }
            org.bukkit.World w = p.getWorld();
            int cx = p.getLocation().getBlockX() >> 4;
            int cz = p.getLocation().getBlockZ() >> 4;
            int stage = plugin.engine.getChunkStage(w, cx, cz);
            if (stage < plugin.cfg.effectsMinStage) {
                /* continue */
            }
            int dur = plugin.cfg.effectsCheckPeriodTicks + 40;
            if (stage >= 2) {
                p.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SLOWNESS, dur, plugin.cfg.effectsSlownessAmpStage2, 1, 0, 1));
            }
            if (stage >= 3) {
                p.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.WEAKNESS, dur, plugin.cfg.effectsWeaknessAmpStage3, 1, 0, 1));
            }
            if (stage >= 4) {
                p.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.MINING_FATIGUE, dur, plugin.cfg.effectsMiningFatigueAmpStage4, 1, 0, 1));
            }
            if (stage >= 5) {
                if (plugin.cfg.effectsDarknessStage5) {
                    p.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.DARKNESS, dur, 0, 1, 0, 1));
                }
            }
            /* continue */
        }
    }

    private void lambda_startIndexTask_3() {
        if (!(running)) {
            return;
        }
        java.util.Iterator local1 = org.bukkit.Bukkit.getWorlds().iterator();
        if (local1.hasNext()) {
            org.bukkit.World world = (org.bukkit.World) local1.next();
            java.util.UUID wid = world.getUID();
            java.util.ArrayDeque q = (java.util.ArrayDeque) indexQueueByWorld.get(wid);
            if (q == null) { /* continue */ }
            if (q.isEmpty()) {
                /* continue */
            }
            int processed = 0;
            if (processed < plugin.cfg.indexChunksPerTickPerWorld) {
                if (!(q.isEmpty())) {
                    org.examplee.palePlugin.tasks.SpreadController$ChunkPos pos = (org.examplee.palePlugin.tasks.SpreadController$ChunkPos) q.pollFirst();
                    if (pos == null) {
                    } else {
                        if (!(world.isChunkLoaded(pos.x, pos.z))) {
                            /* continue */
                        }
                        plugin.engine.indexChunkSurface(world, pos.x, pos.z);
                        processed++;
                        /* continue */
                    }
                }
            }
            /* continue */
        }
    }

    private void lambda_startSpreadTask_2() {
        if (!(running)) {
            return;
        }
        rollStatsSecond();
        double s = (double) java.lang.Math.max(1, java.lang.Math.min(5000, plugin.cfg.speedPerChunk));
        double tSpeed = (s - 1.0) / 4999.0;
        double turboT = java.lang.Math.pow(tSpeed, 8.0);
        int effectiveGlobalCap = (int) java.lang.Math.round(org.examplee.palePlugin.tasks.SpreadController$PaleConfig.lerp((double) plugin.cfg.maxAttemptsPerTickGlobal, (double) plugin.cfg.turboMaxAttemptsPerTickGlobal, turboT));
        int effectivePerWorldCap = (int) java.lang.Math.round(org.examplee.palePlugin.tasks.SpreadController$PaleConfig.lerp((double) plugin.cfg.maxAttemptsPerTickPerWorld, (double) plugin.cfg.turboMaxAttemptsPerTickPerWorld, turboT));
        int globalBudget = effectiveGlobalCap;
        double perSecPerChunk = plugin.cfg.effectiveAttemptsPerSecondPerChunk();
        java.util.Iterator local12 = org.bukkit.Bukkit.getWorlds().iterator();
        if (local12.hasNext()) {
            org.bukkit.World world = (org.bukkit.World) local12.next();
            if (globalBudget <= 0) {
            } else {
                java.util.UUID wid = world.getUID();
                int loadedChunks = ((java.lang.Integer) loadedChunkCountByWorld.getOrDefault(wid, java.lang.Integer.valueOf(0))).intValue();
                if (loadedChunks <= 0) {
                    /* continue */
                }
                org.examplee.palePlugin.store.SourceStore store = plugin.engine.sources(world);
                if (store.size() == 0) {
                    /* continue */
                }
                double carry = ((java.lang.Double) carryAttemptsByWorld.getOrDefault(wid, java.lang.Double.valueOf(0.0))).doubleValue();
                double want = (double) loadedChunks * (perSecPerChunk / 20.0) + carry;
                int attempts = (int) want;
                carryAttemptsByWorld.put(wid, java.lang.Double.valueOf(want - (double) attempts));
                if (attempts <= 0) {
                    /* continue */
                }
                if (store.size() > 5000) {
                    if (plugin.cfg.speedPerChunk > 2500) {
                        attempts = (int) java.lang.Math.ceil((double) attempts * 1.2);
                    }
                }
                attempts = java.lang.Math.min(attempts, effectivePerWorldCap);
                attempts = java.lang.Math.min(attempts, globalBudget);
                if (attempts <= 0) {
                    /* continue */
                }
                int perWorldRemain = java.lang.Math.max(0, effectivePerWorldCap - attempts);
                int globalRemain = java.lang.Math.max(0, globalBudget - attempts);
                if (plugin.cfg.speedPerChunk > 3000) {
                } else {
                }
                int burstMax = 0;
                int extraBurstBudget = java.lang.Math.min(burstMax, java.lang.Math.min(perWorldRemain, globalRemain));
                int extraSpent = 0;
                int i = 0;
                if (i < attempts) {
                    this.totalAttempts = totalAttempts + 1L;
                    org.bukkit.block.Block source = store.getRandomLiveSource(world, rnd, plugin.engine.infectedTypes());
                    if (source == null) {
                    } else {
                        if (plugin.engine.wards(world).isProtected(source.getX(), source.getY(), source.getZ(), plugin.cfg.wardRadius)) {
                            this.totalSkippedProtected = totalSkippedProtected + 1L;
                        } else {
                            if (plugin.engine.trySpreadFromSource(source)) {
                                addInfected(1);
                                if (plugin.cfg.speedPerChunk > 3000) {
                                    if (extraBurstBudget > 0) {
                                        if (rnd.nextInt(100) < 35) {
                                            extraBurstBudget += 255;
                                            extraSpent++;
                                            this.totalAttempts = totalAttempts + 1L;
                                            if (plugin.engine.trySpreadFromSource(source)) {
                                                addInfected(1);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        i++;
                        /* continue */
                    }
                }
                globalBudget = globalBudget - (attempts + extraSpent);
                if (rnd.nextInt(200) == 0) {
                    store.compactIfNeeded();
                }
                /* continue */
            }
        }
    }

    private static java.util.ArrayDeque lambda_enqueueLoadedChunksForIndex_1(java.util.UUID k) {
        return new java.util.ArrayDeque();
    }

    private static java.util.ArrayDeque lambda_onChunkLoad_0(java.util.UUID k) {
        return new java.util.ArrayDeque();
    }

}
