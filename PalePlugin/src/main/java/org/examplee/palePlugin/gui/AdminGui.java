package org.examplee.palePlugin.gui;

import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.examplee.palePlugin.PalePlugin;

public final class AdminGui {
   private final PalePlugin plugin;

   public AdminGui(PalePlugin plugin) {
      this.plugin = plugin;
   }

   public Inventory build(Player p) {
      Inventory inv = Bukkit.createInventory(new AdminGui.Holder(p.getUniqueId()), 27, "Pale: Admin");
      inv.setItem(
         11, this.btn(Material.LEVER, this.plugin.spread.isRunning() ? "Разрастание: ВКЛ" : "Разрастание: ВЫКЛ", List.of("Клик: переключить"), "toggle")
      );
      inv.setItem(
         13,
         this.btn(Material.CLOCK, "Speed: " + this.plugin.cfg.speedPerChunk, List.of("ЛКМ: -50", "ПКМ: +50", "Shift+ЛКМ: -500", "Shift+ПКМ: +500"), "speed")
      );
      inv.setItem(15, this.btn(Material.PAPER, "Выдать себе: Карта заражения", List.of("r=" + this.plugin.cfg.mapItemDefaultRadiusChunks), "map_self"));
      inv.setItem(23, this.btn(Material.BLAZE_ROD, "Выдать себе: Жезл purge", List.of("ПКМ: админ-очистка"), "purge_self"));
      inv.setItem(
         25, this.btn(Material.CARROT_ON_A_STICK, "Выдать себе: Палочка заразы", List.of("uses=" + this.plugin.cfg.infectWandUsesDefault), "wand_self")
      );
      return inv;
   }

   private ItemStack btn(Material mat, String name, List<String> lore, String action) {
      ItemStack it = new ItemStack(mat);
      ItemMeta meta = it.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(name);
         meta.setLore(lore);
         meta.getPersistentDataContainer().set(this.plugin.keys.KEY_GUI_ACTION, PersistentDataType.STRING, action);
         it.setItemMeta(meta);
      }

      return it;
   }

   private static final class Holder implements InventoryHolder {
      final UUID owner;

      Holder(UUID owner) {
         this.owner = owner;
      }

      public Inventory getInventory() {
         return null;
      }
   }
}
