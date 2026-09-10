package org.examplee.plague.pale.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import org.examplee.plague.PlagueConfig;
import org.examplee.plague.pale.PaleEngine;
import org.examplee.plague.pale.PaleTick;

import java.util.List;

/** RMB on infection: cleanse radius. Limited uses (default 2), shown as bar + lore. */
public class PurifierFlintItem extends Item {
    public PurifierFlintItem(Properties props) { super(props); }

    public static int getUses(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.contains("flint_uses")) return PlagueConfig.INSTANCE.pale.flintUses;
        return Math.max(0, tag.getInt("flint_uses"));
    }

    public static void setUses(ItemStack stack, int uses) {
        CompoundTag tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putInt("flint_uses", Math.max(0, uses));
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("item.plague.purifier_flint.lore0", PlagueConfig.INSTANCE.pale.flintRadius));
        lines.add(Component.translatable("item.plague.purifier_flint.uses", getUses(stack)));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) { return true; }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0f * getUses(stack) / (float) Math.max(1, PlagueConfig.INSTANCE.pale.flintUses));
    }

    @Override
    public int getBarColor(ItemStack stack) { return 0xFFD75F; }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (!(ctx.getLevel() instanceof ServerLevel sl)) return InteractionResult.sidedSuccess(true);
        Player player = ctx.getPlayer();
        if (player == null) return InteractionResult.PASS;
        ItemStack stack = ctx.getItemInHand();
        var pos = ctx.getClickedPos();
        if (!PaleEngine.isInfectedBlock(sl.getBlockState(pos)) && !PaleEngine.get().hasInfectedNear(sl, pos)) {
            return InteractionResult.PASS;
        }
        int cleaned = PaleEngine.get().cleanse(sl, pos, PlagueConfig.INSTANCE.pale.flintRadius);
        if (cleaned > 0) PaleTick.addCleansed(cleaned);
        int left = getUses(stack) - 1;
        if (left <= 0) {
            if (!player.isCreative()) stack.shrink(1);
            sl.playSound(null, pos, SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0f, 1.0f);
        } else {
            setUses(stack, left);
            sl.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.7f, 1.2f);
        }
        return InteractionResult.sidedSuccess(false);
    }
}
