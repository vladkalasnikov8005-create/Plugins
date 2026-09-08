package org.examplee.leperClassPlugin.core;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class PluginSettings {
   public final long infectionPhaseMs;
   public final double contactInfectPerHp;
   public final double contactFakeScareChance;
   public final long contactResolveTicks;
   public final long knifeCooldownMs;
   public final int knifeFatigueTicks;
   public final int knifeWeakTicks;
   public final int knifeSlowTicks;
   public final long sneezeCooldownMs;
   public final double sneezeVelocity;
   public final int sneezeMaxDistance;
   public final int sneezePoisonTicks;
   public final int sneezeSlowTicks;
   public final int sneezeWeakTicks;
   public final int sneezeBlindTicks;
   public final double leperHealFromPoison;
   public final double leperHealFromHarm;
   public final double leperDamageFromHeal;
   public final boolean paleHomeEnabled;
   public final int paleHomeMinStage;
   public final int paleHomeVerticalRange;
   public final int paleHomeRegenAmplifier;
   public final boolean deathInfectEnabled;
   public final int deathInfectRadius;
   public final int deathInfectMaxBlocks;
   public final int lanternRadius;
   public final int lanternFuelPerDustSec;
   public final int lanternFuelMaxSec;
   public final int lanternStartFuelSec;

   public PluginSettings(JavaPlugin plugin) {
      FileConfiguration c = plugin.getConfig();
      this.infectionPhaseMs = c.getLong("balance.infection.phase_minutes", 20L) * 60000L;
      this.contactInfectPerHp = c.getDouble("balance.contact_infect_per_hp", 0.015);
      this.contactFakeScareChance = c.getDouble("balance.contact_fake_scare_chance", 0.2);
      this.contactResolveTicks = c.getLong("balance.contact_resolve_minutes", 20L) * 60L * 20L;
      this.knifeCooldownMs = c.getLong("balance.knife.cooldown_minutes", 60L) * 60000L;
      this.knifeFatigueTicks = (int)(c.getLong("balance.knife.fatigue_minutes", 5L) * 60L * 20L);
      this.knifeWeakTicks = (int)(c.getLong("balance.knife.weak_minutes", 10L) * 60L * 20L);
      this.knifeSlowTicks = (int)(c.getLong("balance.knife.slow_minutes", 10L) * 60L * 20L);
      this.sneezeCooldownMs = c.getLong("balance.sneeze.cooldown_seconds", 10L) * 1000L;
      this.sneezeVelocity = c.getDouble("balance.sneeze.velocity", 1.15);
      this.sneezeMaxDistance = c.getInt("balance.sneeze.max_distance", 12);
      this.sneezePoisonTicks = (int)(c.getLong("balance.sneeze.poison_seconds", 10L) * 20L);
      this.sneezeSlowTicks = (int)(c.getLong("balance.sneeze.slow_seconds", 10L) * 20L);
      this.sneezeWeakTicks = (int)(c.getLong("balance.sneeze.weak_seconds", 5L) * 20L);
      this.sneezeBlindTicks = (int)(c.getLong("balance.sneeze.blind_seconds", 10L) * 20L);
      this.leperHealFromPoison = c.getDouble("balance.potion_inversion.heal_from_poison", 6.0);
      this.leperHealFromHarm = c.getDouble("balance.potion_inversion.heal_from_harm", 8.0);
      this.leperDamageFromHeal = c.getDouble("balance.potion_inversion.damage_from_heal", 6.0);
      this.paleHomeEnabled = c.getBoolean("balance.pale_home.enabled", true);
      this.paleHomeMinStage = c.getInt("balance.pale_home.min_stage", 2);
      this.paleHomeVerticalRange = c.getInt("balance.pale_home.vertical_range", 5);
      this.paleHomeRegenAmplifier = c.getInt("balance.pale_home.regen_amplifier", 0);
      this.deathInfectEnabled = c.getBoolean("balance.death_infect.enabled", true);
      this.deathInfectRadius = c.getInt("balance.death_infect.radius", 4);
      this.deathInfectMaxBlocks = c.getInt("balance.death_infect.max_blocks", 150);
      this.lanternRadius = c.getInt("balance.lantern.radius", 16);
      this.lanternFuelPerDustSec = c.getInt("balance.lantern.fuel_per_dust_seconds", 300);
      this.lanternFuelMaxSec = c.getInt("balance.lantern.fuel_max_seconds", 3600);
      this.lanternStartFuelSec = c.getInt("balance.lantern.start_fuel_seconds", 300);
   }
}
