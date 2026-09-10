package org.examplee.plague.pale.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import org.examplee.plague.pale.GreatWardBlock;

import java.util.List;

/** BlockItem showing stored charge in lore. */
public class GreatWardItem extends BlockItem {
    public GreatWardItem(Block block, Properties props) { super(block, props); }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        int c = GreatWardBlock.itemCharge(stack);
        lines.add(Component.translatable("item.plague.great_ward.charge", c / 60, c % 60).withStyle(ChatFormatting.GRAY));
    }
}
