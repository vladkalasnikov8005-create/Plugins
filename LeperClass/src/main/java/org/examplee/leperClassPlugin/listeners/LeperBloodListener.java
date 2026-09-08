package org.examplee.leperClassPlugin.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.examplee.leperClassPlugin.LeperClassPlugin;
import org.examplee.leperClassPlugin.util.InventoryUtil;

public final class LeperBloodListener implements Listener {
   private final LeperClassPlugin plugin;
   private final Map<UUID, Long> knifeCd = new HashMap<>();

   public LeperBloodListener(LeperClassPlugin plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onQuit(PlayerQuitEvent e) {
      this.knifeCd.remove(e.getPlayer().getUniqueId());
   }

   @EventHandler
   public void onKnifeUse(PlayerInteractEvent e) {
      Action a = e.getAction();
      if (a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK) {
         Player p = e.getPlayer();
         ItemStack hand = e.getItem();
         if (hand != null && this.plugin.tags.isSacrificialKnife(hand)) {
            if (!this.plugin.data.isLeper(p)) {
               this.plugin.msg.error(p, "Только прокаженный может добыть кровь этим ножом.");
            } else {
               long now = System.currentTimeMillis();
               long last = this.knifeCd.getOrDefault(p.getUniqueId(), 0L);
               long cd = this.plugin.settings.knifeCooldownMs;
               if (now - last < cd) {
                  long sec = (cd - (now - last)) / 1000L;
                  this.plugin.msg.warn(p, "Ножик еще не готов. Осталось: " + sec + " сек.");
               } else {
                  this.knifeCd.put(p.getUniqueId(), now);
                  p.damage(2.0);
                  InventoryUtil.giveOrDrop(p, this.plugin.items.makeLeperBlood());
                  if (this.plugin.effects.MINING_FATIGUE != null) {
                     p.addPotionEffect(new PotionEffect(this.plugin.effects.MINING_FATIGUE, this.plugin.settings.knifeFatigueTicks, 0));
                  }

                  if (this.plugin.effects.WEAKNESS != null) {
                     p.addPotionEffect(new PotionEffect(this.plugin.effects.WEAKNESS, this.plugin.settings.knifeWeakTicks, 0));
                  }

                  if (this.plugin.effects.SLOW != null) {
                     p.addPotionEffect(new PotionEffect(this.plugin.effects.SLOW, this.plugin.settings.knifeSlowTicks, 0));
                  }

                  this.plugin.msg.error(p, "Вы добыли кровь. Цена высока.");
                  this.plugin.log.info("Self blood extracted by " + p.getName());
               }
            }
         }
      }
   }

   @EventHandler
   public void onLeperKilled(EntityDeathEvent e) {
      if (e.getEntity() instanceof Player victim) {
         if (this.plugin.data.isLeper(victim)) {
            Player killer = victim.getKiller();
            if (killer != null) {
               if (ThreadLocalRandom.current().nextDouble() < 0.6) {
                  e.getDrops().add(this.plugin.items.makeLeperBlood());
                  this.plugin.log.info("Blood dropped from killed leper: " + victim.getName());
               }

               if (!this.plugin.data.isLeper(killer) && ThreadLocalRandom.current().nextDouble() < 0.3) {
                  this.plugin.infection.addHit(killer);
                  this.plugin.msg.warn(killer, "Кровь прокаженного попала в раны. Вы могли заразиться.");
               }
            }
         }
      }
   }
}
