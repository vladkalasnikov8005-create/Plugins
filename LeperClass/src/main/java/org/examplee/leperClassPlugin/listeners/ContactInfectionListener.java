package org.examplee.leperClassPlugin.listeners;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.examplee.leperClassPlugin.LeperClassPlugin;
import org.examplee.leperClassPlugin.util.TextUtil;

public final class ContactInfectionListener implements Listener {
   private final LeperClassPlugin plugin;
   private final Map<UUID, BukkitTask> pendingScare = new ConcurrentHashMap<>();

   public ContactInfectionListener(LeperClassPlugin plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onHitLeper(EntityDamageByEntityEvent e) {
      Player damagerPlayer = this.resolveAttacker(e.getDamager());
      if (damagerPlayer != null && this.plugin.data.isLeper(damagerPlayer)) {
         long now = System.currentTimeMillis();
         if (this.plugin.data.getRageUntil(damagerPlayer) > now) {
            e.setDamage(e.getDamage() * 1.8);
            if (e.getEntity() instanceof Player hit) {
               Vector dir = hit.getLocation().toVector().subtract(damagerPlayer.getLocation().toVector()).normalize();
               hit.setVelocity(hit.getVelocity().add(dir.multiply(1.9)).setY(0.6));
            }
         }
      }

      if (e.getEntity() instanceof Player victim) {
         if (this.plugin.data.isLeper(victim)) {
            if (damagerPlayer != null && !this.plugin.data.isLeper(damagerPlayer)) {
               double hpDamage = Math.max(0.0, e.getFinalDamage());
               double chance = hpDamage * this.plugin.settings.contactInfectPerHp;
               boolean realInfection = ThreadLocalRandom.current().nextDouble() < chance;
               boolean showScare = realInfection || ThreadLocalRandom.current().nextDouble() < this.plugin.settings.contactFakeScareChance;
               if (showScare && !this.pendingScare.containsKey(damagerPlayer.getUniqueId())) {
                  damagerPlayer.sendMessage(TextUtil.ui(ChatColor.DARK_GREEN + "Вы могли заразиться после контакта с прокаженным..."));
                  BukkitTask task = this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
                     this.pendingScare.remove(damagerPlayer.getUniqueId());
                     if (damagerPlayer.isOnline() && !this.plugin.data.isLeper(damagerPlayer)) {
                        if (realInfection) {
                           this.plugin.infection.startInfection(damagerPlayer);
                        } else {
                           damagerPlayer.sendMessage(TextUtil.ui(ChatColor.GREEN + "Фух, кажется пронесло. Вы не заразились."));
                        }
                     }
                  }, this.plugin.settings.contactResolveTicks);
                  this.pendingScare.put(damagerPlayer.getUniqueId(), task);
               }
            }
         }
      }
   }

   private Player resolveAttacker(Entity damager) {
      if (damager instanceof Player) {
         return (Player)damager;
      } else {
         if (damager instanceof Projectile pr) {
            ProjectileSource src = pr.getShooter();
            if (src instanceof Player) {
               return (Player)src;
            }
         }

         return null;
      }
   }
}
