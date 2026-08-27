package org.examplee.leperClassPlugin.core;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

public final class LeperKeys {
    public final org.bukkit.NamespacedKey leperKey;
    public final org.bukkit.NamespacedKey plagueStickKey;
    public final org.bukkit.NamespacedKey plagueBombKey;
    public final org.bukkit.NamespacedKey umbrellaKey;
    public final org.bukkit.NamespacedKey umbrellaTierKey;
    public final org.bukkit.NamespacedKey umbrellaLifetimeKey;
    public final org.bukkit.NamespacedKey umbrellaRemainingKey;
    public final org.bukkit.NamespacedKey vaccineKey;
    public final org.bukkit.NamespacedKey infectionHitsKey;
    public final org.bukkit.NamespacedKey infectionStageKey;
    public final org.bukkit.NamespacedKey infectionNextPhaseKey;
    public final org.bukkit.NamespacedKey dangerBlessKey;
    public final org.bukkit.NamespacedKey rageUntilKey;
    public final org.bukkit.NamespacedKey leperBloodKey;
    public final org.bukkit.NamespacedKey thickBloodKey;
    public final org.bukkit.NamespacedKey sterileBloodKey;
    public final org.bukkit.NamespacedKey sacrificialKnifeKey;
    public final org.bukkit.NamespacedKey sneezeProjectileKey;
    public final org.bukkit.NamespacedKey itemVersionKey;

    public LeperKeys(org.bukkit.plugin.Plugin plugin) {
        super();
        this.leperKey = new org.bukkit.NamespacedKey(plugin, "class_leper");
        this.plagueStickKey = new org.bukkit.NamespacedKey(plugin, "plague_stick");
        this.plagueBombKey = new org.bukkit.NamespacedKey(plugin, "plague_bomb");
        this.umbrellaKey = new org.bukkit.NamespacedKey(plugin, "umbrella");
        this.umbrellaTierKey = new org.bukkit.NamespacedKey(plugin, "umbrella_tier");
        this.umbrellaLifetimeKey = new org.bukkit.NamespacedKey(plugin, "umbrella_lifetime_sec");
        this.umbrellaRemainingKey = new org.bukkit.NamespacedKey(plugin, "umbrella_remaining_sec");
        this.vaccineKey = new org.bukkit.NamespacedKey(plugin, "vaccine_shot");
        this.infectionHitsKey = new org.bukkit.NamespacedKey(plugin, "infection_hits");
        this.infectionStageKey = new org.bukkit.NamespacedKey(plugin, "infection_stage");
        this.infectionNextPhaseKey = new org.bukkit.NamespacedKey(plugin, "infection_time");
        this.dangerBlessKey = new org.bukkit.NamespacedKey(plugin, "danger_blessing");
        this.rageUntilKey = new org.bukkit.NamespacedKey(plugin, "rage_until_ms");
        this.leperBloodKey = new org.bukkit.NamespacedKey(plugin, "leper_blood");
        this.thickBloodKey = new org.bukkit.NamespacedKey(plugin, "thick_leper_blood");
        this.sterileBloodKey = new org.bukkit.NamespacedKey(plugin, "sterile_leper_blood");
        this.sacrificialKnifeKey = new org.bukkit.NamespacedKey(plugin, "sacrificial_knife");
        this.sneezeProjectileKey = new org.bukkit.NamespacedKey(plugin, "sneeze_projectile");
        this.itemVersionKey = new org.bukkit.NamespacedKey(plugin, "item_version");
    }

}
