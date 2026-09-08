package org.examplee.leperClassPlugin.listeners;

import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.examplee.leperClassPlugin.LeperClassPlugin;

public final class ConsumeListener implements Listener {
   private static final Set<Material> MEAT_AND_FISH = EnumSet.of(
      Material.BEEF,
      Material.COOKED_BEEF,
      Material.PORKCHOP,
      Material.COOKED_PORKCHOP,
      Material.CHICKEN,
      Material.COOKED_CHICKEN,
      Material.MUTTON,
      Material.COOKED_MUTTON,
      Material.RABBIT,
      Material.COOKED_RABBIT,
      Material.ROTTEN_FLESH,
      Material.COD,
      Material.COOKED_COD,
      Material.SALMON,
      Material.COOKED_SALMON,
      Material.TROPICAL_FISH,
      Material.PUFFERFISH
   );
   private static final Set<Material> GOLDEN_FOOD = EnumSet.of(Material.GOLDEN_APPLE, Material.ENCHANTED_GOLDEN_APPLE, Material.GOLDEN_CARROT);
   private final LeperClassPlugin plugin;

   public ConsumeListener(LeperClassPlugin plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onConsume(PlayerItemConsumeEvent e) {
      Player p = e.getPlayer();
      ItemStack item = e.getItem();
      if (this.plugin.tags.isLeperBlood(item)) {
         p.addPotionEffect(new PotionEffect(this.plugin.effects.POISON, 240, 1));
         if (!this.plugin.data.isLeper(p)) {
            this.plugin.infection.startInfection(p);
         }
      } else if (this.plugin.tags.isThickLeperBlood(item)) {
         p.addPotionEffect(new PotionEffect(this.plugin.effects.POISON, 160, 1));
         if (this.plugin.effects.NAUSEA != null) {
            p.addPotionEffect(new PotionEffect(this.plugin.effects.NAUSEA, 240, 0));
         }
      } else if (!this.plugin.tags.isSterileLeperBlood(item)) {
         if (this.plugin.data.isLeper(p)) {
            Material type = item.getType();
            if (type.isEdible() && !MEAT_AND_FISH.contains(type) && !GOLDEN_FOOD.contains(type)) {
               e.setCancelled(true);
               this.plugin.msg.error(p, "Прокаженные могут есть только мясо/рыбу и золотую еду.");
            } else {
               if (this.plugin.effects.FIRE_RES != null) {
                  this.plugin.getServer().getScheduler().runTask(this.plugin, () -> p.removePotionEffect(this.plugin.effects.FIRE_RES));
               }

               if (type == Material.ROTTEN_FLESH) {
                  this.plugin.getServer().getScheduler().runTask(this.plugin, () -> p.removePotionEffect(PotionEffectType.HUNGER));
               }

               if (item.getItemMeta() instanceof PotionMeta pm) {
                  boolean poisonPotion = this.potionHas(pm, "POISON");
                  boolean harmPotion = this.potionHas(pm, "HARM") || this.potionHas(pm, "INSTANT_DAMAGE");
                  boolean healingPotion = this.potionHas(pm, "HEAL") || this.potionHas(pm, "INSTANT_HEALTH") || this.potionHas(pm, "REGEN");
                  if (poisonPotion) {
                     this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                        this.plugin.balance.heal(p, this.plugin.settings.leperHealFromPoison);
                        p.removePotionEffect(PotionEffectType.POISON);
                     });
                  }

                  if (harmPotion) {
                     this.plugin.getServer().getScheduler().runTask(this.plugin, () -> this.plugin.balance.heal(p, this.plugin.settings.leperHealFromHarm));
                  }

                  if (healingPotion) {
                     this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                        this.plugin.balance.hurt(p, this.plugin.settings.leperDamageFromHeal);
                        this.plugin.msg.error(p, "Для прокаженного это зелье обернулось болью.");
                     });
                  }

                  if (this.plugin.effects.FIRE_RES != null && this.potionHas(pm, "FIRE_RES")) {
                     e.setCancelled(true);
                     this.plugin.msg.error(p, "Прокаженные не могут пить огнестойкость.");
                  }
               }
            }
         }
      }
   }

   private boolean potionHas(PotionMeta meta, String token) {
      token = token.toUpperCase();

      for (PotionEffect pe : meta.getCustomEffects()) {
         if (pe.getType().getName().toUpperCase().contains(token)) {
            return true;
         }
      }

      try {
         Method m = meta.getClass().getMethod("getBasePotionType");
         Object base = m.invoke(meta);
         if (base != null && base.toString().toUpperCase().contains(token)) {
            return true;
         }
      } catch (Throwable var5) {
      }

      return false;
   }
}
