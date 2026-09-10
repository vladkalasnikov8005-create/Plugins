package org.examplee.plague.pale.item;

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
import net.minecraft.world.level.Level;
import org.examplee.plague.PlagueConfig;

import java.util.List;

/** Throw: cleanses pale AND darkness in radius on impact. */
public class HolyWaterItem extends Item {
    public HolyWaterItem(Properties props) { super(props); }

    @Override
    public boolean isFoil(ItemStack stack) { return true; }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("item.plague.holy_water.lore0", PlagueConfig.INSTANCE.pale.holyWaterRadius));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.sidedSuccess(stack, true);
        HolyWaterProjectile thrown = new HolyWaterProjectile(player, level);
        thrown.setPos(player.getEyePosition().x, player.getEyePosition().y - 0.1, player.getEyePosition().z);
        thrown.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, 1.2f, 1.0f);
        level.addFreshEntity(thrown);
        level.playSound(null, player.blockPosition(), SoundEvents.SPLASH_POTION_THROW, SoundSource.PLAYERS, 0.8f, 1.2f);
        if (!player.isCreative()) stack.shrink(1);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }
}
