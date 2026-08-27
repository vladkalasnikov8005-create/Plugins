package org.examplee.leperClassPlugin.listeners;

import java.util.Collection;
import java.util.Iterator;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.examplee.leperClassPlugin.LeperClassPlugin;

public final class UndeadPotionInversionListener implements org.bukkit.event.Listener {
    private final org.examplee.leperClassPlugin.LeperClassPlugin plugin;

    public UndeadPotionInversionListener(org.examplee.leperClassPlugin.LeperClassPlugin plugin) {
        super();
        this.plugin = plugin;
    }

    @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSplash(org.bukkit.event.entity.PotionSplashEvent e) {
        java.util.Collection effects = e.getPotion().getEffects();
        if (effects.isEmpty()) {
            return;
        }
        java.util.Iterator local3 = e.getAffectedEntities().iterator();
        if (local3.hasNext()) {
            org.bukkit.entity.LivingEntity le = (org.bukkit.entity.LivingEntity) local3.next();
            if (!((le instanceof org.bukkit.entity.Player))) { /* continue */ }
            org.bukkit.entity.Player p = (org.bukkit.entity.Player) le;
            if (!(plugin.data.isLeper(p))) {
                /* continue */
            }
            if (!(hasEffect(effects, "HEAL"))) {
                if (!(hasEffect(effects, "INSTANT_HEALTH"))) {
                    if (hasEffect(effects, "REGEN")) {
                    } else {
                    }
                }
            }
            boolean healLike = false;
            if (!(hasEffect(effects, "HARM"))) {
                if (!(hasEffect(effects, "INSTANT_DAMAGE"))) {
                    if (hasEffect(effects, "POISON")) {
                    } else {
                    }
                }
            }
            boolean harmLike = false;
            if (!healLike) {
                if (harmLike) {
                    e.setIntensity(p, 0.0);
                }
            }
            e.setIntensity(p, 0.0);
            if (healLike) {
                plugin.balance.hurt(p, plugin.settings.leperDamageFromHeal);
                p.removePotionEffect(org.bukkit.potion.PotionEffectType.REGENERATION);
            }
            if (harmLike) {
                plugin.balance.heal(p, plugin.settings.leperHealFromPoison);
                p.removePotionEffect(org.bukkit.potion.PotionEffectType.POISON);
            }
            /* continue */
        }
    }

    @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCloud(org.bukkit.event.entity.AreaEffectCloudApplyEvent e) {
        org.bukkit.entity.AreaEffectCloud cloud = e.getEntity();
        java.util.Collection effects = cloud.getCustomEffects();
        if (effects.isEmpty()) {
            return;
        }
        java.util.Iterator local4 = e.getAffectedEntities().iterator();
        if (local4.hasNext()) {
            org.bukkit.entity.LivingEntity le = (org.bukkit.entity.LivingEntity) local4.next();
            if (!((le instanceof org.bukkit.entity.Player))) { /* continue */ }
            org.bukkit.entity.Player p = (org.bukkit.entity.Player) le;
            if (!(plugin.data.isLeper(p))) {
                /* continue */
            }
            if (!(hasEffect(effects, "HEAL"))) {
                if (!(hasEffect(effects, "INSTANT_HEALTH"))) {
                    if (hasEffect(effects, "REGEN")) {
                    } else {
                    }
                }
            }
            boolean healLike = false;
            if (!(hasEffect(effects, "HARM"))) {
                if (!(hasEffect(effects, "INSTANT_DAMAGE"))) {
                    if (hasEffect(effects, "POISON")) {
                    } else {
                    }
                }
            }
            boolean harmLike = false;
            if (healLike) {
                plugin.balance.hurt(p, java.lang.Math.max(1.0, plugin.settings.leperDamageFromHeal * 0.67));
                p.removePotionEffect(org.bukkit.potion.PotionEffectType.REGENERATION);
            }
            if (harmLike) {
                plugin.balance.heal(p, java.lang.Math.max(1.0, plugin.settings.leperHealFromPoison * 0.67));
                p.removePotionEffect(org.bukkit.potion.PotionEffectType.POISON);
            }
            /* continue */
        }
    }

    private boolean hasEffect(java.util.Collection effects, java.lang.String token) {
        java.lang.String t = token.toUpperCase();
        java.util.Iterator local4 = effects.iterator();
        if (local4.hasNext()) {
            org.bukkit.potion.PotionEffect pe = (org.bukkit.potion.PotionEffect) local4.next();
            org.bukkit.potion.PotionEffectType type = pe.getType();
            if (type != null) {
                if (type.getName() != null) {
                    if (type.getName().toUpperCase().contains(t)) {
                        return true;
                    }
                }
            }
            /* continue */
        }
        return false;
    }

}
