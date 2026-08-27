package org.examplee.leperClassPlugin.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.examplee.leperClassPlugin.LeperClassPlugin;
import org.examplee.leperClassPlugin.util.TextUtil;

import java.util.Arrays;
import java.util.Collections;

public final class LeperMenu {
    private final LeperClassPlugin plugin;

    public LeperMenu(LeperClassPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player admin, Player target) {
        Inventory inv = Bukkit.createInventory(
                new LeperMenuHolder(target.getUniqueId()),
                27,
                TextUtil.gPurpleGreen("LEPER | CONTROL")
        );
        ItemStack bg = plugin.items.bgPane();
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, bg);
        }
        inv.setItem(11, plugin.items.button(
                Material.EMERALD_BLOCK,
                TextUtil.gGreenGray("ДАТЬ КЛАСС"),
                Arrays.asList(
                        ChatColor.GRAY + "Клик: выдать Прокаженного",
                        ChatColor.DARK_GRAY + "Цель: " + target.getName()
                )
        ));
        inv.setItem(15, plugin.items.button(
                Material.BARRIER,
                TextUtil.gRedGray("СНЯТЬ КЛАСС"),
                Arrays.asList(
                        ChatColor.GRAY + "Клик: убрать класс",
                        ChatColor.DARK_GRAY + "Цель: " + target.getName()
                )
        ));
        inv.setItem(17, plugin.items.makeUmbrellaTiny());
        inv.setItem(18, plugin.items.makeUmbrellaWeak());
        inv.setItem(19, plugin.items.makeUmbrellaNormal());
        inv.setItem(20, plugin.items.makeUmbrellaStrong());
        inv.setItem(22, plugin.items.makePlagueStick());
        inv.setItem(23, plugin.items.makeSacrificialKnife());
        inv.setItem(24, plugin.items.makePlagueBomb());
        inv.setItem(25, plugin.items.makeLeperBlood());
        inv.setItem(26, plugin.items.makeVaccine());
        String status = plugin.data.isLeper(target) ? (ChatColor.LIGHT_PURPLE + "Прокаженный") : (ChatColor.GRAY + "нет класса");
        inv.setItem(13, plugin.items.button(
                Material.NAME_TAG,
                TextUtil.gPurpleGray("ЦЕЛЬ: " + target.getName()),
                Collections.singletonList(ChatColor.GRAY + "Сейчас: " + status)
        ));
        admin.openInventory(inv);
    }
}
