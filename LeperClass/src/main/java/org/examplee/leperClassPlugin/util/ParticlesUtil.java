package org.examplee.leperClassPlugin.util;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.World;

public final class ParticlesUtil {
    private ParticlesUtil() {
        super();
    }

    public static void greenDust(org.bukkit.World w, org.bukkit.Location loc, int count, double ox, double oy, double oz, float size) {
        if (w == null) {
            return;
        }
        if (loc == null) {
            return;
        }
        java.lang.String[] tmp1 = new java.lang.String[5];
        tmp1[0] = "DUST";
        tmp1[1] = "REDSTONE";
        tmp1[2] = "ENTITY_EFFECT";
        tmp1[3] = "SPELL_MOB";
        tmp1[4] = "CRIT";
        org.bukkit.Particle dust = org.examplee.leperClassPlugin.util.Compat.particleFirst(tmp1);
        try {
            w.spawnParticle(dust, loc, count, ox, oy, oz, 0.0, new org.bukkit.Particle.DustOptions(org.examplee.leperClassPlugin.util.TextUtil.C_GREEN, size));
        }
        catch (java.lang.Throwable ex) {
            java.lang.String[] tmp2 = new java.lang.String[4];
            tmp2[0] = "ENTITY_EFFECT";
            tmp2[1] = "SPELL_MOB";
            tmp2[2] = "CLOUD";
            tmp2[3] = "CRIT";
            org.bukkit.Particle fallback = org.examplee.leperClassPlugin.util.Compat.particleFirst(tmp2);
            if (fallback != null) {
                w.spawnParticle(fallback, loc, java.lang.Math.max(1, count / 2), ox, oy, oz, 0.0);
            }
            return;
        }
    }

}
