package org.examplee.leperClassPlugin.lantern;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitTask;
import org.examplee.leperClassPlugin.LeperClassPlugin;

/**
 * Карантинный фонарь: прокажённые в радиусе получают слабость,
 * прогресс заражения замораживается. Топливо — светопыль.
 * Данные хранятся в PDC чанков (переживают рестарт без файлов).
 */
public final class LanternManager {
   private final LeperClassPlugin plugin;
   private final Map<UUID, HashMap<Long, HashMap<Long, Integer>>> byWorld = new HashMap<>();
   private BukkitTask task;

   public LanternManager(LeperClassPlugin plugin) {
      this.plugin = plugin;
   }

   private static long packChunk(int cx, int cz) {
      return (long)cx << 32 | (long)cz & 4294967295L;
   }

   private static long packPos(int x, int y, int z) {
      return ((long)x & 67108863L) << 38 | ((long)(y + 2048) & 4095L) << 26 | (long)z & 67108863L;
   }

   public void start() {
      this.stop();

      for (World w : Bukkit.getWorlds()) {
         for (Chunk c : w.getLoadedChunks()) {
            this.loadChunk(c);
         }
      }

      this.task = Bukkit.getScheduler().runTaskTimer(this.plugin, this::tick, 20L, 20L);
   }

   public void stop() {
      if (this.task != null) {
         this.task.cancel();
         this.task = null;
      }

      this.byWorld.clear();
   }

   private HashMap<Long, HashMap<Long, Integer>> world(World w) {
      return this.byWorld.computeIfAbsent(w.getUID(), k -> new HashMap<>());
   }

   public void loadChunk(Chunk c) {
      String raw = c.getPersistentDataContainer().get(this.plugin.keys.lanternChunkKey, PersistentDataType.STRING);
      if (raw != null && !raw.isEmpty()) {
         HashMap<Long, Integer> map = new HashMap<>();

         for (String s : raw.split(";")) {
            String[] p = s.split(",");
            if (p.length == 4) {
               try {
                  int x = Integer.parseInt(p[0]);
                  int y = Integer.parseInt(p[1]);
                  int z = Integer.parseInt(p[2]);
                  int fuel = Integer.parseInt(p[3]);
                  map.put(packPos(x, y, z), fuel);
               } catch (Exception ignored) {
               }
            }
         }

         if (!map.isEmpty()) {
            this.world(c.getWorld()).put(packChunk(c.getX(), c.getZ()), map);
         }
      }
   }

   public void unloadChunk(Chunk c) {
      HashMap<Long, HashMap<Long, Integer>> wm = this.byWorld.get(c.getWorld().getUID());
      if (wm != null) {
         HashMap<Long, Integer> map = wm.remove(packChunk(c.getX(), c.getZ()));
         if (map != null) {
            this.writeChunkPdc(c, map);
         }
      }
   }

   private void writeChunkPdc(Chunk c, HashMap<Long, Integer> map) {
      if (map == null || map.isEmpty()) {
         c.getPersistentDataContainer().remove(this.plugin.keys.lanternChunkKey);
      } else {
         StringBuilder sb = new StringBuilder(map.size() * 20);

         for (Map.Entry<Long, Integer> e : map.entrySet()) {
            long key = e.getKey();
            int x = (int)(key >> 38);
            int y = (int)(key >> 26 & 4095L) - 2048;
            int z = (int)(key << 38 >> 38);
            if (sb.length() > 0) {
               sb.append(';');
            }

            sb.append(x).append(',').append(y).append(',').append(z).append(',').append(e.getValue());
         }

         c.getPersistentDataContainer().set(this.plugin.keys.lanternChunkKey, PersistentDataType.STRING, sb.toString());
      }
   }

   private void syncChunk(World w, int bx, int bz) {
      if (w.isChunkLoaded(bx >> 4, bz >> 4)) {
         Chunk c = w.getChunkAt(bx >> 4, bz >> 4);
         HashMap<Long, HashMap<Long, Integer>> wm = this.byWorld.get(w.getUID());
         HashMap<Long, Integer> map = wm == null ? null : wm.get(packChunk(bx >> 4, bz >> 4));
         this.writeChunkPdc(c, map);
      }
   }

   public void place(Block b, int startFuel) {
      this.world(b.getWorld())
         .computeIfAbsent(packChunk(b.getX() >> 4, b.getZ() >> 4), k -> new HashMap<>())
         .put(packPos(b.getX(), b.getY(), b.getZ()), Math.max(0, startFuel));
      this.syncChunk(b.getWorld(), b.getX(), b.getZ());
   }

   public boolean isLantern(Block b) {
      HashMap<Long, HashMap<Long, Integer>> wm = this.byWorld.get(b.getWorld().getUID());
      if (wm == null) {
         return false;
      } else {
         HashMap<Long, Integer> map = wm.get(packChunk(b.getX() >> 4, b.getZ() >> 4));
         return map != null && map.containsKey(packPos(b.getX(), b.getY(), b.getZ()));
      }
   }

   public boolean removeAt(Block b) {
      HashMap<Long, HashMap<Long, Integer>> wm = this.byWorld.get(b.getWorld().getUID());
      if (wm == null) {
         return false;
      } else {
         long ck = packChunk(b.getX() >> 4, b.getZ() >> 4);
         HashMap<Long, Integer> map = wm.get(ck);
         if (map == null) {
            return false;
         } else {
            boolean removed = map.remove(packPos(b.getX(), b.getY(), b.getZ())) != null;
            if (removed) {
               if (map.isEmpty()) {
                  wm.remove(ck);
               }

               this.syncChunk(b.getWorld(), b.getX(), b.getZ());
            }

            return removed;
         }
      }
   }

   public int fuelAt(Block b) {
      HashMap<Long, HashMap<Long, Integer>> wm = this.byWorld.get(b.getWorld().getUID());
      if (wm == null) {
         return -1;
      } else {
         HashMap<Long, Integer> map = wm.get(packChunk(b.getX() >> 4, b.getZ() >> 4));
         if (map == null) {
            return -1;
         } else {
            Integer v = map.get(packPos(b.getX(), b.getY(), b.getZ()));
            return v == null ? -1 : v;
         }
      }
   }

   /** @return новое количество топлива в секундах, или -1 если это не фонарь */
   public int refuel(Block b) {
      HashMap<Long, HashMap<Long, Integer>> wm = this.byWorld.get(b.getWorld().getUID());
      if (wm == null) {
         return -1;
      } else {
         HashMap<Long, Integer> map = wm.get(packChunk(b.getX() >> 4, b.getZ() >> 4));
         if (map == null) {
            return -1;
         } else {
            long pk = packPos(b.getX(), b.getY(), b.getZ());
            Integer cur = map.get(pk);
            if (cur == null) {
               return -1;
            } else {
               int next = Math.min(this.plugin.settings.lanternFuelMaxSec, cur + this.plugin.settings.lanternFuelPerDustSec);
               map.put(pk, next);
               this.syncChunk(b.getWorld(), b.getX(), b.getZ());
               return next;
            }
         }
      }
   }

   /** Есть ли активный (с топливом) фонарь в радиусе от игрока. */
   public boolean isInLanternRange(Player p) {
      HashMap<Long, HashMap<Long, Integer>> wm = this.byWorld.get(p.getWorld().getUID());
      if (wm == null || wm.isEmpty()) {
         return false;
      } else {
         Location loc = p.getLocation();
         int radius = this.plugin.settings.lanternRadius;
         int rSq = radius * radius;
         int px = loc.getBlockX();
         int py = loc.getBlockY();
         int pz = loc.getBlockZ();
         int cRad = (radius >> 4) + 1;
         int cx = px >> 4;
         int cz = pz >> 4;

         for (int dx = -cRad; dx <= cRad; dx++) {
            for (int dz = -cRad; dz <= cRad; dz++) {
               HashMap<Long, Integer> map = wm.get(packChunk(cx + dx, cz + dz));
               if (map != null) {
                  for (Map.Entry<Long, Integer> e : map.entrySet()) {
                     if (e.getValue() > 0) {
                        long key = e.getKey();
                        int x = (int)(key >> 38);
                        int y = (int)(key >> 26 & 4095L) - 2048;
                        int z = (int)(key << 38 >> 38);
                        int ox = x - px;
                        int oy = y - py;
                        int oz = z - pz;
                        if (ox * ox + oy * oy + oz * oz <= rSq) {
                           return true;
                        }
                     }
                  }
               }
            }
         }

         return false;
      }
   }

   private void tick() {
      for (Map.Entry<UUID, HashMap<Long, HashMap<Long, Integer>>> we : this.byWorld.entrySet()) {
         World w = Bukkit.getWorld(we.getKey());
         if (w != null) {
            for (Map.Entry<Long, HashMap<Long, Integer>> ce : we.getValue().entrySet()) {
               boolean changed = false;

               for (Map.Entry<Long, Integer> le : ce.getValue().entrySet()) {
                  int fuel = le.getValue();
                  if (fuel > 0) {
                     le.setValue(fuel - 1);
                     changed = true;
                     long key = le.getKey();
                     int x = (int)(key >> 38);
                     int y = (int)(key >> 26 & 4095L) - 2048;
                     int z = (int)(key << 38 >> 38);
                     w.spawnParticle(Particle.SOUL_FIRE_FLAME, x + 0.5, y + 0.6, z + 0.5, 3, 0.15, 0.2, 0.15, 0.005);
                     if (fuel - 1 == 0) {
                        w.playSound(new Location(w, x + 0.5, y + 0.5, z + 0.5), org.bukkit.Sound.BLOCK_FIRE_EXTINGUISH, 0.7F, 0.8F);
                     }
                  }
               }

               if (changed) {
                  int ccx = (int)(ce.getKey() >> 32);
                  int ccz = ce.getKey().intValue();
                  if (w.isChunkLoaded(ccx, ccz)) {
                     this.writeChunkPdc(w.getChunkAt(ccx, ccz), ce.getValue());
                  }
               }
            }
         }
      }

      for (Player p : Bukkit.getOnlinePlayers()) {
         if (this.plugin.data.isLeper(p)
            && this.plugin.effects.WEAKNESS != null
            && this.isInLanternRange(p)) {
            p.addPotionEffect(new PotionEffect(this.plugin.effects.WEAKNESS, 100, 0, true, false, true));
         }
      }
   }
}
