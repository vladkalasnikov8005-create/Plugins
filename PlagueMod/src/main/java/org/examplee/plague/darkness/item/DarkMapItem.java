package org.examplee.plague.darkness.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.examplee.plague.PlagueConfig;

import java.util.List;

/** RMB opens the darkness map GUI (client sends request, server replies). */
public class DarkMapItem extends Item {
    public DarkMapItem(Properties props) { super(props); }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("item.plague.dark_map.lore0"));
        lines.add(Component.translatable("item.plague.dark_map.lore1", PlagueConfig.INSTANCE.pale.mapDefaultRadiusChunks));
    }
}
