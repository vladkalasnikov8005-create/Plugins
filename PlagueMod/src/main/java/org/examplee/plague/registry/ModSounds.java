package org.examplee.plague.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import org.examplee.plague.PlagueMod;

public class ModSounds {
    public static SoundEvent SNEEZE;
    public static SoundEvent PLAGUE_HIT;
    public static SoundEvent DARK_SPREAD;
    public static SoundEvent WARD_CHIME;
    public static SoundEvent CONVERSION;

    public static void register() {
        SNEEZE = reg("sneeze");
        PLAGUE_HIT = reg("plague_hit");
        DARK_SPREAD = reg("dark_spread");
        WARD_CHIME = reg("ward_chime");
        CONVERSION = reg("conversion");
    }

    private static SoundEvent reg(String path) {
        Identifier id = PlagueMod.id(path);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }
}
