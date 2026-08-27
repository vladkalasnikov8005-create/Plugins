package org.examplee.leperClassPlugin.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.examplee.leperClassPlugin.LeperClassPlugin;
import org.examplee.leperClassPlugin.util.EntityUtil;
import org.examplee.leperClassPlugin.util.InventoryUtil;

public final class LeperMenuListener implements org.bukkit.event.Listener {
    private final org.examplee.leperClassPlugin.LeperClassPlugin plugin;

    public LeperMenuListener(org.examplee.leperClassPlugin.LeperClassPlugin plugin) {
        super();
        this.plugin = plugin;
    }

    @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onMenuClick(org.bukkit.event.inventory.InventoryClickEvent e) {
        org.bukkit.entity.Player admin = e.getInventory().getHolder();
        if (!((admin instanceof org.examplee.leperClassPlugin.gui.LeperMenuHolder))) {
            return;
        }
        org.examplee.leperClassPlugin.gui.LeperMenuHolder holder = (org.examplee.leperClassPlugin.gui.LeperMenuHolder) admin;
        e.setCancelled(1);
        org.bukkit.entity.Player target = e.getWhoClicked();
        if (!((target instanceof org.bukkit.entity.Player))) {
            return;
        }
        admin = (org.bukkit.entity.Player) target;
        target = org.bukkit.Bukkit.getPlayer(holder.getTarget());
        if (target != null) {
            int slot = e.getRawSlot();
            if (slot < 0) {
                return;
            }
            if (slot < e.getInventory().getSize()) {
                if (slot != 11) {
                    if (slot == 15) {
                        plugin.data.setLeper(target, 0);
                        plugin.infection.cureDataOnly(target);
                        admin.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.GREEN) + target.getName() + " больше не Прокаженный.");
                        plugin.menu.open(admin, target);
                        return;
                    }
                }
                plugin.data.setLeper(target, 1);
                plugin.infection.cureDataOnly(target);
                org.examplee.leperClassPlugin.util.EntityUtil.clearHostileTargets(target, 32.0);
                admin.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.GREEN) + target.getName() + " теперь Прокаженный.");
                plugin.menu.open(admin, target);
                return;
            }
            return;
        }
        slot = e.getRawSlot();
        if (slot < 0) {
            return;
        }
        if (slot < e.getInventory().getSize()) {
            if (slot != 11) {
                if (slot == 15) {
                    plugin.data.setLeper(target, 0);
                    plugin.infection.cureDataOnly(target);
                    admin.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.GREEN) + target.getName() + " больше не Прокаженный.");
                    plugin.menu.open(admin, target);
                    return;
                }
            }
            plugin.data.setLeper(target, 1);
            plugin.infection.cureDataOnly(target);
            org.examplee.leperClassPlugin.util.EntityUtil.clearHostileTargets(target, 32.0);
            admin.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.GREEN) + target.getName() + " теперь Прокаженный.");
            plugin.menu.open(admin, target);
            return;
        }
        if (slot != 11) {
            if (slot == 15) {
                plugin.data.setLeper(target, 0);
                plugin.infection.cureDataOnly(target);
                admin.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.GREEN) + target.getName() + " больше не Прокаженный.");
                plugin.menu.open(admin, target);
                return;
            }
        }
        plugin.data.setLeper(target, 1);
        plugin.infection.cureDataOnly(target);
        org.examplee.leperClassPlugin.util.EntityUtil.clearHostileTargets(target, 32.0);
        admin.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.GREEN) + target.getName() + " теперь Прокаженный.");
        plugin.menu.open(admin, target);
        if (slot == 15) {
            plugin.data.setLeper(target, 0);
            plugin.infection.cureDataOnly(target);
            admin.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.GREEN) + target.getName() + " больше не Прокаженный.");
            plugin.menu.open(admin, target);
            return;
        }
        switch (slot) {
        case 17:
            org.examplee.leperClassPlugin.util.InventoryUtil.giveOrDrop(target, plugin.items.makeUmbrellaTiny());
            break;
            break;
        case 18:
            org.examplee.leperClassPlugin.util.InventoryUtil.giveOrDrop(target, plugin.items.makeUmbrellaWeak());
            break;
            break;
        case 19:
            org.examplee.leperClassPlugin.util.InventoryUtil.giveOrDrop(target, plugin.items.makeUmbrellaNormal());
            break;
            break;
        case 20:
            org.examplee.leperClassPlugin.util.InventoryUtil.giveOrDrop(target, plugin.items.makeUmbrellaStrong());
            break;
            org.examplee.leperClassPlugin.util.InventoryUtil.giveOrDrop(target, plugin.items.makePlagueStick());
            break;
            org.examplee.leperClassPlugin.util.InventoryUtil.giveOrDrop(target, plugin.items.makeSacrificialKnife());
            break;
            org.examplee.leperClassPlugin.util.InventoryUtil.giveOrDrop(target, plugin.items.makePlagueBomb());
            break;
            org.examplee.leperClassPlugin.util.InventoryUtil.giveOrDrop(target, plugin.items.makeLeperBlood());
            break;
            org.examplee.leperClassPlugin.util.InventoryUtil.giveOrDrop(target, plugin.items.makeVaccine());
            break;
        case 21:
            return;
        }
        org.examplee.leperClassPlugin.util.InventoryUtil.giveOrDrop(target, plugin.items.makeUmbrellaTiny());
        org.examplee.leperClassPlugin.util.InventoryUtil.giveOrDrop(target, plugin.items.makeUmbrellaWeak());
        org.examplee.leperClassPlugin.util.InventoryUtil.giveOrDrop(target, plugin.items.makeUmbrellaNormal());
        org.examplee.leperClassPlugin.util.InventoryUtil.giveOrDrop(target, plugin.items.makeUmbrellaStrong());
        org.examplee.leperClassPlugin.util.InventoryUtil.giveOrDrop(target, plugin.items.makePlagueStick());
        org.examplee.leperClassPlugin.util.InventoryUtil.giveOrDrop(target, plugin.items.makeSacrificialKnife());
        org.examplee.leperClassPlugin.util.InventoryUtil.giveOrDrop(target, plugin.items.makePlagueBomb());
        org.examplee.leperClassPlugin.util.InventoryUtil.giveOrDrop(target, plugin.items.makeLeperBlood());
        org.examplee.leperClassPlugin.util.InventoryUtil.giveOrDrop(target, plugin.items.makeVaccine());
    }

}
