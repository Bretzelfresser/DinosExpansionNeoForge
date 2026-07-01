package com.bretzelfresser.dinosexpansion.util;

import com.mojang.serialization.Codec;
import net.minecraft.util.CubicSpline;
import net.minecraft.util.ToFloatFunction;

public class CodecUtils {


    public static final Codec<ToFloatFunction<Float>> IDENTITY_CODEC = Codec.unit(ToFloatFunction.IDENTITY);

    public static final Codec<CubicSpline<Float, ToFloatFunction<Float>>> FLOAT_SPLINE_CODEC = CubicSpline.codec(IDENTITY_CODEC);
}
