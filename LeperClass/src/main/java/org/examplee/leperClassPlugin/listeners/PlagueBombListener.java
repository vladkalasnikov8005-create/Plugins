package org.examplee.leperClassPlugin.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.examplee.leperClassPlugin.LeperClassPlugin;
import org.examplee.leperClassPlugin.util.Compat;
import org.examplee.leperClassPlugin.util.ParticlesUtil;
import org.examplee.leperClassPlugin.util.StunUtil;
import org.examplee.leperClassPlugin.util.TextUtil;

public final class PlagueBombListener implements Listener {
   private final LeperClassPlugin plugin;
   private final Map<UUID, Long> cd = new HashMap<>();

   public PlagueBombListener(LeperClassPlugin plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onQuit(PlayerQuitEvent e) {
      this.cd.remove(e.getPlayer().getUniqueId());
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = false
   )
   public void onUse(PlayerInteractEvent e) {
      if (e.getHand() != EquipmentSlot.OFF_HAND) {
         Player p = e.getPlayer();
         ItemStack used = e.getItem();
         if (used != null && this.plugin.tags.isPlagueBomb(used)) {
            Action a = e.getAction();
            if (a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK) {
               if (a == Action.RIGHT_CLICK_BLOCK && !p.isSneaking()) {
                  e.setCancelled(true);
               }

               long now = System.currentTimeMillis();
               if (now - this.cd.getOrDefault(p.getUniqueId(), 0L) >= 3500L) {
                  this.cd.put(p.getUniqueId(), now);
                  Location center = this.resolveCastLocation(p, e);
                  if (center != null) {
                     this.castCloud(p, center);
                     if (p.getGameMode() != GameMode.CREATIVE) {
                        int amt = used.getAmount() - 1;
                        if (amt <= 0) {
                           p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                        } else {
                           used.setAmount(amt);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private Location resolveCastLocation(Player p, PlayerInteractEvent e) {
      if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock() != null) {
         return e.getClickedBlock().getLocation().add(0.5, 1.0, 0.5);
      } else {
         Block target;
         try {
            target = p.getTargetBlockExact(16);
         } catch (Throwable var6) {
            target = null;
         }

         if (target != null) {
            return target.getLocation().add(0.5, 1.0, 0.5);
         } else {
            Location eye = p.getEyeLocation().clone();
            Vector dir = eye.getDirection().normalize();
            return eye.add(dir.multiply(6.0));
         }
      }
   }

   private void castCloud(Player caster, final Location center) {
      final World w = center.getWorld();
      if (w != null) {
         double radius = 4.8;
         final int durationTicks = 120;

         try {
            if (w.spawnEntity(center, EntityType.AREA_EFFECT_CLOUD) instanceof AreaEffectCloud cloud) {
               cloud.setRadius((float)radius);
               cloud.setDuration(durationTicks);
               cloud.setWaitTime(0);
               cloud.setRadiusPerTick(-((float)(radius / (double)durationTicks)));
               cloud.setColor(TextUtil.C_GREEN);
               cloud.addCustomEffect(new PotionEffect(this.plugin.effects.POISON, durationTicks, 1), true);
               cloud.setSource(caster);
            }
         } catch (Throwable var11) {
         }

         for (Entity en : w.getNearbyEntities(center, radius, 2.8, radius)) {
            if (en instanceof LivingEntity) {
               LivingEntity le = (LivingEntity)en;
               if (!le.getUniqueId().equals(caster.getUniqueId())) {
                  le.addPotionEffect(new PotionEffect(this.plugin.effects.POISON, 120, 1));
                  if (le instanceof Player) {
                     Player pl = (Player)le;
                     StunUtil.stun(this.plugin, pl, 40);
                     if (this.plugin.effects.BLINDNESS != null) {
                        pl.addPotionEffect(new PotionEffect(this.plugin.effects.BLINDNESS, 40, 0, false, false, false));
                     }

                     if (this.plugin.data.isDangerBlessed(caster) && !this.plugin.data.isLeper(pl)) {
                        this.plugin.infection.addHit(pl);
                     }
                  }
               }
            }
         }

         (new BukkitRunnable() {
            int t = 0;

            public void run() {
               if (this.t >= durationTicks) {
                  this.cancel();
               } else {
                  this.t += 10;
                  w.spawnParticle(Compat.particleFirst("CAMPFIRE_COSY_SMOKE", "SMOKE", "CLOUD"), center, 6, 0.6, 0.2, 0.6, 0.01);
                  ParticlesUtil.greenDust(w, center, 10, 0.9, 0.35, 0.9, 1.4F);
               }
            }
         }).runTaskTimer(this.plugin, 0L, 10L);
         if (this.plugin.data.isDangerBlessed(caster)) {
            this.plugin.paleHook.infect(center, 5, 1400);
         }
      }
   }
}
