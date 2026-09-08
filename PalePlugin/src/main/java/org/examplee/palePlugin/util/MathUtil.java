package org.examplee.palePlugin.util;

public final class MathUtil {
   private MathUtil() {
   }

   public static int clamp(int v, int min, int max) {
      return v < min ? min : Math.min(v, max);
   }
}
