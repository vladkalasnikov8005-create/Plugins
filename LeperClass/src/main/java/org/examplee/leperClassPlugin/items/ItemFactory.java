package org.examplee.leperClassPlugin.items;

import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.examplee.leperClassPlugin.core.LeperKeys;
import org.examplee.leperClassPlugin.util.Compat;
import org.examplee.leperClassPlugin.util.TextUtil;

public final class ItemFactory {
    public static final int ITEM_VERSION = 0;
    private final org.examplee.leperClassPlugin.core.LeperKeys keys;

    public ItemFactory(org.examplee.leperClassPlugin.core.LeperKeys keys) {
        super();
        this.keys = keys;
    }

    public org.bukkit.inventory.ItemStack makePlagueStick() {
        org.bukkit.inventory.ItemStack it = new org.bukkit.inventory.ItemStack(org.bukkit.Material.STICK);
        org.bukkit.inventory.meta.ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(org.examplee.leperClassPlugin.util.TextUtil.gPurpleGreen("ПАЛКА ПРОКАЗЫ"));
            meta.setLore(java.util.List.of(org.examplee.leperClassPlugin.util.TextUtil.loreHint("Удар: яд и короткое оглушение"), org.examplee.leperClassPlugin.util.TextUtil.loreWarn("Заражение работает только с благословлением Денжер")));
            org.bukkit.inventory.ItemFlag[] tmp1 = new org.bukkit.inventory.ItemFlag[1];
            tmp1[0] = org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES;
            meta.addItemFlags(tmp1);
            markVersion(meta);
            meta.getPersistentDataContainer().set(keys.plagueStickKey, org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
            it.setItemMeta(meta);
        }
        return it;
    }

    public org.bukkit.inventory.ItemStack makePlagueBomb() {
        java.lang.String[] tmp1 = new java.lang.String[3];
        tmp1[0] = "SLIME_BALL";
        tmp1[1] = "FERMENTED_SPIDER_EYE";
        tmp1[2] = "SPIDER_EYE";
        org.bukkit.Material mat = org.examplee.leperClassPlugin.util.Compat.materialFirst(tmp1);
        org.bukkit.inventory.ItemStack it = new org.bukkit.inventory.ItemStack(mat);
        org.bukkit.inventory.meta.ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(org.examplee.leperClassPlugin.util.TextUtil.gGreenGray("ОБЛАКО ПРОКАЗЫ"));
            meta.setLore(java.util.List.of(org.examplee.leperClassPlugin.util.TextUtil.loreHint("ПКМ: ядовитое облако и оглушение"), org.examplee.leperClassPlugin.util.TextUtil.loreWarn("Очаг и заражение только с благословлением Денжер")));
            markVersion(meta);
            meta.getPersistentDataContainer().set(keys.plagueBombKey, org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
            it.setItemMeta(meta);
        }
        return it;
    }

    public org.bukkit.inventory.ItemStack makeVaccine() {
        org.bukkit.inventory.ItemStack it = makePotion(java.lang.String.valueOf(org.bukkit.ChatColor.AQUA) + "ВАКЦИНА", java.util.List.of(org.examplee.leperClassPlugin.util.TextUtil.loreHint("Полностью излечивает прокажение на ранних стадиях (1-2)")), org.bukkit.Color.fromRGB(150, 255, 255), keys.vaccineKey, "INSTANT_HEAL");
        return it;
    }

    public org.bukkit.inventory.ItemStack makeLeperBlood() {
        return makePotion(java.lang.String.valueOf(org.bukkit.ChatColor.DARK_RED) + "Кровь прокаженного", java.util.List.of(org.examplee.leperClassPlugin.util.TextUtil.loreWarn("Не пей эту дрянь (получишь прокажение)")), org.bukkit.Color.fromRGB(180, 0, 0), keys.leperBloodKey, "WATER");
    }

    public org.bukkit.inventory.ItemStack makeThickLeperBlood() {
        return makePotion(java.lang.String.valueOf(org.bukkit.ChatColor.GRAY) + "Густая кровь прокаженного", java.util.List.of(org.examplee.leperClassPlugin.util.TextUtil.loreHint("Кажется, это лучше не пить")), org.bukkit.Color.fromRGB(90, 90, 90), keys.thickBloodKey, "AWKWARD");
    }

    public org.bukkit.inventory.ItemStack makeSterileLeperBlood() {
        return makePotion(java.lang.String.valueOf(org.bukkit.ChatColor.GOLD) + "Стерильная кровь прокаженного", java.util.List.of(org.examplee.leperClassPlugin.util.TextUtil.loreHint("Бесполезна")), org.bukkit.Color.fromRGB(120, 70, 35), keys.sterileBloodKey, "AWKWARD");
    }

    public org.bukkit.inventory.ItemStack makeSacrificialKnife() {
        java.lang.String[] tmp1 = new java.lang.String[2];
        tmp1[0] = "IRON_SWORD";
        tmp1[1] = "STONE_SWORD";
        org.bukkit.inventory.ItemStack it = new org.bukkit.inventory.ItemStack(org.examplee.leperClassPlugin.util.Compat.materialFirst(tmp1));
        org.bukkit.inventory.meta.ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(org.examplee.leperClassPlugin.util.TextUtil.gRedGray("ЖЕРТВЕННЫЙ НОЖИК"));
            meta.setLore(java.util.List.of(org.examplee.leperClassPlugin.util.TextUtil.loreHint("ПКМ: добыть кровь (только для прокаженного)"), java.lang.String.valueOf(org.bukkit.ChatColor.DARK_GRAY) + "• КД: 1 час"));
            markVersion(meta);
            meta.getPersistentDataContainer().set(keys.sacrificialKnifeKey, org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
            it.setItemMeta(meta);
        }
        return it;
    }

    public org.bukkit.inventory.ItemStack makeUmbrellaTiny() {
        return makeUmbrella(0, 150);
    }

    public org.bukkit.inventory.ItemStack makeUmbrellaWeak() {
        return makeUmbrella(1, 600);
    }

    public org.bukkit.inventory.ItemStack makeUmbrellaNormal() {
        return makeUmbrella(2, 1500);
    }

    public org.bukkit.inventory.ItemStack makeUmbrellaStrong() {
        return makeUmbrella(3, 3000);
    }

    private org.bukkit.inventory.ItemStack makeUmbrella(int tier, int lifetimeSeconds) {
        org.bukkit.inventory.ItemStack it = new org.bukkit.inventory.ItemStack(org.bukkit.Material.STICK, 1);
        org.bukkit.inventory.meta.ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("Зонт");
            int remaining = lifetimeSeconds;
            java.lang.String[] tmp1 = new java.lang.String[4];
            tmp1[0] = java.lang.String.valueOf(org.bukkit.ChatColor.GRAY) + "Держи в левой руке";
            tmp1[1] = java.lang.String.valueOf(org.bukkit.ChatColor.GRAY) + "Защищает от солнца";
            tmp1[2] = java.lang.String.valueOf(org.bukkit.ChatColor.GRAY) + "Уровень: " + tier;
            tmp1[3] = java.lang.String.valueOf(org.bukkit.ChatColor.GRAY) + "Осталось: " + formatSeconds(remaining);
            meta.setLore(java.util.Arrays.asList(tmp1));
            org.bukkit.persistence.PersistentDataContainer pdc = meta.getPersistentDataContainer();
            markVersion(meta);
            pdc.set(keys.umbrellaKey, org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
            pdc.set(keys.umbrellaTierKey, org.bukkit.persistence.PersistentDataType.INTEGER, java.lang.Integer.valueOf(tier));
            pdc.set(keys.umbrellaLifetimeKey, org.bukkit.persistence.PersistentDataType.INTEGER, java.lang.Integer.valueOf(lifetimeSeconds));
            pdc.set(keys.umbrellaRemainingKey, org.bukkit.persistence.PersistentDataType.INTEGER, java.lang.Integer.valueOf(remaining));
            it.setItemMeta(meta);
        }
        return it;
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

    public org.bukkit.inventory.ItemStack bgPane() {
        java.lang.String[] tmp1 = new java.lang.String[4];
        tmp1[0] = "PURPLE_STAINED_GLASS_PANE";
        tmp1[1] = "BLACK_STAINED_GLASS_PANE";
        tmp1[2] = "GRAY_STAINED_GLASS_PANE";
        tmp1[3] = "GLASS_PANE";
        org.bukkit.inventory.ItemStack bg = new org.bukkit.inventory.ItemStack(org.examplee.leperClassPlugin.util.Compat.materialFirst(tmp1));
        org.bukkit.inventory.meta.ItemMeta m = bg.getItemMeta();
        if (m != null) {
            m.setDisplayName(java.lang.String.valueOf(org.bukkit.ChatColor.DARK_GRAY) + "•");
            bg.setItemMeta(m);
        }
        return bg;
    }

    public org.bukkit.inventory.ItemStack button(org.bukkit.Material mat, java.lang.String name, java.util.List lore) {
        org.bukkit.inventory.ItemStack it = new org.bukkit.inventory.ItemStack(mat);
        org.bukkit.inventory.meta.ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            it.setItemMeta(meta);
        }
        return it;
    }

    private org.bukkit.inventory.ItemStack makePotion(java.lang.String name, java.util.List lore, org.bukkit.Color color, org.bukkit.NamespacedKey key, java.lang.String baseTypeName) {
        org.bukkit.inventory.ItemStack it = new org.bukkit.inventory.ItemStack(org.bukkit.Material.POTION);
        org.bukkit.inventory.meta.PotionMeta meta = (org.bukkit.inventory.meta.PotionMeta) it.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            meta.setColor(color);
            setBasePotionCompat(meta, baseTypeName);
            try {
                org.bukkit.inventory.ItemFlag[] tmp1 = new org.bukkit.inventory.ItemFlag[1];
                tmp1[0] = org.bukkit.inventory.ItemFlag.valueOf("HIDE_POTION_EFFECTS");
                meta.addItemFlags(tmp1);
            }
            catch (java.lang.IllegalArgumentException ex) {
                markVersion(meta);
                meta.getPersistentDataContainer().set(key, org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
                it.setItemMeta(meta);
            }
        }
        return it;
    }

    private void markVersion(org.bukkit.inventory.meta.ItemMeta meta) {
        if (meta == null) {
            return;
        }
        meta.getPersistentDataContainer().set(keys.itemVersionKey, org.bukkit.persistence.PersistentDataType.INTEGER, java.lang.Integer.valueOf(2));
    }

    private void setBasePotionCompat(org.bukkit.inventory.meta.PotionMeta meta, java.lang.String potionTypeName) {
        if (meta == null) {
            return;
        }
        if (potionTypeName != null) {
            try {
                java.lang.Class potionTypeClass = java.lang.Class.forName("org.bukkit.potion.PotionType");
                java.lang.Object potionType = java.lang.Enum.valueOf(potionTypeClass.asSubclass(java.lang.Enum.class), potionTypeName);
                try {
                    java.lang.Class[] tmp1 = new java.lang.Class[1];
                    tmp1[0] = potionTypeClass;
                    java.lang.reflect.Method setBasePotionType = meta.getClass().getMethod("setBasePotionType", tmp1);
                    java.lang.Object[] tmp2 = new java.lang.Object[1];
                    tmp2[0] = potionType;
                    setBasePotionType.invoke(meta, tmp2);
                }
                catch (java.lang.Throwable ex) {
                }
            }
            catch (java.lang.Throwable ex) {
            }
            return;
        }
        return;
    }

}
