package com.bretzelfresser.dinosexpansion.client.gui.spyglass;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.client.gui.FormattingUtils;
import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import com.bretzelfresser.dinosexpansion.common.init.ModAttributes;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Locale;
import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public enum DinoStatTypes {
    LEVEL("Level", dino -> Component.literal(String.valueOf(dino.getDinoLevel()))),
    GENDER("Gender", dino -> dino.getGender().getDisplayName()),
    OWNER("Owner", dino -> {
        if (!dino.isTamed()) return Component.translatable("generic." + DinosExpansion.MODID + ".wild");
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getUUID().equals(dino.getOwnerUUID())) {
            return Component.translatable("generic." + DinosExpansion.MODID + ".you");
        }
        if (dino.getOwner() != null) {
            return dino.getOwner().getDisplayName();
        }
        return Component.translatable("generic." + DinosExpansion.MODID + ".tamed");
    }),
    CARRYING_CAPACITY("Carrying Capacity", dino -> Component.literal("" + Math.round(dino.getAttributeValue(ModAttributes.CARRYING_CAPACITY)))),
    HEALTH("Health", BaseDinoEntity::getHealth, BaseDinoEntity::getMaxHealth, 0xFFFF5555),
    HUNGER("Hunger", BaseDinoEntity::getHunger, dino -> (float) dino.getAttributeValue(ModAttributes.MAX_HUNGER), 0xFF55FF55),
    TORPOR("Torpor", BaseDinoEntity::getTorpor, dino -> (float) dino.getAttributeValue(ModAttributes.MAX_TORPOR), 0xFFAA00AA);

    private final String labelName;
    private final boolean isBar;
    private final Function<BaseDinoEntity, Component> textValGetter;
    private final Function<BaseDinoEntity, Float> floatValGetter;
    private final Function<BaseDinoEntity, Float> maxValGetter;
    private final int color;

    // Constructor for text stats
    DinoStatTypes(String labelName, Function<BaseDinoEntity, Component> textValGetter) {
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

    private String getLabelName() {
        return labelName;
    }

    public String getLabelTranslationKey(){
        return "dino_stat_type." + DinosExpansion.MODID + "." + getLabelName().toLowerCase(Locale.ROOT);
    }

    public Component getLabelTranslationComponent(){
        return Component.translatable(getLabelTranslationKey());
    }

    public boolean isBar() {
        return isBar;
    }

    public Component getValueComponent(BaseDinoEntity dino){
        if (isBar){
            return Component.literal(FormattingUtils.DEFAULT_FLOAT_FORMAT.format(getFloatValue(dino)) + "/" + FormattingUtils.DEFAULT_FLOAT_FORMAT.format(getMaxValue(dino)));
        }
        return Component.translatable("spyglass." + DinosExpansion.MODID + ".stat.value", textValGetter.apply(dino));
    }

    public float getPercentage(BaseDinoEntity dino){
        if (!isBar)
            return 0f;
        var maxValue = getMaxValue(dino);
        var value = getFloatValue(dino);
        return maxValue > 0 ? (value / maxValue) : 0f;
    }

    public float getFloatValue(BaseDinoEntity dino) {
        return isBar ? floatValGetter.apply(dino) : 0f;
    }

    public String getFormattedFloatValue(BaseDinoEntity dino) {
        return FormattingUtils.DEFAULT_FLOAT_FORMAT.format(getFloatValue(dino));
    }

    public String getFormattedMaxValue(BaseDinoEntity dino) {
        return FormattingUtils.DEFAULT_FLOAT_FORMAT.format(getMaxValue(dino));
    }

    public float getMaxValue(BaseDinoEntity dino) {
        return isBar ? maxValGetter.apply(dino) : 0f;
    }

    public int getColor() {
        return color;
    }
}
