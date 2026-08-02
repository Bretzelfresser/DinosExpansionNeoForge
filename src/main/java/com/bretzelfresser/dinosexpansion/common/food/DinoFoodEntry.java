package com.bretzelfresser.dinosexpansion.common.food;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.HashMap;
import java.util.Map;

public record DinoFoodEntry(ResourceKey<EntityType<?>> dinoType, Map<ResourceKey<Item>, FoodValues> foods) {

    public record FoodValues(float hungerValue, float tamingValue, boolean tamingOnly) {
        public static final Codec<FoodValues> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("hunger_value").forGetter(FoodValues::hungerValue),
                Codec.FLOAT.fieldOf("taming_value").forGetter(FoodValues::tamingValue),
                Codec.BOOL.fieldOf("tamingOnly").forGetter(FoodValues::tamingOnly)
        ).apply(instance, FoodValues::new));

        public FoodValues(float hungerValue, float tamingValue){
            this(hungerValue, tamingValue, false);
        }
    }

    public static final Codec<DinoFoodEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(Registries.ENTITY_TYPE).fieldOf("dino_type").forGetter(DinoFoodEntry::dinoType),
            Codec.unboundedMap(ResourceKey.codec(Registries.ITEM), FoodValues.CODEC).fieldOf("foods").forGetter(DinoFoodEntry::foods)
    ).apply(instance, DinoFoodEntry::new));

    public static class Builder {
        private final ResourceKey<EntityType<?>> dinoType;
        private final Map<ResourceKey<Item>, FoodValues> foods = new HashMap<>();

        public Builder(ResourceKey<EntityType<?>> dinoType) {
            this.dinoType = dinoType;
        }

        public Builder addFood(Item item, float hunger, float taming) {
            return addFood(BuiltInRegistries.ITEM.getResourceKey(item).orElseThrow(), hunger, taming);
        }

        public Builder addFood(ResourceKey<Item> itemKey, float hunger, float taming) {
            this.foods.put(itemKey, new FoodValues(hunger, taming));
            return this;
        }

        /**
         * will add this food value and this can only be consumed while the entity is being tamed
         * @param item
         * @param hunger
         * @param taming
         * @return
         */
        public Builder addTamingFood(Item item, float hunger, float taming) {
            return addTamingFood(BuiltInRegistries.ITEM.getResourceKey(item).orElseThrow(), hunger, taming);
        }

        /**
         * will add this food, but it can only be eaten while being tamed
         * @param itemKey
         * @param hunger
         * @param taming
         * @return
         */
        public Builder addTamingFood(ResourceKey<Item> itemKey, float hunger, float taming) {
            this.foods.put(itemKey, new FoodValues(hunger, taming, true));
            return this;
        }



        public DinoFoodEntry build() {
            return new DinoFoodEntry(this.dinoType, Map.copyOf(this.foods));
        }
    }

}
