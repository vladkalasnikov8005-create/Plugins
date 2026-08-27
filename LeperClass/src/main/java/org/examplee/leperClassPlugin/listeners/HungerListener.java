package org.examplee.leperClassPlugin.listeners;

import org.bukkit.GameMode;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExhaustionEvent;
import org.examplee.leperClassPlugin.LeperClassPlugin;

public final class HungerListener implements org.bukkit.event.Listener {
    private final org.examplee.leperClassPlugin.LeperClassPlugin plugin;

    public HungerListener(org.examplee.leperClassPlugin.LeperClassPlugin plugin) {
        super();
        this.plugin = plugin;
    }

    @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExhaust(org.bukkit.event.entity.EntityExhaustionEvent e) {
        org.bukkit.entity.HumanEntity local3 = e.getEntity();
        if (!((local3 instanceof org.bukkit.entity.Player))) {
            return;
        }
        org.bukkit.entity.Player p = (org.bukkit.entity.Player) local3;
        if (p.getGameMode() == org.bukkit.GameMode.CREATIVE) {
            return;
        }
        if (p.getGameMode() != org.bukkit.GameMode.SPECTATOR) {
            if (!(plugin.data.isLeper(p))) {
                return;
            }
        }
        if (!(plugin.data.isLeper(p))) {
            return;
        }
        e.setExhaustion(e.getExhaustion() * 4.0F);
    }

}
