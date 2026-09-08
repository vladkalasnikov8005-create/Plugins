package org.examplee.leperClassPlugin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffect;
import org.examplee.leperClassPlugin.LeperClassPlugin;

public final class EffectBlockListener implements Listener {
   private final LeperClassPlugin plugin;

   public EffectBlockListener(LeperClassPlugin plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onEffect(EntityPotionEffectEvent e) {
      if (e.getEntity() instanceof Player p) {
         boolean var6 = this.plugin.data.isLeper(p);
         boolean isStage2 = this.plugin.data.getInfectionStage(p) == 2;
         if (var6 || isStage2) {
            PotionEffect ne = e.getNewEffect();
            if (ne != null && this.plugin.effects.FIRE_RES != null && ne.getType().equals(this.plugin.effects.FIRE_RES)) {
               e.setCancelled(true);
            }
         }
      }
   }
}
