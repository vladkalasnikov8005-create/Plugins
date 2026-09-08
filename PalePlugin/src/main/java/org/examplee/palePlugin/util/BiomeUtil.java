package org.examplee.palePlugin.util;

import java.util.Locale;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;

public final class BiomeUtil {
   private BiomeUtil() {
   }

   public static NamespacedKey parseBiomeKey(String s) {
      if (s == null) {
         return null;
      } else {
         String v = s.trim();
         if (v.isEmpty()) {
            return null;
         } else {
            NamespacedKey direct = NamespacedKey.fromString(v.toLowerCase(Locale.ROOT));
            return direct != null ? direct : NamespacedKey.fromString("minecraft:" + v.toLowerCase(Locale.ROOT));
         }
      }
   }

   public static NamespacedKey biomeKeyOf(Biome biome) {
      if (biome == null) {
         return null;
      } else {
         return biome instanceof Keyed ? biome.getKey() : NamespacedKey.fromString("minecraft:" + biome.toString().toLowerCase(Locale.ROOT));
      }
   }
}
