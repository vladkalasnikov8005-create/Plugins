package org.examplee.palePlugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class PalePlugin extends org.bukkit.plugin.java.JavaPlugin {
    public org.examplee.palePlugin.core.PaleConfig cfg;
    public org.examplee.palePlugin.core.PaleKeys keys;
    public org.examplee.palePlugin.core.PaleMaterials mats;
    public org.examplee.palePlugin.engine.PaleEngine engine;
    public org.examplee.palePlugin.items.PaleItems items;
    public org.examplee.palePlugin.tasks.SpreadController spread;
    public org.examplee.palePlugin.persist.WardsStorage wardsStorage;
    public org.examplee.palePlugin.persist.BiomesStorage biomesStorage;
    public org.examplee.palePlugin.gui.AdminGui adminGui;
    public org.examplee.palePlugin.tasks.AdminPurgeManager purge;

    public PalePlugin() {
        super();
    }

    public void onEnable() {
        this.cfg = new org.examplee.palePlugin.core.PaleConfig(this);
        cfg.setupDefaults();
        cfg.load();
        this.keys = new org.examplee.palePlugin.core.PaleKeys(this);
        this.mats = new org.examplee.palePlugin.core.PaleMaterials(this);
        if (!(mats.resolveOrDisable())) {
            return;
        }
        this.engine = new org.examplee.palePlugin.engine.PaleEngine(this, cfg, mats);
        engine.resolveInfectedBiomeOrDisableBiome();
        this.wardsStorage = new org.examplee.palePlugin.persist.WardsStorage(this);
        this.biomesStorage = new org.examplee.palePlugin.persist.BiomesStorage(this);
        wardsStorage.load(engine);
        biomesStorage.load(engine);
        this.items = new org.examplee.palePlugin.items.PaleItems(this);
        this.spread = new org.examplee.palePlugin.tasks.SpreadController(this);
        spread.refreshLoadedChunkCounts();
        spread.startAlwaysOnTasks();
        this.purge = new org.examplee.palePlugin.tasks.AdminPurgeManager(this);
        this.adminGui = new org.examplee.palePlugin.gui.AdminGui(this);
        org.bukkit.command.PluginCommand c = getCommand("palespread");
        if (c != null) {
            org.examplee.palePlugin.command.PaleSpreadCommand cmd = new org.examplee.palePlugin.command.PaleSpreadCommand(this);
            c.setExecutor(cmd);
            c.setTabCompleter(cmd);
        } else {
            getLogger().warning("Команда /palespread не найдена (проверь plugin.yml).");
        }
        org.bukkit.plugin.PluginManager pm = org.bukkit.Bukkit.getPluginManager();
        pm.registerEvents(new org.examplee.palePlugin.listeners.ChunkListener(this), this);
        pm.registerEvents(new org.examplee.palePlugin.listeners.ItemUseListener(this), this);
        pm.registerEvents(new org.examplee.palePlugin.listeners.BlockListener(this), this);
        pm.registerEvents(new org.examplee.palePlugin.gui.AdminGuiListener(this), this);
        pm.registerEvents(new org.examplee.palePlugin.listeners.AdminPurgeListener(this), this);
        items.registerRecipes();
        getLogger().info("[PalePlugin] Enabled (no contracts). infectedTypes=" + java.lang.String.valueOf(mats.getInfectedTypes()));
    }

    public void onDisable() {
        try {
            if (spread != null) {
                spread.stopAllTasks();
            }
        }
        catch (java.lang.Throwable ex) {
            try {
                if (purge != null) {
                    purge.stopAll();
                }
            }
            catch (java.lang.Throwable ex) {
            }
        }
    }

    public int apiInfect(org.bukkit.Location center, int radius, int maxBlocks) {
        if (engine == null) {
            return 0;
        }
        return engine.apiInfect(center, radius, maxBlocks);
    }

}
