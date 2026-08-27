package org.examplee.palePlugin.listeners;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.examplee.palePlugin.PalePlugin;

public final class ItemUseListener implements org.bukkit.event.Listener {
    private final org.examplee.palePlugin.PalePlugin plugin;
    private final java.util.Map lastSaltUse;
    private final java.util.Map lastWandUse;

    public ItemUseListener(org.examplee.palePlugin.PalePlugin plugin) {
        super();
        this.lastSaltUse = new java.util.HashMap();
        this.lastWandUse = new java.util.HashMap();
        this.plugin = plugin;
    }

    @org.bukkit.event.EventHandler
    public void onSaltUse(org.bukkit.event.player.PlayerInteractEvent e) {
        if (e.getHand() == org.bukkit.inventory.EquipmentSlot.HAND) {
            org.bukkit.inventory.ItemStack item = e.getItem();
            if (plugin.items.isSalt(item)) {
                long now = java.lang.System.currentTimeMillis();
                long last = ((java.lang.Long) lastSaltUse.getOrDefault(e.getPlayer().getUniqueId(), java.lang.Long.valueOf(0L))).longValue();
                if (Long.compare(now - last, plugin.cfg.saltCooldownMs) < 0) {
                    e.setCancelled(true);
                    return;
                }
            }
            return;
        }
        item = e.getItem();
        if (plugin.items.isSalt(item)) {
            now = java.lang.System.currentTimeMillis();
            last = ((java.lang.Long) lastSaltUse.getOrDefault(e.getPlayer().getUniqueId(), java.lang.Long.valueOf(0L))).longValue();
            if (Long.compare(now - last, plugin.cfg.saltCooldownMs) < 0) {
                e.setCancelled(true);
                return;
            }
        }
        now = java.lang.System.currentTimeMillis();
        last = ((java.lang.Long) lastSaltUse.getOrDefault(e.getPlayer().getUniqueId(), java.lang.Long.valueOf(0L))).longValue();
        if (Long.compare(now - last, plugin.cfg.saltCooldownMs) < 0) {
            e.setCancelled(true);
            return;
        }
        lastSaltUse.put(e.getPlayer().getUniqueId(), java.lang.Long.valueOf(now));
        if (e.getClickedBlock() != null) {
        } else {
        }
        org.bukkit.Location center = 0;
        int cleaned = plugin.engine.cleanse(center, plugin.cfg.saltRadius);
        if (cleaned > 0) {
            plugin.spread.addCleansed(cleaned);
        }
        consumeOne(e.getPlayer(), item);
        e.setCancelled(true);
    }

    @org.bukkit.event.EventHandler
    public void onHolyWaterSplash(org.bukkit.event.entity.PotionSplashEvent e) {
        org.bukkit.inventory.ItemStack item = e.getPotion().getItem();
        if (!(plugin.items.isHolyWater(item))) {
            return;
        }
        int cleaned = plugin.engine.cleanse(e.getPotion().getLocation(), plugin.cfg.holyWaterRadius);
        if (cleaned > 0) {
            plugin.spread.addCleansed(cleaned);
        }
    }

    @org.bukkit.event.EventHandler
    public void onPurifierFlintUse(org.bukkit.event.player.PlayerInteractEvent e) {
        if (e.getHand() == org.bukkit.inventory.EquipmentSlot.HAND) {
            if (e.getClickedBlock() != null) {
                org.bukkit.inventory.ItemStack item = e.getItem();
                if (plugin.items.isPurifierFlint(item)) {
                    org.bukkit.block.Block clicked = e.getClickedBlock();
                    if (!(plugin.engine.infectedTypes().contains(clicked.getType()))) {
                        if (!(plugin.engine.hasInfectedNear(clicked))) {
                            e.setCancelled(true);
                            return;
                        }
                    }
                }
                return;
            }
            return;
        }
        if (e.getClickedBlock() != null) {
            item = e.getItem();
            if (plugin.items.isPurifierFlint(item)) {
                clicked = e.getClickedBlock();
                if (!(plugin.engine.infectedTypes().contains(clicked.getType()))) {
                    if (!(plugin.engine.hasInfectedNear(clicked))) {
                        e.setCancelled(true);
                        return;
                    }
                }
            }
            return;
        }
        item = e.getItem();
        if (plugin.items.isPurifierFlint(item)) {
            clicked = e.getClickedBlock();
            if (!(plugin.engine.infectedTypes().contains(clicked.getType()))) {
                if (!(plugin.engine.hasInfectedNear(clicked))) {
                    e.setCancelled(true);
                    return;
                }
            }
        }
        clicked = e.getClickedBlock();
        if (!(plugin.engine.infectedTypes().contains(clicked.getType()))) {
            if (!(plugin.engine.hasInfectedNear(clicked))) {
                e.setCancelled(true);
                return;
            }
        }
        int cleaned = plugin.engine.cleanse(clicked.getLocation().add(0.5, 0.5, 0.5), plugin.cfg.purifierFlintRadius);
        if (cleaned > 0) {
            plugin.spread.addCleansed(cleaned);
        }
        int usesLeft = plugin.items.getPurifierFlintUses(item) - 1;
        if (usesLeft <= 0) {
            e.getPlayer().playSound(e.getPlayer().getLocation(), org.bukkit.Sound.ENTITY_ITEM_BREAK, 1.0F, 1.0F);
            e.getPlayer().getInventory().setItemInMainHand(new org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR));
        } else {
            plugin.items.setPurifierFlintUses(item, usesLeft);
            e.getPlayer().playSound(e.getPlayer().getLocation(), org.bukkit.Sound.BLOCK_FIRE_EXTINGUISH, 0.699999988079071F, 1.2000000476837158F);
        }
        e.setCancelled(true);
    }

    @org.bukkit.event.EventHandler
    public void onMapUse(org.bukkit.event.player.PlayerInteractEvent e) {
        if (e.getHand() == org.bukkit.inventory.EquipmentSlot.HAND) {
            org.bukkit.inventory.ItemStack item = e.getItem();
            if (!(plugin.items.isInfectionMap(item))) {
                return;
            }
        }
        item = e.getItem();
        if (!(plugin.items.isInfectionMap(item))) {
            return;
        }
        int r = plugin.items.getMapRadius(item);
        r = java.lang.Math.max(1, java.lang.Math.min(plugin.cfg.mapMaxRadiusChunks, r));
        plugin.engine.sendMap(e.getPlayer(), r);
        e.setCancelled(true);
    }

    @org.bukkit.event.EventHandler
    public void onWandUse(org.bukkit.event.player.PlayerInteractEvent e) {
        if (e.getHand() == org.bukkit.inventory.EquipmentSlot.HAND) {
            org.bukkit.event.block.Action a = e.getAction();
            if (a == org.bukkit.event.block.Action.RIGHT_CLICK_AIR) {
                org.bukkit.inventory.ItemStack item = e.getItem();
                if (plugin.items.isInfectWand(item)) {
                    long now = java.lang.System.currentTimeMillis();
                    long last = ((java.lang.Long) lastWandUse.getOrDefault(e.getPlayer().getUniqueId(), java.lang.Long.valueOf(0L))).longValue();
                    if (Long.compare(now - last, plugin.cfg.infectWandCooldownMs) < 0) {
                        e.setCancelled(true);
                        return;
                    }
                }
                return;
            }
            if (a == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
                item = e.getItem();
                if (plugin.items.isInfectWand(item)) {
                    now = java.lang.System.currentTimeMillis();
                    last = ((java.lang.Long) lastWandUse.getOrDefault(e.getPlayer().getUniqueId(), java.lang.Long.valueOf(0L))).longValue();
                    if (Long.compare(now - last, plugin.cfg.infectWandCooldownMs) < 0) {
                        e.setCancelled(true);
                        return;
                    }
                }
                return;
            }
            return;
        }
        a = e.getAction();
        if (a == org.bukkit.event.block.Action.RIGHT_CLICK_AIR) {
            item = e.getItem();
            if (plugin.items.isInfectWand(item)) {
                now = java.lang.System.currentTimeMillis();
                last = ((java.lang.Long) lastWandUse.getOrDefault(e.getPlayer().getUniqueId(), java.lang.Long.valueOf(0L))).longValue();
                if (Long.compare(now - last, plugin.cfg.infectWandCooldownMs) < 0) {
                    e.setCancelled(true);
                    return;
                }
            }
            return;
        }
        if (a == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            item = e.getItem();
            if (plugin.items.isInfectWand(item)) {
                now = java.lang.System.currentTimeMillis();
                last = ((java.lang.Long) lastWandUse.getOrDefault(e.getPlayer().getUniqueId(), java.lang.Long.valueOf(0L))).longValue();
                if (Long.compare(now - last, plugin.cfg.infectWandCooldownMs) < 0) {
                    e.setCancelled(true);
                    return;
                }
            }
            return;
        }
        item = e.getItem();
        if (plugin.items.isInfectWand(item)) {
            now = java.lang.System.currentTimeMillis();
            last = ((java.lang.Long) lastWandUse.getOrDefault(e.getPlayer().getUniqueId(), java.lang.Long.valueOf(0L))).longValue();
            if (Long.compare(now - last, plugin.cfg.infectWandCooldownMs) < 0) {
                e.setCancelled(true);
                return;
            }
        }
        now = java.lang.System.currentTimeMillis();
        last = ((java.lang.Long) lastWandUse.getOrDefault(e.getPlayer().getUniqueId(), java.lang.Long.valueOf(0L))).longValue();
        if (Long.compare(now - last, plugin.cfg.infectWandCooldownMs) < 0) {
            e.setCancelled(true);
            return;
        }
        lastWandUse.put(e.getPlayer().getUniqueId(), java.lang.Long.valueOf(now));
        org.bukkit.block.Block centerBlock = e.getClickedBlock();
        if (centerBlock == null) {
            try {
                centerBlock = e.getPlayer().getTargetBlockExact(40);
            }
            catch (java.lang.Throwable ex) {
            }
        }
        if (centerBlock != null) {
        } else {
        }
        center = 0;
        int infected = plugin.engine.infectAreaWand(center);
        if (infected > 0) {
            plugin.spread.addInfected(infected);
            e.getPlayer().playSound(e.getPlayer().getLocation(), org.bukkit.Sound.BLOCK_SCULK_CATALYST_BLOOM, 0.800000011920929F, 1.149999976158142F);
        } else {
            e.getPlayer().playSound(e.getPlayer().getLocation(), org.bukkit.Sound.BLOCK_AMETHYST_BLOCK_HIT, 0.5F, 0.6000000238418579F);
        }
        int usesLeft = plugin.items.getInfectWandUses(item) - 1;
        if (usesLeft <= 0) {
            e.getPlayer().playSound(e.getPlayer().getLocation(), org.bukkit.Sound.ENTITY_ITEM_BREAK, 1.0F, 1.0F);
            e.getPlayer().getInventory().setItemInMainHand(new org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR));
        } else {
            plugin.items.setInfectWandUses(item, usesLeft);
        }
        e.setCancelled(true);
    }

    private void consumeOne(org.bukkit.entity.Player p, org.bukkit.inventory.ItemStack it) {
        if (p.getGameMode() == org.bukkit.GameMode.CREATIVE) {
            return;
        }
        int amt = it.getAmount() - 1;
        if (amt <= 0) {
            p.getInventory().setItemInMainHand(new org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR));
        } else {
            it.setAmount(amt);
        }
    }

}
