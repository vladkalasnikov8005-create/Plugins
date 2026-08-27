package org.examplee.leperClassPlugin.listeners;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.examplee.leperClassPlugin.LeperClassPlugin;
import org.examplee.leperClassPlugin.util.TextUtil;

public final class VaccineListener implements org.bukkit.event.Listener {
    private final org.examplee.leperClassPlugin.LeperClassPlugin plugin;

    public VaccineListener(org.examplee.leperClassPlugin.LeperClassPlugin plugin) {
        super();
        this.plugin = plugin;
    }

    @org.bukkit.event.EventHandler
    public void onUse(org.bukkit.event.player.PlayerInteractEvent e) {
        org.bukkit.inventory.ItemStack used = e.getItem();
        if (used == null) {
            return;
        }
        if (plugin.tags.isVaccine(used)) {
            if (used.getType() == org.bukkit.Material.POTION) {
                org.bukkit.event.block.Action a = e.getAction();
                if (a == org.bukkit.event.block.Action.RIGHT_CLICK_AIR) {
                    e.setCancelled(true);
                    org.bukkit.entity.Player p = e.getPlayer();
                    if (plugin.data.isLeper(p)) {
                        p.sendMessage(org.examplee.leperClassPlugin.util.TextUtil.ui(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Вам это уже не поможет. Вы - Прокаженный."));
                        return;
                    }
                }
                if (a == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
                    e.setCancelled(true);
                    p = e.getPlayer();
                    if (plugin.data.isLeper(p)) {
                        p.sendMessage(org.examplee.leperClassPlugin.util.TextUtil.ui(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Вам это уже не поможет. Вы - Прокаженный."));
                        return;
                    }
                }
                return;
            }
            return;
        }
        if (used.getType() == org.bukkit.Material.POTION) {
            a = e.getAction();
            if (a == org.bukkit.event.block.Action.RIGHT_CLICK_AIR) {
                e.setCancelled(true);
                p = e.getPlayer();
                if (plugin.data.isLeper(p)) {
                    p.sendMessage(org.examplee.leperClassPlugin.util.TextUtil.ui(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Вам это уже не поможет. Вы - Прокаженный."));
                    return;
                }
            }
            if (a == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
                e.setCancelled(true);
                p = e.getPlayer();
                if (plugin.data.isLeper(p)) {
                    p.sendMessage(org.examplee.leperClassPlugin.util.TextUtil.ui(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Вам это уже не поможет. Вы - Прокаженный."));
                    return;
                }
            }
            return;
        }
        a = e.getAction();
        if (a == org.bukkit.event.block.Action.RIGHT_CLICK_AIR) {
            e.setCancelled(true);
            p = e.getPlayer();
            if (plugin.data.isLeper(p)) {
                p.sendMessage(org.examplee.leperClassPlugin.util.TextUtil.ui(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Вам это уже не поможет. Вы - Прокаженный."));
                return;
            }
        }
        if (a == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            e.setCancelled(true);
            p = e.getPlayer();
            if (plugin.data.isLeper(p)) {
                p.sendMessage(org.examplee.leperClassPlugin.util.TextUtil.ui(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Вам это уже не поможет. Вы - Прокаженный."));
                return;
            }
        }
        e.setCancelled(true);
        p = e.getPlayer();
        if (plugin.data.isLeper(p)) {
            p.sendMessage(org.examplee.leperClassPlugin.util.TextUtil.ui(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Вам это уже не поможет. Вы - Прокаженный."));
            return;
        }
        int stage = plugin.data.getInfectionStage(p);
        if (stage != 1) {
            if (stage == 2) {
                plugin.infection.cure(p);
                if (p.getGameMode() != org.bukkit.GameMode.CREATIVE) {
                    int amt = used.getAmount() - 1;
                    if (amt <= 0) {
                        p.getInventory().setItemInMainHand(new org.bukkit.inventory.ItemStack(org.bukkit.Material.GLASS_BOTTLE));
                    } else {
                        used.setAmount(amt);
                    }
                    p.sendMessage(org.examplee.leperClassPlugin.util.TextUtil.ui(java.lang.String.valueOf(org.bukkit.ChatColor.YELLOW) + "Вы не заражены."));
                }
            } else {
                p.sendMessage(org.examplee.leperClassPlugin.util.TextUtil.ui(java.lang.String.valueOf(org.bukkit.ChatColor.YELLOW) + "Вы не заражены."));
            }
        }
        plugin.infection.cure(p);
        if (p.getGameMode() != org.bukkit.GameMode.CREATIVE) {
            amt = used.getAmount() - 1;
            if (amt <= 0) {
                p.getInventory().setItemInMainHand(new org.bukkit.inventory.ItemStack(org.bukkit.Material.GLASS_BOTTLE));
            } else {
                used.setAmount(amt);
            }
            p.sendMessage(org.examplee.leperClassPlugin.util.TextUtil.ui(java.lang.String.valueOf(org.bukkit.ChatColor.YELLOW) + "Вы не заражены."));
        }
    }

}
