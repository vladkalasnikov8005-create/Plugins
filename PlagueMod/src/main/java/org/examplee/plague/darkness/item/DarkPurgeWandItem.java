package org.examplee.plague.darkness.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.examplee.plague.darkness.DarknessEngine;

import java.util.List;

/**
 * Admin Wand of Light. RMB: radius x2 cycling 1..64. Sneak+RMB: purge darkness in radius.
 */
public class DarkPurgeWandItem extends Item {
    public DarkPurgeWandItem(Properties props) { super(props); }

    @Override
    public boolean isFoil(ItemStack stack) { return true; }

    public static int getRadius(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.contains("purge_radius")) return 8;
        return Math.max(1, Math.min(64, tag.getInt("purge_radius")));
    }

    public static void setRadius(ItemStack stack, int r) {
        CompoundTag tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putInt("purge_radius", Math.max(1, Math.min(64, r)));
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("item.plague.dark_purge_wand.lore0"));
        lines.add(Component.translatable("item.plague.dark_purge_wand.lore1"));
        lines.add(Component.translatable("item.plague.dark_purge_wand.radius", getRadius(stack)));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer sp)) return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        if (!sp.hasPermissions(2)) {
            sp.displayClientMessage(Component.translatable("msg.plague.no_perm").withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.pass(stack);
        }
        ServerLevel sl = sp.serverLevel();
        int r = getRadius(stack);
        if (sp.isShiftKeyDown()) {
            int ccx = sp.blockPosition().getX() >> 4, ccz = sp.blockPosition().getZ() >> 4;
            int cleaned = DarknessEngine.get().cleanseChunks(sl, ccx, ccz, r);
            if (cleaned > 0) {
                sp.displayClientMessage(Component.translatable("msg.plague.light_purge", cleaned, r).withStyle(ChatFormatting.GOLD), false);
                sl.playSound(null, sp.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f, 1.4f);
                sl.sendParticles(ParticleTypes.END_ROD, sp.getX(), sp.getY() + 1.0, sp.getZ(), 60, 3.0, 2.0, 3.0, 0.05);
            } else {
                sp.displayClientMessage(Component.translatable("msg.plague.light_empty", r).withStyle(ChatFormatting.GRAY), false);
            }
        } else {
            int next = r >= 64 ? 1 : Math.min(64, r * 2);
            setRadius(stack, next);
            sp.displayClientMessage(Component.translatable("item.plague.dark_purge_wand.radius", next).withStyle(ChatFormatting.GRAY), true);
            sl.playSound(null, sp.blockPosition(), SoundEvents.UI_BUTTON_CLICK, SoundSource.PLAYERS, 0.5f, 1.0f + next / 64.0f);
        }
        return InteractionResultHolder.sidedSuccess(stack, false);
    }
}
