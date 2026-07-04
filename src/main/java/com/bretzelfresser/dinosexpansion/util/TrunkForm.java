package com.bretzelfresser.dinosexpansion.util;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public enum TrunkForm implements StringRepresentable {
    SQUARE,
    CIRCLE {
        @Override
        public Set<BlockPos> calculateBase(BlockPos start, int radius) {
            var posSet = new HashSet<BlockPos>();
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (z * z + x * x <= radius * radius) {
                        posSet.add(start.offset(x, 0, z));
                    }
                }
            }
            return posSet;
        }
    },
    SQUARE_WITH_CUTOUT_EDGES {
        @Override
        public Set<BlockPos> calculateBase(BlockPos start, int radius) {
            var posSet = new HashSet<BlockPos>();
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (radius > 0 && Math.abs(x) == radius && Math.abs(z) == radius) {
                        continue;
                    }
                    posSet.add(start.offset(x, 0, z));
                }
            }
            return posSet;
        }
    };

    public Set<BlockPos> calculateBase(BlockPos start, int radius) {
        var posSet = new HashSet<BlockPos>();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                posSet.add(start.offset(x, 0, z));
            }
        }
        return posSet;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static final Codec<TrunkForm> CODEC = StringRepresentable.fromEnum(TrunkForm::values);
}
