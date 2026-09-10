package org.examplee.plague.leper.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.examplee.plague.PlagueConfig;
import org.examplee.plague.leper.LeperData;

import java.util.List;

/** RMB (leper only): 1 heart damage -> +1 leper blood, heavy debuffs, 60 min cooldown. */
public class SacrificialKnifeItem extends Item {
    public SacrificialKnifeItem(Properties props) { super(props); }

    @Override
    public boolean isFoil(ItemStack stack) { return true; }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("item.plague.sacrificial_knife.lore0"));
        lines.add(Component.translatable("item.plague.sacrificial_knife.lore1"));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide || !(player instanceof ServerPlayer sp)) return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        LeperData d = LeperData.get(sp);
        if (!d.leper()) {
            sp.displayClientMessage(Component.translatable("msg.plague.knife_leper_only").withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.sidedSuccess(stack, false);
        }
        long now = System.currentTimeMillis();
        long cd = PlagueConfig.INSTANCE.leper.knifeCooldownMinutes * 60000L;
        if (now - d.knifeLastMs() < cd) {
            long sec = (cd - (now - d.knifeLastMs())) / 1000L;
            sp.displayClientMessage(Component.translatable("msg.plague.knife_cooldown", sec).withStyle(ChatFormatting.YELLOW), true);
            return InteractionResultHolder.sidedSuccess(stack, false);
        }
        LeperData.set(sp, d.withKnifeLastMs(now));
        sp.hurtServer((ServerLevel) level, level.damageSources().magic(), 2.0f);
        ItemStack blood = new ItemStack(LeperItems.LEPER_BLOOD);
        if (!sp.getInventory().add(blood)) sp.drop(blood, false);
        sp.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, (int) (PlagueConfig.INSTANCE.leper.knifeFatigueMinutes * 60 * 20), 0));
        sp.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, (int) (PlagueConfig.INSTANCE.leper.knifeWeakMinutes * 60 * 20), 0));
        sp.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, (int) (PlagueConfig.INSTANCE.leper.knifeSlowMinutes * 60 * 20), 0));
        sp.displayClientMessage(Component.translatable("msg.plague.blood_price").withStyle(ChatFormatting.RED), true);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }
}
