package org.examplee.leperClassPlugin.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.examplee.leperClassPlugin.LeperClassPlugin;

public final class InstantBrewingListener implements org.bukkit.event.Listener {
    private final org.examplee.leperClassPlugin.LeperClassPlugin plugin;

    public InstantBrewingListener(org.examplee.leperClassPlugin.LeperClassPlugin plugin) {
        super();
        this.plugin = plugin;
    }

    @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.NORMAL, ignoreCancelled = true)
    public void onClick(org.bukkit.event.inventory.InventoryClickEvent e) {
        org.bukkit.inventory.Inventory local3 = e.getInventory();
        if (!((local3 instanceof org.bukkit.inventory.BrewerInventory))) {
            return;
        }
        org.bukkit.inventory.BrewerInventory inv = (org.bukkit.inventory.BrewerInventory) local3;
        plugin.getServer().getScheduler().runTask(plugin, () -> lambda_onClick_0(inv));
    }

    @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.NORMAL, ignoreCancelled = true)
    public void onDrag(org.bukkit.event.inventory.InventoryDragEvent e) {
        org.bukkit.inventory.Inventory local3 = e.getInventory();
        if (!((local3 instanceof org.bukkit.inventory.BrewerInventory))) {
            return;
        }
        org.bukkit.inventory.BrewerInventory inv = (org.bukkit.inventory.BrewerInventory) local3;
        plugin.getServer().getScheduler().runTask(plugin, () -> lambda_onDrag_1(inv));
    }

    private void handle(org.bukkit.inventory.BrewerInventory inv) {
        org.bukkit.inventory.ItemStack ing = inv.getIngredient();
        if (ing == null) {
            return;
        }
        if (ing.getType() != org.bukkit.Material.AIR) {
            org.bukkit.Material type = ing.getType();
            boolean changed = false;
            if (type != org.bukkit.Material.NETHER_WART) {
                if (type != org.bukkit.Material.BLAZE_POWDER) {
                    if (type != org.bukkit.Material.GLISTERING_MELON_SLICE) {
                        if (!changed) {
                            return;
                        }
                    }
                    changed = convert(inv, 2);
                    if (!changed) {
                        return;
                    }
                }
                changed = convert(inv, 1);
                if (type != org.bukkit.Material.GLISTERING_MELON_SLICE) {
                    if (!changed) {
                        return;
                    }
                }
                changed = convert(inv, 2);
                if (!changed) {
                    return;
                }
            }
            changed = convert(inv, 0);
            if (type != org.bukkit.Material.BLAZE_POWDER) {
                if (type != org.bukkit.Material.GLISTERING_MELON_SLICE) {
                    if (!changed) {
                        return;
                    }
                }
                changed = convert(inv, 2);
                if (!changed) {
                    return;
                }
            }
            changed = convert(inv, 1);
            if (type != org.bukkit.Material.GLISTERING_MELON_SLICE) {
                if (!changed) {
                    return;
                }
            }
            changed = convert(inv, 2);
            if (!changed) {
                return;
            }
        }
        type = ing.getType();
        changed = 0;
        if (type != org.bukkit.Material.NETHER_WART) {
            if (type != org.bukkit.Material.BLAZE_POWDER) {
                if (type != org.bukkit.Material.GLISTERING_MELON_SLICE) {
                    if (!changed) {
                        return;
                    }
                }
                changed = convert(inv, 2);
                if (!changed) {
                    return;
                }
            }
            changed = convert(inv, 1);
            if (type != org.bukkit.Material.GLISTERING_MELON_SLICE) {
                if (!changed) {
                    return;
                }
            }
            changed = convert(inv, 2);
            if (!changed) {
                return;
            }
        }
        changed = convert(inv, 0);
        if (type != org.bukkit.Material.BLAZE_POWDER) {
            if (type != org.bukkit.Material.GLISTERING_MELON_SLICE) {
                if (!changed) {
                    return;
                }
            }
            changed = convert(inv, 2);
            if (!changed) {
                return;
            }
        }
        changed = convert(inv, 1);
        if (type != org.bukkit.Material.GLISTERING_MELON_SLICE) {
            if (!changed) {
                return;
            }
        }
        changed = convert(inv, 2);
        if (!changed) {
            return;
        }
        int amount = ing.getAmount() - 1;
        if (amount <= 0) {
            inv.setIngredient(null);
        } else {
            ing.setAmount(amount);
            inv.setIngredient(ing);
        }
    }

    private boolean convert(org.bukkit.inventory.BrewerInventory inv, int mode) {
        boolean changed = false;
        int slot = 0;
        if (slot < 3) {
            org.bukkit.inventory.ItemStack cur = inv.getItem(slot);
            if (cur != null) {
                if (cur.getType() == org.bukkit.Material.AIR) {
                } else {
                    switch (mode) {
                    case 0:
                        if (plugin.tags.isLeperBlood(cur)) {
                            inv.setItem(slot, plugin.items.makeThickLeperBlood());
                            changed = 1;
                            break;
                            if (plugin.tags.isThickLeperBlood(cur)) {
                                inv.setItem(slot, plugin.items.makeSterileLeperBlood());
                                changed = 1;
                                break;
                                if (plugin.tags.isSterileLeperBlood(cur)) {
                                    inv.setItem(slot, plugin.items.makeVaccine());
                                    changed = 1;
                                }
                            }
                        }
                    case 1:
                        if (plugin.tags.isThickLeperBlood(cur)) {
                            inv.setItem(slot, plugin.items.makeSterileLeperBlood());
                            changed = 1;
                            if (plugin.tags.isSterileLeperBlood(cur)) {
                                inv.setItem(slot, plugin.items.makeVaccine());
                                changed = 1;
                            }
                        }
                    case 2:
                        if (plugin.tags.isSterileLeperBlood(cur)) {
                            inv.setItem(slot, plugin.items.makeVaccine());
                            changed = 1;
                        }
                    default:
                    }
                    if (plugin.tags.isLeperBlood(cur)) {
                        inv.setItem(slot, plugin.items.makeThickLeperBlood());
                        changed = 1;
                        if (plugin.tags.isThickLeperBlood(cur)) {
                            inv.setItem(slot, plugin.items.makeSterileLeperBlood());
                            changed = 1;
                            if (plugin.tags.isSterileLeperBlood(cur)) {
                                inv.setItem(slot, plugin.items.makeVaccine());
                                changed = 1;
                            }
                        }
                    }
                }
            }
            slot++;
            /* continue */
        }
        return changed;
    }

    private void lambda_onDrag_1(org.bukkit.inventory.BrewerInventory inv) {
        handle(inv);
    }

    private void lambda_onClick_0(org.bukkit.inventory.BrewerInventory inv) {
        handle(inv);
    }

}
