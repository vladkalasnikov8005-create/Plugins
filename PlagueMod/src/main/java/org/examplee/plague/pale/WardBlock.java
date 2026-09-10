package org.examplee.plague.pale;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;

/** Ward: blocks pale spread in radius. Position stored in PaleWorldData. */
public class WardBlock extends Block {
    public WardBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
                .strength(4.0f).sound(SoundType.AMETHYST).lightLevel(s -> 5));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level instanceof ServerLevel sl) {
            PaleWorldData.get(sl).addWard(pos);
            sl.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0f, 1.2f);
            sl.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 18, 0.35, 0.6, 0.35, 0.01);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!moved && !newState.is(this) && level instanceof ServerLevel sl) {
            PaleWorldData.get(sl).removeWard(pos);
        }
        super.onRemove(state, level, pos, newState, moved);
    }
}
