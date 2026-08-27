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
import org.examplee.palePlugin.store.WardStore;

public final class WardsStorage {
    private final org.examplee.palePlugin.PalePlugin plugin;
    private java.io.File file;
    private org.bukkit.configuration.file.YamlConfiguration cfg;

    public WardsStorage(org.examplee.palePlugin.PalePlugin plugin) {
        super();
        this.plugin = plugin;
    }

    public void load(org.examplee.palePlugin.engine.PaleEngine engine) {
        if (!(plugin.getDataFolder().exists())) {
            plugin.getDataFolder().mkdirs();
        }
        this.file = new java.io.File(plugin.getDataFolder(), "wards.yml");
        this.cfg = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);
        java.util.Iterator local2 = org.bukkit.Bukkit.getWorlds().iterator();
        if (local2.hasNext()) {
            org.bukkit.World w = (org.bukkit.World) local2.next();
            java.util.UUID wid = w.getUID();
            java.util.List list = cfg.getStringList(wid.toString());
            if (list == null) {
                /* continue */
            }
            org.examplee.palePlugin.store.WardStore ws = engine.wards(w);
            java.util.Iterator local7 = list.iterator();
            if (local7.hasNext()) {
                java.lang.String s = (java.lang.String) local7.next();
                java.lang.String[] p = s.split(",");
                if (p.length != 3) {
                    /* continue */
                }
                try {
                    int x = java.lang.Integer.parseInt(p[0]);
                    int y = java.lang.Integer.parseInt(p[1]);
                    int z = java.lang.Integer.parseInt(p[2]);
                    ws.add(x, y, z);
                }
                catch (java.lang.Exception x) {
                    /* continue */
                }
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
        java.io.IOException e = engine.wardsByWorld().entrySet().iterator();
        if (e.hasNext()) {
            java.util.Map.Entry entry = (java.util.Map.Entry) e.next();
            cfg.set(((java.util.UUID) entry.getKey()).toString(), ((org.examplee.palePlugin.store.WardStore) entry.getValue()).serialize());
            /* continue */
        }
        try {
            cfg.save(file);
        }
        catch (java.io.IOException e) {
            plugin.getLogger().warning("[PalePlugin] Не смог сохранить wards.yml: " + e.getMessage());
            return;
        }
    }

    private void lambda_save_0(java.lang.String k) {
        cfg.set(k, null);
    }

}
