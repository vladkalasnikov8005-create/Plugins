package org.examplee.plague.darkness;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;

import java.util.List;

/** Darkness block item, soulbound to the owner UUID. Strangers cannot place it. */
public class DarkBlockItem extends BlockItem {
    public DarkBlockItem(Block block, Properties props) { super(block, props); }

    @Override
    public boolean isFoil(ItemStack stack) { return true; }

    public static String ownerOf(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.contains("dark_owner") ? tag.getString("dark_owner") : "";
    }

    public static void setOwner(ItemStack stack, String uuid) {
        CompoundTag tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putString("dark_owner", uuid);
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        lines.add(Component.translatable("item.plague.dark_block.lore0").withStyle(ChatFormatting.DARK_GRAY));
        lines.add(Component.translatable("item.plague.dark_block.lore1").withStyle(ChatFormatting.DARK_GRAY));
        String owner = ownerOf(stack);
        lines.add(Component.translatable("item.plague.dark_block.owner",
                owner.isEmpty() ? "-" : owner.substring(0, 8)).withStyle(ChatFormatting.GRAY));
    }

    @Override
    protected boolean place(BlockPlaceContext ctx, net.minecraft.world.level.block.state.BlockState state) {
        String owner = ownerOf(ctx.getItemInHand());
        if (ctx.getPlayer() instanceof ServerPlayer sp && !owner.isEmpty()
                && !owner.equals(sp.getUUID().toString())) {
            sp.displayClientMessage(Component.translatable("msg.plague.dark_reject").withStyle(ChatFormatting.DARK_GRAY), true);
            sp.playNotifySound(SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.PLAYERS, 0.5f, 1.6f);
            return false;
        }
        return super.place(ctx, state);
    }

    @Override
    public InteractionResult place(BlockPlaceContext ctx) {
        // Re-check here too: place(BlockPlaceContext, BlockState) covers normal flow,
        // this guards context variants that skip it.
        String owner = ownerOf(ctx.getItemInHand());
        if (ctx.getPlayer() instanceof ServerPlayer sp && !owner.isEmpty()
                && !owner.equals(sp.getUUID().toString())) {
            sp.displayClientMessage(Component.translatable("msg.plague.dark_reject").withStyle(ChatFormatting.DARK_GRAY), true);
            return InteractionResult.FAIL;
        }
        return super.place(ctx);
    }
}
