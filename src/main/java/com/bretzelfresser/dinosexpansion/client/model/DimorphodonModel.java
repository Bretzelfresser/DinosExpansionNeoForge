package com.bretzelfresser.dinosexpansion.client.model;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.common.entity.dinosaur.dimorphodon.Dimorphodon;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DimorphodonModel extends GeoModel<Dimorphodon> {
    @Override
    public ResourceLocation getModelResource(Dimorphodon animatable) {
        return DinosExpansion.modLoc("geo/dimorphodon.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Dimorphodon animatable) {
        String variantName = animatable.getVariant().getName();
        return DinosExpansion.modLoc("textures/entity/dimorphodon/dimorphodon_" + variantName + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(Dimorphodon animatable) {
        return DinosExpansion.modLoc("animations/dimorphodon.animation.json");
    }
}
