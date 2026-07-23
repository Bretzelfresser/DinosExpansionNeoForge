package com.bretzelfresser.dinosexpansion.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.function.Consumer;

public class BoundingBoxUtils {

    public static void forEachPos(BoundingBox box, Consumer<BlockPos> consumer){
        for (int x = box.minX(); x <= box.maxX(); x++) {
            for (int z = box.minZ(); z <= box.maxZ(); z++) {
                for (int y = box.minY(); y <= box.maxY(); y++) {
                    consumer.accept(new BlockPos(x,y,z));
                }
            }
        }
    }
}
