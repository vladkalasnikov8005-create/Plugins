package org.examplee.plague.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.examplee.plague.leper.SneezeProjectile;
import org.examplee.plague.pale.PaleEngine;

import java.util.EnumSet;

/**
 * Plague doctor: melee + infectious sneeze volleys. Spawns at night in pale forest.
 * Never targets lepers (see MobTargetMixin).
 */
public class PlagueDoctorEntity extends Monster {
    public PlagueDoctorEntity(EntityType<? extends PlagueDoctorEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.3)
                .build();
    }

    public static boolean canSpawn(EntityType<? extends Monster> type, ServerLevelAccessor level, MobSpawnType spawnType,
                                  BlockPos pos, RandomSource random) {
        if (!Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random)) return false;
        if (level instanceof ServerLevel sl) {
            int stage = PaleEngine.get().getStage(sl, pos.getX() >> 4, pos.getZ() >> 4);
            return stage >= 1 || random.nextFloat() < 0.05f;
        }
        return random.nextFloat() < 0.05f;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SneezeGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.WITCH_AMBIENT; }
    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.EVOKER_HURT; }
    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.EVOKER_DEATH; }

    /** Ranged sneeze every 6s at targets within 12 blocks with line of sight. */
    static class SneezeGoal extends Goal {
        private final PlagueDoctorEntity mob;
        private int cooldown = 100;

        SneezeGoal(PlagueDoctorEntity mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (mob.getTarget() == null) return false;
            if (cooldown > 0) { cooldown--; return false; }
            return mob.distanceTo(mob.getTarget()) <= 12.0 && mob.getSensing().hasLineOfSight(mob.getTarget());
        }

        @Override
        public boolean canContinueToUse() { return false; }

        @Override
        public void start() {
            cooldown = 120;
            if (!(mob.level() instanceof ServerLevel sl)) return;
            var target = mob.getTarget();
            if (target == null) return;
            SneezeProjectile p = new SneezeProjectile(ModEntities.SNEEZE, mob, sl);
            p.setOwner(mob);
            p.setPos(mob.getEyePosition().x, mob.getEyePosition().y - 0.1, mob.getEyePosition().z);
            Vec3 dir = target.position().add(0, target.getBbHeight() * 0.6, 0).subtract(p.position()).normalize();
            p.setDeltaMovement(dir.scale(1.15));
            sl.addFreshEntity(p);
            mob.playSound(SoundEvents.SLIME_SQUISH, 1.0f, 0.8f);
        }
    }
}
