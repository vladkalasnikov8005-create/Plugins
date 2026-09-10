package org.examplee.plague.leper.item;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.examplee.plague.leper.LeperData;
import org.examplee.plague.leper.LeperLogic;
import org.examplee.plague.pale.PaleEngine;

import java.util.List;

/** RMB: poison cloud (r=4.8, 6s) + instant poison/stun. With Danger blessing: hits + pale infect. */
public class PlagueBombItem extends Item {
    public PlagueBombItem(Properties props) { super(props); }

    @Override
    public boolean isFoil(ItemStack stack) { return true; }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("item.plague.plague_cloud.lore0"));
        lines.add(Component.translatable("item.plague.plague_cloud.lore1"));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) return InteractionResultHolder.pass(stack);
        if (level.isClientSide) return InteractionResultHolder.sidedSuccess(stack, true);
        player.getCooldowns().addCooldown(this, 70);
        ServerLevel sl = (ServerLevel) level;

        Vec3 center;
        HitResult hit = player.pick(16.0, 0.0f, false);
        if (hit instanceof BlockHitResult b && b.getType() == HitResult.Type.BLOCK) {
            BlockPos p = b.getBlockPos();
            center = new Vec3(p.getX() + 0.5, p.getY() + 1.0, p.getZ() + 0.5);
        } else {
            center = player.getEyePosition().add(player.getLookAngle().scale(6.0));
        }

        AreaEffectCloud cloud = new AreaEffectCloud(EntityType.AREA_EFFECT_CLOUD, sl);
        cloud.setPos(center.x, center.y, center.z);
        cloud.setRadius(4.8f);
        cloud.setDuration(120);
        cloud.setWaitTime(0);
        cloud.setRadiusPerTick(-(4.8f / 120f));
        cloud.setFixedColor(0x39FF14);
        cloud.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 1));
        cloud.setOwner(player);
        sl.addFreshEntity(cloud);

        boolean blessed = player instanceof ServerPlayer sp && LeperData.get(sp).blessed();
        AABB box = new AABB(center.x - 4.8, center.y - 2.8, center.z - 4.8, center.x + 4.8, center.y + 2.8, center.z + 4.8);
        for (LivingEntity le : sl.getEntities(player, box, e -> e instanceof LivingEntity)) {
            LivingEntity target = (LivingEntity) le;
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 1));
            if (target instanceof ServerPlayer victim) {
                LeperLogic.stun(victim, 40);
                victim.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0, false, false, false));
                if (blessed && !LeperData.get(victim).leper()) LeperLogic.addHit(victim);
            }
        }
        if (blessed) PaleEngine.get().infectArea(sl, BlockPos.containing(center), 5, 1400);
        if (!player.isCreative()) stack.shrink(1);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }
}
