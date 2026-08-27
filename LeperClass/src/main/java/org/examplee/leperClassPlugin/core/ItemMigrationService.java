package org.examplee.leperClassPlugin.core;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.examplee.leperClassPlugin.LeperClassPlugin;

public final class ItemMigrationService {
    private final org.examplee.leperClassPlugin.LeperClassPlugin plugin;

    public ItemMigrationService(org.examplee.leperClassPlugin.LeperClassPlugin plugin) {
        super();
        this.plugin = plugin;
    }

    public int migratePlayer(org.bukkit.entity.Player p) {
        if (p == null) {
            return 0;
        }
        org.bukkit.inventory.PlayerInventory inv = p.getInventory();
        int changed = 0;
        org.bukkit.inventory.ItemStack[] storage = inv.getStorageContents();
        for (int i = 0; i < storage.length; i++) {
            org.bukkit.inventory.ItemStack old = storage[i];
            storage[i] = migrateItem(storage[i]);
            if (storage[i] != old) {
                changed++;
            }
        }
        inv.setStorageContents(storage);
        org.bukkit.inventory.ItemStack off = inv.getItemInOffHand();
        org.bukkit.inventory.ItemStack oldOff = off;
        off = migrateItem(off);
        inv.setItemInOffHand(off);
        if (off != oldOff) {
            changed++;
        }
        org.bukkit.inventory.ItemStack[] armor = inv.getArmorContents();
        for (int i = 0; i < armor.length; i++) {
            org.bukkit.inventory.ItemStack old = armor[i];
            armor[i] = migrateItem(armor[i]);
            if (armor[i] != old) {
                changed++;
            }
        }
        inv.setArmorContents(armor);
        return changed;
    }

    private org.bukkit.inventory.ItemStack migrateItem(org.bukkit.inventory.ItemStack it) {
        if (it != null) {
            int amount = it.getAmount();
            int version = itemVersion(it);
            org.bukkit.inventory.ItemStack upgraded = null;
            if (version < 2) {
                if (!(plugin.tags.isVaccine(it))) {
                    if (!(plugin.tags.isPlagueStick(it))) {
                        if (!(plugin.tags.isPlagueBomb(it))) {
                            if (!(plugin.tags.isLeperBlood(it))) {
                                if (!(plugin.tags.isThickLeperBlood(it))) {
                                    if (!(plugin.tags.isSterileLeperBlood(it))) {
                                        if (plugin.tags.isSacrificialKnife(it)) {
                                            upgraded = plugin.items.makeSacrificialKnife();
                                            upgraded.setAmount(amount);
                                            return upgraded;
                                        }
                                    }
                                    upgraded = plugin.items.makeSterileLeperBlood();
                                    upgraded.setAmount(amount);
                                    return upgraded;
                                }
                                upgraded = plugin.items.makeThickLeperBlood();
                                upgraded.setAmount(amount);
                                return upgraded;
                            }
                            upgraded = plugin.items.makeLeperBlood();
                            upgraded.setAmount(amount);
                            return upgraded;
                        }
                        upgraded = plugin.items.makePlagueBomb();
                        upgraded.setAmount(amount);
                        return upgraded;
                    }
                    upgraded = plugin.items.makePlagueStick();
                    upgraded.setAmount(amount);
                    return upgraded;
                }
                if (it.getType() == org.bukkit.Material.POTION) {
                    if (!(plugin.tags.isPlagueStick(it))) {
                        if (!(plugin.tags.isPlagueBomb(it))) {
                            if (!(plugin.tags.isLeperBlood(it))) {
                                if (!(plugin.tags.isThickLeperBlood(it))) {
                                    if (!(plugin.tags.isSterileLeperBlood(it))) {
                                        if (plugin.tags.isSacrificialKnife(it)) {
                                            upgraded = plugin.items.makeSacrificialKnife();
                                            upgraded.setAmount(amount);
                                            return upgraded;
                                        }
                                    }
                                    upgraded = plugin.items.makeSterileLeperBlood();
                                    upgraded.setAmount(amount);
                                    return upgraded;
                                }
                                upgraded = plugin.items.makeThickLeperBlood();
                                upgraded.setAmount(amount);
                                return upgraded;
                            }
                            upgraded = plugin.items.makeLeperBlood();
                            upgraded.setAmount(amount);
                            return upgraded;
                        }
                        upgraded = plugin.items.makePlagueBomb();
                        upgraded.setAmount(amount);
                        return upgraded;
                    }
                    upgraded = plugin.items.makePlagueStick();
                    upgraded.setAmount(amount);
                    return upgraded;
                }
                upgraded = plugin.items.makeVaccine();
                upgraded.setAmount(amount);
                return upgraded;
            }
            plugin.umbrella.migrateUmbrellaItem(it);
            return it;
        }
        return null;
    }

    private int itemVersion(org.bukkit.inventory.ItemStack it) {
        org.bukkit.inventory.meta.ItemMeta meta = it.getItemMeta();
        if (meta == null) {
            return 0;
        }
        return ((java.lang.Integer) meta.getPersistentDataContainer().getOrDefault(plugin.keys.itemVersionKey, org.bukkit.persistence.PersistentDataType.INTEGER, java.lang.Integer.valueOf(0))).intValue();
    }

}
