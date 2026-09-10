package org.examplee.plague;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.examplee.plague.command.PlagueCommands;
import org.examplee.plague.darkness.DarknessEngine;
import org.examplee.plague.entity.ModEntities;
import org.examplee.plague.leper.LeperAttachments;
import org.examplee.plague.leper.LeperEvents;
import org.examplee.plague.leper.LeperLogic;
import org.examplee.plague.network.ModNetworking;
import org.examplee.plague.pale.PaleTick;
import org.examplee.plague.registry.ModBlocks;
import org.examplee.plague.registry.ModItems;
import org.examplee.plague.registry.ModSounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PlagueMod: LeperClass + PalePlugin merged into one Fabric mod (MC 26.2, Mojmap).
 * Modules: leper (class/infection), pale (spreading forest), darkness (black blight).
 */
public class PlagueMod implements ModInitializer {
    public static final String MOD_ID = "plague";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        PlagueConfig.load();
        ModSounds.register();
        ModBlocks.register();
        ModItems.register();
        ModEntities.register();
        LeperAttachments.register();
        ModNetworking.register();
        LeperLogic.register();
        LeperEvents.register();
        PaleTick.register();
        DarknessEngine.register();
        PlagueCommands.register();
        LOGGER.info("[PlagueMod] initialized");
    }
}
