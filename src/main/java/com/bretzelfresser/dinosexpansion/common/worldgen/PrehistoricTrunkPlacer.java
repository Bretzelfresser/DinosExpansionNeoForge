package com.bretzelfresser.dinosexpansion.common.worldgen;

import com.bretzelfresser.dinosexpansion.common.init.ModTreePlacers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class PrehistoricTrunkPlacer extends TrunkPlacer {
    public static final MapCodec<PrehistoricTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(
            instance -> trunkPlacerParts(instance)
                    .and(Codec.BOOL.fieldOf("mega").orElse(false).forGetter(tp -> tp.mega))
                    .apply(instance, PrehistoricTrunkPlacer::new)
    );

    private final boolean mega;

    public PrehistoricTrunkPlacer(int baseHeight, int heightRandA, int heightRandB, boolean mega) {
        super(baseHeight, heightRandA, heightRandB);
        this.mega = mega;
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return ModTreePlacers.PREHISTORIC_TRUNK_PLACER.get();
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(
            LevelSimulatedReader level,
            BiConsumer<BlockPos, BlockState> blockSetter,
            RandomSource random,
            int freeSteps,
            BlockPos pos,
            TreeConfiguration config
    ) {
        setDirtAt(level, blockSetter, random, pos.below(), config);
        List<FoliagePlacer.FoliageAttachment> foliageNodes = new ArrayList<>();

        if (this.mega) {
            // Mega tree (2x2 trunk)
            setDirtAt(level, blockSetter, random, pos.below().east(), config);
            setDirtAt(level, blockSetter, random, pos.below().south(), config);
            setDirtAt(level, blockSetter, random, pos.below().south().east(), config);

            for (int y = 0; y < freeSteps; y++) {
                placeLog(level, blockSetter, random, pos.above(y), config);
                placeLog(level, blockSetter, random, pos.above(y).east(), config);
                placeLog(level, blockSetter, random, pos.above(y).south(), config);
                placeLog(level, blockSetter, random, pos.above(y).south().east(), config);
            }

            // Root flare at the bottom 2 blocks
            for (int y = 0; y < 2; y++) {
                placeLog(level, blockSetter, random, pos.above(y).north(), config);
                placeLog(level, blockSetter, random, pos.above(y).south().south(), config);
                placeLog(level, blockSetter, random, pos.above(y).west(), config);
                placeLog(level, blockSetter, random, pos.above(y).east().east(), config);
            }

            foliageNodes.add(new FoliagePlacer.FoliageAttachment(pos.above(freeSteps), 0, true));
        } else {
            // Standard tree (1x1 trunk)
            for (int y = 0; y < freeSteps; y++) {
                placeLog(level, blockSetter, random, pos.above(y), config);
            }

            // Small root flare at the bottom
            placeLog(level, blockSetter, random, pos.north(), config);
            placeLog(level, blockSetter, random, pos.south(), config);
            placeLog(level, blockSetter, random, pos.east(), config);
            placeLog(level, blockSetter, random, pos.west(), config);

            foliageNodes.add(new FoliagePlacer.FoliageAttachment(pos.above(freeSteps), 0, false));
        }

        return foliageNodes;
    }
}
