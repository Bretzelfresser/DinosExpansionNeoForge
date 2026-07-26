package com.bretzelfresser.dinosexpansion.common.entity.base;

public enum DinoGender {
    MALE,
    FEMALE;

    public static DinoGender byId(int id) {
        if (id < 0 || id >= values().length) {
            return MALE;
        }
        return values()[id];
    }
}
