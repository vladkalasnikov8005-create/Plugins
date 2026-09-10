package org.examplee.plague.entity;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import org.examplee.plague.PlagueMod;

/** Humanoid doctor: beak mask + wide-brim hat + long coat. Procedural walk animation. */
public class PlagueDoctorModel extends HierarchicalModel<PlagueDoctorEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(PlagueMod.id("plague_doctor"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart armL, armR, legL, legR;

    public PlagueDoctorModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.armL = root.getChild("arm_l");
        this.armR = root.getChild("arm_r");
        this.legL = root.getChild("leg_l");
        this.legR = root.getChild("leg_r");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4, -8, -4, 8, 8, 8),
                PartPose.offset(0, 0, 0));
        // Beak mask protruding forward (+z).
        head.addOrReplaceChild("beak",
                CubeListBuilder.create().texOffs(32, 0).addBox(-2, -5, 4, 4, 3, 6),
                PartPose.offset(0, 0, 0));
        // Wide-brim hat + crown.
        head.addOrReplaceChild("hat",
                CubeListBuilder.create().texOffs(0, 32).addBox(-7, -9, -7, 14, 1, 14)
                        .texOffs(0, 48).addBox(-4, -15, -4, 8, 6, 8),
                PartPose.offset(0, 0, 0));
        // Long coat body.
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(16, 16).addBox(-4, 0, -2, 8, 14, 4),
                PartPose.offset(0, 0, 0));
        root.addOrReplaceChild("arm_r",
                CubeListBuilder.create().texOffs(40, 16).addBox(-3, -2, -2, 4, 12, 4),
                PartPose.offset(-5, 2, 0));
        root.addOrReplaceChild("arm_l",
                CubeListBuilder.create().texOffs(40, 16).addBox(-1, -2, -2, 4, 12, 4),
                PartPose.offset(5, 2, 0));
        root.addOrReplaceChild("leg_r",
                CubeListBuilder.create().texOffs(0, 16).addBox(-2, 0, -2, 4, 12, 4),
                PartPose.offset(-1.9f, 12, 0));
        root.addOrReplaceChild("leg_l",
                CubeListBuilder.create().texOffs(0, 16).addBox(-2, 0, -2, 4, 12, 4),
                PartPose.offset(1.9f, 12, 0));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(PlagueDoctorEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        head.xRot = headPitch * Mth.DEG_TO_RAD;
        legR.xRot = Mth.cos(limbSwing * 0.6662f) * 1.4f * limbSwingAmount;
        legL.xRot = Mth.cos(limbSwing * 0.6662f + Mth.PI) * 1.4f * limbSwingAmount;
        armR.xRot = Mth.cos(limbSwing * 0.6662f + Mth.PI) * 1.4f * limbSwingAmount;
        armL.xRot = Mth.cos(limbSwing * 0.6662f) * 1.4f * limbSwingAmount;
        // Idle sway.
        root.yRot = Mth.cos(ageInTicks * 0.05f) * 0.03f;
    }
}
