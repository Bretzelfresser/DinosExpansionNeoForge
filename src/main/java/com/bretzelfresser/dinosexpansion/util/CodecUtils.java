package com.bretzelfresser.dinosexpansion.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.CubicSpline;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class CodecUtils {


    public static final Codec<ToFloatFunction<Float>> IDENTITY_CODEC = Codec.unit(ToFloatFunction.IDENTITY);

    public static final Codec<CubicSpline<Float, ToFloatFunction<Float>>> FLOAT_SPLINE_CODEC = CubicSpline.codec(IDENTITY_CODEC);


    public static <T> Codec<HolderSet<T>> makeBetterListCodec(Codec<Holder<T>> elementCodec, Codec<HolderSet<T>> listCodec) {
        return Codec.either(elementCodec, listCodec).xmap(
                // De-serialize: Convert Either<Holder<T>, HolderSet<T>> into HolderSet<T>
                either -> either.map(HolderSet::direct, list -> list),

                // Serialize: Convert HolderSet<T> into Either<Holder<T>, HolderSet<T>>
                holderSet -> {
                    if (holderSet.size() == 1) {
                        return Either.left(holderSet.get(0));
                    }
                    return Either.right(holderSet);
                }
        );
    }
}
