package com.bretzelfresser.dinosexpansion.common.entity.base;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import net.minecraft.network.chat.Component;

public enum DinoGender {
    MALE(fromDefault("male")),
    FEMALE(fromDefault("female"));


    private final String translationKey;

    DinoGender(String translationKey) {
        this.translationKey = translationKey;
    }

    public Component getDisplayName() {
        return Component.translatable(translationKey);
    }

    public String getTranslationKey() {
        return translationKey;
    }

    public static String fromDefault(String name) {
        return "gender." + DinosExpansion.MODID + "." + name;
    }

    public static DinoGender byId(int id) {
        if (id < 0 || id >= values().length) {
            return MALE;
        }
        return values()[id];
    }
}
