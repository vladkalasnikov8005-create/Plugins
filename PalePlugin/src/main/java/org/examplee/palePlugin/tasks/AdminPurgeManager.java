package org.examplee.palePlugin.tasks;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.examplee.palePlugin.PalePlugin;

public final class AdminPurgeManager {
    private final org.examplee.palePlugin.PalePlugin plugin;
    private final java.util.Map tasksByPlayer;

    public AdminPurgeManager(org.examplee.palePlugin.PalePlugin plugin) {
        super();
        this.tasksByPlayer = new java.util.HashMap();
        this.plugin = plugin;
    }

    public void stopAll() {
        java.util.Iterator local1 = tasksByPlayer.values().iterator();
        if (local1.hasNext()) {
            org.bukkit.scheduler.BukkitTask t = (org.bukkit.scheduler.BukkitTask) local1.next();
            t.cancel();
            /* continue */
        }
        tasksByPlayer.clear();
    }

    public void start(org.bukkit.entity.Player p, org.bukkit.Location center) {
        org.bukkit.World w = center.getWorld();
        if (w == null) {
            return;
        }
        org.bukkit.scheduler.BukkitTask old = (org.bukkit.scheduler.BukkitTask) tasksByPlayer.remove(p.getUniqueId());
        if (old != null) {
            old.cancel();
        }
        int cx = center.getBlockX() >> 4;
        int cz = center.getBlockZ() >> 4;
        java.util.ArrayDeque q = new java.util.ArrayDeque();
        int r = plugin.cfg.adminPurgeRadiusChunks;
        int dz = -r;
        if (dz <= r) {
            int dx = -r;
            if (dx <= r) {
                q.addLast(new org.examplee.palePlugin.tasks.AdminPurgeManager$ChunkPos(cx + dx, cz + dz));
                dx++;
                /* continue */
            }
            dz++;
            /* continue */
        }
        p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.GREEN) + "[Pale] Запущена админ-очистка: r=" + r + " чанков");
        long[] tmp1 = new long[1];
        tmp1[0] = 0L;
        dz = tmp1;
        dx = org.bukkit.Bukkit.getScheduler().runTaskTimer(plugin, () -> lambda_start_0(q, w, cleanedTotal, p), 1L, 1L);
        tasksByPlayer.put(p.getUniqueId(), task);
    }

    private void lambda_start_0(java.util.ArrayDeque q, org.bukkit.World w, long[] cleanedTotal, org.bukkit.entity.Player p) {
        int processed = 0;
        if (processed < plugin.cfg.adminPurgeChunksPerTick) {
            if (!(q.isEmpty())) {
                org.examplee.palePlugin.tasks.AdminPurgeManager$ChunkPos pos = (org.examplee.palePlugin.tasks.AdminPurgeManager$ChunkPos) q.pollFirst();
                if (pos == null) {
                } else {
                    if (plugin.cfg.adminPurgeOnlyLoadedChunks) {
                        if (!(w.isChunkLoaded(pos.x, pos.z))) {
                            /* continue */
                        }
                    }
                    if (!(plugin.cfg.adminPurgeOnlyLoadedChunks)) {
                        try {
                            w.getChunkAt(pos.x, pos.z);
                        }
                        catch (java.lang.Exception ex) {
                        }
                    } else {
                        if (!(w.isChunkLoaded(pos.x, pos.z))) {
                            /* continue */
                        }
                    }
                    cleaned = plugin.engine.purgeChunkSurface(w, pos.x, pos.z, plugin.cfg.adminPurgeDepth);
                    cleanedTotal[0] = cleanedTotal[0] + (long) cleaned;
                    if (cleaned > 0) {
                        plugin.spread.addCleansed(cleaned);
                    }
                    processed++;
                    /* continue */
                }
            }
        }
        if (q.isEmpty()) {
            pos = (org.bukkit.scheduler.BukkitTask) tasksByPlayer.remove(p.getUniqueId());
            if (t != null) {
                t.cancel();
            }
            p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.YELLOW) + "[Pale] Очистка завершена. Удалено блоков: " + cleanedTotal[0]);
        }
    }

}
