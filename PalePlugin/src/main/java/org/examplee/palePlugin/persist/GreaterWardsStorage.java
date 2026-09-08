package org.examplee.palePlugin.persist;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.examplee.palePlugin.PalePlugin;
import org.examplee.palePlugin.engine.PaleEngine;
import org.examplee.palePlugin.store.GreaterWardStore;

public final class GreaterWardsStorage {
   private final PalePlugin plugin;
   private File file;
   private YamlConfiguration cfg;

   public GreaterWardsStorage(PalePlugin plugin) {
      this.plugin = plugin;
   }

   public void load(PaleEngine engine) {
      if (!this.plugin.getDataFolder().exists()) {
         this.plugin.getDataFolder().mkdirs();
      }

      this.file = new File(this.plugin.getDataFolder(), "greater_wards.yml");
      this.cfg = YamlConfiguration.loadConfiguration(this.file);

      for (World w : Bukkit.getWorlds()) {
         UUID wid = w.getUID();
         List<String> list = this.cfg.getStringList(wid.toString());
         if (list != null) {
            GreaterWardStore ws = engine.greaterWards(w);

            for (String s : list) {
               String[] p = s.split(",");
               if (p.length >= 3) {
                  try {
                     int x = Integer.parseInt(p[0]);
                     int y = Integer.parseInt(p[1]);
                     int z = Integer.parseInt(p[2]);
                     int charge = p.length >= 4 ? Integer.parseInt(p[3]) : 0;
                     ws.add(x, y, z, charge);
                  } catch (Exception ignored) {
                  }
               }
            }
         }
      }
   }

   public void save(PaleEngine engine) {
      String data = this.buildYaml(engine);
      if (data != null) {
         this.writeToDisk(data);
      }
   }

   public void saveAsync(PaleEngine engine) {
      String data = this.buildYaml(engine);
      if (data != null) {
         Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> this.writeToDisk(data));
      }
   }

   private String buildYaml(PaleEngine engine) {
      if (this.file == null) {
         return null;
      } else {
         if (this.cfg == null) {
            this.cfg = new YamlConfiguration();
         }

         this.cfg.getKeys(false).forEach(k -> this.cfg.set(k, null));

         for (Entry<UUID, GreaterWardStore> entry : engine.greaterWardsByWorld().entrySet()) {
            this.cfg.set(entry.getKey().toString(), entry.getValue().serialize());
         }

         return this.cfg.saveToString();
      }
   }

   private void writeToDisk(String data) {
      try {
         java.nio.file.Files.writeString(this.file.toPath(), data, java.nio.charset.StandardCharsets.UTF_8);
      } catch (IOException ex) {
         this.plugin.getLogger().warning("[PalePlugin] Не смог сохранить greater_wards.yml: " + ex.getMessage());
      }
   }
}
