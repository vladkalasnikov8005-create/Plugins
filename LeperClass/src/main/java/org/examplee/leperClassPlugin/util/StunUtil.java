package org.examplee.leperClassPlugin.util;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.examplee.leperClassPlugin.LeperClassPlugin;

public final class StunUtil {
    private StunUtil() {
        super();
    }

    public static void stun(org.examplee.leperClassPlugin.LeperClassPlugin plugin, org.bukkit.entity.Player p, int ticks) {
        plugin.movementLock.lock(p);
        java.util.UUID id = p.getUniqueId();
        org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> lambda$stun$0(id, plugin), (long) ticks);
    }

    private static void lambda$stun$0(java.util.UUID id, org.examplee.leperClassPlugin.LeperClassPlugin plugin) {
        org.bukkit.entity.Player pl = org.bukkit.Bukkit.getPlayer(id);
        if (pl != null) {
            plugin.movementLock.unlock(pl);
        }
    }

}
