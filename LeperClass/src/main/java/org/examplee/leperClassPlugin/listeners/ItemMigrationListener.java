package org.examplee.leperClassPlugin.listeners;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.examplee.leperClassPlugin.LeperClassPlugin;

public final class ItemMigrationListener implements org.bukkit.event.Listener {
    private static final long RESCAN_COOLDOWN_MS = 0L;
    private final org.examplee.leperClassPlugin.LeperClassPlugin plugin;
    private final java.util.Map lastScan;

    public ItemMigrationListener(org.examplee.leperClassPlugin.LeperClassPlugin plugin) {
        super();
        this.lastScan = new java.util.concurrent.ConcurrentHashMap();
        this.plugin = plugin;
    }

    @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.MONITOR)
    public void onJoin(org.bukkit.event.player.PlayerJoinEvent e) {
        migratePlayerNow(e.getPlayer());
    }

    @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.MONITOR)
    public void onQuit(org.bukkit.event.player.PlayerQuitEvent e) {
        lastScan.remove(e.getPlayer().getUniqueId());
    }

    @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.MONITOR, ignoreCancelled = true)
    public void onClick(org.bukkit.event.inventory.InventoryClickEvent e) {
        org.bukkit.entity.HumanEntity local3 = e.getWhoClicked();
        if ((local3 instanceof org.bukkit.entity.Player)) {
            org.bukkit.entity.Player p = (org.bukkit.entity.Player) local3;
            queueMigrate(p);
        }
    }

    @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.MONITOR, ignoreCancelled = true)
    public void onDrag(org.bukkit.event.inventory.InventoryDragEvent e) {
        org.bukkit.entity.HumanEntity local3 = e.getWhoClicked();
        if ((local3 instanceof org.bukkit.entity.Player)) {
            org.bukkit.entity.Player p = (org.bukkit.entity.Player) local3;
            queueMigrate(p);
        }
    }

    private void queueMigrate(org.bukkit.entity.Player p) {
        long now = java.lang.System.currentTimeMillis();
        long last = ((java.lang.Long) lastScan.getOrDefault(p.getUniqueId(), java.lang.Long.valueOf(0L))).longValue();
        if (Long.compare(now - last, 3000L) < 0) {
            return;
        }
        lastScan.put(p.getUniqueId(), java.lang.Long.valueOf(now));
        plugin.getServer().getScheduler().runTask(plugin, () -> lambda_queueMigrate_0(p));
    }

    private void migratePlayerNow(org.bukkit.entity.Player p) {
        org.bukkit.inventory.PlayerInventory inv = p.getInventory();
        int changed = 0;
        org.bukkit.inventory.ItemStack[] storage = inv.getStorageContents();
        int i = 0;
        if (i < storage.length) {
            org.bukkit.inventory.ItemStack old = storage[i];
            storage[i] = migrateItem(storage[i]);
            if (storage[i] != old) {
                changed++;
            }
            i++;
            /* continue */
        }
        inv.setStorageContents(storage);
        i = inv.getItemInOffHand();
        old = migrateItem(inv.getItemInOffHand());
        inv.setItemInOffHand(off);
        if (off != oldOff) {
            changed++;
        }
        org.bukkit.inventory.ItemStack[] armor = inv.getArmorContents();
        i = 0;
        if (i < armor.length) {
            old = armor[i];
            armor[i] = migrateItem(armor[i]);
            if (armor[i] != old) {
                changed++;
            }
            i++;
            /* continue */
        }
        inv.setArmorContents(armor);
        if (changed > 0) {
            plugin.log.info("Item migration updated slots=" + changed + " player=" + p.getName());
        }
    }

    private org.bukkit.inventory.ItemStack migrateItem(org.bukkit.inventory.ItemStack it) {
        if (it != null) {
            int amount = it.getAmount();
            int version = itemVersion(it);
            if (version < 2) {
                if (!(plugin.tags.isVaccine(it))) {
                    if (!(plugin.tags.isPlagueStick(it))) {
                        if (!(plugin.tags.isPlagueBomb(it))) {
                            if (!(plugin.tags.isLeperBlood(it))) {
                                if (!(plugin.tags.isThickLeperBlood(it))) {
                                    if (!(plugin.tags.isSterileLeperBlood(it))) {
                                        if (plugin.tags.isSacrificialKnife(it)) {
                                            org.bukkit.inventory.ItemStack upgraded = plugin.items.makeSacrificialKnife();
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
        if (it != null) {
            org.bukkit.inventory.meta.ItemMeta meta = it.getItemMeta();
            if (meta == null) {
                return 0;
            }
        }
        return 0;
    }

    private void lambda_queueMigrate_0(org.bukkit.entity.Player p) {
        migratePlayerNow(p);
    }

}
