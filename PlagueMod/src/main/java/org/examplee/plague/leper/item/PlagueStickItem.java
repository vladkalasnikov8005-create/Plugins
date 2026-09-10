package org.examplee.plague.leper.item;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** RMB: sneeze sound + particles (0.7s cd). Hits handled in LeperEvents (poison + stun + hit). */
public class PlagueStickItem extends Item {
    public PlagueStickItem(Properties props) { super(props); }

    @Override
    public boolean isFoil(ItemStack stack) { return true; }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("item.plague.plague_stick.lore0"));
        lines.add(Component.translatable("item.plague.plague_stick.lore1"));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) return InteractionResultHolder.pass(stack);
        player.getCooldowns().addCooldown(this, 14);
        level.playSound(player, player.blockPosition(), SoundEvents.PANDA_SNEEZE, SoundSource.PLAYERS, 0.85f, 0.95f);
        if (!level.isClientSide && level instanceof net.minecraft.server.level.ServerLevel sl) {
            Vec3 eye = player.getEyePosition();
            sl.sendParticles(net.minecraft.core.particles.DustParticleOptions.fromColor(0x39FF14, 1.6f),
                    eye.x, eye.y, eye.z, 10, 0.18, 0.18, 0.18, 0.0);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
