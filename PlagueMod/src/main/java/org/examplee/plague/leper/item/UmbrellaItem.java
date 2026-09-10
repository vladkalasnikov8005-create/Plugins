package org.examplee.plague.leper.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

import java.util.List;

/**
 * Sun umbrella, held in offhand. Durability = remaining seconds in NBT,
 * shown as lore + vanilla item bar. Tiers: 0/150s, 1/600s, 2/1500s, 3/3000s.
 */
public class UmbrellaItem extends Item {
    private final int tier;
    private final int lifetime;

    public UmbrellaItem(Properties props, int tier, int lifetime) {
        super(props);
        this.tier = tier;
        this.lifetime = lifetime;
    }

    public int tier() { return tier; }
    public int lifetime() { return lifetime; }

    public static int getRemaining(ItemStack stack) {
        if (!(stack.getItem() instanceof UmbrellaItem u)) return 0;
        CompoundTag tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.contains("umbrella_remaining")) return u.lifetime;
        return Math.max(0, Math.min(u.lifetime, tag.getInt("umbrella_remaining")));
    }

    public static void setRemaining(ItemStack stack, int remaining) {
        if (!(stack.getItem() instanceof UmbrellaItem u)) return;
        CompoundTag tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putInt("umbrella_remaining", Math.max(0, Math.min(u.lifetime, remaining)));
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("item.plague.umbrella.lore0").withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("item.plague.umbrella.lore1").withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("item.plague.umbrella.tier", tier).withStyle(ChatFormatting.GRAY));
        int r = getRemaining(stack);
        lines.add(Component.translatable("item.plague.umbrella.left", String.format("%02d:%02d", r / 60, r % 60)).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) { return true; }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0f * getRemaining(stack) / (float) lifetime);
    }

    @Override
    public int getBarColor(ItemStack stack) { return 0x39FF14; }
}
