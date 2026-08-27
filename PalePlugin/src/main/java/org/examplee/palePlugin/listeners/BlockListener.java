package org.examplee.palePlugin.listeners;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.examplee.palePlugin.PalePlugin;

public final class BlockListener implements org.bukkit.event.Listener {
    private final org.examplee.palePlugin.PalePlugin plugin;

    public BlockListener(org.examplee.palePlugin.PalePlugin plugin) {
        super();
        this.plugin = plugin;
    }

    @org.bukkit.event.EventHandler
    public void onPlace(org.bukkit.event.block.BlockPlaceEvent e) {
        org.bukkit.block.Block b = e.getBlockPlaced();
        if (!(plugin.engine.infectedTypes().contains(b.getType()))) {
            if (plugin.items.isWard(e.getItemInHand())) {
                if (!(e.getPlayer().hasPermission("pale.use"))) {
                    if (!(e.getPlayer().hasPermission("pale.admin"))) {
                        e.setCancelled(1);
                        return;
                    }
                }
                plugin.engine.wards(b.getWorld()).add(b.getX(), b.getY(), b.getZ());
                b.getWorld().playSound(b.getLocation(), org.bukkit.Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0F, 1.2000000476837158F);
                b.getWorld().spawnParticle(org.bukkit.Particle.END_ROD, b.getLocation().add(0.5, 1.0, 0.5), 18, 0.35, 0.6, 0.35, 0.01);
                plugin.wardsStorage.save(plugin.engine);
            }
        }
        plugin.engine.sources(b.getWorld()).add(b.getX(), b.getY(), b.getZ());
        if (plugin.items.isWard(e.getItemInHand())) {
            if (!(e.getPlayer().hasPermission("pale.use"))) {
                if (!(e.getPlayer().hasPermission("pale.admin"))) {
                    e.setCancelled(1);
                    return;
                }
            }
            plugin.engine.wards(b.getWorld()).add(b.getX(), b.getY(), b.getZ());
            b.getWorld().playSound(b.getLocation(), org.bukkit.Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0F, 1.2000000476837158F);
            b.getWorld().spawnParticle(org.bukkit.Particle.END_ROD, b.getLocation().add(0.5, 1.0, 0.5), 18, 0.35, 0.6, 0.35, 0.01);
            plugin.wardsStorage.save(plugin.engine);
        }
    }

    @org.bukkit.event.EventHandler
    public void onBreak(org.bukkit.event.block.BlockBreakEvent e) {
        org.bukkit.block.Block b = e.getBlock();
        if (plugin.engine.infectedTypes().contains(b.getType())) {
            plugin.engine.sources(b.getWorld()).remove(b.getX(), b.getY(), b.getZ());
        }
        boolean removed = plugin.engine.wards(b.getWorld()).remove(b.getX(), b.getY(), b.getZ());
        if (removed) {
            plugin.wardsStorage.save(plugin.engine);
            e.setDropItems(0);
            b.getWorld().dropItemNaturally(b.getLocation().add(0.5, 0.5, 0.5), plugin.items.makeWard(1));
        }
    }

}
