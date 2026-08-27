package org.examplee.palePlugin.util;

public final class MathUtil {
    private MathUtil() {
        super();
    }

    public static int clamp(int v, int min, int max) {
        if (v < min) {
            return min;
        }
        return java.lang.Math.min(v, max);
    }

}
