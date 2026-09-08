package org.examplee.leperClassPlugin.infection;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.examplee.leperClassPlugin.LeperClassPlugin;
import org.examplee.leperClassPlugin.util.Compat;
import org.examplee.leperClassPlugin.util.EntityUtil;
import org.examplee.leperClassPlugin.util.TextUtil;

public final class InfectionManager {
   private final LeperClassPlugin plugin;

   public InfectionManager(LeperClassPlugin plugin) {
      this.plugin = plugin;
   }

   public void addHit(Player target) {
      if (!this.plugin.data.isLeper(target)) {
         int hits = Math.min(3, this.plugin.data.getInfectionHits(target) + 1);
         this.plugin.data.setInfectionHits(target, hits);
         if (hits >= 3) {
            this.startInfection(target);
         }
      }
   }

   public void startInfection(Player p) {
      if (this.plugin.data.getInfectionStage(p) <= 0 && !this.plugin.data.isLeper(p)) {
         this.plugin.data.setInfectionStage(p, 1);
         this.plugin.data.setInfectionNextPhaseMs(p, System.currentTimeMillis() + this.plugin.settings.infectionPhaseMs);
         p.sendMessage(TextUtil.ui(ChatColor.DARK_GREEN + "Вы чувствуете себя странно... Кажется, вы заразились."));
         p.playSound(p.getLocation(), Compat.soundFirst("ENTITY_ZOMBIE_INFECT", "ENTITY_ZOMBIE_VILLAGER_CURE"), 1.0F, 0.5F);
         this.plugin.log.info("Infection stage1 started for " + p.getName());
      }
   }

   public void checkProgression(Player p, long nowMs) {
      if (!this.plugin.data.isLeper(p)) {
         int stage = this.plugin.data.getInfectionStage(p);
         if (stage != 0) {
            Long next = this.plugin.data.getInfectionNextPhaseMs(p);
            if (next != null && this.plugin.lanterns != null && this.plugin.lanterns.isInLanternRange(p)) {
               // Карантинный фонарь: болезнь заморожена, дедлайн сдвигается вперёд
               this.plugin.data.setInfectionNextPhaseMs(p, next + 1000L);
               return;
            }

            if (next != null && nowMs >= next) {
               if (stage == 1) {
                  this.plugin.data.setInfectionStage(p, 2);
                  this.plugin.data.setInfectionNextPhaseMs(p, nowMs + this.plugin.settings.infectionPhaseMs);
                  p.sendMessage(TextUtil.ui(ChatColor.RED + "Вам стало хуже. Ваша кожа начала гореть на солнце!"));
                  this.plugin.log.info("Infection stage2 started for " + p.getName());
               } else {
                  if (stage == 2) {
                     this.plugin.data.clearInfection(p);
                     this.plugin.data.setLeper(p, true);
                     if (this.plugin.effects.FIRE_RES != null) {
                        p.removePotionEffect(this.plugin.effects.FIRE_RES);
                     }

                     EntityUtil.clearHostileTargets(p, 32.0);
                     p.sendMessage(TextUtil.ui(ChatColor.DARK_RED + "Инфекция поглотила вас полностью. Вы стали Прокаженным."));
                     this.plugin.log.info("Player converted to leper: " + p.getName());
                  }
               }
            }
         }
      }
   }

   public void cure(Player p) {
      this.plugin.data.clearInfection(p);
      p.sendMessage(TextUtil.ui(ChatColor.AQUA + "Вы приняли вакцину. Инфекция отступила!"));
      this.plugin.log.info("Infection cured for " + p.getName());
   }

   public void cureDataOnly(Player p) {
      this.plugin.data.clearInfection(p);
   }
}
