package org.examplee.leperClassPlugin.core;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

public final class PaleHook {
    private Object pluginRef;
    private Method apiInfect;

    public PaleHook() {
    }

    public void hook() {
        try {
            Plugin pl = Bukkit.getPluginManager().getPlugin("PalePlugin");
            if (pl == null || !pl.isEnabled()) {
                this.pluginRef = null;
                this.apiInfect = null;
                return;
            }
            this.apiInfect = pl.getClass().getMethod("apiInfect", Location.class, int.class, int.class);
            this.pluginRef = pl;
        } catch (Throwable t) {
            this.pluginRef = null;
            this.apiInfect = null;
        }
    }

    public int infect(Location loc, int radius, int maxBlocks) {
        if (pluginRef == null || apiInfect == null || loc == null) {
            return 0;
        }
        try {
            Object res = apiInfect.invoke(pluginRef, loc, radius, maxBlocks);
            if (res instanceof Integer i) {
                return i;
            }
            return 0;
        } catch (Throwable ignored) {
            return 0;
        }
    }
}
