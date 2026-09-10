package org.examplee.plague.pale.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.examplee.plague.PlagueConfig;

import java.util.List;

/** RMB opens the infection map GUI (client sends request, server replies with data). */
public class PaleMapItem extends Item {
    public PaleMapItem(Properties props) { super(props); }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("item.plague.pale_map.lore0"));
        lines.add(Component.translatable("item.plague.pale_map.lore1", PlagueConfig.INSTANCE.pale.mapDefaultRadiusChunks));
    }
}
