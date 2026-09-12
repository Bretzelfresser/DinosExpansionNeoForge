package com.bretzelfresser.dinosexpansion.common.entity.util;

import net.minecraft.world.entity.Entity;

public class EntityUtils {

    public static boolean isMovingVertically(Entity entity) {
        return isMovingVertically(entity, 1E-3);
    }
    public static boolean isMovingVertically(Entity entity, double tolerance) {
        return Math.pow(entity.xo - entity.getX(), 2) + Math.pow(entity.zo - entity.getZ(), 2) >= Math.pow(tolerance, 2);
    }
}
