package org.examplee.palePlugin.util;

import java.util.Locale;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;

public final class BiomeUtil {
    private BiomeUtil() {
        super();
    }

    public static org.bukkit.NamespacedKey parseBiomeKey(java.lang.String s) {
        if (s != null) {
            java.lang.String v = s.trim();
            if (!(v.isEmpty())) {
                org.bukkit.NamespacedKey direct = org.bukkit.NamespacedKey.fromString(v.toLowerCase(java.util.Locale.ROOT));
                if (direct != null) {
                    return direct;
                }
            }
            return null;
        }
        return null;
    }

    public static org.bukkit.NamespacedKey biomeKeyOf(org.bukkit.block.Biome biome) {
        if (biome != null) {
            if ((biome instanceof org.bukkit.Keyed)) {
                org.bukkit.Keyed keyed = biome;
                return keyed.getKey();
            }
        }
        return null;
    }

}
