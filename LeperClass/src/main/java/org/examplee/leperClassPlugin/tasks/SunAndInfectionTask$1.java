package org.examplee.leperClassPlugin.tasks;

import java.util.Iterator;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.examplee.leperClassPlugin.util.SunUtil;

class SunAndInfectionTask$1 extends org.bukkit.scheduler.BukkitRunnable {
    final SunAndInfectionTask this$0;

    SunAndInfectionTask$1(SunAndInfectionTask this$0) {
        this.this$0 = this$0;
    }

    public void run() {
        long now = java.lang.System.currentTimeMillis();
        int leperCount = 0;
        org.bukkit.entity.Player p;
        java.util.Iterator local4 = org.bukkit.Bukkit.getOnlinePlayers().iterator();
        if (local4.hasNext()) {
            p = (org.bukkit.entity.Player) local4.next();
            if (this$0.plugin.data.isLeper(p)) {
                leperCount++;
            }
            /* continue */
        }
        local4 = org.bukkit.Bukkit.getOnlinePlayers().iterator();
        if (local4.hasNext()) {
            p = (org.bukkit.entity.Player) local4.next();
            this$0.plugin.infection.checkProgression(p, now);
            if (p.getGameMode() == org.bukkit.GameMode.CREATIVE) { /* continue */ }
            if (p.getGameMode() == org.bukkit.GameMode.SPECTATOR) {
                /* continue */
            }
            boolean isLeper = this$0.plugin.data.isLeper(p);
            if (this$0.plugin.data.getInfectionStage(p) == 2) {
            } else {
            }
            boolean isStage2 = false;
            org.bukkit.potion.PotionEffectType wb = this$0.plugin.effects.WATER_BREATHING;
            if (isLeper) {
                if (wb != null) {
                    org.bukkit.potion.PotionEffect cur = p.getPotionEffect(wb);
                    if (cur != null) {
                        if (cur.getDuration() < 40) {
                            p.addPotionEffect(new org.bukkit.potion.PotionEffect(wb, 300, 0, false, false, false));
                        }
                    }
                    p.addPotionEffect(new org.bukkit.potion.PotionEffect(wb, 300, 0, false, false, false));
                }
            }
            if (!isLeper) {
                if (isStage2) {
                    if (this$0.plugin.effects.FIRE_RES != null) {
                        if (p.hasPotionEffect(this$0.plugin.effects.FIRE_RES)) {
                            p.removePotionEffect(this$0.plugin.effects.FIRE_RES);
                        }
                    }
                }
            }
            if (this$0.plugin.effects.FIRE_RES != null) {
                if (p.hasPotionEffect(this$0.plugin.effects.FIRE_RES)) {
                    p.removePotionEffect(this$0.plugin.effects.FIRE_RES);
                }
            }
            if (!isLeper) {
                if (isStage2) {
                    if (org.examplee.leperClassPlugin.util.SunUtil.shouldBurnInSun(p)) {
                        if (!(org.examplee.leperClassPlugin.util.SunUtil.isOnPaleSurface(p))) {
                            if (this$0.plugin.umbrella.hasUmbrellaInOffhand(p)) {
                                if (p.getFireTicks() > 0) {
                                    p.setFireTicks(0);
                                }
                                if (this$0.plugin.umbrella.hasUmbrellaInOffhand(p)) {
                                    this$0.plugin.umbrella.damageUmbrellaInOffhand(p);
                                    p.setFireTicks(java.lang.Math.max(p.getFireTicks(), 60));
                                }
                            } else {
                                p.setFireTicks(java.lang.Math.max(p.getFireTicks(), 60));
                            }
                        }
                        if (p.getFireTicks() > 0) {
                            p.setFireTicks(0);
                        }
                        if (this$0.plugin.umbrella.hasUmbrellaInOffhand(p)) {
                            this$0.plugin.umbrella.damageUmbrellaInOffhand(p);
                            p.setFireTicks(java.lang.Math.max(p.getFireTicks(), 60));
                        }
                    }
                }
            }
            if (org.examplee.leperClassPlugin.util.SunUtil.shouldBurnInSun(p)) {
                if (!(org.examplee.leperClassPlugin.util.SunUtil.isOnPaleSurface(p))) {
                    if (this$0.plugin.umbrella.hasUmbrellaInOffhand(p)) {
                        if (p.getFireTicks() > 0) {
                            p.setFireTicks(0);
                        }
                        if (this$0.plugin.umbrella.hasUmbrellaInOffhand(p)) {
                            this$0.plugin.umbrella.damageUmbrellaInOffhand(p);
                            p.setFireTicks(java.lang.Math.max(p.getFireTicks(), 60));
                        }
                    } else {
                        p.setFireTicks(java.lang.Math.max(p.getFireTicks(), 60));
                    }
                }
                if (p.getFireTicks() > 0) {
                    p.setFireTicks(0);
                }
                if (this$0.plugin.umbrella.hasUmbrellaInOffhand(p)) {
                    this$0.plugin.umbrella.damageUmbrellaInOffhand(p);
                    p.setFireTicks(java.lang.Math.max(p.getFireTicks(), 60));
                }
            }
            if (!isLeper) {
                /* continue */
            }
            this$0.applyPopulationBuffs(p, leperCount);
            if (p.getFoodLevel() <= 4) {
                if (Long.compare(this$0.plugin.data.getRageUntil(p), now) < 0) {
                    this$0.plugin.data.setRageUntil(p, now + 180000L);
                    p.sendMessage("§cЯрость охватила вас!");
                }
            }
            if (p.getFoodLevel() > 4) {
                if (Long.compare(this$0.plugin.data.getRageUntil(p), 0L) > 0) {
                    this$0.plugin.data.setRageUntil(p, 0L);
                }
            }
            if (Long.compare(this$0.plugin.data.getRageUntil(p), now) > 0) {
                if (this$0.plugin.effects.STRENGTH != null) {
                    p.addPotionEffect(new org.bukkit.potion.PotionEffect(this$0.plugin.effects.STRENGTH, 80, 1, false, false, true));
                }
                if (this$0.plugin.effects.SPEED != null) {
                    p.addPotionEffect(new org.bukkit.potion.PotionEffect(this$0.plugin.effects.SPEED, 80, 1, false, false, true));
                }
            }
            if (p.getFoodLevel() <= 0) {
                if (this$0.plugin.effects.BLINDNESS != null) {
                    p.addPotionEffect(new org.bukkit.potion.PotionEffect(this$0.plugin.effects.BLINDNESS, 80, 0, false, false, true));
                }
                if (this$0.plugin.effects.SLOW != null) {
                    p.addPotionEffect(new org.bukkit.potion.PotionEffect(this$0.plugin.effects.SLOW, 80, 2, false, false, true));
                }
            }
            cur = p.getNearbyEntities(20.0, 12.0, 20.0).iterator();
            if (cur.hasNext()) {
                org.bukkit.entity.Entity en = (org.bukkit.entity.Entity) cur.next();
                if (!((en instanceof org.bukkit.entity.Mob))) { /* continue */ }
                org.bukkit.entity.Mob mob = (org.bukkit.entity.Mob) en;
                org.bukkit.entity.EntityType t = mob.getType();
                if (t != org.bukkit.entity.EntityType.IRON_GOLEM) {
                    if (t == org.bukkit.entity.EntityType.SNOW_GOLEM) {
                        if (mob.getTarget() != null) {
                            if (!(mob.getTarget().getUniqueId().equals(p.getUniqueId()))) {
                                mob.setTarget(p);
                            }
                        }
                        mob.setTarget(p);
                    }
                }
                if (mob.getTarget() != null) {
                    if (!(mob.getTarget().getUniqueId().equals(p.getUniqueId()))) {
                        mob.setTarget(p);
                    }
                }
                mob.setTarget(p);
                /* continue */
            }
            /* continue */
        }
    }

}
