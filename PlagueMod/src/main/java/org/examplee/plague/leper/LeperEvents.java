package org.examplee.plague.leper;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.TypedActionResult;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.examplee.plague.PlagueConfig;
import org.examplee.plague.leper.item.LeperItems;
import org.examplee.plague.network.ModNetworking;
import org.examplee.plague.pale.PaleEngine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Damage scaling, contact infection, food gate, death handling, brewing chain. */
public class LeperEvents {
    private static final Set<Item> LEPER_FOOD = new HashSet<>();
    private static final Set<UUID> APPLYING = new HashSet<>();
    private record PendingScare(boolean real, long resolveAt) {}
    private static final Map<UUID, PendingScare> PENDING = new HashMap<>();

    static {
        Item[] foods = {Items.BEEF, Items.COOKED_BEEF, Items.PORKCHOP, Items.COOKED_PORKCHOP,
                Items.CHICKEN, Items.COOKED_CHICKEN, Items.MUTTON, Items.COOKED_MUTTON,
                Items.RABBIT, Items.COOKED_RABBIT, Items.ROTTEN_FLESH,
                Items.COD, Items.COOKED_COD, Items.SALMON, Items.COOKED_SALMON,
                Items.TROPICAL_FISH, Items.PUFFERFISH,
                Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE, Items.GOLDEN_CARROT};
        LEPER_FOOD.addAll(java.util.Arrays.asList(foods));
    }

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(LeperEvents::onDamage);
        AttackEntityCallback.EVENT.register(LeperEvents::onAttack);
        UseItemCallback.EVENT.register(LeperEvents::onUseItem);
        ServerLivingEntityEvents.AFTER_DEATH.register(LeperEvents::onDeath);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer p = handler.player;
            if (LeperData.get(p).leper()) {
                p.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 300, 0, false, false, false));
            }
            ModNetworking.sendInfection(p);
            ModNetworking.sendZone(p);
        });
        ServerPlayerEvents.AFTER_RESPAWN.register((oldP, newP, alive) -> {
            LeperData d = oldP.getAttached(LeperAttachments.LEPER);
            if (d != null) LeperData.set(newP, d);
        });
        FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> {
            builder.registerItemRecipe(new ItemStack(LeperItems.LEPER_BLOOD), Items.NETHER_WART, new ItemStack(LeperItems.THICK_BLOOD));
            builder.registerItemRecipe(new ItemStack(LeperItems.THICK_BLOOD), Items.BLAZE_POWDER, new ItemStack(LeperItems.STERILE_BLOOD));
            builder.registerItemRecipe(new ItemStack(LeperItems.STERILE_BLOOD), Items.GLISTERING_MELON_SLICE, new ItemStack(LeperItems.VACCINE));
        });
    }

    private static boolean onDamage(LivingEntity victim, net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (!(victim.level() instanceof ServerLevel level)) return true;
        if (APPLYING.contains(victim.getUUID())) return true;

        // Rage-boosted leper attacks: x1.8 + knock-up.
        if (source.getEntity() instanceof ServerPlayer attacker) {
            LeperData ad = LeperData.get(attacker);
            if (ad.leper() && ad.rageUntil() > System.currentTimeMillis()) {
                APPLYING.add(victim.getUUID());
                boolean ok = victim.hurtServer(level, source, amount * 1.8f);
                APPLYING.remove(victim.getUUID());
                if (ok && victim instanceof Player hit) {
                    Vec3 dir = hit.position().subtract(attacker.position()).normalize();
                    hit.push(dir.x * 1.9, 0.6, dir.z * 1.9);
                }
                return false;
            }
            // Contact infection: non-leper hits leper.
            if (victim instanceof ServerPlayer leperVictim && LeperData.get(leperVictim).leper() && !ad.leper()) {
                double chance = Math.max(0.0, amount) * PlagueConfig.INSTANCE.leper.contactInfectPerHp;
                boolean real = Math.random() < chance;
                boolean scare = real || Math.random() < PlagueConfig.INSTANCE.leper.contactFakeScareChance;
                if (scare && !PENDING.containsKey(attacker.getUUID())) {
                    attacker.displayClientMessage(Component.translatable("msg.plague.contact_scare").withStyle(ChatFormatting.DARK_GREEN), false);
                    PENDING.put(attacker.getUUID(), new PendingScare(real,
                            System.currentTimeMillis() + PlagueConfig.INSTANCE.leper.contactResolveMinutes * 60000L));
                }
            }
        }

        if (victim instanceof ServerPlayer sp) {
            LeperData d = LeperData.get(sp);
            // Plague doctor bites infect (50%).
            if (source.getEntity() instanceof org.examplee.plague.entity.PlagueDoctorEntity
                    && !d.leper() && !d.blessed() && sp.getRandom().nextFloat() < 0.5f) {
                LeperLogic.addHit(sp);
                return true;
            }
            if (!d.leper()) return true;
            APPLYING.add(sp.getUUID());
            boolean result;
            if (source.is(DamageTypes.MAGIC) || source.is(DamageTypes.INDIRECT_MAGIC)) {
                // Harm heals lepers (potion inversion; poison/heal handled in tick).
                sp.heal((float) PlagueConfig.INSTANCE.leper.healFromHarm);
                sp.removeEffect(MobEffects.POISON);
                result = false;
            } else if (source.is(DamageTypeTags.IS_FIRE)) {
                result = sp.hurtServer(level, source, amount * 4.0f);
            } else {
                result = sp.hurtServer(level, source, amount * 0.05f);
            }
            APPLYING.remove(sp.getUUID());
            return false;
        }
        return true;
    }

    private static InteractionResult onAttack(Player player, Level level, InteractionHand hand, net.minecraft.world.entity.Entity target, net.minecraft.world.phys.EntityHitResult hit) {
        if (level.isClientSide || !(player instanceof ServerPlayer attacker)) return InteractionResult.PASS;
        ItemStack held = attacker.getItemInHand(hand);
        if (held.is(LeperItems.PLAGUE_STICK) && target instanceof LivingEntity le) {
            le.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
            if (level instanceof ServerLevel sl) {
                sl.sendParticles(net.minecraft.core.particles.DustParticleOptions.fromColor(0x39FF14, 1.6f),
                        target.getX(), target.getY() + 1.0, target.getZ(), 20, 0.4, 0.6, 0.4, 0.0);
            }
            if (target instanceof ServerPlayer tp) {
                LeperLogic.stun(tp, 40);
                tp.displayClientMessage(Component.translatable("msg.plague.stunned").withStyle(ChatFormatting.DARK_GREEN), true);
                if (LeperData.get(attacker).blessed() && !LeperData.get(tp).leper()) LeperLogic.addHit(tp);
            }
        }
        return InteractionResult.PASS;
    }

    private static TypedActionResult<ItemStack> onUseItem(Player player, Level level, InteractionHand hand) {
        if (level.isClientSide || !(player instanceof ServerPlayer sp)) return TypedActionResult.pass(player.getItemInHand(hand));
        ItemStack stack = sp.getItemInHand(hand);
        if (LeperData.get(sp).leper() && stack.isEdible() && !LEPER_FOOD.contains(stack.getItem())) {
            sp.displayClientMessage(Component.translatable("msg.plague.meat_only").withStyle(ChatFormatting.RED), true);
            return TypedActionResult.fail(stack);
        }
        return TypedActionResult.pass(stack);
    }

    private static void onDeath(LivingEntity victim, net.minecraft.world.damagesource.DamageSource source) {
        if (!(victim instanceof ServerPlayer dead)) return;
        if (!(victim.level() instanceof ServerLevel level)) return;
        if (!LeperData.get(dead).leper()) return;
        if (PlagueConfig.INSTANCE.leper.deathInfectEnabled) {
            int infected = PaleEngine.get().infectArea(level, dead.blockPosition(),
                    PlagueConfig.INSTANCE.leper.deathInfectRadius, PlagueConfig.INSTANCE.leper.deathInfectMaxBlocks);
            if (infected > 0) {
                level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, dead.getX(), dead.getY() + 1.0, dead.getZ(), 60, 2.0, 1.0, 2.0, 0.02);
                level.playSound(null, BlockPos.containing(dead.position()), SoundEvents.ZOMBIE_DEATH, SoundSource.PLAYERS, 0.8f, 0.6f);
            }
        }
        if (Math.random() < 0.6) dead.spawnAtLocation(new ItemStack(LeperItems.LEPER_BLOOD));
        if (source.getEntity() instanceof ServerPlayer killer && !LeperData.get(killer).leper() && Math.random() < 0.3) {
            LeperLogic.addHit(killer);
            killer.displayClientMessage(Component.translatable("msg.plague.blood_wounds").withStyle(ChatFormatting.YELLOW), false);
        }
    }

    /** Resolve delayed contact-infection scares. Called from the per-second tick. */
    static void tickPendingScare(ServerPlayer p) {
        PendingScare ps = PENDING.get(p.getUUID());
        if (ps == null) return;
        if (System.currentTimeMillis() >= ps.resolveAt()) {
            PENDING.remove(p.getUUID());
            if (LeperData.get(p).leper()) return;
            if (ps.real()) {
                LeperLogic.startInfection(p);
            } else {
                p.displayClientMessage(Component.translatable("msg.plague.scare_over").withStyle(ChatFormatting.GREEN), false);
            }
        }
    }
}
