package org.examplee.leperClassPlugin.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public final class SunUtil {
    private SunUtil() {
        super();
    }

    public static boolean isOnPaleSurface(org.bukkit.entity.Player p) {
        org.bukkit.Location loc = p.getLocation();
        org.bukkit.block.Block in = loc.getBlock();
        if (in.getType().name().contains("PALE")) {
            return true;
        }
        org.bukkit.block.Block under = in.getRelative(org.bukkit.block.BlockFace.DOWN);
        return under.getType().name().contains("PALE");
    }

    public static boolean shouldBurnInSun(org.bukkit.entity.Player p) {
        org.bukkit.World w = p.getWorld();
        if (w.getEnvironment() == org.bukkit.World.Environment.NORMAL) {
            if (w.hasStorm()) {
                return false;
            }
            if (!(w.isThundering())) {
                long time = w.getTime();
                if (Long.compare(time, 0L) < 0) {
                    return false;
                }
                if (Long.compare(time, 12300L) <= 0) {
                    org.bukkit.Location loc = p.getLocation();
                    int highestY = w.getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ());
                    if (loc.getBlockY() + 1 < highestY) {
                        return false;
                    }
                }
                return false;
            }
            return false;
        }
        return false;
    }

}
