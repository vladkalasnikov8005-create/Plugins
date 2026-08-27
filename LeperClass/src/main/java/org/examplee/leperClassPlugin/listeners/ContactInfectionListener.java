package org.examplee.leperClassPlugin.listeners;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.examplee.leperClassPlugin.LeperClassPlugin;
import org.examplee.leperClassPlugin.util.TextUtil;

public final class ContactInfectionListener implements org.bukkit.event.Listener {
    private final org.examplee.leperClassPlugin.LeperClassPlugin plugin;
    private final java.util.Map pendingScare;

    public ContactInfectionListener(org.examplee.leperClassPlugin.LeperClassPlugin plugin) {
        super();
        this.pendingScare = new java.util.concurrent.ConcurrentHashMap();
        this.plugin = plugin;
    }

    @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.MONITOR, ignoreCancelled = true)
    public void onHitLeper(org.bukkit.event.entity.EntityDamageByEntityEvent e) {
        org.bukkit.entity.Player damagerPlayer = resolveAttacker(e.getDamager());
        if (damagerPlayer == null) {
            org.bukkit.entity.Player attacker = e.getEntity();
            if (!((attacker instanceof org.bukkit.entity.Player))) {
                return;
            }
            org.bukkit.entity.Player victim = (org.bukkit.entity.Player) attacker;
            return;
        }
        if (!(plugin.data.isLeper(damagerPlayer))) {
            attacker = e.getEntity();
            if (!((attacker instanceof org.bukkit.entity.Player))) {
                return;
            }
            victim = (org.bukkit.entity.Player) attacker;
            return;
        }
        long now = java.lang.System.currentTimeMillis();
        if (Long.compare(plugin.data.getRageUntil(damagerPlayer), now) <= 0) {
            attacker = e.getEntity();
            if (!((attacker instanceof org.bukkit.entity.Player))) {
                return;
            }
            victim = (org.bukkit.entity.Player) attacker;
            return;
        }
        e.setDamage(e.getDamage() * 1.8);
        org.bukkit.util.Vector dir = e.getEntity();
        if (!((dir instanceof org.bukkit.entity.Player))) {
            attacker = e.getEntity();
            if (!((attacker instanceof org.bukkit.entity.Player))) {
                return;
            }
            victim = (org.bukkit.entity.Player) attacker;
            return;
        }
        org.bukkit.entity.Player hit = (org.bukkit.entity.Player) dir;
        dir = hit.getLocation().toVector().subtract(damagerPlayer.getLocation().toVector()).normalize();
        hit.setVelocity(hit.getVelocity().add(dir.multiply(1.9)).setY(0.6));
        attacker = e.getEntity();
        if (!((attacker instanceof org.bukkit.entity.Player))) {
            return;
        }
        victim = (org.bukkit.entity.Player) attacker;
        if (plugin.data.isLeper(victim)) {
            attacker = damagerPlayer;
            if (attacker == null) {
                return;
            }
            if (plugin.data.isLeper(attacker)) {
                return;
            }
        }
        attacker = damagerPlayer;
        if (attacker == null) {
            return;
        }
        if (plugin.data.isLeper(attacker)) {
            return;
        }
        hit = java.lang.Math.max(0.0, e.getFinalDamage());
        double chance = hpDamage * plugin.settings.contactInfectPerHp;
        if (Double.compare(java.util.concurrent.ThreadLocalRandom.current().nextDouble(), chance) < 0) {
        } else {
        }
        boolean realInfection = false;
        if (!realInfection) {
            if (Double.compare(java.util.concurrent.ThreadLocalRandom.current().nextDouble(), plugin.settings.contactFakeScareChance) < 0) {
            } else {
            }
        }
        boolean showScare = false;
        if (showScare) {
            if (!(pendingScare.containsKey(attacker.getUniqueId()))) {
                attacker.sendMessage(org.examplee.leperClassPlugin.util.TextUtil.ui(java.lang.String.valueOf(org.bukkit.ChatColor.DARK_GREEN) + "Вы могли заразиться после контакта с прокаженным..."));
                org.bukkit.scheduler.BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> lambda_onHitLeper_0(attacker, realInfection), plugin.settings.contactResolveTicks);
                pendingScare.put(attacker.getUniqueId(), task);
            }
        }
    }

    private org.bukkit.entity.Player resolveAttacker(org.bukkit.entity.Entity damager) {
        if (!((damager instanceof org.bukkit.entity.Player))) {
            if ((damager instanceof org.bukkit.entity.Projectile)) {
                org.bukkit.entity.Projectile pr = (org.bukkit.entity.Projectile) damager;
                org.bukkit.projectiles.ProjectileSource src = pr.getShooter();
                if ((src instanceof org.bukkit.entity.Player)) {
                    org.bukkit.entity.Player p = (org.bukkit.entity.Player) src;
                    return p;
                }
            }
        }
        p = (org.bukkit.entity.Player) damager;
        return p;
    }

    private void lambda_onHitLeper_0(org.bukkit.entity.Player attacker, boolean realInfection) {
        pendingScare.remove(attacker.getUniqueId());
        if (!(attacker.isOnline())) {
            return;
        }
        if (plugin.data.isLeper(attacker)) {
            return;
        }
        if (realInfection) {
            plugin.infection.startInfection(attacker);
        } else {
            attacker.sendMessage(org.examplee.leperClassPlugin.util.TextUtil.ui(java.lang.String.valueOf(org.bukkit.ChatColor.GREEN) + "Фух, кажется пронесло. Вы не заразились."));
        }
    }

}
