package org.examplee.plague.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import org.examplee.plague.registry.ModBlocks;

/**
 * Gloom stalker: darkness predator. Empowered in the dark, burns in daylight.
 * Spawns on darkness blocks. Never targets lepers (see MobTargetMixin).
 */
public class GloomStalkerEntity extends Monster {
    public GloomStalkerEntity(EntityType<? extends GloomStalkerEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.32)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.2)
                .build();
    }

    public static boolean canSpawn(EntityType<? extends Monster> type, ServerLevelAccessor level, MobSpawnType spawnType,
                                  BlockPos pos, RandomSource random) {
        if (!Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random)) return false;
        return level.getBlockState(pos.below()).is(ModBlocks.DARKNESS_BLOCK)
                || level.getBlockState(pos).is(ModBlocks.DARKNESS_BLOCK);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, false));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.9));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 10.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide || tickCount % 20 != 0) return;
        // Burns in direct daylight.
        if (level().dimension() == Level.OVERWORLD) {
            long dayTime = level().getDayTime() % 24000L;
            if (dayTime >= 0 && dayTime <= 12300 && !level().isRaining()
                    && level().canSeeSky(blockPosition().above())
                    && level().getBrightness(LightLayer.SKY, blockPosition()) >= 14) {
                setRemainingFireTicks(Math.max(getRemainingFireTicks(), 80));
            }
        }
        // Empowered in darkness.
        if (level().getMaxLocalRawBrightness(blockPosition()) < 4) {
            addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 0, true, false, false));
            addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, 0, true, false, false));
        }
    }

    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.RAVAGER_AMBIENT; }
    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.RAVAGER_HURT; }
    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.RAVAGER_DEATH; }
}
