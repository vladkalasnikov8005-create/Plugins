package org.examplee.plague.leper;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.examplee.plague.PlagueConfig;
import org.jetbrains.annotations.Nullable;

/**
 * Quarantine lantern: weakens lepers in radius, freezes infection.
 * Fuel = glowstone dust (RMB). Positions stored in LanternData.
 */
public class QuarantineLanternBlock extends Block {
    public QuarantineLanternBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
                .strength(3.5f).sound(SoundType.LANTERN).lightLevel(s -> 10));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level instanceof ServerLevel sl) {
            LanternData.get(sl).place(pos, PlagueConfig.INSTANCE.leper.lanternStartFuelSec);
            sl.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.8f, 1.6f);
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 20, 0.3, 0.3, 0.3, 0.02);
            if (placer instanceof Player p) {
                p.displayClientMessage(Component.translatable("msg.plague.lantern_placed",
                        PlagueConfig.INSTANCE.leper.lanternStartFuelSec / 60).withStyle(ChatFormatting.GREEN), false);
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!moved && !newState.is(this) && level instanceof ServerLevel sl) {
            LanternData.get(sl).remove(pos);
        }
        super.onRemove(state, level, pos, newState, moved);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level instanceof ServerLevel sl)) return InteractionResult.sidedSuccess(true);
        LanternData data = LanternData.get(sl);
        if (!data.isLantern(pos)) return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hit.getHand());
        if (held.is(Items.GLOWSTONE_DUST)) {
            int fuel = data.refuel(pos);
            if (fuel >= 0) {
                if (!player.isCreative()) held.shrink(1);
                sl.playSound(null, pos, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 0.8f, 1.4f);
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 12, 0.25, 0.25, 0.25, 0.01);
                player.displayClientMessage(Component.translatable("msg.plague.lantern_fuel", fuel / 60, fuel % 60).withStyle(ChatFormatting.GREEN), true);
            }
            return InteractionResult.sidedSuccess(false);
        }
        int fuel = data.fuelAt(pos);
        if (fuel > 0) {
            player.displayClientMessage(Component.translatable("msg.plague.lantern_fuel", fuel / 60, fuel % 60).withStyle(ChatFormatting.YELLOW), true);
        } else {
            player.displayClientMessage(Component.translatable("msg.plague.lantern_empty").withStyle(ChatFormatting.RED), true);
        }
        return InteractionResult.sidedSuccess(false);
    }
}
