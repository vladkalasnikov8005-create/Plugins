package org.examplee.plague.pale.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.examplee.plague.PlagueConfig;
import org.examplee.plague.pale.PaleTick;

import java.util.List;

/** Admin wand: RMB starts mass purge around the player (queue processed over ticks). */
public class PurgeWandItem extends Item {
    public PurgeWandItem(Properties props) { super(props); }

    @Override
    public boolean isFoil(ItemStack stack) { return true; }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("item.plague.purge_wand.lore0"));
        lines.add(Component.translatable("item.plague.purge_wand.lore1", PlagueConfig.INSTANCE.pale.purgeRadiusChunks));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer sp)) return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        if (!sp.hasPermissions(2)) {
            sp.displayClientMessage(Component.translatable("msg.plague.no_perm").withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.pass(stack);
        }
        if (player.getCooldowns().isOnCooldown(stack)) return InteractionResultHolder.pass(stack);
        player.getCooldowns().addCooldown(this, 40);
        PaleTick.startPurge(sp);
        sp.displayClientMessage(Component.translatable("msg.plague.purge_start", PlagueConfig.INSTANCE.pale.purgeRadiusChunks)
                .withStyle(ChatFormatting.GREEN), false);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }
}
