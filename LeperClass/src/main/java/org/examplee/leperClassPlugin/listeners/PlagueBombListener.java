package org.examplee.leperClassPlugin.listeners;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;
import org.examplee.leperClassPlugin.LeperClassPlugin;
import org.examplee.leperClassPlugin.util.StunUtil;
import org.examplee.leperClassPlugin.util.TextUtil;

public final class PlagueBombListener implements org.bukkit.event.Listener {
    private final org.examplee.leperClassPlugin.LeperClassPlugin plugin;
    private final java.util.Map cd;

    public PlagueBombListener(org.examplee.leperClassPlugin.LeperClassPlugin plugin) {
        super();
        this.cd = new java.util.HashMap();
        this.plugin = plugin;
    }

    @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST, ignoreCancelled = false)
    public void onUse(org.bukkit.event.player.PlayerInteractEvent e) {
        if (e.getHand() != org.bukkit.inventory.EquipmentSlot.OFF_HAND) {
            org.bukkit.entity.Player p = e.getPlayer();
            org.bukkit.inventory.ItemStack used = e.getItem();
            if (used == null) {
                return;
            }
            if (plugin.tags.isPlagueBomb(used)) {
                org.bukkit.event.block.Action a = e.getAction();
                if (a == org.bukkit.event.block.Action.RIGHT_CLICK_AIR) {
                    if (a != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
                        long now = java.lang.System.currentTimeMillis();
                        if (Long.compare(now - ((java.lang.Long) cd.getOrDefault(p.getUniqueId(), java.lang.Long.valueOf(0L))).longValue(), 3500L) >= 0) {
                            cd.put(p.getUniqueId(), java.lang.Long.valueOf(now));
                            org.bukkit.Location center = resolveCastLocation(p, e);
                            if (center == null) {
                                return;
                            }
                        }
                        return;
                    }
                    if (p.isSneaking()) {
                        now = java.lang.System.currentTimeMillis();
                        if (Long.compare(now - ((java.lang.Long) cd.getOrDefault(p.getUniqueId(), java.lang.Long.valueOf(0L))).longValue(), 3500L) >= 0) {
                            cd.put(p.getUniqueId(), java.lang.Long.valueOf(now));
                            center = resolveCastLocation(p, e);
                            if (center == null) {
                                return;
                            }
                        }
                        return;
                    }
                    e.setCancelled(true);
                    now = java.lang.System.currentTimeMillis();
                    if (Long.compare(now - ((java.lang.Long) cd.getOrDefault(p.getUniqueId(), java.lang.Long.valueOf(0L))).longValue(), 3500L) >= 0) {
                        cd.put(p.getUniqueId(), java.lang.Long.valueOf(now));
                        center = resolveCastLocation(p, e);
                        if (center == null) {
                            return;
                        }
                    }
                    return;
                }
                if (a == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
                    if (a != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
                        now = java.lang.System.currentTimeMillis();
                        if (Long.compare(now - ((java.lang.Long) cd.getOrDefault(p.getUniqueId(), java.lang.Long.valueOf(0L))).longValue(), 3500L) >= 0) {
                            cd.put(p.getUniqueId(), java.lang.Long.valueOf(now));
                            center = resolveCastLocation(p, e);
                            if (center == null) {
                                return;
                            }
                        }
                        return;
                    }
                    if (p.isSneaking()) {
                        now = java.lang.System.currentTimeMillis();
                        if (Long.compare(now - ((java.lang.Long) cd.getOrDefault(p.getUniqueId(), java.lang.Long.valueOf(0L))).longValue(), 3500L) >= 0) {
                            cd.put(p.getUniqueId(), java.lang.Long.valueOf(now));
                            center = resolveCastLocation(p, e);
                            if (center == null) {
                                return;
                            }
                        }
                        return;
                    }
                    e.setCancelled(true);
                    now = java.lang.System.currentTimeMillis();
                    if (Long.compare(now - ((java.lang.Long) cd.getOrDefault(p.getUniqueId(), java.lang.Long.valueOf(0L))).longValue(), 3500L) >= 0) {
                        cd.put(p.getUniqueId(), java.lang.Long.valueOf(now));
                        center = resolveCastLocation(p, e);
                        if (center == null) {
                            return;
                        }
                    }
                    return;
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
        if (plugin.tags.isPlagueBomb(used)) {
            a = e.getAction();
            if (a == org.bukkit.event.block.Action.RIGHT_CLICK_AIR) {
                if (a != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
                    now = java.lang.System.currentTimeMillis();
                    if (Long.compare(now - ((java.lang.Long) cd.getOrDefault(p.getUniqueId(), java.lang.Long.valueOf(0L))).longValue(), 3500L) >= 0) {
                        cd.put(p.getUniqueId(), java.lang.Long.valueOf(now));
                        center = resolveCastLocation(p, e);
                        if (center == null) {
                            return;
                        }
                    }
                    return;
                }
                if (p.isSneaking()) {
                    now = java.lang.System.currentTimeMillis();
                    if (Long.compare(now - ((java.lang.Long) cd.getOrDefault(p.getUniqueId(), java.lang.Long.valueOf(0L))).longValue(), 3500L) >= 0) {
                        cd.put(p.getUniqueId(), java.lang.Long.valueOf(now));
                        center = resolveCastLocation(p, e);
                        if (center == null) {
                            return;
                        }
                    }
                    return;
                }
                e.setCancelled(true);
                now = java.lang.System.currentTimeMillis();
                if (Long.compare(now - ((java.lang.Long) cd.getOrDefault(p.getUniqueId(), java.lang.Long.valueOf(0L))).longValue(), 3500L) >= 0) {
                    cd.put(p.getUniqueId(), java.lang.Long.valueOf(now));
                    center = resolveCastLocation(p, e);
                    if (center == null) {
                        return;
                    }
                }
                return;
            }
            if (a == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
                if (a != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
                    now = java.lang.System.currentTimeMillis();
                    if (Long.compare(now - ((java.lang.Long) cd.getOrDefault(p.getUniqueId(), java.lang.Long.valueOf(0L))).longValue(), 3500L) >= 0) {
                        cd.put(p.getUniqueId(), java.lang.Long.valueOf(now));
                        center = resolveCastLocation(p, e);
                        if (center == null) {
                            return;
                        }
                    }
                    return;
                }
                if (p.isSneaking()) {
                    now = java.lang.System.currentTimeMillis();
                    if (Long.compare(now - ((java.lang.Long) cd.getOrDefault(p.getUniqueId(), java.lang.Long.valueOf(0L))).longValue(), 3500L) >= 0) {
                        cd.put(p.getUniqueId(), java.lang.Long.valueOf(now));
                        center = resolveCastLocation(p, e);
                        if (center == null) {
                            return;
                        }
                    }
                    return;
                }
                e.setCancelled(true);
                now = java.lang.System.currentTimeMillis();
                if (Long.compare(now - ((java.lang.Long) cd.getOrDefault(p.getUniqueId(), java.lang.Long.valueOf(0L))).longValue(), 3500L) >= 0) {
                    cd.put(p.getUniqueId(), java.lang.Long.valueOf(now));
                    center = resolveCastLocation(p, e);
                    if (center == null) {
                        return;
                    }
                }
                return;
            }
            return;
        }
        a = e.getAction();
        if (a == org.bukkit.event.block.Action.RIGHT_CLICK_AIR) {
            if (a != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
                now = java.lang.System.currentTimeMillis();
                if (Long.compare(now - ((java.lang.Long) cd.getOrDefault(p.getUniqueId(), java.lang.Long.valueOf(0L))).longValue(), 3500L) >= 0) {
                    cd.put(p.getUniqueId(), java.lang.Long.valueOf(now));
                    center = resolveCastLocation(p, e);
                    if (center == null) {
                        return;
                    }
                }
                return;
            }
            if (p.isSneaking()) {
                now = java.lang.System.currentTimeMillis();
                if (Long.compare(now - ((java.lang.Long) cd.getOrDefault(p.getUniqueId(), java.lang.Long.valueOf(0L))).longValue(), 3500L) >= 0) {
                    cd.put(p.getUniqueId(), java.lang.Long.valueOf(now));
                    center = resolveCastLocation(p, e);
                    if (center == null) {
                        return;
                    }
                }
                return;
            }
            e.setCancelled(true);
            now = java.lang.System.currentTimeMillis();
            if (Long.compare(now - ((java.lang.Long) cd.getOrDefault(p.getUniqueId(), java.lang.Long.valueOf(0L))).longValue(), 3500L) >= 0) {
                cd.put(p.getUniqueId(), java.lang.Long.valueOf(now));
                center = resolveCastLocation(p, e);
                if (center == null) {
                    return;
                }
            }
            return;
        }
        if (a == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            if (a != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
                now = java.lang.System.currentTimeMillis();
                if (Long.compare(now - ((java.lang.Long) cd.getOrDefault(p.getUniqueId(), java.lang.Long.valueOf(0L))).longValue(), 3500L) >= 0) {
                    cd.put(p.getUniqueId(), java.lang.Long.valueOf(now));
                    center = resolveCastLocation(p, e);
                    if (center == null) {
                        return;
                    }
                }
                return;
            }
            if (p.isSneaking()) {
                now = java.lang.System.currentTimeMillis();
                if (Long.compare(now - ((java.lang.Long) cd.getOrDefault(p.getUniqueId(), java.lang.Long.valueOf(0L))).longValue(), 3500L) >= 0) {
                    cd.put(p.getUniqueId(), java.lang.Long.valueOf(now));
                    center = resolveCastLocation(p, e);
                    if (center == null) {
                        return;
                    }
                }
                return;
            }
            e.setCancelled(true);
            now = java.lang.System.currentTimeMillis();
            if (Long.compare(now - ((java.lang.Long) cd.getOrDefault(p.getUniqueId(), java.lang.Long.valueOf(0L))).longValue(), 3500L) >= 0) {
                cd.put(p.getUniqueId(), java.lang.Long.valueOf(now));
                center = resolveCastLocation(p, e);
                if (center == null) {
                    return;
                }
            }
            return;
        }
        if (a != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            now = java.lang.System.currentTimeMillis();
            if (Long.compare(now - ((java.lang.Long) cd.getOrDefault(p.getUniqueId(), java.lang.Long.valueOf(0L))).longValue(), 3500L) >= 0) {
                cd.put(p.getUniqueId(), java.lang.Long.valueOf(now));
                center = resolveCastLocation(p, e);
                if (center == null) {
                    return;
                }
            }
            return;
        }
        if (p.isSneaking()) {
            now = java.lang.System.currentTimeMillis();
            if (Long.compare(now - ((java.lang.Long) cd.getOrDefault(p.getUniqueId(), java.lang.Long.valueOf(0L))).longValue(), 3500L) >= 0) {
                cd.put(p.getUniqueId(), java.lang.Long.valueOf(now));
                center = resolveCastLocation(p, e);
                if (center == null) {
                    return;
                }
            }
            return;
        }
        e.setCancelled(true);
        now = java.lang.System.currentTimeMillis();
        if (Long.compare(now - ((java.lang.Long) cd.getOrDefault(p.getUniqueId(), java.lang.Long.valueOf(0L))).longValue(), 3500L) >= 0) {
            cd.put(p.getUniqueId(), java.lang.Long.valueOf(now));
            center = resolveCastLocation(p, e);
            if (center == null) {
                return;
            }
        }
        cd.put(p.getUniqueId(), java.lang.Long.valueOf(now));
        center = resolveCastLocation(p, e);
        if (center == null) {
            return;
        }
        castCloud(p, center);
        if (p.getGameMode() != org.bukkit.GameMode.CREATIVE) {
            int amt = used.getAmount() - 1;
            if (amt <= 0) {
                p.getInventory().setItemInMainHand(new org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR));
            } else {
                used.setAmount(amt);
            }
        }
    }

    private org.bukkit.Location resolveCastLocation(org.bukkit.entity.Player p, org.bukkit.event.player.PlayerInteractEvent e) {
        if (e.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            try {
                org.bukkit.block.Block target = p.getTargetBlockExact(16);
            }
            catch (java.lang.Throwable ex) {
                target = null;
                if (target != null) {
                    return target.getLocation().add(0.5, 1.0, 0.5);
                }
            }
        }
        if (e.getClickedBlock() == null) {
            try {
                target = p.getTargetBlockExact(16);
            }
            catch (java.lang.Throwable ex) {
            }
        }
        return e.getClickedBlock().getLocation().add(0.5, 1.0, 0.5);
    }

    private void castCloud(org.bukkit.entity.Player caster, org.bukkit.Location center) {
        org.bukkit.World w = center.getWorld();
        if (w == null) {
            return;
        }
        double radius = 4.8;
        int durationTicks = 120;
        try {
            org.bukkit.entity.Entity ent = w.spawnEntity(center, org.bukkit.entity.EntityType.AREA_EFFECT_CLOUD);
            if ((ent instanceof org.bukkit.entity.AreaEffectCloud)) {
                org.bukkit.entity.AreaEffectCloud cloud = (org.bukkit.entity.AreaEffectCloud) ent;
                cloud.setRadius((float) radius);
                cloud.setDuration(durationTicks);
                cloud.setWaitTime(0);
                cloud.setRadiusPerTick(-((float) (radius / (double) durationTicks)));
                cloud.setColor(org.examplee.leperClassPlugin.util.TextUtil.C_GREEN);
                cloud.addCustomEffect(new org.bukkit.potion.PotionEffect(plugin.effects.POISON, durationTicks, 1), 1);
                cloud.setSource(caster);
            }
        }
        catch (java.lang.Throwable ent) {
            ent = w.getNearbyEntities(center, radius, 2.8, radius).iterator();
            if (ent.hasNext()) {
                cloud = (org.bukkit.entity.Entity) ent.next();
                if (!((en instanceof org.bukkit.entity.LivingEntity))) { /* continue */ }
                org.bukkit.entity.LivingEntity le = (org.bukkit.entity.LivingEntity) en;
                if (le.getUniqueId().equals(caster.getUniqueId())) {
                    /* continue */
                }
                le.addPotionEffect(new org.bukkit.potion.PotionEffect(plugin.effects.POISON, 120, 1));
                if ((le instanceof org.bukkit.entity.Player)) {
                    org.bukkit.entity.Player pl = (org.bukkit.entity.Player) le;
                    org.examplee.leperClassPlugin.util.StunUtil.stun(plugin, pl, 40);
                    if (plugin.effects.BLINDNESS != null) {
                        pl.addPotionEffect(new org.bukkit.potion.PotionEffect(plugin.effects.BLINDNESS, 40, 0, false, false, false));
                    }
                    if (plugin.data.isDangerBlessed(caster)) {
                        if (!(plugin.data.isLeper(pl))) {
                            plugin.infection.addHit(pl);
                        }
                    }
                }
                /* continue */
            }
            new org.examplee.leperClassPlugin.listeners.PlagueBombListener$1(this, durationTicks, w, center).runTaskTimer(plugin, 0L, 10L);
            if (plugin.data.isDangerBlessed(caster)) {
                plugin.paleHook.infect(center, 5, 1400);
            }
            return;
        }
    }

}
