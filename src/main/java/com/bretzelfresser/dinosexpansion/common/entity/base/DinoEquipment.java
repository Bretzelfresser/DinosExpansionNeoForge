package com.bretzelfresser.dinosexpansion.common.entity.base;

import java.util.Optional;

public enum DinoEquipment {
    SADDLE,
    ARMOR,
    CHEST;


    public static DinoEquipment byId(int id) {
        if (id < 0 || id >= values().length) {
            throw new IllegalArgumentException(String.format( "index %d is out of bounds for bounds [0, %d]", id, values().length));
        }
        return values()[id];
    }
    public static Optional<DinoEquipment> optionalById(int id) {
        if (id < 0 || id >= values().length) {
            return Optional.empty();
        }
        return Optional.of(values()[id]);
    }
}
