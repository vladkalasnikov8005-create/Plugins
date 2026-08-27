package org.examplee.leperClassPlugin.util;

import java.util.Iterator;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

public final class EntityUtil {
    private EntityUtil() {
        super();
    }

    public static void clearHostileTargets(org.bukkit.entity.Player p, double radius) {
        java.util.Iterator local3 = p.getNearbyEntities(radius, radius, radius).iterator();
        if (local3.hasNext()) {
            org.bukkit.entity.Entity e = (org.bukkit.entity.Entity) local3.next();
            if ((e instanceof org.bukkit.entity.Mob)) {
                org.bukkit.entity.Mob mob = (org.bukkit.entity.Mob) e;
                if (mob.getTarget() != null) {
                    if (mob.getTarget().getUniqueId().equals(p.getUniqueId())) {
                        mob.setTarget(null);
                    }
                }
            }
            /* continue */
        }
    }

}
