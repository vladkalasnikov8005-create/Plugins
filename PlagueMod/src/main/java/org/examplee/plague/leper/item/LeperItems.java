package org.examplee.plague.leper.item;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import org.examplee.plague.PlagueMod;

public class LeperItems {
    public static Item PLAGUE_STICK;
    public static Item PLAGUE_BOMB;
    public static Item VACCINE;
    public static Item LEPER_BLOOD;
    public static Item THICK_BLOOD;
    public static Item STERILE_BLOOD;
    public static Item SACRIFICIAL_KNIFE;
    public static Item UMBRELLA_TINY;
    public static Item UMBRELLA_WEAK;
    public static Item UMBRELLA_NORMAL;
    public static Item UMBRELLA_STRONG;
    public static Item SNEEZE_GLOB;

    public static void register() {
        PLAGUE_STICK = Registry.register(BuiltInRegistries.ITEM, PlagueMod.id("plague_stick"),
                new PlagueStickItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
        PLAGUE_BOMB = Registry.register(BuiltInRegistries.ITEM, PlagueMod.id("plague_cloud"),
                new PlagueBombItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));
        VACCINE = Registry.register(BuiltInRegistries.ITEM, PlagueMod.id("vaccine"),
                new VaccineItem(new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));
        LEPER_BLOOD = Registry.register(BuiltInRegistries.ITEM, PlagueMod.id("leper_blood"),
                new BloodItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON), 0));
        THICK_BLOOD = Registry.register(BuiltInRegistries.ITEM, PlagueMod.id("thick_blood"),
                new BloodItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON), 1));
        STERILE_BLOOD = Registry.register(BuiltInRegistries.ITEM, PlagueMod.id("sterile_blood"),
                new BloodItem(new Item.Properties().stacksTo(16).rarity(Rarity.COMMON), 2));
        SACRIFICIAL_KNIFE = Registry.register(BuiltInRegistries.ITEM, PlagueMod.id("sacrificial_knife"),
                new SacrificialKnifeItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
        UMBRELLA_TINY = Registry.register(BuiltInRegistries.ITEM, PlagueMod.id("umbrella_tiny"),
                new UmbrellaItem(new Item.Properties().stacksTo(1).rarity(Rarity.COMMON), 0, 150));
        UMBRELLA_WEAK = Registry.register(BuiltInRegistries.ITEM, PlagueMod.id("umbrella_weak"),
                new UmbrellaItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON), 1, 600));
        UMBRELLA_NORMAL = Registry.register(BuiltInRegistries.ITEM, PlagueMod.id("umbrella_normal"),
                new UmbrellaItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 2, 1500));
        UMBRELLA_STRONG = Registry.register(BuiltInRegistries.ITEM, PlagueMod.id("umbrella_strong"),
                new UmbrellaItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), 3, 3000));
        SNEEZE_GLOB = Registry.register(BuiltInRegistries.ITEM, PlagueMod.id("sneeze_glob"),
                new Item(new Item.Properties().stacksTo(16)));
    }

    public static boolean isUmbrella(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof UmbrellaItem;
    }
}
