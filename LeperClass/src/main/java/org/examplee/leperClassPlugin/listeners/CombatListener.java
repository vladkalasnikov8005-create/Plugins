package org.examplee.leperClassPlugin.listeners;

import java.util.EnumSet;
import java.util.Set;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.examplee.leperClassPlugin.LeperClassPlugin;

public final class CombatListener implements org.bukkit.event.Listener {
    private static final double NON_FIRE_DAMAGE_MULT = 0.0;
    private static final double FIRE_DAMAGE_MULT = 0.0;
    private static java.util.Set FIRE_CAUSES;
    private final org.examplee.leperClassPlugin.LeperClassPlugin plugin;

    public CombatListener(org.examplee.leperClassPlugin.LeperClassPlugin plugin) {
        super();
        this.plugin = plugin;
    }

    @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(org.bukkit.event.entity.EntityDamageEvent e) {
        org.bukkit.entity.Entity local3 = e.getEntity();
        if (!((local3 instanceof org.bukkit.entity.Player))) {
            return;
        }
        org.bukkit.entity.Player p = (org.bukkit.entity.Player) local3;
        if (!(plugin.data.isLeper(p))) {
            return;
        }
        if (org.examplee.leperClassPlugin.listeners.CombatListener.FIRE_CAUSES.contains(e.getCause())) {
            e.setDamage(e.getDamage() * 4.0);
        } else {
            e.setDamage(e.getDamage() * 0.05);
        }
    }

    static {
        org.examplee.leperClassPlugin.listeners.CombatListener.FIRE_CAUSES = java.util.EnumSet.of(org.bukkit.event.entity.EntityDamageEvent.DamageCause.FIRE, org.bukkit.event.entity.EntityDamageEvent.DamageCause.FIRE_TICK, org.bukkit.event.entity.EntityDamageEvent.DamageCause.LAVA, org.bukkit.event.entity.EntityDamageEvent.DamageCause.HOT_FLOOR);
    }

}
