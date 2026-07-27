package com.bretzelfresser.dinosexpansion.client.gui.spyglass;

import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import com.bretzelfresser.dinosexpansion.common.init.ModAttributes;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public enum DinoStatTypes {
    LEVEL("Level", dino -> String.valueOf(dino.getDinoLevel())),
    GENDER("Gender", dino -> dino.getGender().name()),
    OWNER("Owner", dino -> {
        if (!dino.isTamed()) return "Wild";
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getUUID().equals(dino.getOwnerUUID())) {
            return "You";
        }
        if (dino.getOwner() != null) {
            return dino.getOwner().getDisplayName().getString();
        }
        return "Tamed";
    }),
    HEALTH("Health", BaseDinoEntity::getHealth, BaseDinoEntity::getMaxHealth, 0xFFFF5555),
    HUNGER("Hunger", BaseDinoEntity::getHunger, dino -> (float) dino.getAttributeValue(ModAttributes.MAX_HUNGER), 0xFF55FF55),
    TORPOR("Torpor", BaseDinoEntity::getTorpor, dino -> (float) dino.getAttributeValue(ModAttributes.MAX_TORPOR), 0xFFAA00AA);

    private final String labelName;
    private final boolean isBar;
    private final Function<BaseDinoEntity, String> textValGetter;
    private final Function<BaseDinoEntity, Float> floatValGetter;
    private final Function<BaseDinoEntity, Float> maxValGetter;
    private final int color;

    // Constructor for text stats
    DinoStatTypes(String labelName, Function<BaseDinoEntity, String> textValGetter) {
        this.labelName = labelName;
        this.isBar = false;
        this.textValGetter = textValGetter;
        this.floatValGetter = null;
        this.maxValGetter = null;
        this.color = 0xFFFFFFFF;
    }

    // Constructor for progress bar stats
    DinoStatTypes(String labelName, Function<BaseDinoEntity, Float> floatValGetter, Function<BaseDinoEntity, Float> maxValGetter, int color) {
        this.labelName = labelName;
        this.isBar = true;
        this.textValGetter = null;
        this.floatValGetter = floatValGetter;
        this.maxValGetter = maxValGetter;
        this.color = color;
    }

    public String getLabelName() {
        return labelName;
    }

    public boolean isBar() {
        return isBar;
    }

    public String getValueString(BaseDinoEntity dino) {
        if (isBar) {
            return (int) getFloatValue(dino) + "/" + (int) getMaxValue(dino);
        }
        return textValGetter.apply(dino);
    }

    public float getFloatValue(BaseDinoEntity dino) {
        return isBar ? floatValGetter.apply(dino) : 0f;
    }

    public float getMaxValue(BaseDinoEntity dino) {
        return isBar ? maxValGetter.apply(dino) : 0f;
    }

    public int getColor() {
        return color;
    }
}
