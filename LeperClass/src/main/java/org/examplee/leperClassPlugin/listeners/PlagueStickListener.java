package org.examplee.leperClassPlugin.listeners;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.examplee.leperClassPlugin.LeperClassPlugin;
import org.examplee.leperClassPlugin.util.Compat;
import org.examplee.leperClassPlugin.util.ParticlesUtil;
import org.examplee.leperClassPlugin.util.StunUtil;
import org.examplee.leperClassPlugin.util.TextUtil;

public final class PlagueStickListener implements org.bukkit.event.Listener {
    private final org.examplee.leperClassPlugin.LeperClassPlugin plugin;
    private final java.util.Map rcCooldown;

    public PlagueStickListener(org.examplee.leperClassPlugin.LeperClassPlugin plugin) {
        super();
        this.rcCooldown = new java.util.HashMap();
        this.plugin = plugin;
    }

    @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST, ignoreCancelled = false)
    public void onRightClick(org.bukkit.event.player.PlayerInteractEvent e) {
        if (e.getHand() != org.bukkit.inventory.EquipmentSlot.OFF_HAND) {
            org.bukkit.entity.Player p = e.getPlayer();
            org.bukkit.inventory.ItemStack used = e.getItem();
            if (used == null) {
                return;
            }
            if (plugin.tags.isPlagueStick(used)) {
                org.bukkit.event.block.Action a = e.getAction();
                if (a == org.bukkit.event.block.Action.RIGHT_CLICK_AIR) {
                    if (a != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
                        long now = java.lang.System.currentTimeMillis();
                        if (Long.compare(now - ((java.lang.Long) rcCooldown.getOrDefault(p.getUniqueId(), java.lang.Long.valueOf(0L))).longValue(), 700L) < 0) {
                            return;
                        }
                    }
                    if (p.isSneaking()) {
                        now = java.lang.System.currentTimeMillis();
                        if (Long.compare(now - ((java.lang.Long) rcCooldown.getOrDefault(p.getUniqueId(), java.lang.Long.valueOf(0L))).longValue(), 700L) < 0) {
                            return;
                        }
                    }
                    e.setCancelled(1);
                    now = java.lang.System.currentTimeMillis();
                    if (Long.compare(now - ((java.lang.Long) rcCooldown.getOrDefault(p.getUniqueId(), java.lang.Long.valueOf(0L))).longValue(), 700L) < 0) {
                        return;
                    }
                }
                if (a == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
                    if (a != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
                        now = java.lang.System.currentTimeMillis();
                        if (Long.compare(now - ((java.lang.Long) rcCooldown.getOrDefault(p.getUniqueId(), java.lang.Long.valueOf(0L))).longValue(), 700L) < 0) {
                            return;
                        }
                    }
                    if (p.isSneaking()) {
                        now = java.lang.System.currentTimeMillis();
                        if (Long.compare(now - ((java.lang.Long) rcCooldown.getOrDefault(p.getUniqueId(), java.lang.Long.valueOf(0L))).longValue(), 700L) < 0) {
                            return;
                        }
                    }
                    e.setCancelled(1);
                    now = java.lang.System.currentTimeMillis();
                    if (Long.compare(now - ((java.lang.Long) rcCooldown.getOrDefault(p.getUniqueId(), java.lang.Long.valueOf(0L))).longValue(), 700L) < 0) {
                        return;
                    }
                }
                return;
            }
            return;
        }
        p = e.getPlayer();
        used = e.getItem();
        if (used == null) {
            return;
        }
        if (plugin.tags.isPlagueStick(used)) {
            a = e.getAction();
            if (a == org.bukkit.event.block.Action.RIGHT_CLICK_AIR) {
                if (a != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
                    now = java.lang.System.currentTimeMillis();
                    if (Long.compare(now - ((java.lang.Long) rcCooldown.getOrDefault(p.getUniqueId(), java.lang.Long.valueOf(0L))).longValue(), 700L) < 0) {
                        return;
                    }
                }
                if (p.isSneaking()) {
                    now = java.lang.System.currentTimeMillis();
                    if (Long.compare(now - ((java.lang.Long) rcCooldown.getOrDefault(p.getUniqueId(), java.lang.Long.valueOf(0L))).longValue(), 700L) < 0) {
                        return;
                    }
                }
                e.setCancelled(1);
                now = java.lang.System.currentTimeMillis();
                if (Long.compare(now - ((java.lang.Long) rcCooldown.getOrDefault(p.getUniqueId(), java.lang.Long.valueOf(0L))).longValue(), 700L) < 0) {
                    return;
                }
            }
            if (a == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
                if (a != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
                    now = java.lang.System.currentTimeMillis();
                    if (Long.compare(now - ((java.lang.Long) rcCooldown.getOrDefault(p.getUniqueId(), java.lang.Long.valueOf(0L))).longValue(), 700L) < 0) {
                        return;
                    }
                }
                if (p.isSneaking()) {
                    now = java.lang.System.currentTimeMillis();
                    if (Long.compare(now - ((java.lang.Long) rcCooldown.getOrDefault(p.getUniqueId(), java.lang.Long.valueOf(0L))).longValue(), 700L) < 0) {
                        return;
                    }
                }
                e.setCancelled(1);
                now = java.lang.System.currentTimeMillis();
                if (Long.compare(now - ((java.lang.Long) rcCooldown.getOrDefault(p.getUniqueId(), java.lang.Long.valueOf(0L))).longValue(), 700L) < 0) {
                    return;
                }
            }
            return;
        }
        a = e.getAction();
        if (a == org.bukkit.event.block.Action.RIGHT_CLICK_AIR) {
            if (a != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
                now = java.lang.System.currentTimeMillis();
                if (Long.compare(now - ((java.lang.Long) rcCooldown.getOrDefault(p.getUniqueId(), java.lang.Long.valueOf(0L))).longValue(), 700L) < 0) {
                    return;
                }
            }
            if (p.isSneaking()) {
                now = java.lang.System.currentTimeMillis();
                if (Long.compare(now - ((java.lang.Long) rcCooldown.getOrDefault(p.getUniqueId(), java.lang.Long.valueOf(0L))).longValue(), 700L) < 0) {
                    return;
                }
            }
            e.setCancelled(1);
            now = java.lang.System.currentTimeMillis();
            if (Long.compare(now - ((java.lang.Long) rcCooldown.getOrDefault(p.getUniqueId(), java.lang.Long.valueOf(0L))).longValue(), 700L) < 0) {
                return;
            }
        }
        if (a == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            if (a != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
                now = java.lang.System.currentTimeMillis();
                if (Long.compare(now - ((java.lang.Long) rcCooldown.getOrDefault(p.getUniqueId(), java.lang.Long.valueOf(0L))).longValue(), 700L) < 0) {
                    return;
                }
            }
            if (p.isSneaking()) {
                now = java.lang.System.currentTimeMillis();
                if (Long.compare(now - ((java.lang.Long) rcCooldown.getOrDefault(p.getUniqueId(), java.lang.Long.valueOf(0L))).longValue(), 700L) < 0) {
                    return;
                }
            }
            e.setCancelled(1);
            now = java.lang.System.currentTimeMillis();
            if (Long.compare(now - ((java.lang.Long) rcCooldown.getOrDefault(p.getUniqueId(), java.lang.Long.valueOf(0L))).longValue(), 700L) < 0) {
                return;
            }
        }
        if (a != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            now = java.lang.System.currentTimeMillis();
            if (Long.compare(now - ((java.lang.Long) rcCooldown.getOrDefault(p.getUniqueId(), java.lang.Long.valueOf(0L))).longValue(), 700L) < 0) {
                return;
            }
        }
        if (p.isSneaking()) {
            now = java.lang.System.currentTimeMillis();
            if (Long.compare(now - ((java.lang.Long) rcCooldown.getOrDefault(p.getUniqueId(), java.lang.Long.valueOf(0L))).longValue(), 700L) < 0) {
                return;
            }
        }
        e.setCancelled(1);
        now = java.lang.System.currentTimeMillis();
        if (Long.compare(now - ((java.lang.Long) rcCooldown.getOrDefault(p.getUniqueId(), java.lang.Long.valueOf(0L))).longValue(), 700L) < 0) {
            return;
        }
        rcCooldown.put(p.getUniqueId(), java.lang.Long.valueOf(now));
        java.lang.String[] tmp1 = new java.lang.String[3];
        tmp1[0] = "ENTITY_PANDA_SNEEZE";
        tmp1[1] = "ENTITY_SLIME_SQUISH";
        tmp1[2] = "ENTITY_SLIME_SQUISH_SMALL";
        org.bukkit.Sound s = org.examplee.leperClassPlugin.util.Compat.soundFirst(tmp1);
        p.getWorld().playSound(p.getLocation(), s, 0.8500000238418579F, 0.949999988079071F);
        org.bukkit.Location eye = p.getEyeLocation();
        org.examplee.leperClassPlugin.util.ParticlesUtil.greenDust(p.getWorld(), eye, 10, 0.18, 0.18, 0.18, 1.600000023841858F);
    }

    @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHit(org.bukkit.event.entity.EntityDamageByEntityEvent e) {
        org.bukkit.entity.LivingEntity target = e.getDamager();
        if (!((target instanceof org.bukkit.entity.Player))) {
            return;
        }
        org.bukkit.entity.Player damager = (org.bukkit.entity.Player) target;
        org.bukkit.inventory.ItemStack hand = e.getEntity();
        if (!((hand instanceof org.bukkit.entity.LivingEntity))) {
            return;
        }
        target = (org.bukkit.entity.LivingEntity) hand;
        hand = damager.getInventory().getItemInMainHand();
        if (!(plugin.tags.isPlagueStick(hand))) {
            return;
        }
        target.addPotionEffect(new org.bukkit.potion.PotionEffect(plugin.effects.POISON, 100, 1));
        if ((target instanceof org.bukkit.entity.Player)) {
            org.bukkit.entity.Player tp = (org.bukkit.entity.Player) target;
            org.examplee.leperClassPlugin.util.StunUtil.stun(plugin, tp, 40);
            tp.sendMessage(org.examplee.leperClassPlugin.util.TextUtil.ui(java.lang.String.valueOf(org.bukkit.ChatColor.DARK_GREEN) + "Тебя оглушила проказа!"));
            if (plugin.data.isDangerBlessed(damager)) {
                if (!(plugin.data.isLeper(tp))) {
                    plugin.infection.addHit(tp);
                }
            }
        }
        org.examplee.leperClassPlugin.util.ParticlesUtil.greenDust(target.getWorld(), target.getLocation().add(0.0, 1.0, 0.0), 20, 0.4, 0.6, 0.4, 1.600000023841858F);
    }

}
