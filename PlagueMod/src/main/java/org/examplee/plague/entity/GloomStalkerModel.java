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

/** Low quadruped predator with back spikes. Diagonal-leg gait. */
public class GloomStalkerModel extends HierarchicalModel<GloomStalkerEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(PlagueMod.id("gloom_stalker"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart legFL, legFR, legBL, legBR;

    public GloomStalkerModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.legFL = root.getChild("leg_fl");
        this.legFR = root.getChild("leg_fr");
        this.legBL = root.getChild("leg_bl");
        this.legBR = root.getChild("leg_br");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4, -10, -7, 8, 8, 14),
                PartPose.offset(0, 18, 0));
        // Back spikes.
        body.addOrReplaceChild("spikes",
                CubeListBuilder.create().texOffs(44, 0)
                        .addBox(-1, -14, -5, 2, 4, 2)
                        .addBox(-1, -14, 0, 2, 4, 2)
                        .addBox(-1, -14, 5, 2, 4, 2),
                PartPose.offset(0, 0, 0));
        root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 22).addBox(-3, -4, -2, 6, 6, 6),
                PartPose.offset(0, 13, 7));
        root.addOrReplaceChild("leg_fl",
                CubeListBuilder.create().texOffs(30, 22).addBox(-1.5f, 0, -1.5f, 3, 6, 3),
                PartPose.offset(-3, 18, 5));
        root.addOrReplaceChild("leg_fr",
                CubeListBuilder.create().texOffs(30, 22).addBox(-1.5f, 0, -1.5f, 3, 6, 3),
                PartPose.offset(3, 18, 5));
        root.addOrReplaceChild("leg_bl",
                CubeListBuilder.create().texOffs(30, 22).addBox(-1.5f, 0, -1.5f, 3, 6, 3),
                PartPose.offset(-3, 18, -5));
        root.addOrReplaceChild("leg_br",
                CubeListBuilder.create().texOffs(30, 22).addBox(-1.5f, 0, -1.5f, 3, 6, 3),
                PartPose.offset(3, 18, -5));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(GloomStalkerEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        head.yRot = netHeadYaw * Mth.DEG_TO_RAD * 0.6f;
        head.xRot = headPitch * Mth.DEG_TO_RAD * 0.6f + Mth.cos(ageInTicks * 0.1f) * 0.08f;
        float gait = limbSwing * 1.2f;
        legFL.xRot = Mth.cos(gait) * 1.1f * limbSwingAmount;
        legBR.xRot = Mth.cos(gait) * 1.1f * limbSwingAmount;
        legFR.xRot = Mth.cos(gait + Mth.PI) * 1.1f * limbSwingAmount;
        legBL.xRot = Mth.cos(gait + Mth.PI) * 1.1f * limbSwingAmount;
    }
}
