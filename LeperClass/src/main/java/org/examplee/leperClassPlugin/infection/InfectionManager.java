package org.examplee.leperClassPlugin.infection;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.examplee.leperClassPlugin.LeperClassPlugin;
import org.examplee.leperClassPlugin.util.Compat;
import org.examplee.leperClassPlugin.util.EntityUtil;
import org.examplee.leperClassPlugin.util.TextUtil;

public final class InfectionManager {
    private final org.examplee.leperClassPlugin.LeperClassPlugin plugin;

    public InfectionManager(org.examplee.leperClassPlugin.LeperClassPlugin plugin) {
        super();
        this.plugin = plugin;
    }

    public void addHit(org.bukkit.entity.Player target) {
        if (plugin.data.isLeper(target)) {
            return;
        }
        int hits = java.lang.Math.min(3, plugin.data.getInfectionHits(target) + 1);
        plugin.data.setInfectionHits(target, hits);
        if (hits >= 3) {
            startInfection(target);
        }
    }

    public void startInfection(org.bukkit.entity.Player p) {
        if (plugin.data.getInfectionStage(p) > 0) {
            return;
        }
        if (plugin.data.isLeper(p)) {
            return;
        }
        plugin.data.setInfectionStage(p, 1);
        plugin.data.setInfectionNextPhaseMs(p, java.lang.System.currentTimeMillis() + plugin.settings.infectionPhaseMs);
        p.sendMessage(org.examplee.leperClassPlugin.util.TextUtil.ui(java.lang.String.valueOf(org.bukkit.ChatColor.DARK_GREEN) + "Вы чувствуете себя странно... Кажется, вы заразились."));
        java.lang.String[] tmp1 = new java.lang.String[2];
        tmp1[0] = "ENTITY_ZOMBIE_INFECT";
        tmp1[1] = "ENTITY_ZOMBIE_VILLAGER_CURE";
        p.playSound(p.getLocation(), org.examplee.leperClassPlugin.util.Compat.soundFirst(tmp1), 1.0F, 0.5F);
        plugin.log.info("Infection stage1 started for " + p.getName());
    }

    public void checkProgression(org.bukkit.entity.Player p, long nowMs) {
        if (!(plugin.data.isLeper(p))) {
            int stage = plugin.data.getInfectionStage(p);
            if (stage != 0) {
                java.lang.Long next = plugin.data.getInfectionNextPhaseMs(p);
                if (next == null) {
                    return;
                }
                if (Long.compare(nowMs, next.longValue()) < 0) {
                    return;
                }
            }
            return;
        }
        stage = plugin.data.getInfectionStage(p);
        if (stage != 0) {
            next = plugin.data.getInfectionNextPhaseMs(p);
            if (next == null) {
                return;
            }
            if (Long.compare(nowMs, next.longValue()) < 0) {
                return;
            }
        }
        next = plugin.data.getInfectionNextPhaseMs(p);
        if (next == null) {
            return;
        }
        if (Long.compare(nowMs, next.longValue()) < 0) {
            return;
        }
        if (stage == 1) {
            plugin.data.setInfectionStage(p, 2);
            plugin.data.setInfectionNextPhaseMs(p, nowMs + plugin.settings.infectionPhaseMs);
            p.sendMessage(org.examplee.leperClassPlugin.util.TextUtil.ui(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + "Вам стало хуже. Ваша кожа начала гореть на солнце!"));
            plugin.log.info("Infection stage2 started for " + p.getName());
            return;
        }
        if (stage == 2) {
            plugin.data.clearInfection(p);
            plugin.data.setLeper(p, 1);
            if (plugin.effects.FIRE_RES != null) {
                p.removePotionEffect(plugin.effects.FIRE_RES);
            }
            org.examplee.leperClassPlugin.util.EntityUtil.clearHostileTargets(p, 32.0);
            p.sendMessage(org.examplee.leperClassPlugin.util.TextUtil.ui(java.lang.String.valueOf(org.bukkit.ChatColor.DARK_RED) + "Инфекция поглотила вас полностью. Вы стали Прокаженным."));
            plugin.log.info("Player converted to leper: " + p.getName());
        }
    }

    public void cure(org.bukkit.entity.Player p) {
        plugin.data.clearInfection(p);
        p.sendMessage(org.examplee.leperClassPlugin.util.TextUtil.ui(java.lang.String.valueOf(org.bukkit.ChatColor.AQUA) + "Вы приняли вакцину. Инфекция отступила!"));
        plugin.log.info("Infection cured for " + p.getName());
    }

    public void cureDataOnly(org.bukkit.entity.Player p) {
        plugin.data.clearInfection(p);
    }

}
