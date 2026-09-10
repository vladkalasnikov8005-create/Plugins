package org.examplee.plague.leper;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.examplee.plague.PlagueConfig;
import org.examplee.plague.entity.ModEntities;
import org.examplee.plague.leper.item.LeperItems;

/** Snot glob: poison + slow + weakness + blindness on hit. Range-limited (~24 ticks). */
public class SneezeProjectile extends ThrowableItemProjectile {
    public SneezeProjectile(EntityType<? extends SneezeProjectile> type, Level level) {
        super(type, level);
    }

    public SneezeProjectile(EntityType<? extends SneezeProjectile> type, LivingEntity owner, Level level) {
        super(type, owner, level);
    }

    @Override
    protected ItemStack getDefaultItem() {
        return new ItemStack(LeperItems.SNEEZE_GLOB);
    }

    public static void spawn(ServerPlayer source) {
        ServerLevel level = source.serverLevel();
        SneezeProjectile p = new SneezeProjectile(ModEntities.SNEEZE, source, level);
        p.setOwner(source);
        p.setPos(source.getEyePosition().x, source.getEyePosition().y - 0.1, source.getEyePosition().z);
        p.setDeltaMovement(source.getLookAngle().scale(PlagueConfig.INSTANCE.leper.sneezeVelocity));
        level.addFreshEntity(p);
        level.playSound(null, source.blockPosition(), SoundEvents.SLIME_SQUISH, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && tickCount > 24) discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        if (level().isClientSide) return;
        if (hit.getEntity() instanceof LivingEntity le) {
            var c = PlagueConfig.INSTANCE.leper;
            le.addEffect(new MobEffectInstance(MobEffects.POISON, (int) (c.sneezePoisonSeconds * 20), 0));
            le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, (int) (c.sneezeSlowSeconds * 20), 2));
            le.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, (int) (c.sneezeWeakSeconds * 20), 1));
            le.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, (int) (c.sneezeBlindSeconds * 20), 0));
        }
        discard();
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (!level().isClientSide) discard();
    }

    @Override
    protected Component getTypeName() {
        return Component.translatable("entity.plague.sneeze");
    }
}
