package com.bretzelfresser.dinosexpansion.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.function.Predicate;

@OnlyIn(Dist.CLIENT)
public class CustomLayer<T extends GeoAnimatable> extends GeoRenderLayer<T> {

    protected final Predicate<T> shouldRender;
    protected final ResourceLocation layerLocation;

    public CustomLayer(GeoRenderer<T> entityRendererIn, Predicate<T> shouldRender, ResourceLocation layerLocation) {
        super(entityRendererIn);
        this.shouldRender = shouldRender;
        this.layerLocation = layerLocation;
    }

    @Override
    public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if (shouldRender.test(animatable)) {
            RenderType type = RenderType.entityCutout(layerLocation);
            poseStack.pushPose();
            getRenderer().reRender(getDefaultBakedModel(animatable), poseStack, bufferSource, animatable, type,
                    bufferSource.getBuffer(type), partialTick, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
            poseStack.popPose();
        }
    }
}
