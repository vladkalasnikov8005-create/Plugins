package org.examplee.leperClassPlugin.umbrella;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.examplee.leperClassPlugin.LeperClassPlugin;

public final class UmbrellaListener implements org.bukkit.event.Listener {
    private final org.examplee.leperClassPlugin.LeperClassPlugin plugin;

    public UmbrellaListener(org.examplee.leperClassPlugin.LeperClassPlugin plugin) {
        super();
        this.plugin = plugin;
    }

    @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.LOWEST)
    public void onQuit(org.bukkit.event.player.PlayerQuitEvent e) {
        org.bukkit.entity.Player p = e.getPlayer();
        plugin.umbrella.flushOffhand(p);
        plugin.umbrella.forget(p);
    }

    @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.LOWEST)
    public void onSwap(org.bukkit.event.player.PlayerSwapHandItemsEvent e) {
        org.bukkit.entity.Player p = e.getPlayer();
        plugin.umbrella.flushOffhand(p);
        plugin.umbrella.forget(p);
    }

    @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.LOWEST)
    public void onInvClick(org.bukkit.event.inventory.InventoryClickEvent e) {
        org.bukkit.entity.HumanEntity local3 = e.getWhoClicked();
        if (!((local3 instanceof org.bukkit.entity.Player))) {
            return;
        }
        org.bukkit.entity.Player p = (org.bukkit.entity.Player) local3;
        if (!(plugin.umbrella.isTracking(p))) {
            return;
        }
        plugin.umbrella.flushOffhand(p);
        plugin.umbrella.forget(p);
    }

    @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.LOWEST)
    public void onInvDrag(org.bukkit.event.inventory.InventoryDragEvent e) {
        org.bukkit.entity.HumanEntity local3 = e.getWhoClicked();
        if (!((local3 instanceof org.bukkit.entity.Player))) {
            return;
        }
        org.bukkit.entity.Player p = (org.bukkit.entity.Player) local3;
        if (!(plugin.umbrella.isTracking(p))) {
            return;
        }
        plugin.umbrella.flushOffhand(p);
        plugin.umbrella.forget(p);
    }

    @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.LOWEST)
    public void onDrop(org.bukkit.event.player.PlayerDropItemEvent e) {
        org.bukkit.entity.Player p = e.getPlayer();
        if (!(plugin.umbrella.isTracking(p))) {
            return;
        }
        plugin.umbrella.flushOffhand(p);
        plugin.umbrella.forget(p);
    }

    @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.LOWEST)
    public void onDeath(org.bukkit.event.entity.PlayerDeathEvent e) {
        org.bukkit.entity.Player p = e.getEntity();
        plugin.umbrella.flushOffhand(p);
        plugin.umbrella.forget(p);
    }

}
