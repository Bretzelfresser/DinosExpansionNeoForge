package com.bretzelfresser.dinosexpansion.client.renderer;

import com.bretzelfresser.dinosexpansion.client.model.CertosaurusModel;
import com.bretzelfresser.dinosexpansion.common.entity.Certosaurus;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CertosaurusRenderer extends GeoEntityRenderer<Certosaurus> {
    public CertosaurusRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new CertosaurusModel());
    }
}
