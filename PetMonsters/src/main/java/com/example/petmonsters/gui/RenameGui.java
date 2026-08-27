package com.example.petmonsters.gui;

import com.example.petmonsters.PetMonstersPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

/**
 * Opens an Anvil-style inventory so the player can type a pet name.
 * The first slot is a pre-filled paper item; when the player takes the
 * output (slot 2) the name is applied. Closing cancels naming.
 */
public class RenameGui {

    private static final int RESULT_SLOT = 2;

    private final PetMonstersPlugin plugin;
    private final Map<Player, LivingEntity> pending = new HashMap<>();
    private final Map<Player, Inventory> openInventories = new HashMap<>();

    public RenameGui(PetMonstersPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, LivingEntity pet) {
        Inventory inv = Bukkit.createInventory(null, 9, "Name your pet");
        // Use an anvil recipe-like presentation: fill first slot with a name tag
        ItemStack nameTag = new ItemStack(org.bukkit.Material.NAME_TAG);
        ItemMeta meta = nameTag.getItemMeta();
        if (meta != null) {
            String current = pet.getCustomName();
            meta.setDisplayName(current != null ? current : pet.getName());
            nameTag.setItemMeta(meta);
        }
        inv.setItem(0, nameTag);
        inv.setItem(2, cloneWithName(nameTag, ""));

        pending.put(player, pet);
        openInventories.put(player, inv);
        player.openInventory(inv);
    }

    public void handleClick(Player player, org.bukkit.event.inventory.InventoryClickEvent event) {
        if (!pending.containsKey(player)) {
            return;
        }
        Inventory inv = openInventories.get(player);

        if (event.getClickedInventory() != inv) {
            return;
        }

        event.setCancelled(true);

        if (event.getSlot() == RESULT_SLOT) {
            ItemStack result = event.getCurrentItem();
            if (result != null && result.hasItemMeta() && result.getItemMeta().hasDisplayName()) {
                String name = result.getItemMeta().getDisplayName();
                LivingEntity pet = pending.remove(player);
                openInventories.remove(player);
                player.closeInventory();
                plugin.getPetManager().rename(player, name);
            }
        }
    }

    public void handleClose(Player player) {
        pending.remove(player);
        openInventories.remove(player);
    }

    private ItemStack cloneWithName(ItemStack source, String name) {
        ItemStack out = source.clone();
        ItemMeta meta = out.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            out.setItemMeta(meta);
        }
        return out;
    }
}
