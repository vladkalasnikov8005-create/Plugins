package org.examplee.leperClassPlugin.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.examplee.leperClassPlugin.LeperClassPlugin;
import org.examplee.leperClassPlugin.util.EntityUtil;
import org.examplee.leperClassPlugin.util.InventoryUtil;

public final class LeperMenuListener implements Listener {
   private final LeperClassPlugin plugin;

   public LeperMenuListener(LeperClassPlugin plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onMenuClick(InventoryClickEvent e) {
      if (e.getInventory().getHolder() instanceof LeperMenuHolder holder) {
         e.setCancelled(true);
         if (e.getWhoClicked() instanceof Player admin) {
            Player target = Bukkit.getPlayer(holder.getTarget());
            if (target != null) {
               int slot = e.getRawSlot();
               if (slot >= 0 && slot < e.getInventory().getSize()) {
                  if (slot == 11) {
                     this.plugin.data.setLeper(target, true);
                     this.plugin.infection.cureDataOnly(target);
                     EntityUtil.clearHostileTargets(target, 32.0);
                     admin.sendMessage(ChatColor.GREEN + target.getName() + " теперь Прокаженный.");
                     this.plugin.menu.open(admin, target);
                  } else if (slot == 15) {
                     this.plugin.data.setLeper(target, false);
                     this.plugin.infection.cureDataOnly(target);
                     admin.sendMessage(ChatColor.GREEN + target.getName() + " больше не Прокаженный.");
                     this.plugin.menu.open(admin, target);
                  } else {
                     switch (slot) {
                        case 17:
                           InventoryUtil.giveOrDrop(target, this.plugin.items.makeUmbrellaTiny());
                           break;
                        case 18:
                           InventoryUtil.giveOrDrop(target, this.plugin.items.makeUmbrellaWeak());
                           break;
                        case 19:
                           InventoryUtil.giveOrDrop(target, this.plugin.items.makeUmbrellaNormal());
                           break;
                        case 20:
                           InventoryUtil.giveOrDrop(target, this.plugin.items.makeUmbrellaStrong());
                        case 21:
                        default:
                           break;
                        case 22:
                           InventoryUtil.giveOrDrop(target, this.plugin.items.makePlagueStick());
                           break;
                        case 23:
                           InventoryUtil.giveOrDrop(target, this.plugin.items.makeSacrificialKnife());
                           break;
                        case 24:
                           InventoryUtil.giveOrDrop(target, this.plugin.items.makePlagueBomb());
                           break;
                        case 25:
                           InventoryUtil.giveOrDrop(target, this.plugin.items.makeLeperBlood());
                           break;
                        case 26:
                           InventoryUtil.giveOrDrop(target, this.plugin.items.makeVaccine());
                     }
                  }
               }
            }
         }
      }
   }
}
