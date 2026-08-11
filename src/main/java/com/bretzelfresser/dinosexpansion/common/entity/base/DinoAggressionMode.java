package com.bretzelfresser.dinosexpansion.common.entity.base;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import net.minecraft.network.chat.Component;

public enum DinoAggressionMode {
    PASSIVE(fromDefault("passive")),
    NEUTRAL(fromDefault("neutral")),
    AGGRESSIVE(fromDefault("aggressive"));

    private final String translationKey;

    DinoAggressionMode(String translationKey) {
        this.translationKey = translationKey;
    }

    public Component getDisplayName() {
        return Component.translatable(translationKey);
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public static String fromDefault(String name) {
        return "aggression." + DinosExpansion.MODID + "." + name;
    }

    public static DinoAggressionMode byId(int id) {
        DinoAggressionMode[] values = values();
        if (id < 0 || id >= values.length) {
            return NEUTRAL;
        }
        return values[id];
    }
}
