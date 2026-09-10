package org.examplee.plague.leper.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.examplee.plague.leper.LeperData;
import org.examplee.plague.leper.LeperLogic;

import java.util.List;

/** Drinkable blood. kind: 0 raw (infects), 1 thick (sick), 2 sterile (useless). */
public class BloodItem extends Item {
    private final int kind;

    public BloodItem(Properties props, int kind) {
        super(props.food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.1f).alwaysEdible().build()));
        this.kind = kind;
    }

    @Override
    public boolean isFoil(ItemStack stack) { return kind < 2; }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("item.plague.blood" + kind + ".lore0"));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity user) {
        ItemStack out = super.finishUsingItem(stack, level, user);
        if (level.isClientSide) return out;
        if (kind == 0) {
            user.addEffect(new MobEffectInstance(MobEffects.POISON, 240, 1));
            if (user instanceof ServerPlayer sp && !LeperData.get(sp).leper()) LeperLogic.startInfection(sp);
        } else if (kind == 1) {
            user.addEffect(new MobEffectInstance(MobEffects.POISON, 160, 1));
            user.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 240, 0));
        }
        return out;
    }
}
