package org.examplee.leperClassPlugin.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.examplee.leperClassPlugin.LeperClassPlugin;
import org.examplee.leperClassPlugin.util.Compat;
import org.examplee.leperClassPlugin.util.ParticlesUtil;
import org.examplee.leperClassPlugin.util.StunUtil;
import org.examplee.leperClassPlugin.util.TextUtil;

public final class PlagueStickListener implements Listener {
   private final LeperClassPlugin plugin;
   private final Map<UUID, Long> rcCooldown = new HashMap<>();

   public PlagueStickListener(LeperClassPlugin plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onQuit(PlayerQuitEvent e) {
      this.rcCooldown.remove(e.getPlayer().getUniqueId());
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = false
   )
   public void onRightClick(PlayerInteractEvent e) {
      if (e.getHand() != EquipmentSlot.OFF_HAND) {
         Player p = e.getPlayer();
         ItemStack used = e.getItem();
         if (used != null && this.plugin.tags.isPlagueStick(used)) {
            Action a = e.getAction();
            if (a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK) {
               if (a == Action.RIGHT_CLICK_BLOCK && !p.isSneaking()) {
                  e.setCancelled(true);
               }

               long now = System.currentTimeMillis();
               if (now - this.rcCooldown.getOrDefault(p.getUniqueId(), 0L) >= 700L) {
                  this.rcCooldown.put(p.getUniqueId(), now);
                  Sound s = Compat.soundFirst("ENTITY_PANDA_SNEEZE", "ENTITY_SLIME_SQUISH", "ENTITY_SLIME_SQUISH_SMALL");
                  p.getWorld().playSound(p.getLocation(), s, 0.85F, 0.95F);
                  Location eye = p.getEyeLocation();
                  ParticlesUtil.greenDust(p.getWorld(), eye, 10, 0.18, 0.18, 0.18, 1.6F);
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onHit(EntityDamageByEntityEvent e) {
      if (e.getDamager() instanceof Player damager) {
         if (e.getEntity() instanceof LivingEntity target) {
            ItemStack var7 = damager.getInventory().getItemInMainHand();
            if (this.plugin.tags.isPlagueStick(var7)) {
               target.addPotionEffect(new PotionEffect(this.plugin.effects.POISON, 100, 1));
               if (target instanceof Player tp) {
                  StunUtil.stun(this.plugin, tp, 40);
                  tp.sendMessage(TextUtil.ui(ChatColor.DARK_GREEN + "Тебя оглушила проказа!"));
                  if (this.plugin.data.isDangerBlessed(damager) && !this.plugin.data.isLeper(tp)) {
                     this.plugin.infection.addHit(tp);
                  }
               }

               ParticlesUtil.greenDust(target.getWorld(), target.getLocation().add(0.0, 1.0, 0.0), 20, 0.4, 0.6, 0.4, 1.6F);
            }
         }
      }
   }
}
