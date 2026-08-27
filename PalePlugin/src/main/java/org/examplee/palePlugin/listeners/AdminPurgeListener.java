package org.examplee.palePlugin.listeners;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.examplee.palePlugin.PalePlugin;

public final class AdminPurgeListener implements org.bukkit.event.Listener {
    private final org.examplee.palePlugin.PalePlugin plugin;

    public AdminPurgeListener(org.examplee.palePlugin.PalePlugin plugin) {
        super();
        this.plugin = plugin;
    }

    @org.bukkit.event.EventHandler
    public void onUse(org.bukkit.event.player.PlayerInteractEvent e) {
        if (e.getHand() == org.bukkit.inventory.EquipmentSlot.HAND) {
            org.bukkit.inventory.ItemStack it = e.getItem();
            if (plugin.items.isAdminPurgeWand(it)) {
                if (!(e.getPlayer().hasPermission("pale.admin"))) {
                    e.setCancelled(true);
                    return;
                }
            }
            return;
        }
        it = e.getItem();
        if (plugin.items.isAdminPurgeWand(it)) {
            if (!(e.getPlayer().hasPermission("pale.admin"))) {
                e.setCancelled(true);
                return;
            }
        }
        if (!(e.getPlayer().hasPermission("pale.admin"))) {
            e.setCancelled(true);
            return;
        }
        if (e.getClickedBlock() != null) {
        } else {
        }
        org.bukkit.Location center = 0;
        plugin.purge.start(e.getPlayer(), center);
        e.setCancelled(true);
    }

}
