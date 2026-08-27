package org.examplee.leperClassPlugin.listeners;

import org.bukkit.entity.Enemy;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.examplee.leperClassPlugin.LeperClassPlugin;

public final class MobIgnoreListener implements org.bukkit.event.Listener {
    private final org.examplee.leperClassPlugin.LeperClassPlugin plugin;

    public MobIgnoreListener(org.examplee.leperClassPlugin.LeperClassPlugin plugin) {
        super();
        this.plugin = plugin;
    }

    @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTarget(org.bukkit.event.entity.EntityTargetLivingEntityEvent e) {
        org.bukkit.entity.EntityType type = e.getTarget();
        if (!((type instanceof org.bukkit.entity.Player))) {
            return;
        }
        org.bukkit.entity.Player p = (org.bukkit.entity.Player) type;
        if (plugin.data.isLeper(p)) {
            type = e.getEntity().getType();
            if (type == org.bukkit.entity.EntityType.IRON_GOLEM) {
                return;
            }
            if (type == org.bukkit.entity.EntityType.SNOW_GOLEM) {
                return;
            }
        }
        type = e.getEntity().getType();
        if (type == org.bukkit.entity.EntityType.IRON_GOLEM) {
            return;
        }
        if (type == org.bukkit.entity.EntityType.SNOW_GOLEM) {
            return;
        }
        if ((e.getEntity() instanceof org.bukkit.entity.Enemy)) {
            e.setCancelled(1);
        }
    }

}
