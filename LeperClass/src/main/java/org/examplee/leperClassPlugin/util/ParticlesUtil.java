package org.examplee.leperClassPlugin.util;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.Particle.DustOptions;

public final class ParticlesUtil {
   private ParticlesUtil() {
   }

   public static void greenDust(World w, Location loc, int count, double ox, double oy, double oz, float size) {
      if (w != null && loc != null) {
         Particle dust = Compat.particleFirst("DUST", "REDSTONE", "ENTITY_EFFECT", "SPELL_MOB", "CRIT");

         try {
            w.spawnParticle(dust, loc, count, ox, oy, oz, 0.0, new DustOptions(TextUtil.C_GREEN, size));
         } catch (Throwable var13) {
            Particle fallback = Compat.particleFirst("ENTITY_EFFECT", "SPELL_MOB", "CLOUD", "CRIT");
            if (fallback != null) {
               w.spawnParticle(fallback, loc, Math.max(1, count / 2), ox, oy, oz, 0.0);
            }
         }
      }
   }
}
