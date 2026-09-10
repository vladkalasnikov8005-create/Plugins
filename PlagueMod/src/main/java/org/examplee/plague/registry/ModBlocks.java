package org.examplee.plague.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.examplee.plague.PlagueMod;
import org.examplee.plague.darkness.DarkBlockItem;
import org.examplee.plague.darkness.DarknessBlock;
import org.examplee.plague.leper.QuarantineLanternBlock;
import org.examplee.plague.pale.GreatWardBlock;
import org.examplee.plague.pale.WardBlock;
import org.examplee.plague.pale.item.GreatWardItem;

public class ModBlocks {
    public static Block QUARANTINE_LANTERN;
    public static Block WARD_BLOCK;
    public static Block GREAT_WARD_BLOCK;
    public static Block DARKNESS_BLOCK;

    public static void register() {
        QUARANTINE_LANTERN = blockWithItem("quarantine_lantern", new QuarantineLanternBlock());
        WARD_BLOCK = blockWithItem("ward", new WardBlock());
        GREAT_WARD_BLOCK = Registry.register(BuiltInRegistries.BLOCK, PlagueMod.id("great_ward"), new GreatWardBlock());
        Registry.register(BuiltInRegistries.ITEM, PlagueMod.id("great_ward"), new GreatWardItem(GREAT_WARD_BLOCK, new Item.Properties()));
        DARKNESS_BLOCK = Registry.register(BuiltInRegistries.BLOCK, PlagueMod.id("darkness"), new DarknessBlock());
        Registry.register(BuiltInRegistries.ITEM, PlagueMod.id("darkness"), new DarkBlockItem(DARKNESS_BLOCK, new Item.Properties()));
    }

    private static Block blockWithItem(String path, Block block) {
        Registry.register(BuiltInRegistries.BLOCK, PlagueMod.id(path), block);
        Registry.register(BuiltInRegistries.ITEM, PlagueMod.id(path), new BlockItem(block, new Item.Properties()));
        return block;
    }
}
