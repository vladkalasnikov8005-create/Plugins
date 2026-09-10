package org.examplee.plague.pale.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.examplee.plague.PlagueConfig;
import org.examplee.plague.darkness.DarknessEngine;
import org.examplee.plague.entity.ModEntities;
import org.examplee.plague.pale.PaleEngine;
import org.examplee.plague.pale.PaleTick;

public class HolyWaterProjectile extends ThrowableItemProjectile {
    public HolyWaterProjectile(EntityType<? extends HolyWaterProjectile> type, Level level) {
        super(type, level);
    }

    public HolyWaterProjectile(LivingEntity owner, Level level) {
        super(ModEntities.HOLY_WATER, owner, level);
    }

    @Override
    protected ItemStack getDefaultItem() {
        return new ItemStack(PaleItems.HOLY_WATER);
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (!(level() instanceof ServerLevel sl)) return;
        BlockPos center = BlockPos.containing(hit.getLocation());
        int r = PlagueConfig.INSTANCE.pale.holyWaterRadius;
        int cleaned = PaleEngine.get().cleanse(sl, center, r);
        if (cleaned > 0) PaleTick.addCleansed(cleaned);
        int darkCleaned = DarknessEngine.get().cleanse(sl, center, r);
        if (darkCleaned > 0 && getOwner() instanceof ServerPlayer p) {
            p.displayClientMessage(Component.translatable("msg.plague.holywater_dark", darkCleaned)
                    .withStyle(ChatFormatting.GREEN), false);
        }
        sl.playSound(null, center, SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL, 0.8f, 1.2f);
        sl.sendParticles(ParticleTypes.END_ROD, center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5, 25, 0.5, 0.8, 0.5, 0.02);
        discard();
    }

    @Override
    protected Component getTypeName() {
        return Component.translatable("entity.plague.holy_water");
    }
}
