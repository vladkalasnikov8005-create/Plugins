package org.examplee.leperClassPlugin.core;

import org.bukkit.potion.PotionEffectType;
import org.examplee.leperClassPlugin.util.Compat;

public final class EffectRegistry {
    public final org.bukkit.potion.PotionEffectType FIRE_RES;
    public final org.bukkit.potion.PotionEffectType POISON;
    public final org.bukkit.potion.PotionEffectType SLOW;
    public final org.bukkit.potion.PotionEffectType BLINDNESS;
    public final org.bukkit.potion.PotionEffectType NAUSEA;
    public final org.bukkit.potion.PotionEffectType WATER_BREATHING;
    public final org.bukkit.potion.PotionEffectType WEAKNESS;
    public final org.bukkit.potion.PotionEffectType MINING_FATIGUE;
    public final org.bukkit.potion.PotionEffectType REGEN;
    public final org.bukkit.potion.PotionEffectType SPEED;
    public final org.bukkit.potion.PotionEffectType STRENGTH;

    public EffectRegistry() {
        super();
        this.FIRE_RES = org.examplee.leperClassPlugin.util.Compat.effect("FIRE_RESISTANCE");
        this.POISON = org.examplee.leperClassPlugin.util.Compat.effect("POISON");
        java.lang.String[] tmp1 = new java.lang.String[2];
        tmp1[0] = "SLOWNESS";
        tmp1[1] = "SLOW";
        this.SLOW = org.examplee.leperClassPlugin.util.Compat.effectFirst(tmp1);
        this.BLINDNESS = org.examplee.leperClassPlugin.util.Compat.effect("BLINDNESS");
        java.lang.String[] tmp2 = new java.lang.String[2];
        tmp2[0] = "NAUSEA";
        tmp2[1] = "CONFUSION";
        this.NAUSEA = org.examplee.leperClassPlugin.util.Compat.effectFirst(tmp2);
        this.WATER_BREATHING = org.examplee.leperClassPlugin.util.Compat.effect("WATER_BREATHING");
        this.WEAKNESS = org.examplee.leperClassPlugin.util.Compat.effect("WEAKNESS");
        java.lang.String[] tmp3 = new java.lang.String[2];
        tmp3[0] = "MINING_FATIGUE";
        tmp3[1] = "SLOW_DIGGING";
        this.MINING_FATIGUE = org.examplee.leperClassPlugin.util.Compat.effectFirst(tmp3);
        java.lang.String[] tmp4 = new java.lang.String[1];
        tmp4[0] = "REGENERATION";
        this.REGEN = org.examplee.leperClassPlugin.util.Compat.effectFirst(tmp4);
        this.SPEED = org.examplee.leperClassPlugin.util.Compat.effect("SPEED");
        java.lang.String[] tmp5 = new java.lang.String[2];
        tmp5[0] = "STRENGTH";
        tmp5[1] = "INCREASE_DAMAGE";
        this.STRENGTH = org.examplee.leperClassPlugin.util.Compat.effectFirst(tmp5);
    }

}
