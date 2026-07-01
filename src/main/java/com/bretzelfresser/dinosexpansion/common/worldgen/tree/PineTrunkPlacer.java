package com.bretzelfresser.dinosexpansion.common.worldgen.tree;

import com.bretzelfresser.dinosexpansion.common.init.ModTreePlacers;
import com.bretzelfresser.dinosexpansion.util.CodecUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.*;
import net.minecraft.util.valueproviders.*;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

import java.util.*;
import java.util.function.BiConsumer;

public class PineTrunkPlacer extends TrunkPlacer {

    public record TrunkConfig(IntProvider bottomRadius, IntProvider topRadius, TrunkForm form) {
    }

    public static final Codec<TrunkConfig> TRUNK_CONFIG_CODEC = RecordCodecBuilder.create(objectInstance -> objectInstance.group(
            IntProvider.CODEC.fieldOf("bottomRadius").orElse(ConstantInt.of(2)).forGetter(TrunkConfig::bottomRadius),
            IntProvider.CODEC.fieldOf("topRadius").orElse(ConstantInt.of(0)).forGetter(TrunkConfig::topRadius),
            StringRepresentable.fromEnum(TrunkForm::values).fieldOf("form").orElse(TrunkForm.SQUARE_WITH_CUTOUT_EDGES).forGetter(TrunkConfig::form)
    ).apply(objectInstance, TrunkConfig::new));

    public static final MapCodec<PineTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(
            instance -> trunkPlacerParts(instance)
                    .and(FloatProvider.CODEC.fieldOf("startPercentage").orElse(ConstantFloat.of(0.4f)).forGetter(tp -> tp.startPercentage))
                    .and(FloatProvider.CODEC.fieldOf("branchPercentage").orElse(ConstantFloat.of(0.4f)).forGetter(tp -> tp.branchPercentage))
                    .and(CodecUtils.FLOAT_SPLINE_CODEC.optionalFieldOf("trunk_thickness_spline").forGetter(tp -> Optional.ofNullable(tp.trunkThicknessSpline)))
                    .and(TRUNK_CONFIG_CODEC.fieldOf("trunk_config").orElse(new TrunkConfig(ConstantInt.of(2), ConstantInt.of(0), TrunkForm.SQUARE_WITH_CUTOUT_EDGES)).forGetter(tp -> tp.cfg))
                    .apply(instance, PineTrunkPlacer::new)
    );

    public enum TrunkForm implements StringRepresentable {
        SQUARE,
        CIRCLE {
            @Override
            Set<BlockPos> calculateBase(BlockPos start, int radius) {
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
            Set<BlockPos> calculateBase(BlockPos start, int radius) {
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

        Set<BlockPos> calculateBase(BlockPos start, int radius) {
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
    }

    protected final FloatProvider startPercentage, branchPercentage;
    protected final CubicSpline<Float, ToFloatFunction<Float>> trunkThicknessSpline;
    protected final TrunkConfig cfg;


    public PineTrunkPlacer(int minHeight, int additionalHeightRandA, int additionalHeightRandB, FloatProvider startPercentage, FloatProvider branchPercentage, Optional<CubicSpline<Float, ToFloatFunction<Float>>> trunkThicknessSpline, TrunkConfig cfg) {
        super(minHeight, additionalHeightRandA, additionalHeightRandB);
        this.startPercentage = startPercentage;
        this.branchPercentage = branchPercentage;
        this.trunkThicknessSpline = trunkThicknessSpline.orElse(null);
        this.cfg = cfg;
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return ModTreePlacers.PINE_TRUNK_PLACER.get();
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


        int radiusBottom = Math.max(0, cfg.bottomRadius.sample(random));
        int radiusTop = Math.max(0, cfg.topRadius.sample(random));
        var basePositions = cfg.form.calculateBase(pos, radiusBottom);
        basePositions.forEach(p -> setDirtAt(level, blockSetter, random, p.below(), config));
        float heightBranchStartPercentage = Mth.clamp(startPercentage.sample(random), 0f, 1f);
        int startBranchesY = Math.round((float) freeSteps * heightBranchStartPercentage);

        List<FoliagePlacer.FoliageAttachment> foliageNodes = new ArrayList<>();
        var mutablePos = pos.mutable();
        //spline defining the radius curve depedning on the height
        var densitySpline = this.trunkThicknessSpline == null ? CubicSpline.builder(ToFloatFunction.IDENTITY)
                .addPoint(0, 0, 0)
                .addPoint(heightBranchStartPercentage, 0, 0)
                .addPoint(1, 1, 0)
                .build() : this.trunkThicknessSpline;
        for (int y = 0; y < freeSteps; y++) {
            int radius = Math.round(Mth.lerp(densitySpline.apply((float) y / (float) freeSteps), (float) radiusBottom, (float) radiusTop));
            var positions = cfg.form().calculateBase(mutablePos.offset(0, y, 0), radius);
            positions.forEach(p -> {
                this.placeLog(level, blockSetter, random, p, config);
            });
        }
        foliageNodes.add(new FoliagePlacer.FoliageAttachment(mutablePos.offset(0, freeSteps, 0), 0, false));

        return foliageNodes;
    }

    public static class Builder {
        private final int minHeight;
        private final int additionalHeightRandA;
        private final int additionalHeightRandB;
        private FloatProvider startPercentage = ConstantFloat.of(0.4f);
        private FloatProvider branchPercentage = ConstantFloat.of(0.4f);
        private IntProvider bottomRadius = ConstantInt.of(2);
        private IntProvider topRadius = ConstantInt.of(0);
        private TrunkForm form = TrunkForm.SQUARE_WITH_CUTOUT_EDGES;
        private CubicSpline<Float, ToFloatFunction<Float>> trunkThicknessSpline = null;

        public Builder(int minHeight, int additionalHeightRandA, int additionalHeightRandB) {
            this.minHeight = minHeight;
            this.additionalHeightRandA = additionalHeightRandA;
            this.additionalHeightRandB = additionalHeightRandB;
        }

        public Builder constantRadius(int radius) {
            return constantRadius(ConstantInt.of(radius));
        }

        public Builder constantRadius(IntProvider radius) {
            bottomRadius(radius);
            topRadius(radius);
            return this;
        }

        public Builder startPercentage(float startPercentage) {
            return startPercentage(ConstantFloat.of(startPercentage));
        }

        public Builder startPercentage(FloatProvider startPercentage) {
            this.startPercentage = startPercentage;
            return this;
        }

        public Builder thicknessSpline(float beginSmaller) {
            var clamped = Mth.clamp(beginSmaller, 0.01f, 0.99f);
            return thicknessSpline(CubicSpline.builder(ToFloatFunction.IDENTITY)
                    .addPoint(0, 0, 0)
                    .addPoint(clamped, 0, 0)
                    .addPoint(1, 1, 0)
                    .build());
        }

        public Builder thicknessSplineLinear() {
            return thicknessSpline(CubicSpline.builder(ToFloatFunction.IDENTITY)
                    .addPoint(0, 0, 1)
                    .addPoint(1, 1, 1)
                    .build());
        }

        public Builder thicknessSpline(CubicSpline<Float, ToFloatFunction<Float>> spline) {
            this.trunkThicknessSpline = spline;
            return this;
        }

        public Builder branchPercentage(float branchPercentage) {
            return branchPercentage(ConstantFloat.of(branchPercentage));
        }

        public Builder branchPercentage(FloatProvider branchPercentage) {
            this.branchPercentage = branchPercentage;
            return this;
        }

        public Builder bottomRadius(int bottomRadius) {
            return bottomRadius(ConstantInt.of(bottomRadius));
        }

        public Builder bottomRadius(IntProvider bottomRadius) {
            this.bottomRadius = bottomRadius;
            return this;
        }

        public Builder topRadius(int topRadius) {
            return topRadius(ConstantInt.of(topRadius));
        }

        public Builder topRadius(IntProvider topRadius) {
            this.topRadius = topRadius;
            return this;
        }

        public Builder form(TrunkForm form) {
            this.form = form;
            return this;
        }

        public PineTrunkPlacer build() {
            return new PineTrunkPlacer(
                    this.minHeight,
                    this.additionalHeightRandA,
                    this.additionalHeightRandB,
                    this.startPercentage,
                    this.branchPercentage,
                    Optional.ofNullable(this.trunkThicknessSpline),
                    new TrunkConfig(this.bottomRadius, this.topRadius, this.form)
            );
        }
    }
}
