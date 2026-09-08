package org.examplee.palePlugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.examplee.palePlugin.command.PaleSpreadCommand;
import org.examplee.palePlugin.core.PaleConfig;
import org.examplee.palePlugin.core.PaleKeys;
import org.examplee.palePlugin.core.PaleMaterials;
import org.examplee.palePlugin.darkness.DarknessManager;
import org.examplee.palePlugin.engine.PaleEngine;
import org.examplee.palePlugin.gui.AdminGui;
import org.examplee.palePlugin.gui.AdminGuiListener;
import org.examplee.palePlugin.items.PaleItems;
import org.examplee.palePlugin.listeners.AdminPurgeListener;
import org.examplee.palePlugin.listeners.BlockListener;
import org.examplee.palePlugin.listeners.ChunkListener;
import org.examplee.palePlugin.listeners.ItemUseListener;
import org.examplee.palePlugin.persist.BiomesStorage;
import org.examplee.palePlugin.persist.GreaterWardsStorage;
import org.examplee.palePlugin.persist.WardsStorage;
import org.examplee.palePlugin.tasks.AdminPurgeManager;
import org.examplee.palePlugin.tasks.GreaterWardTask;
import org.examplee.palePlugin.tasks.SpreadController;

public final class PalePlugin extends JavaPlugin {
   public PaleConfig cfg;
   public PaleKeys keys;
   public PaleMaterials mats;
   public PaleEngine engine;
   public PaleItems items;
   public SpreadController spread;
   public WardsStorage wardsStorage;
   public GreaterWardsStorage greaterWardsStorage;
   public BiomesStorage biomesStorage;
   public AdminGui adminGui;
   public AdminPurgeManager purge;
   public DarknessManager darkness;
   private GreaterWardTask greaterWardTask;
   private org.bukkit.scheduler.BukkitTask autosaveTask;

   public void onEnable() {
      this.cfg = new PaleConfig(this);
      this.cfg.setupDefaults();
      this.cfg.load();
      this.keys = new PaleKeys(this);
      this.mats = new PaleMaterials(this);
      if (this.mats.resolveOrDisable()) {
         this.engine = new PaleEngine(this, this.cfg, this.mats);
         this.engine.resolveInfectedBiomeOrDisableBiome();
         this.wardsStorage = new WardsStorage(this);
         this.greaterWardsStorage = new GreaterWardsStorage(this);
         this.biomesStorage = new BiomesStorage(this);
         this.wardsStorage.load(this.engine);
         this.greaterWardsStorage.load(this.engine);
         this.biomesStorage.load(this.engine);
         this.items = new PaleItems(this);
         this.spread = new SpreadController(this);
         this.spread.refreshLoadedChunkCounts();
         this.spread.startAlwaysOnTasks();
         this.purge = new AdminPurgeManager(this);
         this.adminGui = new AdminGui(this);
         this.darkness = new DarknessManager(this);
         this.darkness.start();
         PluginCommand c = this.getCommand("palespread");
         if (c != null) {
            PaleSpreadCommand cmd = new PaleSpreadCommand(this);
            c.setExecutor(cmd);
            c.setTabCompleter(cmd);
         } else {
            this.getLogger().warning("Команда /palespread не найдена (проверь plugin.yml).");
         }

         PluginManager pm = Bukkit.getPluginManager();
         pm.registerEvents(new ChunkListener(this), this);
         pm.registerEvents(new ItemUseListener(this), this);
         pm.registerEvents(new BlockListener(this), this);
         pm.registerEvents(new AdminGuiListener(this), this);
         pm.registerEvents(new AdminPurgeListener(this), this);
         pm.registerEvents(this.darkness, this);
         this.greaterWardTask = new GreaterWardTask(this);
         this.greaterWardTask.start();
         this.startAutosave();
         this.getLogger().info("[PalePlugin] Enabled (no contracts). infectedTypes=" + this.mats.getInfectedTypes());
      }
   }

   public void onDisable() {
      if (this.autosaveTask != null) {
         this.autosaveTask.cancel();
         this.autosaveTask = null;
      }

      if (this.greaterWardTask != null) {
         this.greaterWardTask.stop();
         this.greaterWardTask = null;
      }

      try {
         if (this.spread != null) {
            this.spread.stopAllTasks();
         }
      } catch (Throwable t) {
         this.getLogger().warning("[PalePlugin] Ошибка при остановке задач распространения: " + t);
      }

      try {
         if (this.purge != null) {
            this.purge.stopAll();
         }
      } catch (Throwable t) {
         this.getLogger().warning("[PalePlugin] Ошибка при остановке задач очистки: " + t);
      }

      try {
         if (this.wardsStorage != null) {
            this.wardsStorage.save(this.engine);
         }
      } catch (Throwable t) {
         this.getLogger().severe("[PalePlugin] НЕ УДАЛОСЬ сохранить wards.yml: " + t);
      }

      try {
         if (this.greaterWardsStorage != null) {
            this.greaterWardsStorage.save(this.engine);
         }
      } catch (Throwable t) {
         this.getLogger().severe("[PalePlugin] НЕ УДАЛОСЬ сохранить greater_wards.yml: " + t);
      }

      try {
         if (this.biomesStorage != null) {
            this.biomesStorage.save(this.engine);
         }
      } catch (Throwable t) {
         this.getLogger().severe("[PalePlugin] НЕ УДАЛОСЬ сохранить biomes.yml: " + t);
      }

      try {
         if (this.darkness != null) {
            this.darkness.stop();
         }
      } catch (Throwable t) {
         this.getLogger().severe("[PalePlugin] НЕ УДАЛОСЬ сохранить darkness.yml: " + t);
      }

      this.getLogger().info("[PalePlugin] Disabled.");
   }

   private void startAutosave() {
      if (this.autosaveTask != null) {
         this.autosaveTask.cancel();
         this.autosaveTask = null;
      }

      long minutes = this.cfg == null ? 5L : this.cfg.autosaveMinutes;
      if (minutes > 0L) {
         long periodTicks = minutes * 60L * 20L;
         this.autosaveTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            try {
               this.wardsStorage.saveAsync(this.engine);
               this.greaterWardsStorage.saveAsync(this.engine);
               this.biomesStorage.saveAsync(this.engine);
               if (this.darkness != null) {
                  this.darkness.saveToDisk(true);
               }
            } catch (Throwable t) {
               this.getLogger().warning("[PalePlugin] Ошибка автосохранения: " + t);
            }
         }, periodTicks, periodTicks);
         this.getLogger().info("[PalePlugin] Автосохранение каждые " + minutes + " мин.");
      } else {
         this.getLogger().info("[PalePlugin] Автосохранение отключено (storage.autosaveMinutes=0).");
      }
   }

   public int apiInfect(Location center, int radius, int maxBlocks) {
      return this.engine == null ? 0 : this.engine.apiInfect(center, radius, maxBlocks);
   }

   /** API для других плагинов: стадия заражения чанка в точке (0..5). */
   public int apiGetStageAt(Location loc) {
      if (this.engine == null || loc == null || loc.getWorld() == null) {
         return 0;
      } else {
         return this.engine.getChunkStage(loc.getWorld(), loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
      }
   }
}
