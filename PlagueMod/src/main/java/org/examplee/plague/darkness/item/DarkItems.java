package org.examplee.plague.darkness.item;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import org.examplee.plague.PlagueMod;

public class DarkItems {
    public static Item DARK_MAP;
    public static Item DARK_PURGE_WAND;

    public static void register() {
        DARK_MAP = Registry.register(BuiltInRegistries.ITEM, PlagueMod.id("dark_map"),
                new DarkMapItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
        DARK_PURGE_WAND = Registry.register(BuiltInRegistries.ITEM, PlagueMod.id("dark_purge_wand"),
                new DarkPurgeWandItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    }
}
