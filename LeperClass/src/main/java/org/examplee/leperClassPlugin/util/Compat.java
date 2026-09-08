package org.examplee.leperClassPlugin.util;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.potion.PotionEffectType;

public final class Compat {
   private Compat() {
   }

   public static Material materialFirst(String... names) {
      for (String n : names) {
         Material m = Material.getMaterial(n);
         if (m != null) {
            return m;
         }
      }

      return Material.STONE;
   }

   public static Sound soundFirst(String... names) {
      for (String n : names) {
         try {
            Object v = Sound.class.getField(n).get(null);
            if (v instanceof Sound) {
               return (Sound)v;
            }
         } catch (Throwable var7) {
         }
      }

      return Sound.BLOCK_ANVIL_USE;
   }

   public static Particle particleFirst(String... names) {
      for (String n : names) {
         try {
            return Particle.valueOf(n);
         } catch (Throwable var6) {
         }
      }

      Particle[] all = Particle.values();
      return all.length > 0 ? all[0] : null;
   }

   public static PotionEffectType effect(String name) {
      return PotionEffectType.getByName(name);
   }

   public static PotionEffectType effectFirst(String... names) {
      for (String n : names) {
         PotionEffectType t = PotionEffectType.getByName(n);
         if (t != null) {
            return t;
         }
      }

      return null;
   }
}
