package org.examplee.leperClassPlugin.lantern;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.examplee.leperClassPlugin.LeperClassPlugin;
import org.examplee.leperClassPlugin.util.TextUtil;

public final class LanternListener implements Listener {
   private final LeperClassPlugin plugin;

   public LanternListener(LeperClassPlugin plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onChunkLoad(ChunkLoadEvent e) {
      this.plugin.lanterns.loadChunk(e.getChunk());
   }

   @EventHandler
   public void onChunkUnload(ChunkUnloadEvent e) {
      this.plugin.lanterns.unloadChunk(e.getChunk());
   }

   @EventHandler
   public void onPlace(BlockPlaceEvent e) {
      if (this.plugin.tags.isQuarantineLantern(e.getItemInHand())) {
         Block b = e.getBlockPlaced();
         this.plugin.lanterns.place(b, this.plugin.settings.lanternStartFuelSec);
         b.getWorld().playSound(b.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.8F, 1.6F);
         b.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, b.getLocation().add(0.5, 0.6, 0.5), 20, 0.3, 0.3, 0.3, 0.02);
         this.plugin.msg.ok(
            e.getPlayer(),
            "Карантинный фонарь установлен. Топлива: " + this.plugin.settings.lanternStartFuelSec / 60 + " мин."
         );
      }
   }

   @EventHandler
   public void onBreak(BlockBreakEvent e) {
      Block b = e.getBlock();
      if (this.plugin.lanterns.removeAt(b)) {
         e.setDropItems(false);
         b.getWorld().dropItemNaturally(b.getLocation().add(0.5, 0.5, 0.5), this.plugin.items.makeQuarantineLantern());
         b.getWorld().playSound(b.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.8F, 1.0F);
      }
   }

   @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
   public void onRefuel(PlayerInteractEvent e) {
      if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getHand() == EquipmentSlot.HAND) {
         Block b = e.getClickedBlock();
         if (b != null && b.getType() == Material.SOUL_LANTERN && this.plugin.lanterns.isLantern(b)) {
            Player p = e.getPlayer();
            ItemStack hand = e.getItem();
            if (hand != null && hand.getType() == Material.GLOWSTONE_DUST) {
               e.setCancelled(true);
               int fuel = this.plugin.lanterns.refuel(b);
               if (fuel >= 0) {
                  if (p.getGameMode() != org.bukkit.GameMode.CREATIVE) {
                     hand.setAmount(hand.getAmount() - 1);
                  }

                  b.getWorld().playSound(b.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 0.8F, 1.4F);
                  b.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, b.getLocation().add(0.5, 0.6, 0.5), 12, 0.25, 0.25, 0.25, 0.01);
                  this.plugin.msg.ok(p, "Фонарь заправлен. Топлива: " + fuel / 60 + " мин. " + fuel % 60 + " сек.");
               }
            } else {
               int fuel = this.plugin.lanterns.fuelAt(b);
               if (fuel >= 0) {
                  p.sendMessage(
                     TextUtil.ui(
                        fuel > 0
                           ? ChatColor.YELLOW + "Топлива: " + fuel / 60 + " мин. " + fuel % 60 + " сек."
                           : ChatColor.RED + "Фонарь погас. Заправьте светопылью (ПКМ)."
                     )
                  );
               }
            }
         }
      }
   }
}
