package com.bretzelfresser.dinosexpansion.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.function.Predicate;

@OnlyIn(Dist.CLIENT)
public class DisableBoneLayer<T extends GeoAnimatable> extends GeoRenderLayer<T> {

    protected final Predicate<T> activatable;
    protected final String[] boneNames;
    public DisableBoneLayer(GeoRenderer<T> entityRendererIn, Predicate<T> activatable, String... boneNames) {
        super(entityRendererIn);
        this.activatable = activatable;
        this.boneNames = boneNames;
    }

    @Override
    public void preRender(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        for (String name : this.boneNames){
            bakedModel.getBone(name).ifPresent(bone -> bone.setHidden(!activatable.test(animatable)));
        }
    }
}
