package org.examplee.palePlugin.tasks;

import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitTask;
import org.examplee.palePlugin.PalePlugin;
import org.examplee.palePlugin.store.GreaterWardStore;

/**
 * Великий оберег: раз в N секунд очищает ровно 1 заражённый блок в радиусе,
 * расходуя заряд (пополняется святой водой). Без заряда — только блокирует
 * распространение, но не чистит.
 */
public final class GreaterWardTask {
   private final PalePlugin plugin;
   private final Random rnd = new Random();
   private BukkitTask task;

   public GreaterWardTask(PalePlugin plugin) {
      this.plugin = plugin;
   }

   public void start() {
      this.stop();
      long period = this.plugin.cfg.greaterWardCleanIntervalSec * 20L;
      this.task = Bukkit.getScheduler().runTaskTimer(this.plugin, this::cycle, period, period);
   }

   public void stop() {
      if (this.task != null) {
         this.task.cancel();
         this.task = null;
      }
   }

   private void cycle() {
      int radius = this.plugin.cfg.greaterWardRadius;
      int intervalSec = this.plugin.cfg.greaterWardCleanIntervalSec;

      for (Entry<UUID, GreaterWardStore> we : this.plugin.engine.greaterWardsByWorld().entrySet()) {
         World w = Bukkit.getWorld(we.getKey());
         if (w != null) {
            for (GreaterWardStore.Entry ward : we.getValue().all()) {
               if (ward.charge > 0 && w.isChunkLoaded(ward.x >> 4, ward.z >> 4)) {
                  w.spawnParticle(Particle.ELECTRIC_SPARK, ward.x + 0.5, ward.y + 1.2, ward.z + 0.5, 5, 0.3, 0.4, 0.3, 0.01);
                  Block target = this.findInfectedBlock(w, ward, radius);
                  if (target != null) {
                     int cleaned = this.plugin.engine.cleanse(target.getLocation(), 0);
                     if (cleaned > 0) {
                        ward.charge = Math.max(0, ward.charge - intervalSec);
                        this.plugin.spread.addCleansed(cleaned);
                        w.spawnParticle(
                           Particle.END_ROD,
                           target.getX() + 0.5,
                           target.getY() + 0.5,
                           target.getZ() + 0.5,
                           10,
                           0.3,
                           0.3,
                           0.3,
                           0.02
                        );
                        if (ward.charge == 0) {
                           w.playSound(
                              new Location(w, ward.x + 0.5, ward.y + 0.5, ward.z + 0.5),
                              org.bukkit.Sound.BLOCK_BEACON_DEACTIVATE,
                              0.8F,
                              0.9F
                           );
                        }
                     }
                  }
               }
            }
         }
      }
   }

   /** Ищет один заражённый блок в радиусе оберега (случайные колонны). */
   private Block findInfectedBlock(World w, GreaterWardStore.Entry ward, int radius) {
      int yMin = Math.max(w.getMinHeight(), ward.y - 24);
      int yMax = Math.min(w.getMaxHeight() - 1, ward.y + 24);

      for (int attempt = 0; attempt < 30; attempt++) {
         double angle = this.rnd.nextDouble() * Math.PI * 2.0;
         double dist = Math.sqrt(this.rnd.nextDouble()) * radius;
         int tx = ward.x + (int)Math.round(Math.cos(angle) * dist);
         int tz = ward.z + (int)Math.round(Math.sin(angle) * dist);
         if (w.isChunkLoaded(tx >> 4, tz >> 4)) {
            for (int yy = yMax; yy >= yMin; yy--) {
               Block b = w.getBlockAt(tx, yy, tz);
               Material t = b.getType();
               if (this.plugin.engine.infectedTypes().contains(t)) {
                  return b;
               }
            }
         }
      }

      return null;
   }
}
