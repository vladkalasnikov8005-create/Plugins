package org.examplee.plague.pale.item;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.examplee.plague.PlagueConfig;
import org.examplee.plague.pale.PaleEngine;
import org.examplee.plague.pale.PaleTick;

import java.util.List;

/** RMB: cleanse pale in radius (clicked block or self). 1.5s cooldown, consumes 1. */
public class SaltItem extends Item {
    public SaltItem(Properties props) { super(props); }

    @Override
    public boolean isFoil(ItemStack stack) { return true; }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("item.plague.pale_salt.lore0", PlagueConfig.INSTANCE.pale.saltRadius));
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (!(ctx.getLevel() instanceof ServerLevel sl)) return InteractionResult.sidedSuccess(true);
        return doCleanse(sl, ctx.getPlayer(), ctx.getClickedPos(), ctx.getItemInHand());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel sl)) return InteractionResultHolder.sidedSuccess(stack, true);
        doCleanse(sl, player, player.blockPosition(), stack);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    private InteractionResult doCleanse(ServerLevel level, Player player, BlockPos center, ItemStack stack) {
        if (player == null) return InteractionResult.PASS;
        if (player.getCooldowns().isOnCooldown(stack)) return InteractionResult.PASS;
        player.getCooldowns().addCooldown(this, (int) (PlagueConfig.INSTANCE.pale.saltCooldownMs / 50L));
        int cleaned = PaleEngine.get().cleanse(level, center, PlagueConfig.INSTANCE.pale.saltRadius);
        if (cleaned > 0) PaleTick.addCleansed(cleaned);
        if (!player.isCreative()) stack.shrink(1);
        return InteractionResult.sidedSuccess(false);
    }
}
