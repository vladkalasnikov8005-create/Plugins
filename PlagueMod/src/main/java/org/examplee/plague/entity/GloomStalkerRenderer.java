package org.examplee.plague.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import org.examplee.plague.PlagueMod;

public class GloomStalkerRenderer extends MobRenderer<GloomStalkerEntity, GloomStalkerModel> {
    private static final Identifier TEXTURE = PlagueMod.id("textures/entity/gloom_stalker.png");

    public GloomStalkerRenderer(EntityRendererProvider.Context context) {
        super(context, new GloomStalkerModel(context.bakeLayer(GloomStalkerModel.LAYER)), 0.6f);
    }

    @Override
    public Identifier getTextureLocation(GloomStalkerEntity entity) {
        return TEXTURE;
    }
}
