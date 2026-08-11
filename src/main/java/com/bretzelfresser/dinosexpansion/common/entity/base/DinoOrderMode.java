package com.bretzelfresser.dinosexpansion.common.entity.base;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import net.minecraft.network.chat.Component;

public enum DinoOrderMode {
    WANDER(fromDefault("wander")),
    FOLLOW(fromDefault("follow")),
    STAY(fromDefault("stay"));

    private final String translationKey;

    DinoOrderMode(String translationKey) {
        this.translationKey = translationKey;
    }

    public Component getDisplayName() {
        return Component.translatable(translationKey);
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public static String fromDefault(String name) {
        return "order." + DinosExpansion.MODID + "." + name;
    }

    public static DinoOrderMode byId(int id) {
        DinoOrderMode[] values = values();
        if (id < 0 || id >= values.length) {
            return FOLLOW;
        }
        return values[id];
    }
}
