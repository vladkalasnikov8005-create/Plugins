package org.examplee.palePlugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.examplee.palePlugin.PalePlugin;

public final class ChunkListener implements org.bukkit.event.Listener {
    private final org.examplee.palePlugin.PalePlugin plugin;

    public ChunkListener(org.examplee.palePlugin.PalePlugin plugin) {
        super();
        this.plugin = plugin;
    }

    @org.bukkit.event.EventHandler
    public void onLoad(org.bukkit.event.world.ChunkLoadEvent e) {
        plugin.spread.onChunkLoad(e.getWorld(), e.getChunk().getX(), e.getChunk().getZ());
    }

    @org.bukkit.event.EventHandler
    public void onUnload(org.bukkit.event.world.ChunkUnloadEvent e) {
        plugin.spread.onChunkUnload(e.getWorld());
    }

}
