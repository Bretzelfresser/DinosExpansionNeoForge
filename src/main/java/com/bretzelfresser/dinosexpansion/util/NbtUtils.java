package com.bretzelfresser.dinosexpansion.util;

import net.minecraft.nbt.CompoundTag;
import org.apache.commons.lang3.function.TriConsumer;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class NbtUtils {

    /**
     * this will  get the value from the nbt and set it to something but only if that value exists to avoid setting values to 0
     * @param nbt the nbt we want it to read from
     * @param name the name of the value
     * @param valueGetter the value getter normally a lambda like <i>CompoundNBT::getInt</i>
     * @param valueSetter this is the setter which will be given the value from the getter and then should set some value
     * @param <T> the type of the value
     */
    public static <T> void setIfExists(CompoundTag nbt, String name, BiFunction<CompoundTag, String, T> valueGetter, Consumer<T> valueSetter){
        if (nbt.contains(name)){
            T value = valueGetter.apply(nbt, name);
            valueSetter.accept(value);
        }
    }

    /**
     *
     * @param tag the tag we want to save it to
     * @param name the name of what the tag will be saved to
     * @param valueSetter the CompoundTag function where we want to set the value to
     * @param value the actual optional which will hold the value and will be written if present
     * @param <T> the type of optional we want to write
     */
    public static <T> void putIfPresent(CompoundTag tag, String name, TriConsumer<CompoundTag, String, T> valueSetter, Optional<T> value){
        value.ifPresent(t -> valueSetter.accept(tag, name, t));
    }


}
