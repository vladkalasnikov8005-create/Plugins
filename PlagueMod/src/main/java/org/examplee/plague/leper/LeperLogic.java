package org.examplee.plague.leper;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import org.examplee.plague.PlagueConfig;
import org.examplee.plague.leper.item.LeperItems;
import org.examplee.plague.leper.item.UmbrellaItem;
import org.examplee.plague.network.ModNetworking;
import org.examplee.plague.pale.PaleEngine;

import java.util.List;

/** Per-second tick for lepers + infected: sun, buffs, rage, inversion, golems. */
public class LeperLogic {
    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(LeperLogic::tickWorld);
    }

    private static void tickWorld(ServerLevel level) {
        List<ServerPlayer> players = level.players();
        if (players.isEmpty()) return;
        long time = level.getGameTime();
        boolean second = (time % 20) == 0;

        if (second) LanternData.get(level).tickSecond(level);

        MinecraftServer server = level.getServer();
        int leperCount = 0;
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (LeperData.get(p).leper()) leperCount++;
        }

        for (ServerPlayer p : players) {
            if (p.isCreative() || p.isSpectator()) continue;
            checkProgression(p);
            if (!second) continue;
            LeperData d = LeperData.get(p);
            boolean isLeper = d.leper();
            boolean isStage2 = d.stage() == 2;

            boolean paleHome = false;
            if (isLeper || isStage2) {
                paleHome = isNearPale(p, PlagueConfig.INSTANCE.leper.paleHomeVerticalRange)
                        || (PlagueConfig.INSTANCE.leper.paleHomeEnabled
                        && PaleEngine.get().getStage(level, p.blockPosition().getX() >> 4, p.blockPosition().getZ() >> 4)
                        >= PlagueConfig.INSTANCE.leper.paleHomeMinStage);
            }

            if (isLeper && !p.hasEffect(MobEffects.WATER_BREATHING)) {
                p.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 300, 0, false, false, false));
            }
            if ((isLeper || isStage2) && p.hasEffect(MobEffects.FIRE_RESISTANCE)) {
                p.removeEffect(MobEffects.FIRE_RESISTANCE);
            }
            if (isLeper && p.hasEffect(MobEffects.HUNGER)) {
                p.removeEffect(MobEffects.HUNGER);
            }

            if ((isLeper || isStage2) && shouldBurnInSun(p)) {
                ItemStack off = p.getOffhandItem();
                boolean umbrella = LeperItems.isUmbrella(off);
                if (!paleHome && !umbrella) {
                    p.setRemainingFireTicks(Math.max(p.getRemainingFireTicks(), 60));
                } else {
                    if (p.getRemainingFireTicks() > 0) p.setRemainingFireTicks(0);
                    if (umbrella) damageUmbrella(p, off);
                }
            }

            if (isLeper) {
                boolean lantern = LanternData.get(level).isInRange(p.blockPosition());
                if (paleHome && !lantern) {
                    p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80,
                            PlagueConfig.INSTANCE.leper.paleHomeRegenAmplifier, true, false, true));
                }
                if (lantern) {
                    p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0, true, false, true));
                }
                applyPopulationBuffs(p, leperCount);

                long now = System.currentTimeMillis();
                if (p.getFoodData().getFoodLevel() <= 4 && d.rageUntil() < now) {
                    LeperData.set(p, d.withRageUntil(now + 180000L));
                    p.displayClientMessage(Component.translatable("msg.plague.rage").withStyle(ChatFormatting.RED), true);
                    d = LeperData.get(p);
                }
                if (p.getFoodData().getFoodLevel() > 4 && d.rageUntil() > 0) {
                    LeperData.set(p, d.withRageUntil(0L));
                    d = LeperData.get(p);
                }
                if (d.rageUntil() > now) {
                    p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 80, 1, false, false, true));
                    p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 80, 1, false, false, true));
                }
                if (p.getFoodData().getFoodLevel() <= 0) {
                    p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0, false, false, true));
                    p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 2, false, false, true));
                }

                // Potion inversion on rising edges (poison/regen are detectable; harm/heal via damage hook).
                boolean hasPoison = p.hasEffect(MobEffects.POISON);
                boolean hasRegen = p.hasEffect(MobEffects.REGENERATION);
                if (hasPoison && !d.hadPoison()) {
                    p.removeEffect(MobEffects.POISON);
                    p.heal((float) PlagueConfig.INSTANCE.leper.healFromPoison);
                }
                if (hasRegen && !d.hadRegen() && !paleHome) {
                    p.removeEffect(MobEffects.REGENERATION);
                    p.hurtServer(level, level.damageSources().magic(), (float) PlagueConfig.INSTANCE.leper.damageFromHeal);
                    p.displayClientMessage(Component.translatable("msg.plague.heal_pain").withStyle(ChatFormatting.RED), true);
                }
                if (hasPoison != d.hadPoison() || hasRegen != d.hadRegen()) {
                    LeperData.set(p, LeperData.get(p).withEdges(hasPoison, hasRegen));
                }

                // Golems aggro lepers.
                AABB box = p.getBoundingBox().inflate(20.0, 12.0, 20.0);
                for (Mob mob : level.getEntitiesOfClass(Mob.class, box, m -> m instanceof IronGolem || m instanceof SnowGolem)) {
                    if (mob.getTarget() == null || !mob.getTarget().equals(p)) mob.setTarget(p);
                }
            }

            LeperEvents.tickPendingScare(p);
            ModNetworking.sendInfection(p);
            ModNetworking.sendZone(p);
        }
    }

    private static void applyPopulationBuffs(ServerPlayer p, int lepers) {
        int regen = -1, speed = -1, str = -1;
        if (lepers >= 7) { regen = 1; speed = 2; str = 2; }
        else if (lepers >= 5) { regen = 0; speed = 1; str = 0; }
        else if (lepers >= 3) { regen = 0; speed = 0; }
        if (regen >= 0) p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, regen, false, false, true));
        if (speed >= 0) p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 80, speed, false, false, true));
        if (str >= 0) p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 80, str, false, false, true));
    }

    private static void damageUmbrella(ServerPlayer p, ItemStack off) {
        int remaining = UmbrellaItem.getRemaining(off) - 1;
        if (remaining <= 0) {
            p.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, ItemStack.EMPTY);
            p.playNotifySound(SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0f, 1.0f);
        } else {
            UmbrellaItem.setRemaining(off, remaining);
        }
    }

    /** Full movement stun via extreme slowness (safe, no ability hacking). */
    public static void stun(ServerPlayer p, int ticks) {
        p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks, 10, false, false, false));
    }

    public static boolean shouldBurnInSun(ServerPlayer p) {
        ServerLevel level = p.serverLevel();
        if (level.dimension() != Level.OVERWORLD) return false;
        if (level.isRaining() || level.isThundering()) return false;
        long dayTime = level.getDayTime() % 24000L;
        if (dayTime < 0 || dayTime > 12300) return false;
        BlockPos pos = p.blockPosition();
        if (!level.canSeeSky(pos.above())) return false;
        return level.getBrightness(LightLayer.SKY, pos) >= 14;
    }

    public static boolean isNearPale(ServerPlayer p, int verticalRange) {
        ServerLevel level = p.serverLevel();
        BlockPos base = p.blockPosition();
        int minY = Math.max(level.getMinY(), base.getY() - verticalRange);
        int maxY = Math.min(level.getMaxY() - 1, base.getY() + verticalRange);
        for (int y = minY; y <= maxY; y++) {
            var key = BuiltInRegistries.BLOCK.getKey(level.getBlockState(new BlockPos(base.getX(), y, base.getZ())).getBlock());
            if (key.getPath().contains("pale")) return true;
        }
        return false;
    }

    // ---- infection API ----

    public static void addHit(ServerPlayer target) {
        LeperData d = LeperData.get(target);
        if (d.leper()) return;
        int hits = Math.min(3, d.hits() + 1);
        LeperData.set(target, d.withHits(hits));
        if (hits >= 3) startInfection(target);
        ModNetworking.sendInfection(target);
    }

    public static void startInfection(ServerPlayer p) {
        LeperData d = LeperData.get(p);
        if (d.stage() > 0 || d.leper()) return;
        LeperData.set(p, d.withStage(1).withNextPhaseMs(
                System.currentTimeMillis() + PlagueConfig.INSTANCE.leper.infectionPhaseMinutes * 60000L));
        p.displayClientMessage(Component.translatable("msg.plague.infected").withStyle(ChatFormatting.DARK_GREEN), false);
        p.playNotifySound(SoundEvents.ZOMBIE_AMBIENT, SoundSource.PLAYERS, 1.0f, 0.5f);
        ModNetworking.sendInfection(p);
    }

    public static void checkProgression(ServerPlayer p) {
        LeperData d = LeperData.get(p);
        if (d.leper() || d.stage() == 0) return;
        long now = System.currentTimeMillis();
        if (LanternData.get(p.serverLevel()).isInRange(p.blockPosition())) {
            LeperData.set(p, d.withNextPhaseMs(d.nextPhaseMs() + 1000L));
            return;
        }
        if (now >= d.nextPhaseMs()) {
            if (d.stage() == 1) {
                LeperData.set(p, d.withStage(2).withNextPhaseMs(now + PlagueConfig.INSTANCE.leper.infectionPhaseMinutes * 60000L));
                p.displayClientMessage(Component.translatable("msg.plague.stage2").withStyle(ChatFormatting.RED), false);
            } else if (d.stage() == 2) {
                setLeper(p, true);
            }
        }
        ModNetworking.sendInfection(p);
    }

    public static void setLeper(ServerPlayer p, boolean val) {
        LeperData d = LeperData.get(p);
        if (val) {
            LeperData.set(p, d.cured().withLeper(true));
            p.removeEffect(MobEffects.FIRE_RESISTANCE);
            AABB box = p.getBoundingBox().inflate(32.0);
            for (Mob mob : p.serverLevel().getEntitiesOfClass(Mob.class, box)) {
                if (p.equals(mob.getTarget())) mob.setTarget(null);
            }
            p.displayClientMessage(Component.translatable("msg.plague.converted").withStyle(ChatFormatting.DARK_RED), false);
            ModNetworking.sendCinematic(p);
        } else {
            LeperData.set(p, d.cured().withLeper(false));
        }
        ModNetworking.sendInfection(p);
    }

    public static void cure(ServerPlayer p) {
        LeperData.set(p, LeperData.get(p).cured());
        p.displayClientMessage(Component.translatable("msg.plague.cured").withStyle(ChatFormatting.AQUA), false);
        ModNetworking.sendInfection(p);
    }
}
