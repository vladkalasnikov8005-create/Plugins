package org.examplee.palePlugin.persist;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.examplee.palePlugin.PalePlugin;
import org.examplee.palePlugin.engine.PaleEngine;
import org.examplee.palePlugin.store.BiomeStore;

public final class BiomesStorage {
    private final org.examplee.palePlugin.PalePlugin plugin;
    private java.io.File file;
    private org.bukkit.configuration.file.YamlConfiguration cfg;

    public BiomesStorage(org.examplee.palePlugin.PalePlugin plugin) {
        super();
        this.plugin = plugin;
    }

    public void load(org.examplee.palePlugin.engine.PaleEngine engine) {
        if (!(plugin.getDataFolder().exists())) {
            plugin.getDataFolder().mkdirs();
        }
        this.file = new java.io.File(plugin.getDataFolder(), "biomes.yml");
        this.cfg = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);
        java.util.Iterator local2 = org.bukkit.Bukkit.getWorlds().iterator();
        if (local2.hasNext()) {
            org.bukkit.World w = (org.bukkit.World) local2.next();
            java.util.UUID wid = w.getUID();
            java.util.List list = cfg.getStringList(wid.toString());
            if (list == null) { /* continue */ }
            if (list.isEmpty()) {
                /* continue */
            }
            org.examplee.palePlugin.store.BiomeStore bs = engine.biomes(w);
            java.util.Iterator local7 = list.iterator();
            if (local7.hasNext()) {
                java.lang.String s = (java.lang.String) local7.next();
                bs.loadSerializedLine(s);
                /* continue */
            }
            /* continue */
        }
    }

    public void save(org.examplee.palePlugin.engine.PaleEngine engine) {
        if (file == null) {
            return;
        }
        if (cfg == null) {
            this.cfg = new org.bukkit.configuration.file.YamlConfiguration();
        }
        cfg.getKeys(false).forEach((java.lang.String p0) -> lambda_save_0(p0));
        java.io.IOException e = engine.biomesByWorld().entrySet().iterator();
        if (e.hasNext()) {
            java.util.Map.Entry entry = (java.util.Map.Entry) e.next();
            cfg.set(((java.util.UUID) entry.getKey()).toString(), ((org.examplee.palePlugin.store.BiomeStore) entry.getValue()).serialize());
            /* continue */
        }
        try {
            cfg.save(file);
        }
        catch (java.io.IOException e) {
            plugin.getLogger().warning("[PalePlugin] Не смог сохранить biomes.yml: " + e.getMessage());
            return;
        }
    }

    private void lambda_save_0(java.lang.String k) {
        cfg.set(k, null);
    }

}
