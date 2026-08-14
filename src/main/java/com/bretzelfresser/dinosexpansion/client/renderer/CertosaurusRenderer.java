package com.bretzelfresser.dinosexpansion.client.renderer;

import com.bretzelfresser.dinosexpansion.client.layer.DisableBoneLayer;
import com.bretzelfresser.dinosexpansion.client.model.CertosaurusModel;
import com.bretzelfresser.dinosexpansion.common.entity.dinosaur.ceratosaurus.Certosaurus;
import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CertosaurusRenderer extends GeoEntityRenderer<Certosaurus> {
    public CertosaurusRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new CertosaurusModel());
        addRenderLayer(new DisableBoneLayer<>(this, BaseDinoEntity::isSaddled, "saddle"));
        addRenderLayer(new DisableBoneLayer<>(this, BaseDinoEntity::isChested, "chests"));
    }
}
