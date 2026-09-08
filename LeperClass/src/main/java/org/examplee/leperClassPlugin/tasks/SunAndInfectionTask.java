package org.examplee.leperClassPlugin.tasks;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.examplee.leperClassPlugin.LeperClassPlugin;
import org.examplee.leperClassPlugin.util.SunUtil;

public final class SunAndInfectionTask {
   private final LeperClassPlugin plugin;
   private BukkitRunnable task;

   public SunAndInfectionTask(LeperClassPlugin plugin) {
      this.plugin = plugin;
   }

   public void start() {
      this.stop();
      this.task = new BukkitRunnable() {
         public void run() {
            long now = System.currentTimeMillis();
            int leperCount = 0;

            for (Player p : Bukkit.getOnlinePlayers()) {
               if (SunAndInfectionTask.this.plugin.data.isLeper(p)) {
                  leperCount++;
               }
            }

            for (Player px : Bukkit.getOnlinePlayers()) {
               SunAndInfectionTask.this.plugin.infection.checkProgression(px, now);
               if (px.getGameMode() != GameMode.CREATIVE && px.getGameMode() != GameMode.SPECTATOR) {
                  boolean isLeper = SunAndInfectionTask.this.plugin.data.isLeper(px);
                  boolean isStage2 = SunAndInfectionTask.this.plugin.data.getInfectionStage(px) == 2;
                  boolean paleHome = false;
                  if (isLeper || isStage2) {
                     paleHome = SunUtil.isNearPale(px, SunAndInfectionTask.this.plugin.settings.paleHomeVerticalRange)
                        || SunAndInfectionTask.this.plugin.settings.paleHomeEnabled
                           && SunAndInfectionTask.this.plugin.paleHook.getStageAt(px.getLocation())
                              >= SunAndInfectionTask.this.plugin.settings.paleHomeMinStage;
                  }

                  PotionEffectType wb = SunAndInfectionTask.this.plugin.effects.WATER_BREATHING;
                  if (isLeper && wb != null) {
                     PotionEffect cur = px.getPotionEffect(wb);
                     if (cur == null || cur.getDuration() < 40) {
                        px.addPotionEffect(new PotionEffect(wb, 300, 0, false, false, false));
                     }
                  }

                  if ((isLeper || isStage2)
                     && SunAndInfectionTask.this.plugin.effects.FIRE_RES != null
                     && px.hasPotionEffect(SunAndInfectionTask.this.plugin.effects.FIRE_RES)) {
                     px.removePotionEffect(SunAndInfectionTask.this.plugin.effects.FIRE_RES);
                  }

                  if ((isLeper || isStage2) && SunUtil.shouldBurnInSun(px)) {
                     if (!paleHome && !SunAndInfectionTask.this.plugin.umbrella.hasUmbrellaInOffhand(px)) {
                        px.setFireTicks(Math.max(px.getFireTicks(), 60));
                     } else {
                        if (px.getFireTicks() > 0) {
                           px.setFireTicks(0);
                        }

                        if (SunAndInfectionTask.this.plugin.umbrella.hasUmbrellaInOffhand(px)) {
                           SunAndInfectionTask.this.plugin.umbrella.damageUmbrellaInOffhand(px);
                        }
                     }
                  }

                  if (isLeper) {
                     if (paleHome
                        && SunAndInfectionTask.this.plugin.effects.REGEN != null
                        && !SunAndInfectionTask.this.plugin.lanterns.isInLanternRange(px)) {
                        px.addPotionEffect(
                           new PotionEffect(
                              SunAndInfectionTask.this.plugin.effects.REGEN,
                              80,
                              SunAndInfectionTask.this.plugin.settings.paleHomeRegenAmplifier,
                              true,
                              false,
                              true
                           )
                        );
                     }

                     SunAndInfectionTask.this.applyPopulationBuffs(px, leperCount);
                     if (px.getFoodLevel() <= 4 && SunAndInfectionTask.this.plugin.data.getRageUntil(px) < now) {
                        SunAndInfectionTask.this.plugin.data.setRageUntil(px, now + 180000L);
                        px.sendMessage("§cЯрость охватила вас!");
                     }

                     if (px.getFoodLevel() > 4 && SunAndInfectionTask.this.plugin.data.getRageUntil(px) > 0L) {
                        SunAndInfectionTask.this.plugin.data.setRageUntil(px, 0L);
                     }

                     if (SunAndInfectionTask.this.plugin.data.getRageUntil(px) > now) {
                        if (SunAndInfectionTask.this.plugin.effects.STRENGTH != null) {
                           px.addPotionEffect(new PotionEffect(SunAndInfectionTask.this.plugin.effects.STRENGTH, 80, 1, false, false, true));
                        }

                        if (SunAndInfectionTask.this.plugin.effects.SPEED != null) {
                           px.addPotionEffect(new PotionEffect(SunAndInfectionTask.this.plugin.effects.SPEED, 80, 1, false, false, true));
                        }
                     }

                     if (px.getFoodLevel() <= 0) {
                        if (SunAndInfectionTask.this.plugin.effects.BLINDNESS != null) {
                           px.addPotionEffect(new PotionEffect(SunAndInfectionTask.this.plugin.effects.BLINDNESS, 80, 0, false, false, true));
                        }

                        if (SunAndInfectionTask.this.plugin.effects.SLOW != null) {
                           px.addPotionEffect(new PotionEffect(SunAndInfectionTask.this.plugin.effects.SLOW, 80, 2, false, false, true));
                        }
                     }

                     for (Entity en : px.getNearbyEntities(20.0, 12.0, 20.0)) {
                        if (en instanceof Mob) {
                           Mob mob = (Mob)en;
                           EntityType t = mob.getType();
                           if ((t == EntityType.IRON_GOLEM || t == EntityType.SNOW_GOLEM)
                              && (mob.getTarget() == null || !mob.getTarget().getUniqueId().equals(px.getUniqueId()))) {
                              mob.setTarget(px);
                           }
                        }
                     }
                  }
               }
            }
         }
      };
      this.task.runTaskTimer(this.plugin, 20L, 20L);
   }

   private void applyPopulationBuffs(Player p, int lepers) {
      int regenAmp = -1;
      int speedAmp = -1;
      int strAmp = -1;
      if (lepers >= 7) {
         regenAmp = 1;
         speedAmp = 2;
         strAmp = 2;
      } else if (lepers >= 5) {
         regenAmp = 0;
         speedAmp = 1;
         strAmp = 0;
      } else if (lepers >= 3) {
         regenAmp = 0;
         speedAmp = 0;
      }

      if (regenAmp >= 0 && this.plugin.effects.REGEN != null) {
         p.addPotionEffect(new PotionEffect(this.plugin.effects.REGEN, 80, regenAmp, false, false, true));
      }

      if (speedAmp >= 0 && this.plugin.effects.SPEED != null) {
         p.addPotionEffect(new PotionEffect(this.plugin.effects.SPEED, 80, speedAmp, false, false, true));
      }

      if (strAmp >= 0 && this.plugin.effects.STRENGTH != null) {
         p.addPotionEffect(new PotionEffect(this.plugin.effects.STRENGTH, 80, strAmp, false, false, true));
      }
   }

   public void stop() {
      if (this.task != null) {
         this.task.cancel();
         this.task = null;
      }
   }
}
