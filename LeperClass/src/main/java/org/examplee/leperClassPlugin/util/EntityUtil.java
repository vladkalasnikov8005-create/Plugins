package org.examplee.leperClassPlugin.util;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

public final class EntityUtil {
   private EntityUtil() {
   }

   public static void clearHostileTargets(Player p, double radius) {
      for (Entity e : p.getNearbyEntities(radius, radius, radius)) {
         if (e instanceof Mob) {
            Mob mob = (Mob)e;
            if (mob.getTarget() != null && mob.getTarget().getUniqueId().equals(p.getUniqueId())) {
               mob.setTarget(null);
            }
         }
      }
   }
}
