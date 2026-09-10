package org.examplee.plague.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.examplee.plague.leper.LeperAttachments;
import org.examplee.plague.leper.LeperData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Lepers starve x4 faster. */
@Mixin(Player.class)
public class ExhaustionMixin {
    @ModifyVariable(method = "causeFoodExhaustion", at = @At("HEAD"), argsOnly = true)
    private float plague$hunger(float amount) {
        Player self = (Player) (Object) this;
        if (self instanceof ServerPlayer sp) {
            LeperData d = sp.getAttached(LeperAttachments.LEPER);
            if (d != null && d.leper()) return amount * 4.0f;
        }
        return amount;
    }
}
