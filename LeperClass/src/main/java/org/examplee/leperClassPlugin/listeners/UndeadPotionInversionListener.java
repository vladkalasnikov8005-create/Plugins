package org.examplee.leperClassPlugin.listeners;

import java.util.Collection;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.examplee.leperClassPlugin.LeperClassPlugin;

public final class UndeadPotionInversionListener implements Listener {
   private final LeperClassPlugin plugin;

   public UndeadPotionInversionListener(LeperClassPlugin plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onSplash(PotionSplashEvent e) {
      Collection<PotionEffect> effects = e.getPotion().getEffects();
      if (!effects.isEmpty()) {
         for (LivingEntity le : e.getAffectedEntities()) {
            if (le instanceof Player) {
               Player p = (Player)le;
               if (this.plugin.data.isLeper(p)) {
                  boolean healLike = this.hasEffect(effects, "HEAL") || this.hasEffect(effects, "INSTANT_HEALTH") || this.hasEffect(effects, "REGEN");
                  boolean harmLike = this.hasEffect(effects, "HARM") || this.hasEffect(effects, "INSTANT_DAMAGE") || this.hasEffect(effects, "POISON");
                  if (healLike || harmLike) {
                     e.setIntensity(p, 0.0);
                  }

                  if (healLike) {
                     this.plugin.balance.hurt(p, this.plugin.settings.leperDamageFromHeal);
                     p.removePotionEffect(PotionEffectType.REGENERATION);
                  }

                  if (harmLike) {
                     this.plugin.balance.heal(p, this.plugin.settings.leperHealFromPoison);
                     p.removePotionEffect(PotionEffectType.POISON);
                  }
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onCloud(AreaEffectCloudApplyEvent e) {
      AreaEffectCloud cloud = e.getEntity();
      Collection<PotionEffect> effects = cloud.getCustomEffects();
      if (!effects.isEmpty()) {
         for (LivingEntity le : e.getAffectedEntities()) {
            if (le instanceof Player) {
               Player p = (Player)le;
               if (this.plugin.data.isLeper(p)) {
                  boolean healLike = this.hasEffect(effects, "HEAL") || this.hasEffect(effects, "INSTANT_HEALTH") || this.hasEffect(effects, "REGEN");
                  boolean harmLike = this.hasEffect(effects, "HARM") || this.hasEffect(effects, "INSTANT_DAMAGE") || this.hasEffect(effects, "POISON");
                  if (healLike) {
                     this.plugin.balance.hurt(p, Math.max(1.0, this.plugin.settings.leperDamageFromHeal * 0.67));
                     p.removePotionEffect(PotionEffectType.REGENERATION);
                  }

                  if (harmLike) {
                     this.plugin.balance.heal(p, Math.max(1.0, this.plugin.settings.leperHealFromPoison * 0.67));
                     p.removePotionEffect(PotionEffectType.POISON);
                  }
               }
            }
         }
      }
   }

   private boolean hasEffect(Collection<PotionEffect> effects, String token) {
      String t = token.toUpperCase();

      for (PotionEffect pe : effects) {
         PotionEffectType type = pe.getType();
         if (type != null && type.getName() != null && type.getName().toUpperCase().contains(t)) {
            return true;
         }
      }

      return false;
   }
}
