package org.examplee.leperClassPlugin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.examplee.leperClassPlugin.LeperClassPlugin;

public final class JoinQuitDeathListener implements Listener {
   private final LeperClassPlugin plugin;

   public JoinQuitDeathListener(LeperClassPlugin plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onJoin(PlayerJoinEvent e) {
      Player p = e.getPlayer();
      if (this.plugin.data.isLeper(p) && this.plugin.effects.WATER_BREATHING != null) {
         p.addPotionEffect(new PotionEffect(this.plugin.effects.WATER_BREATHING, 300, 0, false, false, false));
      }
   }

   @EventHandler
   public void onQuit(PlayerQuitEvent e) {
      this.plugin.umbrella.resetCarry(e.getPlayer());
      this.plugin.movementLock.release(e.getPlayer());
   }

   @EventHandler
   public void onDeath(PlayerDeathEvent e) {
      Player dead = e.getEntity();
      this.plugin.umbrella.resetCarry(dead);
      this.plugin.movementLock.release(dead);
      if (this.plugin.settings.deathInfectEnabled && this.plugin.data.isLeper(dead)) {
         int infected = this.plugin
            .paleHook
            .infect(dead.getLocation(), this.plugin.settings.deathInfectRadius, this.plugin.settings.deathInfectMaxBlocks);
         if (infected > 0) {
            dead.getWorld()
               .spawnParticle(org.bukkit.Particle.SPORE_BLOSSOM_AIR, dead.getLocation().add(0.0, 1.0, 0.0), 60, 2.0, 1.0, 2.0, 0.02);
            dead.getWorld().playSound(dead.getLocation(), org.bukkit.Sound.ENTITY_ZOMBIE_DEATH, 0.8F, 0.6F);
            this.plugin.log.info("Death of leper " + dead.getName() + " infected " + infected + " blocks");
         }
      }
   }
}
