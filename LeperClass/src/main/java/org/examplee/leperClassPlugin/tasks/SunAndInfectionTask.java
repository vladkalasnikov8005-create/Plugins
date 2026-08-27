package org.examplee.leperClassPlugin.tasks;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.examplee.leperClassPlugin.LeperClassPlugin;

public final class SunAndInfectionTask {
    private final org.examplee.leperClassPlugin.LeperClassPlugin plugin;
    private org.bukkit.scheduler.BukkitRunnable task;

    public SunAndInfectionTask(org.examplee.leperClassPlugin.LeperClassPlugin plugin) {
        super();
        this.plugin = plugin;
    }

    public void start() {
        stop();
        this.task = new org.examplee.leperClassPlugin.tasks.SunAndInfectionTask$1(this);
        task.runTaskTimer(plugin, 20L, 20L);
    }

    private void applyPopulationBuffs(org.bukkit.entity.Player p, int lepers) {
        int regenAmp = -1;
        int speedAmp = -1;
        int strAmp = -1;
        if (lepers >= 7) {
            regenAmp = 1;
            speedAmp = 2;
            strAmp = 2;
        } else {
            if (lepers >= 5) {
                regenAmp = 0;
                speedAmp = 1;
                strAmp = 0;
            } else {
                if (lepers >= 3) {
                    regenAmp = 0;
                    speedAmp = 0;
                }
            }
        }
        if (regenAmp >= 0) {
            if (plugin.effects.REGEN != null) {
                p.addPotionEffect(new org.bukkit.potion.PotionEffect(plugin.effects.REGEN, 80, regenAmp, false, false, true));
            }
        }
        if (speedAmp >= 0) {
            if (plugin.effects.SPEED != null) {
                p.addPotionEffect(new org.bukkit.potion.PotionEffect(plugin.effects.SPEED, 80, speedAmp, false, false, true));
            }
        }
        if (strAmp >= 0) {
            if (plugin.effects.STRENGTH != null) {
                p.addPotionEffect(new org.bukkit.potion.PotionEffect(plugin.effects.STRENGTH, 80, strAmp, false, false, true));
            }
        }
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            this.task = null;
        }
    }

}
