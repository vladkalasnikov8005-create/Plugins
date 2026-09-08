package org.examplee.leperClassPlugin.listeners;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.examplee.leperClassPlugin.LeperClassPlugin;
import org.examplee.leperClassPlugin.util.TextUtil;

public final class VaccineListener implements Listener {
   private final LeperClassPlugin plugin;

   public VaccineListener(LeperClassPlugin plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onUse(PlayerInteractEvent e) {
      ItemStack used = e.getItem();
      if (used != null && this.plugin.tags.isVaccine(used)) {
         if (used.getType() == Material.POTION) {
            Action a = e.getAction();
            if (a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK) {
               e.setCancelled(true);
               Player p = e.getPlayer();
               if (this.plugin.data.isLeper(p)) {
                  p.sendMessage(TextUtil.ui(ChatColor.RED + "Вам это уже не поможет. Вы - Прокаженный."));
               } else {
                  int stage = this.plugin.data.getInfectionStage(p);
                  if (stage != 1 && stage != 2) {
                     p.sendMessage(TextUtil.ui(ChatColor.YELLOW + "Вы не заражены."));
                  } else {
                     this.plugin.infection.cure(p);
                     if (p.getGameMode() != GameMode.CREATIVE) {
                        int amt = used.getAmount() - 1;
                        if (amt <= 0) {
                           p.getInventory().setItemInMainHand(new ItemStack(Material.GLASS_BOTTLE));
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
}
