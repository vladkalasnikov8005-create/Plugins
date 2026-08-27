package org.examplee.leperClassPlugin.umbrella;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.examplee.leperClassPlugin.LeperClassPlugin;
import org.examplee.leperClassPlugin.util.Compat;

public final class UmbrellaManager {
    private final org.examplee.leperClassPlugin.LeperClassPlugin plugin;
    private final java.util.Map remainingCache;
    private final java.util.Map loreDisplayCache;

    public UmbrellaManager(org.examplee.leperClassPlugin.LeperClassPlugin plugin) {
        super();
        this.remainingCache = new java.util.concurrent.ConcurrentHashMap();
        this.loreDisplayCache = new java.util.concurrent.ConcurrentHashMap();
        this.plugin = plugin;
    }

    public boolean hasUmbrellaInOffhand(org.bukkit.entity.Player p) {
        org.bukkit.inventory.ItemStack off = p.getInventory().getItemInOffHand();
        migrateUmbrellaItem(off);
        return plugin.tags.isUmbrella(off);
    }

    public boolean isTracking(org.bukkit.entity.Player p) {
        return remainingCache.containsKey(p.getUniqueId());
    }

    public void resetCarry(org.bukkit.entity.Player p) {
    }

    public void damageUmbrellaInOffhand(org.bukkit.entity.Player p) {
        org.bukkit.inventory.ItemStack off = p.getInventory().getItemInOffHand();
        migrateUmbrellaItem(off);
        if (!(plugin.tags.isUmbrella(off))) {
            remainingCache.remove(p.getUniqueId());
            return;
        }
        java.util.UUID id = p.getUniqueId();
        int remaining = ((java.lang.Integer) remainingCache.computeIfAbsent(id, x -> lambda$damageUmbrellaInOffhand$0(off, x))).intValue();
        remaining += 255;
        if (remaining <= 0) {
            remainingCache.remove(id);
            p.getInventory().setItemInOffHand(new org.bukkit.inventory.ItemStack(org.bukkit.Material.AIR));
            java.lang.String[] tmp1 = new java.lang.String[2];
            tmp1[0] = "ENTITY_ITEM_BREAK";
            tmp1[1] = "BLOCK_ANVIL_BREAK";
            p.playSound(p.getLocation(), org.examplee.leperClassPlugin.util.Compat.soundFirst(tmp1), 1.0F, 1.0F);
            return;
        }
        remainingCache.put(id, java.lang.Integer.valueOf(remaining));
        int lastShown = ((java.lang.Integer) loreDisplayCache.getOrDefault(id, java.lang.Integer.valueOf(-1))).intValue();
        if (lastShown != -1) {
            if (java.lang.Math.abs(lastShown - remaining) < 30) {
                if (remaining <= 30) {
                    loreDisplayCache.put(id, java.lang.Integer.valueOf(remaining));
                    updateUmbrellaLore(off, remaining);
                }
            }
        }
        loreDisplayCache.put(id, java.lang.Integer.valueOf(remaining));
        updateUmbrellaLore(off, remaining);
    }

    public void flushOffhand(org.bukkit.entity.Player p) {
        java.lang.Integer remaining = (java.lang.Integer) remainingCache.get(p.getUniqueId());
        if (remaining != null) {
            org.bukkit.inventory.ItemStack off = p.getInventory().getItemInOffHand();
            if (plugin.tags.isUmbrella(off)) {
                org.bukkit.inventory.meta.ItemMeta meta = off.getItemMeta();
                if (meta == null) {
                    return;
                }
            }
            remainingCache.remove(p.getUniqueId());
            return;
        }
        off = p.getInventory().getItemInOffHand();
        if (plugin.tags.isUmbrella(off)) {
            meta = off.getItemMeta();
            if (meta == null) {
                return;
            }
        }
        remainingCache.remove(p.getUniqueId());
        meta = off.getItemMeta();
        if (meta == null) {
            return;
        }
        org.bukkit.persistence.PersistentDataContainer pdc = meta.getPersistentDataContainer();
        int tier = ((java.lang.Integer) pdc.getOrDefault(plugin.keys.umbrellaTierKey, org.bukkit.persistence.PersistentDataType.INTEGER, java.lang.Integer.valueOf(1))).intValue();
        pdc.set(plugin.keys.umbrellaRemainingKey, org.bukkit.persistence.PersistentDataType.INTEGER, remaining);
        java.lang.String[] tmp1 = new java.lang.String[4];
        tmp1[0] = "§7Держи в левой руке";
        tmp1[1] = "§7Защищает от солнца";
        tmp1[2] = "§7Уровень: " + tier;
        tmp1[3] = "§7Осталось: " + formatSeconds(remaining.intValue());
        meta.setLore(java.util.Arrays.asList(tmp1));
        off.setItemMeta(meta);
    }

    public void forget(org.bukkit.entity.Player p) {
        remainingCache.remove(p.getUniqueId());
    }

    public void flushAllOnline() {
        java.util.Iterator local1 = org.bukkit.Bukkit.getOnlinePlayers().iterator();
        if (local1.hasNext()) {
            org.bukkit.entity.Player p = (org.bukkit.entity.Player) local1.next();
            flushOffhand(p);
            /* continue */
        }
    }

    private int readRemainingFromItem(org.bukkit.inventory.ItemStack off) {
        migrateUmbrellaItem(off);
        org.bukkit.inventory.meta.ItemMeta meta = off.getItemMeta();
        if (meta == null) {
            return 0;
        }
        org.bukkit.persistence.PersistentDataContainer pdc = meta.getPersistentDataContainer();
        int lifetime = ((java.lang.Integer) pdc.getOrDefault(plugin.keys.umbrellaLifetimeKey, org.bukkit.persistence.PersistentDataType.INTEGER, java.lang.Integer.valueOf(600))).intValue();
        return ((java.lang.Integer) pdc.getOrDefault(plugin.keys.umbrellaRemainingKey, org.bukkit.persistence.PersistentDataType.INTEGER, java.lang.Integer.valueOf(lifetime))).intValue();
    }

    public boolean migrateUmbrellaItem(org.bukkit.inventory.ItemStack it) {
        if (plugin.tags.isUmbrella(it)) {
            org.bukkit.inventory.meta.ItemMeta meta = it.getItemMeta();
            if (meta == null) {
                return false;
            }
        }
        return false;
    }

    private void updateUmbrellaLore(org.bukkit.inventory.ItemStack it, int remaining) {
        if (it == null) {
            return;
        }
        if (plugin.tags.isUmbrella(it)) {
            org.bukkit.inventory.meta.ItemMeta meta = it.getItemMeta();
            if (meta == null) {
                return;
            }
        }
        meta = it.getItemMeta();
        if (meta == null) {
            return;
        }
        int tier = ((java.lang.Integer) meta.getPersistentDataContainer().getOrDefault(plugin.keys.umbrellaTierKey, org.bukkit.persistence.PersistentDataType.INTEGER, java.lang.Integer.valueOf(1))).intValue();
        java.lang.String[] tmp1 = new java.lang.String[4];
        tmp1[0] = "§7Держи в левой руке";
        tmp1[1] = "§7Защищает от солнца";
        tmp1[2] = "§7Уровень: " + tier;
        tmp1[3] = "§7Осталось: " + formatSeconds(remaining);
        meta.setLore(java.util.Arrays.asList(tmp1));
        it.setItemMeta(meta);
    }

    private int lifetimeByTier(int tier) {
        if (tier > 0) {
            if (tier != 1) {
                if (tier == 2) {
                    return 1500;
                }
            }
            return 600;
        }
        return 150;
    }

    private java.lang.String formatSeconds(int total) {
        int s = java.lang.Math.max(0, total);
        int min = s / 60;
        int sec = s % 60;
        java.lang.Object[] tmp1 = new java.lang.Object[2];
        tmp1[0] = java.lang.Integer.valueOf(min);
        tmp1[1] = java.lang.Integer.valueOf(sec);
        return java.lang.String.format("%02d:%02d", tmp1);
    }

    private java.lang.Integer lambda$damageUmbrellaInOffhand$0(org.bukkit.inventory.ItemStack off, java.util.UUID x) {
        return java.lang.Integer.valueOf(readRemainingFromItem(off));
    }

}
