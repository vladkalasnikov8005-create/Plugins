package org.examplee.palePlugin.tasks;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.examplee.palePlugin.PalePlugin;

public final class AdminPurgeManager {
   private final PalePlugin plugin;
   private final Map<UUID, BukkitTask> tasksByPlayer = new HashMap<>();

   public AdminPurgeManager(PalePlugin plugin) {
      this.plugin = plugin;
   }

   public void stopAll() {
      for (BukkitTask t : this.tasksByPlayer.values()) {
         t.cancel();
      }

      this.tasksByPlayer.clear();
   }

   public void start(Player p, Location center) {
      World w = center.getWorld();
      if (w != null) {
         BukkitTask old = this.tasksByPlayer.remove(p.getUniqueId());
         if (old != null) {
            old.cancel();
         }

         int cx = center.getBlockX() >> 4;
         int cz = center.getBlockZ() >> 4;
         ArrayDeque<AdminPurgeManager.ChunkPos> q = new ArrayDeque<>();
         int r = this.plugin.cfg.adminPurgeRadiusChunks;

         for (int dz = -r; dz <= r; dz++) {
            for (int dx = -r; dx <= r; dx++) {
               q.addLast(new AdminPurgeManager.ChunkPos(cx + dx, cz + dz));
            }
         }

         p.sendMessage(ChatColor.GREEN + "[Pale] Запущена админ-очистка: r=" + r + " чанков");
         long[] cleanedTotal = new long[]{0L};
         BukkitTask task = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
            int processed = 0;

            while (processed < this.plugin.cfg.adminPurgeChunksPerTick && !q.isEmpty()) {
               AdminPurgeManager.ChunkPos pos = q.pollFirst();
               if (pos == null) {
                  break;
               }

               if (!this.plugin.cfg.adminPurgeOnlyLoadedChunks || w.isChunkLoaded(pos.x, pos.z)) {
                  if (!this.plugin.cfg.adminPurgeOnlyLoadedChunks) {
                     try {
                        w.getChunkAt(pos.x, pos.z);
                     } catch (Exception var8x) {
                     }
                  } else if (!w.isChunkLoaded(pos.x, pos.z)) {
                     continue;
                  }

                  int cleaned = this.plugin.engine.purgeChunkSurface(w, pos.x, pos.z, this.plugin.cfg.adminPurgeDepth);
                  cleanedTotal[0] += (long)cleaned;
                  if (cleaned > 0) {
                     this.plugin.spread.addCleansed(cleaned);
                  }

                  processed++;
               }
            }

            if (q.isEmpty()) {
               BukkitTask t = this.tasksByPlayer.remove(p.getUniqueId());
               if (t != null) {
                  t.cancel();
               }

               p.sendMessage(ChatColor.YELLOW + "[Pale] Очистка завершена. Удалено блоков: " + cleanedTotal[0]);
            }
         }, 1L, 1L);
         this.tasksByPlayer.put(p.getUniqueId(), task);
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
}
