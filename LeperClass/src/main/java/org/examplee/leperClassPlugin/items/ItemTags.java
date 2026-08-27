package org.examplee.leperClassPlugin.items;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.examplee.leperClassPlugin.core.LeperKeys;

public final class ItemTags {
    private final org.examplee.leperClassPlugin.core.LeperKeys keys;

    public ItemTags(org.examplee.leperClassPlugin.core.LeperKeys keys) {
        super();
        this.keys = keys;
    }

    private boolean has(org.bukkit.inventory.ItemStack it, org.bukkit.NamespacedKey key) {
        if (it == null) {
            return false;
        }
        if (it.getType() != org.bukkit.Material.AIR) {
            org.bukkit.inventory.meta.ItemMeta meta = it.getItemMeta();
            if (meta == null) {
                return false;
            }
        }
        return false;
    }

    public boolean isPlagueStick(org.bukkit.inventory.ItemStack it) {
        return has(it, keys.plagueStickKey);
    }

    public boolean isPlagueBomb(org.bukkit.inventory.ItemStack it) {
        return has(it, keys.plagueBombKey);
    }

    public boolean isUmbrella(org.bukkit.inventory.ItemStack it) {
        return has(it, keys.umbrellaKey);
    }

    public boolean isVaccine(org.bukkit.inventory.ItemStack it) {
        return has(it, keys.vaccineKey);
    }

    public boolean isLeperBlood(org.bukkit.inventory.ItemStack it) {
        return has(it, keys.leperBloodKey);
    }

    public boolean isThickLeperBlood(org.bukkit.inventory.ItemStack it) {
        return has(it, keys.thickBloodKey);
    }

    public boolean isSterileLeperBlood(org.bukkit.inventory.ItemStack it) {
        return has(it, keys.sterileBloodKey);
    }

    public boolean isSacrificialKnife(org.bukkit.inventory.ItemStack it) {
        return has(it, keys.sacrificialKnifeKey);
    }

}
