package org.examplee.leperClassPlugin.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public final class SunUtil {
   private SunUtil() {
   }

   public static boolean isOnPaleSurface(Player p) {
      Location loc = p.getLocation();
      Block in = loc.getBlock();
      if (in.getType().name().contains("PALE")) {
         return true;
      } else {
         Block under = in.getRelative(BlockFace.DOWN);
         return under.getType().name().contains("PALE");
      }
   }

   /**
    * Игрок стоит на чём-то бледном или бледный блок есть в колонне
    * на verticalRange блоков вверх/вниз от него.
    */
   public static boolean isNearPale(Player p, int verticalRange) {
      Location loc = p.getLocation();
      World w = p.getWorld();
      int x = loc.getBlockX();
      int y = loc.getBlockY();
      int z = loc.getBlockZ();
      int minY = Math.max(w.getMinHeight(), y - verticalRange);
      int maxY = Math.min(w.getMaxHeight() - 1, y + verticalRange);

      for (int yy = minY; yy <= maxY; yy++) {
         if (w.getBlockAt(x, yy, z).getType().name().contains("PALE")) {
            return true;
         }
      }

      return false;
   }

   public static boolean shouldBurnInSun(Player p) {
      World w = p.getWorld();
      if (w.getEnvironment() != Environment.NORMAL) {
         return false;
      } else if (!w.hasStorm() && !w.isThundering()) {
         long time = w.getTime();
         if (time >= 0L && time <= 12300L) {
            Location loc = p.getLocation();
            int highestY = w.getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ());
            return loc.getBlockY() + 1 < highestY ? false : loc.getBlock().getLightFromSky() >= 14;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }
}
