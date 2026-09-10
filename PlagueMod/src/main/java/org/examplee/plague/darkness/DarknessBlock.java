package org.examplee.plague.darkness;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;

/**
 * A REAL new block (not recolored concrete): the heart of the black blight.
 * No drops when broken; placement is owner-gated in {@link DarkBlockItem}.
 */
public class DarknessBlock extends Block {
    public DarknessBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK)
                .strength(3.0f, 6.0f).sound(SoundType.MUD));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level instanceof ServerLevel sl) {
            DarknessWorldData.get(sl).register(pos);
            sl.playSound(null, pos, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.BLOCKS, 1.0f, 0.5f);
            sl.sendParticles(ParticleTypes.SCULK_SOUL, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 15, 0.4, 0.4, 0.4, 0.05);
            if (placer instanceof Player p) {
                p.displayClientMessage(Component.translatable("msg.plague.dark_awake").withStyle(ChatFormatting.DARK_GRAY), false);
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!moved && !newState.is(this) && level instanceof ServerLevel sl) {
            DarknessWorldData.get(sl).unregister(pos);
        }
        super.onRemove(state, level, pos, newState, moved);
    }
}
