package org.examplee.plague.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import org.examplee.plague.PlagueMod;

public class PlagueDoctorRenderer extends MobRenderer<PlagueDoctorEntity, PlagueDoctorModel> {
    private static final Identifier TEXTURE = PlagueMod.id("textures/entity/plague_doctor.png");

    public PlagueDoctorRenderer(EntityRendererProvider.Context context) {
        super(context, new PlagueDoctorModel(context.bakeLayer(PlagueDoctorModel.LAYER)), 0.5f);
    }

    @Override
    public Identifier getTextureLocation(PlagueDoctorEntity entity) {
        return TEXTURE;
    }
}
