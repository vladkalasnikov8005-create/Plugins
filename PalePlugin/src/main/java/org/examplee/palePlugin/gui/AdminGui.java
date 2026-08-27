package org.examplee.palePlugin.gui;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.examplee.palePlugin.PalePlugin;

public final class AdminGui {
    private final org.examplee.palePlugin.PalePlugin plugin;

    public AdminGui(org.examplee.palePlugin.PalePlugin plugin) {
        super();
        this.plugin = plugin;
    }

    public org.bukkit.inventory.Inventory build(org.bukkit.entity.Player p) {
        org.bukkit.inventory.Inventory inv = org.bukkit.Bukkit.createInventory(new org.examplee.palePlugin.gui.AdminGui$Holder(p.getUniqueId()), 27, "Pale: Admin");
        if (plugin.spread.isRunning()) {
        } else {
        }
        setItem(inv, 11.btn(this, org.bukkit.Material.LEVER, java.util.List.of("Клик: переключить"), "toggle"));
        inv.setItem(13, btn(org.bukkit.Material.CLOCK, "Speed: " + plugin.cfg.speedPerChunk, java.util.List.of("ЛКМ: -50", "ПКМ: +50", "Shift+ЛКМ: -500", "Shift+ПКМ: +500"), "speed"));
        inv.setItem(15, btn(org.bukkit.Material.PAPER, "Выдать себе: Карта заражения", java.util.List.of("r=" + plugin.cfg.mapItemDefaultRadiusChunks), "map_self"));
        inv.setItem(23, btn(org.bukkit.Material.BLAZE_ROD, "Выдать себе: Жезл purge", java.util.List.of("ПКМ: админ-очистка"), "purge_self"));
        inv.setItem(25, btn(org.bukkit.Material.CARROT_ON_A_STICK, "Выдать себе: Палочка заразы", java.util.List.of("uses=" + plugin.cfg.infectWandUsesDefault), "wand_self"));
        return inv;
    }

    private org.bukkit.inventory.ItemStack btn(org.bukkit.Material mat, java.lang.String name, java.util.List lore, java.lang.String action) {
        org.bukkit.inventory.ItemStack it = new org.bukkit.inventory.ItemStack(mat);
        org.bukkit.inventory.meta.ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            meta.getPersistentDataContainer().set(plugin.keys.KEY_GUI_ACTION, org.bukkit.persistence.PersistentDataType.STRING, action);
            it.setItemMeta(meta);
        }
        return it;
    }

}
