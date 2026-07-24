package com.bretzelfresser.dinosexpansion.client.renderer;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.client.layer.CustomLayer;
import com.bretzelfresser.dinosexpansion.client.model.CertosaurusModel;
import com.bretzelfresser.dinosexpansion.common.entity.BaseDinoEntity;
import com.bretzelfresser.dinosexpansion.common.entity.Certosaurus;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CertosaurusRenderer extends GeoEntityRenderer<Certosaurus> {
    public CertosaurusRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new CertosaurusModel());
        addRenderLayer(new CustomLayer<>(this, BaseDinoEntity::isSaddled, DinosExpansion.modLoc("textures/entity/ceratosaurus/ceratosaurus_saddle.png")));
        //addRenderLayer(new DisableBoneLayer<>(this, BaseDinoEntity::isChested, "chests"));
    }
}
