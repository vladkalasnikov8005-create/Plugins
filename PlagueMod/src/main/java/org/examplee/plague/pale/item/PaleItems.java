package org.examplee.plague.pale.item;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import org.examplee.plague.PlagueMod;

public class PaleItems {
    public static Item SALT;
    public static Item HOLY_WATER;
    public static Item PURIFIER_FLINT;
    public static Item PALE_MAP;
    public static Item INFECT_WAND;
    public static Item PURGE_WAND;

    public static void register() {
        SALT = Registry.register(BuiltInRegistries.ITEM, PlagueMod.id("pale_salt"),
                new SaltItem(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)));
        HOLY_WATER = Registry.register(BuiltInRegistries.ITEM, PlagueMod.id("holy_water"),
                new HolyWaterItem(new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));
        PURIFIER_FLINT = Registry.register(BuiltInRegistries.ITEM, PlagueMod.id("purifier_flint"),
                new PurifierFlintItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
        PALE_MAP = Registry.register(BuiltInRegistries.ITEM, PlagueMod.id("pale_map"),
                new PaleMapItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
        INFECT_WAND = Registry.register(BuiltInRegistries.ITEM, PlagueMod.id("infect_wand"),
                new InfectWandItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
        PURGE_WAND = Registry.register(BuiltInRegistries.ITEM, PlagueMod.id("purge_wand"),
                new PurgeWandItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    }
}
