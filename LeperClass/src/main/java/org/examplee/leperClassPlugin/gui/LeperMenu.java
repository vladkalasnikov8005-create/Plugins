package org.examplee.leperClassPlugin.gui;

import java.util.Arrays;
import java.util.Collections;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.examplee.leperClassPlugin.LeperClassPlugin;
import org.examplee.leperClassPlugin.util.TextUtil;

public final class LeperMenu {
   private final LeperClassPlugin plugin;

   public LeperMenu(LeperClassPlugin plugin) {
      this.plugin = plugin;
   }

   public void open(Player admin, Player target) {
      Inventory inv = Bukkit.createInventory(new LeperMenuHolder(target.getUniqueId()), 27, TextUtil.gPurpleGreen("LEPER | CONTROL"));
      ItemStack bg = this.plugin.items.bgPane();

      for (int i = 0; i < inv.getSize(); i++) {
         inv.setItem(i, bg);
      }

      inv.setItem(
         11,
         this.plugin
            .items
            .button(
               Material.EMERALD_BLOCK,
               TextUtil.gGreenGray("ДАТЬ КЛАСС"),
               Arrays.asList(ChatColor.GRAY + "Клик: выдать Прокаженного", ChatColor.DARK_GRAY + "Цель: " + target.getName())
            )
      );
      inv.setItem(
         15,
         this.plugin
            .items
            .button(
               Material.BARRIER,
               TextUtil.gRedGray("СНЯТЬ КЛАСС"),
               Arrays.asList(ChatColor.GRAY + "Клик: убрать класс", ChatColor.DARK_GRAY + "Цель: " + target.getName())
            )
      );
      inv.setItem(17, this.plugin.items.makeUmbrellaTiny());
      inv.setItem(18, this.plugin.items.makeUmbrellaWeak());
      inv.setItem(19, this.plugin.items.makeUmbrellaNormal());
      inv.setItem(20, this.plugin.items.makeUmbrellaStrong());
      inv.setItem(22, this.plugin.items.makePlagueStick());
      inv.setItem(23, this.plugin.items.makeSacrificialKnife());
      inv.setItem(24, this.plugin.items.makePlagueBomb());
      inv.setItem(25, this.plugin.items.makeLeperBlood());
      inv.setItem(26, this.plugin.items.makeVaccine());
      inv.setItem(
         13,
         this.plugin
            .items
            .button(
               Material.NAME_TAG,
               TextUtil.gPurpleGray("ЦЕЛЬ: " + target.getName()),
               Collections.singletonList(
                  ChatColor.GRAY + "Сейчас: " + (this.plugin.data.isLeper(target) ? ChatColor.DARK_RED + "Прокаженный" : ChatColor.GREEN + "Обычный")
               )
            )
      );
      admin.openInventory(inv);
   }
}
