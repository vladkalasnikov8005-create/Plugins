package org.examplee.palePlugin.gui;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.examplee.palePlugin.PalePlugin;
import org.examplee.palePlugin.util.InvUtil;
import org.examplee.palePlugin.util.MathUtil;

public final class AdminGuiListener implements Listener {
   private final PalePlugin plugin;

   public AdminGuiListener(PalePlugin plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onClick(InventoryClickEvent e) {
      if ("Pale: Admin".equals(e.getView().getTitle())) {
         e.setCancelled(true);
         if (e.getWhoClicked() instanceof Player p) {
            ItemStack it = e.getCurrentItem();
            if (it != null) {
               ItemMeta meta = it.getItemMeta();
               if (meta != null) {
                  String action = (String)meta.getPersistentDataContainer().get(this.plugin.keys.KEY_GUI_ACTION, PersistentDataType.STRING);
                  if (action != null) {
                     switch (action) {
                        case "toggle":
                           if (!p.hasPermission("pale.admin")) {
                              p.sendMessage(ChatColor.RED + "Нет прав.");
                              return;
                           }

                           this.plugin.spread.setRunning(!this.plugin.spread.isRunning());
                           p.openInventory(this.plugin.adminGui.build(p));
                           break;
                        case "speed":
                           if (!p.hasPermission("pale.admin")) {
                              p.sendMessage(ChatColor.RED + "Нет прав.");
                              return;
                           }

                           int delta = 0;
                           boolean right = e.isRightClick();
                           boolean shift = e.isShiftClick();
                           if (!right && !shift) {
                              delta = -50;
                           }

                           if (right && !shift) {
                              delta = 50;
                           }

                           if (!right && shift) {
                              delta = -500;
                           }

                           if (right && shift) {
                              delta = 500;
                           }

                           this.plugin.cfg.speedPerChunk = MathUtil.clamp(this.plugin.cfg.speedPerChunk + delta, 1, 5000);
                           this.plugin.getConfig().set("spread.speedPerChunk", this.plugin.cfg.speedPerChunk);
                           this.plugin.saveConfig();
                           p.openInventory(this.plugin.adminGui.build(p));
                           break;
                        case "map_self":
                           InvUtil.giveOrDrop(p, this.plugin.items.makeInfectionMap(1, this.plugin.cfg.mapItemDefaultRadiusChunks));
                           p.sendMessage(ChatColor.GREEN + "Выдано: карта заражения.");
                           break;
                        case "purge_self":
                           if (!p.hasPermission("pale.admin")) {
                              p.sendMessage(ChatColor.RED + "Нет прав.");
                              return;
                           }

                           InvUtil.giveOrDrop(p, this.plugin.items.makeAdminPurgeWand(1));
                           p.sendMessage(ChatColor.GREEN + "Выдано: жезл purge.");
                           break;
                        case "wand_self":
                           InvUtil.giveOrDrop(p, this.plugin.items.makeInfectWand(1, this.plugin.cfg.infectWandUsesDefault));
                           p.sendMessage(ChatColor.GREEN + "Выдано: палочка заразы.");
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onDrag(InventoryDragEvent e) {
      if ("Pale: Admin".equals(e.getView().getTitle())) {
         e.setCancelled(true);
      }
   }
}
