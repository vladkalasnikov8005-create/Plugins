package org.examplee.plague.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.levelgen.Heightmap;
import org.examplee.plague.PlagueMod;
import org.examplee.plague.leper.SneezeProjectile;
import org.examplee.plague.pale.item.HolyWaterProjectile;

public class ModEntities {
    public static EntityType<PlagueDoctorEntity> PLAGUE_DOCTOR;
    public static EntityType<GloomStalkerEntity> GLOOM_STALKER;
    public static EntityType<SneezeProjectile> SNEEZE;
    public static EntityType<HolyWaterProjectile> HOLY_WATER;

    public static void register() {
        PLAGUE_DOCTOR = Registry.register(BuiltInRegistries.ENTITY_TYPE, PlagueMod.id("plague_doctor"),
                EntityType.Builder.of(PlagueDoctorEntity::new, MobCategory.MONSTER).sized(0.6f, 1.95f).build("plague_doctor"));
        GLOOM_STALKER = Registry.register(BuiltInRegistries.ENTITY_TYPE, PlagueMod.id("gloom_stalker"),
                EntityType.Builder.of(GloomStalkerEntity::new, MobCategory.MONSTER).sized(0.8f, 1.4f).build("gloom_stalker"));
        SNEEZE = Registry.register(BuiltInRegistries.ENTITY_TYPE, PlagueMod.id("sneeze"),
                EntityType.Builder.<SneezeProjectile>of(SneezeProjectile::new, MobCategory.MISC).sized(0.25f, 0.25f).build("sneeze"));
        HOLY_WATER = Registry.register(BuiltInRegistries.ENTITY_TYPE, PlagueMod.id("holy_water"),
                EntityType.Builder.<HolyWaterProjectile>of(HolyWaterProjectile::new, MobCategory.MISC).sized(0.25f, 0.25f).build("holy_water"));

        FabricDefaultAttributeRegistry.register(PLAGUE_DOCTOR, PlagueDoctorEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(GLOOM_STALKER, GloomStalkerEntity.createAttributes());

        net.minecraft.world.entity.SpawnPlacements.register(PLAGUE_DOCTOR, SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, PlagueDoctorEntity::canSpawn);
        net.minecraft.world.entity.SpawnPlacements.register(GLOOM_STALKER, SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, GloomStalkerEntity::canSpawn);

        Registry.register(BuiltInRegistries.ITEM, PlagueMod.id("plague_doctor_egg"),
                new SpawnEggItem(PLAGUE_DOCTOR, 0x2b2b2b, 0x39ff14, new Item.Properties()));
        Registry.register(BuiltInRegistries.ITEM, PlagueMod.id("gloom_stalker_egg"),
                new SpawnEggItem(GLOOM_STALKER, 0x0a0a12, 0x6a5acd, new Item.Properties()));
    }
}
