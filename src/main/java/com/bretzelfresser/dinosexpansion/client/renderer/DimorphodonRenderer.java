package com.bretzelfresser.dinosexpansion.client.renderer;

import com.bretzelfresser.dinosexpansion.client.layer.DisableBoneLayer;
import com.bretzelfresser.dinosexpansion.client.model.DimorphodonModel;
import com.bretzelfresser.dinosexpansion.common.entity.dinosaur.dimorphodon.Dimorphodon;
import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class DimorphodonRenderer extends GeoEntityRenderer<Dimorphodon> {
    public DimorphodonRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DimorphodonModel());
        addRenderLayer(new DisableBoneLayer<>(this, BaseDinoEntity::isSaddled, "saddle"));
        addRenderLayer(new DisableBoneLayer<>(this, BaseDinoEntity::isChested, "chests"));
    }
}
