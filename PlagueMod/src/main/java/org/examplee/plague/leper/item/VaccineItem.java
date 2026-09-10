package org.examplee.plague.leper.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.examplee.plague.leper.LeperData;
import org.examplee.plague.leper.LeperLogic;

import java.util.List;

/** RMB: cures infection stages 1-2. Useless for full lepers. */
public class VaccineItem extends Item {
    public VaccineItem(Properties props) { super(props); }

    @Override
    public boolean isFoil(ItemStack stack) { return true; }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("item.plague.vaccine.lore0"));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide || !(player instanceof ServerPlayer sp)) return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        LeperData d = LeperData.get(sp);
        if (d.leper()) {
            sp.displayClientMessage(Component.translatable("msg.plague.vaccine_too_late").withStyle(ChatFormatting.RED), true);
        } else if (d.stage() != 1 && d.stage() != 2) {
            sp.displayClientMessage(Component.translatable("msg.plague.not_infected").withStyle(ChatFormatting.YELLOW), true);
        } else {
            LeperLogic.cure(sp);
            if (!sp.isCreative()) {
                stack.shrink(1);
                if (stack.isEmpty()) sp.setItemInHand(hand, new ItemStack(Items.GLASS_BOTTLE));
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, false);
    }
}
