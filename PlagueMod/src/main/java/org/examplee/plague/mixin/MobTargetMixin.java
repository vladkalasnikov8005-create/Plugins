package org.examplee.plague.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.monster.Enemy;
import org.examplee.plague.leper.LeperAttachments;
import org.examplee.plague.leper.LeperData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Hostile mobs (except golems) never target lepers. */
@Mixin(Mob.class)
public class MobTargetMixin {
    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void plague$ignoreLepers(LivingEntity target, CallbackInfo ci) {
        Mob self = (Mob) (Object) this;
        if (target instanceof ServerPlayer sp && self instanceof Enemy
                && !(self instanceof IronGolem) && !(self instanceof SnowGolem)) {
            LeperData d = sp.getAttached(LeperAttachments.LEPER);
            if (d != null && d.leper() && !sp.equals(self.getTarget())) {
                ci.cancel();
            }
        }
    }
}
