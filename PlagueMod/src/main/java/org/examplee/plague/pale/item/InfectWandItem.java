package org.examplee.plague.pale.item;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.examplee.plague.PlagueConfig;
import org.examplee.plague.pale.PaleEngine;
import org.examplee.plague.pale.PaleTick;

import java.util.List;

/** RMB: infect radius (default 16 uses). */
public class InfectWandItem extends Item {
    public InfectWandItem(Properties props) { super(props); }

    public static int getUses(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.contains("wand_uses")) return PlagueConfig.INSTANCE.pale.wandUses;
        return Math.max(0, tag.getInt("wand_uses"));
    }

    public static void setUses(ItemStack stack, int uses) {
        CompoundTag tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putInt("wand_uses", Math.max(0, uses));
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("item.plague.infect_wand.lore0", PlagueConfig.INSTANCE.pale.wandRadius));
        lines.add(Component.translatable("item.plague.infect_wand.uses", getUses(stack)));
    }

    @Override
    public boolean isFoil(ItemStack stack) { return true; }

    @Override
    public boolean isBarVisible(ItemStack stack) { return true; }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0f * getUses(stack) / (float) Math.max(1, PlagueConfig.INSTANCE.pale.wandUses));
    }

    @Override
    public int getBarColor(ItemStack stack) { return 0x39FF14; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel sl)) return InteractionResultHolder.sidedSuccess(stack, true);
        if (player.getCooldowns().isOnCooldown(stack)) return InteractionResultHolder.pass(stack);
        player.getCooldowns().addCooldown(this, (int) (PlagueConfig.INSTANCE.pale.wandCooldownMs / 50L));
        BlockPos center;
        HitResult hit = player.pick(40.0, 0.0f, false);
        if (hit instanceof BlockHitResult b && b.getType() == HitResult.Type.BLOCK) {
            center = b.getBlockPos();
        } else {
            center = player.blockPosition();
        }
        int infected = PaleEngine.get().infectAreaWand(sl, center);
        if (infected > 0) {
            PaleTick.addInfected(infected);
            sl.playSound(null, center, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.PLAYERS, 0.8f, 1.15f);
        } else {
            sl.playSound(null, center, SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 0.5f, 0.6f);
        }
        int left = getUses(stack) - 1;
        if (left <= 0) {
            if (!player.isCreative()) stack.shrink(1);
            sl.playSound(null, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0f, 1.0f);
        } else {
            setUses(stack, left);
        }
        return InteractionResultHolder.sidedSuccess(stack, false);
    }
}
