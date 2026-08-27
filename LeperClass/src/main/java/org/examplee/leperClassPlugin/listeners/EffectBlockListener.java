package org.examplee.leperClassPlugin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffect;
import org.examplee.leperClassPlugin.LeperClassPlugin;

public final class EffectBlockListener implements org.bukkit.event.Listener {
    private final org.examplee.leperClassPlugin.LeperClassPlugin plugin;

    public EffectBlockListener(org.examplee.leperClassPlugin.LeperClassPlugin plugin) {
        super();
        this.plugin = plugin;
    }

    @org.bukkit.event.EventHandler
    public void onEffect(org.bukkit.event.entity.EntityPotionEffectEvent e) {
        boolean isLeper = e.getEntity();
        if (!((isLeper instanceof org.bukkit.entity.Player))) {
            return;
        }
        org.bukkit.entity.Player p = (org.bukkit.entity.Player) isLeper;
        isLeper = plugin.data.isLeper(p);
        if (plugin.data.getInfectionStage(p) != 2) {
            boolean isStage2 = false;
            if (!isLeper) {
                if (!isStage2) {
                    return;
                }
            }
        }
        isStage2 = 0;
        if (!isLeper) {
            if (!isStage2) {
                return;
            }
        }
        org.bukkit.potion.PotionEffect ne = e.getNewEffect();
        if (ne != null) {
            if (plugin.effects.FIRE_RES != null) {
                if (ne.getType().equals(plugin.effects.FIRE_RES)) {
                    e.setCancelled(true);
                }
            }
        }
    }

}
