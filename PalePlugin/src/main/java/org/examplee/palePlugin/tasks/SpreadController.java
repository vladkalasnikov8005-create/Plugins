package org.examplee.palePlugin.tasks;

import java.util.ArrayDeque;
import java.util.HashMap;
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
   private final PalePlugin plugin;
   private boolean running = false;
   private BukkitTask spreadTask;
   private BukkitTask indexTask;
   private BukkitTask statsSecondTask;
   private BukkitTask playerEffectsTask;
   private BukkitTask stepEffectsTask;
   private final Map<UUID, Double> carryAttemptsByWorld = new HashMap<>();
   private final Map<UUID, ArrayDeque<SpreadController.ChunkPos>> indexQueueByWorld = new HashMap<>();
   private final Map<UUID, Integer> loadedChunkCountByWorld = new HashMap<>();
   private long totalAttempts = 0L;
   private long totalInfected = 0L;
   private long totalSkippedProtected = 0L;
   private long totalCleansed = 0L;
   private final int[] infectedPerSec = new int[60];
   private int infectedSecIndex = 0;
   private long lastEpochSecond = -1L;
   private final Random rnd = new Random();
   private static final String PERM_ADMIN = "pale.admin";
   private static final String PERM_EFFECT_IMMUNE = "pale.effect.immune";
   private final NamespacedKey KEY_LEPER_CLASS = NamespacedKey.fromString("leperclass:class_leper");

   public SpreadController(PalePlugin plugin) {
      this.plugin = plugin;
   }

   public boolean isRunning() {
      return this.running;
   }

   public void setRunning(boolean val) {
      if (val) {
         this.startRunning();
      } else {
         this.stopRunning();
      }
   }

   public void startAlwaysOnTasks() {
      this.startStatsSecondTask();
      this.startPlayerEffectsTask();
      this.startStepEffectsTask();
   }

   public void stopAllTasks() {
      this.stopRunning();
      this.stopStatsSecondTask();
      this.stopPlayerEffectsTask();
      this.stopStepEffectsTask();
   }

   public void startRunning() {
      if (!this.running) {
         this.running = true;
         this.refreshLoadedChunkCounts();
         this.enqueueLoadedChunksForIndex();
         this.startIndexTask();
         this.startSpreadTask();
      }
   }

   public void stopRunning() {
      this.running = false;
      this.stopSpreadTask();
      this.stopIndexTask();
   }

   public void addCleansed(int n) {
      this.totalCleansed = this.totalCleansed + (long)Math.max(0, n);
   }

   public void addInfected(int n) {
      if (n > 0) {
         this.totalInfected += (long)n;
         this.infectedPerSec[this.infectedSecIndex] = this.infectedPerSec[this.infectedSecIndex] + n;
      }
   }

   public void refreshLoadedChunkCounts() {
      this.loadedChunkCountByWorld.clear();

      for (World w : Bukkit.getWorlds()) {
         this.loadedChunkCountByWorld.put(w.getUID(), w.getLoadedChunks().length);
      }
   }

   public void onChunkLoad(World w, int cx, int cz) {
      UUID wid = w.getUID();
      this.loadedChunkCountByWorld.put(wid, this.loadedChunkCountByWorld.getOrDefault(wid, 0) + 1);
      if (this.running) {
         this.indexQueueByWorld.computeIfAbsent(wid, k -> new ArrayDeque<>()).addLast(new SpreadController.ChunkPos(cx, cz));
      }
   }

   public void onChunkUnload(World w) {
      UUID wid = w.getUID();
      this.loadedChunkCountByWorld.put(wid, Math.max(0, this.loadedChunkCountByWorld.getOrDefault(wid, 0) - 1));
   }

   private void enqueueLoadedChunksForIndex() {
      for (World world : Bukkit.getWorlds()) {
         UUID wid = world.getUID();
         ArrayDeque<SpreadController.ChunkPos> q = this.indexQueueByWorld.computeIfAbsent(wid, k -> new ArrayDeque<>());

         for (Chunk ch : world.getLoadedChunks()) {
            q.addLast(new SpreadController.ChunkPos(ch.getX(), ch.getZ()));
         }
      }
   }

   private void startSpreadTask() {
      this.stopSpreadTask();
      this.spreadTask = Bukkit.getScheduler()
         .runTaskTimer(
            this.plugin,
            () -> {
               if (this.running) {
                  this.rollStatsSecond();
                  double s = (double)Math.max(1, Math.min(5000, this.plugin.cfg.speedPerChunk));
                  double tSpeed = (s - 1.0) / 4999.0;
                  double turboT = Math.pow(tSpeed, 8.0);
                  int effectiveGlobalCap = (int)Math.round(
                     SpreadController.PaleConfig.lerp(
                        (double)this.plugin.cfg.maxAttemptsPerTickGlobal, (double)this.plugin.cfg.turboMaxAttemptsPerTickGlobal, turboT
                     )
                  );
                  int effectivePerWorldCap = (int)Math.round(
                     SpreadController.PaleConfig.lerp(
                        (double)this.plugin.cfg.maxAttemptsPerTickPerWorld, (double)this.plugin.cfg.turboMaxAttemptsPerTickPerWorld, turboT
                     )
                  );
                  int globalBudget = effectiveGlobalCap;
                  double perSecPerChunk = this.plugin.cfg.effectiveAttemptsPerSecondPerChunk();

                  for (World world : Bukkit.getWorlds()) {
                     if (globalBudget <= 0) {
                        break;
                     }

                     UUID wid = world.getUID();
                     int loadedChunks = this.loadedChunkCountByWorld.getOrDefault(wid, 0);
                     if (loadedChunks > 0) {
                        SourceStore store = this.plugin.engine.sources(world);
                        if (store.size() != 0) {
                           double carry = this.carryAttemptsByWorld.getOrDefault(wid, 0.0);
                           double want = (double)loadedChunks * (perSecPerChunk / 20.0) + carry;
                           int attempts = (int)want;
                           this.carryAttemptsByWorld.put(wid, want - (double)attempts);
                           if (attempts > 0) {
                              if (store.size() > 5000 && this.plugin.cfg.speedPerChunk > 2500) {
                                 attempts = (int)Math.ceil((double)attempts * 1.2);
                              }

                              attempts = Math.min(attempts, effectivePerWorldCap);
                              attempts = Math.min(attempts, globalBudget);
                              if (attempts > 0) {
                                 int perWorldRemain = Math.max(0, effectivePerWorldCap - attempts);
                                 int globalRemain = Math.max(0, globalBudget - attempts);
                                 int burstMax = this.plugin.cfg.speedPerChunk > 3000 ? Math.max(0, attempts / 2) : 0;
                                 int extraBurstBudget = Math.min(burstMax, Math.min(perWorldRemain, globalRemain));
                                 int extraSpent = 0;
                                 int i = 0;

                                 while (true) {
                                    if (i < attempts) {
                                       this.totalAttempts++;
                                       Block source = store.getRandomLiveSource(world, this.rnd, this.plugin.engine.infectedTypes());
                                       if (source != null) {
                                          if (this.plugin.engine.isWardProtected(world, source.getX(), source.getY(), source.getZ())) {
                                             this.totalSkippedProtected++;
                                          } else if (this.plugin.engine.trySpreadFromSource(source)) {
                                             this.addInfected(1);
                                             if (this.plugin.cfg.speedPerChunk > 3000 && extraBurstBudget > 0 && this.rnd.nextInt(100) < 35) {
                                                extraBurstBudget--;
                                                extraSpent++;
                                                this.totalAttempts++;
                                                if (this.plugin.engine.trySpreadFromSource(source)) {
                                                   this.addInfected(1);
                                                }
                                             }
                                          }

                                          i++;
                                          continue;
                                       }
                                    }

                                    globalBudget -= attempts + extraSpent;
                                    if (this.rnd.nextInt(200) == 0) {
                                       store.compactIfNeeded();
                                    }
                                    break;
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            },
            1L,
            1L
         );
   }

   private void stopSpreadTask() {
      if (this.spreadTask != null) {
         this.spreadTask.cancel();
         this.spreadTask = null;
      }
   }

   private void startIndexTask() {
      this.stopIndexTask();
      if (this.plugin.cfg.indexChunksPerTickPerWorld > 0) {
         this.indexTask = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
            if (this.running) {
               for (World world : Bukkit.getWorlds()) {
                  UUID wid = world.getUID();
                  ArrayDeque<SpreadController.ChunkPos> q = this.indexQueueByWorld.get(wid);
                  if (q != null && !q.isEmpty()) {
                     int processed = 0;

                     while (processed < this.plugin.cfg.indexChunksPerTickPerWorld && !q.isEmpty()) {
                        SpreadController.ChunkPos pos = q.pollFirst();
                        if (pos == null) {
                           break;
                        }

                        if (world.isChunkLoaded(pos.x, pos.z)) {
                           this.plugin.engine.indexChunkSurface(world, pos.x, pos.z);
                           processed++;
                        }
                     }
                  }
               }
            }
         }, 1L, 1L);
      }
   }

   private void stopIndexTask() {
      if (this.indexTask != null) {
         this.indexTask.cancel();
         this.indexTask = null;
      }
   }

   private void startStatsSecondTask() {
      this.stopStatsSecondTask();
      this.statsSecondTask = Bukkit.getScheduler().runTaskTimer(this.plugin, this::rollStatsSecond, 20L, 20L);
   }

   private void stopStatsSecondTask() {
      if (this.statsSecondTask != null) {
         this.statsSecondTask.cancel();
         this.statsSecondTask = null;
      }
   }

   private void rollStatsSecond() {
      long sec = System.currentTimeMillis() / 1000L;
      if (this.lastEpochSecond == -1L) {
         this.lastEpochSecond = sec;
      } else {
         long diff = sec - this.lastEpochSecond;
         if (diff > 0L) {
            int steps = (int)Math.min(diff, 60L);

            for (int i = 0; i < steps; i++) {
               this.infectedSecIndex = (this.infectedSecIndex + 1) % 60;
               this.infectedPerSec[this.infectedSecIndex] = 0;
            }

            this.lastEpochSecond = sec;
         }
      }
   }

   private int infectedLastMinute() {
      int sum = 0;

      for (int v : this.infectedPerSec) {
         sum += v;
      }

      return sum;
   }

   private void startPlayerEffectsTask() {
      this.stopPlayerEffectsTask();
      this.playerEffectsTask = Bukkit.getScheduler()
         .runTaskTimer(
            this.plugin,
            () -> {
               if (this.plugin.cfg.effectsEnabled && this.plugin.cfg.stagesEnabled) {
                  for (Player p : Bukkit.getOnlinePlayers()) {
                     if (!p.hasPermission("pale.effect.immune") && !p.hasPermission("pale.admin") && !this.isLeper(p)) {
                        World w = p.getWorld();
                        int cx = p.getLocation().getBlockX() >> 4;
                        int cz = p.getLocation().getBlockZ() >> 4;
                        int stage = this.plugin.engine.getChunkStage(w, cx, cz);
                        if (stage >= this.plugin.cfg.effectsMinStage) {
                           int dur = this.plugin.cfg.effectsCheckPeriodTicks + 40;
                           if (stage >= 2) {
                              p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, dur, this.plugin.cfg.effectsSlownessAmpStage2, true, false, true));
                           }

                           if (stage >= 3) {
                              p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, dur, this.plugin.cfg.effectsWeaknessAmpStage3, true, false, true));
                           }

                           if (stage >= 4) {
                              p.addPotionEffect(
                                 new PotionEffect(PotionEffectType.MINING_FATIGUE, dur, this.plugin.cfg.effectsMiningFatigueAmpStage4, true, false, true)
                              );
                           }

                           if (stage >= 5 && this.plugin.cfg.effectsDarknessStage5) {
                              p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, dur, 0, true, false, true));
                           }
                        }
                     }
                  }
               }
            },
            20L,
            (long)this.plugin.cfg.effectsCheckPeriodTicks
         );
   }

   private void stopPlayerEffectsTask() {
      if (this.playerEffectsTask != null) {
         this.playerEffectsTask.cancel();
         this.playerEffectsTask = null;
      }
   }

   private void startStepEffectsTask() {
      this.stopStepEffectsTask();
      this.stepEffectsTask = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
         for (Player p : Bukkit.getOnlinePlayers()) {
            GameMode gm = p.getGameMode();
            if (gm != GameMode.SPECTATOR && gm != GameMode.CREATIVE) {
               Block under = p.getLocation().getBlock().getRelative(BlockFace.DOWN);
               if (this.plugin.engine.infectedTypes().contains(under.getType())) {
                  int dur = this.plugin.cfg.stepEffectsDurationTicks;
                  if (this.isLeper(p)) {
                     p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, dur, 0, true, false, true));
                     p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, dur, 0, true, false, true));
                     p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, dur, 0, true, false, true));
                  } else if (!p.hasPermission("pale.admin") && !p.hasPermission("pale.effect.immune")) {
                     p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, dur, 0, true, false, true));
                     p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, dur, 0, true, false, true));
                     p.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, dur, 0, true, false, true));
                  }
               }
            }
         }
      }, 10L, (long)this.plugin.cfg.stepEffectsCheckPeriodTicks);
   }

   private void stopStepEffectsTask() {
      if (this.stepEffectsTask != null) {
         this.stepEffectsTask.cancel();
         this.stepEffectsTask = null;
      }
   }

   private boolean isLeper(Player p) {
      if (this.KEY_LEPER_CLASS == null) {
         return false;
      } else {
         Byte v = (Byte)p.getPersistentDataContainer().get(this.KEY_LEPER_CLASS, PersistentDataType.BYTE);
         return v != null && v == 1;
      }
   }

   public void sendInfo(CommandSender sender) {
      sender.sendMessage(
         ChatColor.GRAY
            + "[Pale] running="
            + this.running
            + " speed="
            + this.plugin.cfg.speedPerChunk
            + " rate≈"
            + String.format(Locale.US, "%.6f", this.plugin.cfg.effectiveAttemptsPerSecondPerChunk())
      );
      sender.sendMessage(
         ChatColor.GRAY
            + "[Pale] infected total="
            + this.totalInfected
            + " attempts total="
            + this.totalAttempts
            + " skippedProtected="
            + this.totalSkippedProtected
            + " cleansed total="
            + this.totalCleansed
            + " infected/min="
            + this.infectedLastMinute()
      );

      for (World w : Bukkit.getWorlds()) {
         UUID wid = w.getUID();
         int loaded = this.loadedChunkCountByWorld.getOrDefault(wid, 0);
         int sources = this.plugin.engine.sources(w).size();
         int wards = this.plugin.engine.wards(w).size();
         int biomeCells = this.plugin.engine.biomes(w).size();
         sender.sendMessage(
            ChatColor.DARK_GRAY + "world=" + w.getName() + " loaded=" + loaded + " sources=" + sources + " wards=" + wards + " biomeCells=" + biomeCells
         );
      }
   }

   private static final class ChunkPos {
      final int x;
      final int z;

      ChunkPos(int x, int z) {
         this.x = x;
         this.z = z;
      }
   }

   private static final class PaleConfig {
      static double lerp(double a, double b, double t) {
         if (t <= 0.0) {
            return a;
         } else {
            return t >= 1.0 ? b : a + (b - a) * t;
         }
      }
   }
}
