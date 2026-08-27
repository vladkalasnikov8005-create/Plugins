package org.examplee.palePlugin.items;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice.MaterialChoice;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.examplee.palePlugin.PalePlugin;
import org.examplee.palePlugin.util.InvUtil;
import org.examplee.palePlugin.util.Msg;

public final class PaleItems {
    private final org.examplee.palePlugin.PalePlugin plugin;

    public PaleItems(org.examplee.palePlugin.PalePlugin plugin) {
        super();
        this.plugin = plugin;
    }

    public void registerRecipes() {
        try {
            org.bukkit.inventory.ItemStack salt2 = makeSalt(2);
            org.bukkit.inventory.ShapelessRecipe r = new org.bukkit.inventory.ShapelessRecipe(new org.bukkit.NamespacedKey(plugin, "pale_salt_recipe"), salt2);
            r.addIngredient(org.bukkit.Material.GLOWSTONE_DUST);
            r.addIngredient(org.bukkit.Material.BONE_MEAL);
            org.bukkit.Bukkit.addRecipe(r);
        }
        catch (java.lang.Exception salt2) {
            try {
                org.bukkit.inventory.ItemStack holy = makeHolyWater(1);
                r = new org.bukkit.inventory.ShapelessRecipe(new org.bukkit.NamespacedKey(plugin, "pale_holywater_recipe"), holy);
                r.addIngredient(new org.bukkit.inventory.RecipeChoice.MaterialChoice(org.bukkit.Material.SPLASH_POTION));
                r.addIngredient(org.bukkit.Material.GHAST_TEAR);
                org.bukkit.Bukkit.addRecipe(r);
            }
            catch (java.lang.Exception ex) {
            }
        }
    }

    public org.bukkit.inventory.ItemStack makeSalt(int amount) {
        org.bukkit.inventory.ItemStack it = new org.bukkit.inventory.ItemStack(org.bukkit.Material.GLOWSTONE_DUST, amount);
        org.bukkit.inventory.meta.ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(org.examplee.palePlugin.util.Msg.g("[Pale] Очищающая соль"));
            meta.setLore(java.util.List.of("ПКМ: очищает заражение в радиусе " + plugin.cfg.saltRadius));
            meta.getPersistentDataContainer().set(plugin.keys.KEY_SALT, org.bukkit.persistence.PersistentDataType.BYTE, java.lang.Byte.valueOf(1));
            it.setItemMeta(meta);
        }
        return it;
    }

    public boolean isSalt(org.bukkit.inventory.ItemStack it) {
        return hasByte(it, plugin.keys.KEY_SALT);
    }

    public org.bukkit.inventory.ItemStack makeHolyWater(int amount) {
        org.bukkit.inventory.ItemStack it = new org.bukkit.inventory.ItemStack(org.bukkit.Material.SPLASH_POTION, amount);
        org.bukkit.inventory.meta.ItemMeta im = it.getItemMeta();
        if ((im instanceof org.bukkit.inventory.meta.PotionMeta)) {
            org.bukkit.inventory.meta.PotionMeta pm = (org.bukkit.inventory.meta.PotionMeta) im;
            pm.setDisplayName(org.examplee.palePlugin.util.Msg.g("[Pale] Святая вода"));
            pm.setLore(java.util.List.of("Брось: очищает заражение в радиусе " + plugin.cfg.holyWaterRadius));
            pm.getPersistentDataContainer().set(plugin.keys.KEY_HOLY_WATER, org.bukkit.persistence.PersistentDataType.BYTE, java.lang.Byte.valueOf(1));
            it.setItemMeta(pm);
        }
        return it;
    }

    public boolean isHolyWater(org.bukkit.inventory.ItemStack it) {
        return hasByte(it, plugin.keys.KEY_HOLY_WATER);
    }

    public org.bukkit.inventory.ItemStack makeWard(int amount) {
        org.bukkit.inventory.ItemStack it = new org.bukkit.inventory.ItemStack(org.bukkit.Material.AMETHYST_BLOCK, amount);
        org.bukkit.inventory.meta.ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(org.examplee.palePlugin.util.Msg.g("[Pale] Оберег"));
            meta.setLore(java.util.List.of("Ставь: блокирует заражение в радиусе " + plugin.cfg.wardRadius));
            meta.getPersistentDataContainer().set(plugin.keys.KEY_WARD, org.bukkit.persistence.PersistentDataType.BYTE, java.lang.Byte.valueOf(1));
            it.setItemMeta(meta);
        }
        return it;
    }

    public boolean isWard(org.bukkit.inventory.ItemStack it) {
        return hasByte(it, plugin.keys.KEY_WARD);
    }

    public org.bukkit.inventory.ItemStack makePurifierFlint(int amount) {
        org.bukkit.inventory.ItemStack it = new org.bukkit.inventory.ItemStack(org.bukkit.Material.FLINT_AND_STEEL, amount);
        org.bukkit.inventory.meta.ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(org.examplee.palePlugin.util.Msg.g("[Pale] Очищающее огниво"));
            meta.setLore(java.util.List.of("ПКМ по заражению: очищает радиус " + plugin.cfg.purifierFlintRadius, "Использований: " + plugin.cfg.purifierFlintUsesDefault));
            org.bukkit.persistence.PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(plugin.keys.KEY_PURIFIER_FLINT, org.bukkit.persistence.PersistentDataType.BYTE, java.lang.Byte.valueOf(1));
            pdc.set(plugin.keys.KEY_PURIFIER_FLINT_USES, org.bukkit.persistence.PersistentDataType.INTEGER, java.lang.Integer.valueOf(plugin.cfg.purifierFlintUsesDefault));
            it.setItemMeta(meta);
        }
        return it;
    }

    public boolean isPurifierFlint(org.bukkit.inventory.ItemStack it) {
        if (it == null) {
            return false;
        }
        if (it.getType() != org.bukkit.Material.FLINT_AND_STEEL) {
            return false;
        }
        return hasByte(it, plugin.keys.KEY_PURIFIER_FLINT);
    }

    public int getPurifierFlintUses(org.bukkit.inventory.ItemStack it) {
        org.bukkit.inventory.meta.ItemMeta meta = it.getItemMeta();
        if (meta == null) {
            return 0;
        }
        java.lang.Integer v = (java.lang.Integer) meta.getPersistentDataContainer().get(plugin.keys.KEY_PURIFIER_FLINT_USES, org.bukkit.persistence.PersistentDataType.INTEGER);
        if (v == null) {
        } else {
        }
        return 0;
    }

    public void setPurifierFlintUses(org.bukkit.inventory.ItemStack it, int usesLeft) {
        org.bukkit.inventory.meta.ItemMeta meta = it.getItemMeta();
        if (meta == null) {
            return;
        }
        org.bukkit.persistence.PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(plugin.keys.KEY_PURIFIER_FLINT_USES, org.bukkit.persistence.PersistentDataType.INTEGER, java.lang.Integer.valueOf(usesLeft));
        meta.setLore(java.util.List.of("ПКМ по заражению: очищает радиус " + plugin.cfg.purifierFlintRadius, "Использований: " + usesLeft));
        it.setItemMeta(meta);
    }

    public org.bukkit.inventory.ItemStack makeInfectionMap(int amount, int radiusChunks) {
        org.bukkit.inventory.ItemStack it = new org.bukkit.inventory.ItemStack(org.bukkit.Material.PAPER, amount);
        org.bukkit.inventory.meta.ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(org.examplee.palePlugin.util.Msg.g("[Pale] Карта заражения"));
            meta.setLore(java.util.List.of("ПКМ: показать карту", "Радиус: " + radiusChunks + " чанков"));
            org.bukkit.persistence.PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(plugin.keys.KEY_MAP_ITEM, org.bukkit.persistence.PersistentDataType.BYTE, java.lang.Byte.valueOf(1));
            pdc.set(plugin.keys.KEY_MAP_RADIUS, org.bukkit.persistence.PersistentDataType.INTEGER, java.lang.Integer.valueOf(radiusChunks));
            it.setItemMeta(meta);
        }
        return it;
    }

    public boolean isInfectionMap(org.bukkit.inventory.ItemStack it) {
        if (it == null) {
            return false;
        }
        if (it.getType() != org.bukkit.Material.PAPER) {
            return false;
        }
        return hasByte(it, plugin.keys.KEY_MAP_ITEM);
    }

    public int getMapRadius(org.bukkit.inventory.ItemStack it) {
        org.bukkit.inventory.meta.ItemMeta meta = it.getItemMeta();
        if (meta == null) {
            return plugin.cfg.mapItemDefaultRadiusChunks;
        }
        java.lang.Integer r = (java.lang.Integer) meta.getPersistentDataContainer().get(plugin.keys.KEY_MAP_RADIUS, org.bukkit.persistence.PersistentDataType.INTEGER);
        if (r == null) {
        } else {
        }
        return 0;
    }

    public org.bukkit.inventory.ItemStack makeInfectWand(int amount, int uses) {
        org.bukkit.inventory.ItemStack it = new org.bukkit.inventory.ItemStack(org.bukkit.Material.CARROT_ON_A_STICK, amount);
        org.bukkit.inventory.meta.ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(org.examplee.palePlugin.util.Msg.g("[Pale] Палочка заразы"));
            meta.setLore(java.util.List.of("ПКМ: заражает радиус " + plugin.cfg.infectWandRadius, "Использований: " + uses));
            org.bukkit.persistence.PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(plugin.keys.KEY_INFECT_WAND, org.bukkit.persistence.PersistentDataType.BYTE, java.lang.Byte.valueOf(1));
            pdc.set(plugin.keys.KEY_INFECT_WAND_USES, org.bukkit.persistence.PersistentDataType.INTEGER, java.lang.Integer.valueOf(uses));
            it.setItemMeta(meta);
        }
        return it;
    }

    public boolean isInfectWand(org.bukkit.inventory.ItemStack it) {
        if (it == null) {
            return false;
        }
        if (it.getType() != org.bukkit.Material.CARROT_ON_A_STICK) {
            return false;
        }
        return hasByte(it, plugin.keys.KEY_INFECT_WAND);
    }

    public int getInfectWandUses(org.bukkit.inventory.ItemStack it) {
        org.bukkit.inventory.meta.ItemMeta meta = it.getItemMeta();
        if (meta == null) {
            return 0;
        }
        java.lang.Integer v = (java.lang.Integer) meta.getPersistentDataContainer().get(plugin.keys.KEY_INFECT_WAND_USES, org.bukkit.persistence.PersistentDataType.INTEGER);
        if (v == null) {
        } else {
        }
        return 0;
    }

    public void setInfectWandUses(org.bukkit.inventory.ItemStack it, int usesLeft) {
        org.bukkit.inventory.meta.ItemMeta meta = it.getItemMeta();
        if (meta == null) {
            return;
        }
        meta.getPersistentDataContainer().set(plugin.keys.KEY_INFECT_WAND_USES, org.bukkit.persistence.PersistentDataType.INTEGER, java.lang.Integer.valueOf(usesLeft));
        meta.setLore(java.util.List.of("ПКМ: заражает радиус " + plugin.cfg.infectWandRadius, "Использований: " + usesLeft));
        it.setItemMeta(meta);
    }

    public org.bukkit.inventory.ItemStack makeAdminPurgeWand(int amount) {
        org.bukkit.inventory.ItemStack it = new org.bukkit.inventory.ItemStack(org.bukkit.Material.BLAZE_ROD, amount);
        org.bukkit.inventory.meta.ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(org.examplee.palePlugin.util.Msg.g("[Pale] Админ: Жезл очищения"));
            meta.setLore(java.util.List.of("Только для админов", "ПКМ: массовая очистка", "Радиус: " + plugin.cfg.adminPurgeRadiusChunks + " чанков", "Глубина: " + plugin.cfg.adminPurgeDepth));
            meta.getPersistentDataContainer().set(plugin.keys.KEY_ADMIN_PURGE, org.bukkit.persistence.PersistentDataType.BYTE, java.lang.Byte.valueOf(1));
            it.setItemMeta(meta);
        }
        return it;
    }

    public boolean isAdminPurgeWand(org.bukkit.inventory.ItemStack it) {
        if (it == null) {
            return false;
        }
        if (it.getType() != org.bukkit.Material.BLAZE_ROD) {
            return false;
        }
        return hasByte(it, plugin.keys.KEY_ADMIN_PURGE);
    }

    public void giveOrDrop(org.bukkit.entity.Player p, org.bukkit.inventory.ItemStack it) {
        org.examplee.palePlugin.util.InvUtil.giveOrDrop(p, it);
    }

    private boolean hasByte(org.bukkit.inventory.ItemStack it, org.bukkit.NamespacedKey key) {
        if (it != null) {
            org.bukkit.inventory.meta.ItemMeta meta = it.getItemMeta();
            if (meta == null) {
                return false;
            }
        }
        return false;
    }

}
