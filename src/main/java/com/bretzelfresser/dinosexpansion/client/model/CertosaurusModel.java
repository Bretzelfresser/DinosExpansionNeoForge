package com.bretzelfresser.dinosexpansion.client.model;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.common.entity.Certosaurus;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class CertosaurusModel extends GeoModel<Certosaurus> {
    @Override
    public ResourceLocation getModelResource(Certosaurus animatable) {
        return DinosExpansion.modLoc("geo/ceratosaurus.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Certosaurus animatable) {
        return DinosExpansion.modLoc("textures/entity/ceratosaurus/ceratosaurus_common.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Certosaurus animatable) {
        return DinosExpansion.modLoc("animations/ceratosaurus.json");
    }
}
