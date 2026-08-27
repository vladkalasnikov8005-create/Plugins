package org.examplee.leperClassPlugin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.examplee.leperClassPlugin.LeperClassPlugin;

public final class JoinQuitDeathListener implements org.bukkit.event.Listener {
    private final org.examplee.leperClassPlugin.LeperClassPlugin plugin;

    public JoinQuitDeathListener(org.examplee.leperClassPlugin.LeperClassPlugin plugin) {
        super();
        this.plugin = plugin;
    }

    @org.bukkit.event.EventHandler
    public void onJoin(org.bukkit.event.player.PlayerJoinEvent e) {
        org.bukkit.entity.Player p = e.getPlayer();
        if (plugin.data.isLeper(p)) {
            if (plugin.effects.WATER_BREATHING != null) {
                p.addPotionEffect(new org.bukkit.potion.PotionEffect(plugin.effects.WATER_BREATHING, 300, 0, false, false, false));
            }
        }
    }

    @org.bukkit.event.EventHandler
    public void onQuit(org.bukkit.event.player.PlayerQuitEvent e) {
        plugin.umbrella.resetCarry(e.getPlayer());
        plugin.movementLock.release(e.getPlayer());
    }

    @org.bukkit.event.EventHandler
    public void onDeath(org.bukkit.event.entity.PlayerDeathEvent e) {
        plugin.umbrella.resetCarry(e.getEntity());
        plugin.movementLock.release(e.getEntity());
    }

}
