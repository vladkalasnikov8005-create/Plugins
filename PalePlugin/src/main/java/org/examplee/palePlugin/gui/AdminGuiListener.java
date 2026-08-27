package org.examplee.palePlugin.gui;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.examplee.palePlugin.PalePlugin;
import org.examplee.palePlugin.util.InvUtil;
import org.examplee.palePlugin.util.MathUtil;

public final class AdminGuiListener implements org.bukkit.event.Listener {
    private final org.examplee.palePlugin.PalePlugin plugin;

    public AdminGuiListener(org.examplee.palePlugin.PalePlugin plugin) {
        super();
        this.plugin = plugin;
    }

    @org.bukkit.event.EventHandler
    public void onClick(org.bukkit.event.inventory.InventoryClickEvent e) {
        if ("Pale: Admin".equals(e.getView().getTitle())) {
            e.setCancelled(true);
            org.bukkit.inventory.ItemStack it = e.getWhoClicked();
            if (!((it instanceof org.bukkit.entity.Player))) {
                return;
            }
            org.bukkit.entity.Player p = (org.bukkit.entity.Player) it;
            return;
        }
        e.setCancelled(true);
        it = e.getWhoClicked();
        if (!((it instanceof org.bukkit.entity.Player))) {
            return;
        }
        p = (org.bukkit.entity.Player) it;
        it = e.getCurrentItem();
        if (it != null) {
            org.bukkit.inventory.meta.ItemMeta meta = it.getItemMeta();
            if (meta != null) {
                java.lang.String action = (java.lang.String) meta.getPersistentDataContainer().get(plugin.keys.KEY_GUI_ACTION, org.bukkit.persistence.PersistentDataType.STRING);
                if (action == null) {
                    return;
                }
            }
            return;
        }
        meta = it.getItemMeta();
        if (meta != null) {
            action = (java.lang.String) meta.getPersistentDataContainer().get(plugin.keys.KEY_GUI_ACTION, org.bukkit.persistence.PersistentDataType.STRING);
            if (action == null) {
                return;
            }
        }
        action = (java.lang.String) meta.getPersistentDataContainer().get(plugin.keys.KEY_GUI_ACTION, org.bukkit.persistence.PersistentDataType.STRING);
        if (action == null) {
            return;
        }
        java.lang.String local6 = action;
        int local7 = -1;
        switch (local6.hashCode()) {
        default:
            break;
        }
        if (!(local6.equals("toggle"))) {
            switch (local7) {
            case 0:
                if (p.hasPermission("pale.admin")) {
                    if (plugin.spread.isRunning()) {
                        setRunning(0);
                        p.openInventory(plugin.adminGui.build(p));
                        break;
                        if (!(p.hasPermission("pale.admin"))) {
                            p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Нет прав.");
                            return;
                        }
                    }
                    1.setRunning(0);
                    p.openInventory(plugin.adminGui.build(p));
                    if (!(p.hasPermission("pale.admin"))) {
                        p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Нет прав.");
                        return;
                    }
                }
                p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Нет прав.");
                return;
            }
            if (p.hasPermission("pale.admin")) {
                if (plugin.spread.isRunning()) {
                    setRunning(0);
                    p.openInventory(plugin.adminGui.build(p));
                    if (!(p.hasPermission("pale.admin"))) {
                        p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Нет прав.");
                        return;
                    }
                }
                1.setRunning(0);
                p.openInventory(plugin.adminGui.build(p));
                if (!(p.hasPermission("pale.admin"))) {
                    p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Нет прав.");
                    return;
                }
            }
            p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Нет прав.");
            return;
        }
        local7 = 0;
        if (!(local6.equals("speed"))) {
            switch (local7) {
            case 0:
                if (p.hasPermission("pale.admin")) {
                    if (plugin.spread.isRunning()) {
                        setRunning(0);
                        p.openInventory(plugin.adminGui.build(p));
                        break;
                        if (!(p.hasPermission("pale.admin"))) {
                            p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Нет прав.");
                            return;
                        }
                    }
                    1.setRunning(0);
                    p.openInventory(plugin.adminGui.build(p));
                    if (!(p.hasPermission("pale.admin"))) {
                        p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Нет прав.");
                        return;
                    }
                }
                p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Нет прав.");
                return;
            }
            if (p.hasPermission("pale.admin")) {
                if (plugin.spread.isRunning()) {
                    setRunning(0);
                    p.openInventory(plugin.adminGui.build(p));
                    if (!(p.hasPermission("pale.admin"))) {
                        p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Нет прав.");
                        return;
                    }
                }
                1.setRunning(0);
                p.openInventory(plugin.adminGui.build(p));
                if (!(p.hasPermission("pale.admin"))) {
                    p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Нет прав.");
                    return;
                }
            }
            p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Нет прав.");
            return;
        }
        local7 = 1;
        if (!(local6.equals("map_self"))) {
            switch (local7) {
            case 0:
                if (p.hasPermission("pale.admin")) {
                    if (plugin.spread.isRunning()) {
                        setRunning(0);
                        p.openInventory(plugin.adminGui.build(p));
                        break;
                        if (!(p.hasPermission("pale.admin"))) {
                            p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Нет прав.");
                            return;
                        }
                    }
                    1.setRunning(0);
                    p.openInventory(plugin.adminGui.build(p));
                    if (!(p.hasPermission("pale.admin"))) {
                        p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Нет прав.");
                        return;
                    }
                }
                p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Нет прав.");
                return;
            }
            if (p.hasPermission("pale.admin")) {
                if (plugin.spread.isRunning()) {
                    setRunning(0);
                    p.openInventory(plugin.adminGui.build(p));
                    if (!(p.hasPermission("pale.admin"))) {
                        p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Нет прав.");
                        return;
                    }
                }
                1.setRunning(0);
                p.openInventory(plugin.adminGui.build(p));
                if (!(p.hasPermission("pale.admin"))) {
                    p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Нет прав.");
                    return;
                }
            }
            p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Нет прав.");
            return;
        }
        local7 = 2;
        if (!(local6.equals("purge_self"))) {
            switch (local7) {
            case 0:
                if (p.hasPermission("pale.admin")) {
                    if (plugin.spread.isRunning()) {
                        setRunning(0);
                        p.openInventory(plugin.adminGui.build(p));
                        break;
                        if (!(p.hasPermission("pale.admin"))) {
                            p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Нет прав.");
                            return;
                        }
                    }
                    1.setRunning(0);
                    p.openInventory(plugin.adminGui.build(p));
                    if (!(p.hasPermission("pale.admin"))) {
                        p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Нет прав.");
                        return;
                    }
                }
                p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Нет прав.");
                return;
            }
            if (p.hasPermission("pale.admin")) {
                if (plugin.spread.isRunning()) {
                    setRunning(0);
                    p.openInventory(plugin.adminGui.build(p));
                    if (!(p.hasPermission("pale.admin"))) {
                        p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Нет прав.");
                        return;
                    }
                }
                1.setRunning(0);
                p.openInventory(plugin.adminGui.build(p));
                if (!(p.hasPermission("pale.admin"))) {
                    p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Нет прав.");
                    return;
                }
            }
            p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Нет прав.");
            return;
        }
        local7 = 3;
        if (!(local6.equals("wand_self"))) {
            switch (local7) {
            case 0:
                if (p.hasPermission("pale.admin")) {
                    if (plugin.spread.isRunning()) {
                        setRunning(0);
                        p.openInventory(plugin.adminGui.build(p));
                        break;
                        if (!(p.hasPermission("pale.admin"))) {
                            p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Нет прав.");
                            return;
                        }
                    }
                    1.setRunning(0);
                    p.openInventory(plugin.adminGui.build(p));
                    if (!(p.hasPermission("pale.admin"))) {
                        p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Нет прав.");
                        return;
                    }
                }
                p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Нет прав.");
                return;
            }
            if (p.hasPermission("pale.admin")) {
                if (plugin.spread.isRunning()) {
                    setRunning(0);
                    p.openInventory(plugin.adminGui.build(p));
                    if (!(p.hasPermission("pale.admin"))) {
                        p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Нет прав.");
                        return;
                    }
                }
                1.setRunning(0);
                p.openInventory(plugin.adminGui.build(p));
                if (!(p.hasPermission("pale.admin"))) {
                    p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Нет прав.");
                    return;
                }
            }
            p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Нет прав.");
            return;
        }
        local7 = 4;
        switch (local7) {
        case 0:
            if (p.hasPermission("pale.admin")) {
                if (plugin.spread.isRunning()) {
                    setRunning(0);
                    p.openInventory(plugin.adminGui.build(p));
                    break;
                    if (!(p.hasPermission("pale.admin"))) {
                        p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Нет прав.");
                        return;
                    }
                }
                1.setRunning(0);
                p.openInventory(plugin.adminGui.build(p));
                if (!(p.hasPermission("pale.admin"))) {
                    p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Нет прав.");
                    return;
                }
            }
            p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Нет прав.");
            return;
        }
        if (p.hasPermission("pale.admin")) {
            if (plugin.spread.isRunning()) {
                setRunning(0);
                p.openInventory(plugin.adminGui.build(p));
                if (!(p.hasPermission("pale.admin"))) {
                    p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Нет прав.");
                    return;
                }
            }
            1.setRunning(0);
            p.openInventory(plugin.adminGui.build(p));
            if (!(p.hasPermission("pale.admin"))) {
                p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Нет прав.");
                return;
            }
        }
        p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Нет прав.");
        if (plugin.spread.isRunning()) {
            setRunning(0);
            p.openInventory(plugin.adminGui.build(p));
            if (!(p.hasPermission("pale.admin"))) {
                p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Нет прав.");
                return;
            }
        }
        1.setRunning(0);
        p.openInventory(plugin.adminGui.build(p));
        if (!(p.hasPermission("pale.admin"))) {
            p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Нет прав.");
            return;
        }
        delta = 0;
        right = e.isRightClick();
        shift = e.isShiftClick();
        if (right == 0) {
            if (shift == 0) {
                delta = -50;
            }
        }
        if (right != 0) {
            if (shift == 0) {
                delta = 50;
            }
        }
        if (right == 0) {
            if (shift != 0) {
                delta = -500;
            }
        }
        if (right != 0) {
            if (shift != 0) {
                delta = 500;
            }
        }
        plugin.cfg.speedPerChunk = org.examplee.palePlugin.util.MathUtil.clamp(plugin.cfg.speedPerChunk + delta, 1, 5000);
        plugin.getConfig().set("spread.speedPerChunk", java.lang.Integer.valueOf(plugin.cfg.speedPerChunk));
        plugin.saveConfig();
        p.openInventory(plugin.adminGui.build(p));
        org.examplee.palePlugin.util.InvUtil.giveOrDrop(p, plugin.items.makeInfectionMap(1, plugin.cfg.mapItemDefaultRadiusChunks));
        p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.GREEN) + "Выдано: карта заражения.");
        if (!(p.hasPermission("pale.admin"))) {
            p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Нет прав.");
            return;
        }
        org.examplee.palePlugin.util.InvUtil.giveOrDrop(p, plugin.items.makeAdminPurgeWand(1));
        p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.GREEN) + "Выдано: жезл purge.");
        org.examplee.palePlugin.util.InvUtil.giveOrDrop(p, plugin.items.makeInfectWand(1, plugin.cfg.infectWandUsesDefault));
        p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.GREEN) + "Выдано: палочка заразы.");
    }

    @org.bukkit.event.EventHandler
    public void onDrag(org.bukkit.event.inventory.InventoryDragEvent e) {
        if ("Pale: Admin".equals(e.getView().getTitle())) {
            e.setCancelled(true);
        }
    }

}
