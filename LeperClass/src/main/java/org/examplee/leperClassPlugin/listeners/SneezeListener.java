package org.examplee.leperClassPlugin.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.examplee.leperClassPlugin.LeperClassPlugin;

public final class SneezeListener implements org.bukkit.event.Listener {
    private final org.examplee.leperClassPlugin.LeperClassPlugin plugin;

    public SneezeListener(org.examplee.leperClassPlugin.LeperClassPlugin plugin) {
        super();
        this.plugin = plugin;
    }

    @org.bukkit.event.EventHandler
    public void onSneezeHit(org.bukkit.event.entity.ProjectileHitEvent e) {
        org.bukkit.entity.Projectile pr = e.getEntity();
        java.lang.Byte v = (java.lang.Byte) pr.getPersistentDataContainer().get(plugin.keys.sneezeProjectileKey, org.bukkit.persistence.PersistentDataType.BYTE);
        if (v == null) {
            return;
        }
        if (v.byteValue() != 1) {
            return;
        }
        org.bukkit.entity.Entity local5 = e.getHitEntity();
        if ((local5 instanceof org.bukkit.entity.LivingEntity)) {
            org.bukkit.entity.LivingEntity le = (org.bukkit.entity.LivingEntity) local5;
            if (plugin.effects.POISON != null) {
                le.addPotionEffect(new org.bukkit.potion.PotionEffect(plugin.effects.POISON, plugin.settings.sneezePoisonTicks, 0));
            }
            if (plugin.effects.SLOW != null) {
                le.addPotionEffect(new org.bukkit.potion.PotionEffect(plugin.effects.SLOW, plugin.settings.sneezeSlowTicks, 2));
            }
            if (plugin.effects.WEAKNESS != null) {
                le.addPotionEffect(new org.bukkit.potion.PotionEffect(plugin.effects.WEAKNESS, plugin.settings.sneezeWeakTicks, 1));
            }
            if (plugin.effects.BLINDNESS != null) {
                le.addPotionEffect(new org.bukkit.potion.PotionEffect(plugin.effects.BLINDNESS, plugin.settings.sneezeBlindTicks, 0));
            }
        }
        pr.remove();
    }

}
