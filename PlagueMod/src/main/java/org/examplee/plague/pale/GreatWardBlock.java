package org.examplee.plague.pale;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.examplee.plague.PlagueConfig;
import org.examplee.plague.pale.item.PaleItems;
import org.examplee.plague.registry.ModBlocks;
import org.jetbrains.annotations.Nullable;

/** Great ward: bigger radius + cleanses 1 block per interval, spends charge (holy water). */
public class GreatWardBlock extends Block {
    public GreatWardBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN)
                .strength(5.0f).sound(SoundType.AMETHYST).lightLevel(s -> 12));
    }

    public static int itemCharge(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return Math.max(0, tag.getInt("ward_charge"));
    }

    public static ItemStack chargedStack(int charge) {
        ItemStack stack = new ItemStack(ModBlocks.GREAT_WARD_BLOCK);
        CompoundTag tag = new CompoundTag();
        tag.putInt("ward_charge", Math.max(0, charge));
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level instanceof ServerLevel sl) {
            int charge = itemCharge(stack);
            PaleWorldData.get(sl).addGreatWard(pos, charge);
            sl.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0f, 1.4f);
            sl.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 40, 0.6, 1.0, 0.6, 0.02);
            if (placer instanceof Player p) {
                p.displayClientMessage(Component.translatable("msg.plague.greatward_placed", charge / 60, charge % 60)
                        .withStyle(ChatFormatting.GREEN), false);
            }
        }
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level instanceof ServerLevel sl) {
            int charge = PaleWorldData.get(sl).removeGreatWard(pos);
            if (charge >= 0) popResource(sl, pos, chargedStack(charge));
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level instanceof ServerLevel sl)) return InteractionResult.sidedSuccess(true);
        PaleWorldData data = PaleWorldData.get(sl);
        Integer charge = data.greatWardCharge(pos);
        if (charge == null) return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hit.getHand());
        if (held.is(PaleItems.HOLY_WATER)) {
            int max = PlagueConfig.INSTANCE.pale.greatWardChargeMaxSec;
            if (charge >= max) {
                player.displayClientMessage(Component.translatable("msg.plague.greatward_full", max / 60).withStyle(ChatFormatting.YELLOW), true);
            } else {
                int next = data.addGreatCharge(pos, PlagueConfig.INSTANCE.pale.greatWardChargePerBottleSec, max);
                if (!player.isCreative()) held.shrink(1);
                sl.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0f, 1.5f);
                sl.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 25, 0.4, 0.6, 0.4, 0.02);
                player.displayClientMessage(Component.translatable("msg.plague.greatward_charged", next / 60, next % 60)
                        .withStyle(ChatFormatting.GREEN), true);
            }
            return InteractionResult.sidedSuccess(false);
        }
        if (held.isEmpty()) {
            if (charge > 0) {
                player.displayClientMessage(Component.translatable("msg.plague.greatward_charge", charge / 60, charge % 60)
                        .withStyle(ChatFormatting.GREEN), true);
            } else {
                player.displayClientMessage(Component.translatable("msg.plague.greatward_empty").withStyle(ChatFormatting.RED), true);
            }
        }
        return InteractionResult.sidedSuccess(false);
    }
}
