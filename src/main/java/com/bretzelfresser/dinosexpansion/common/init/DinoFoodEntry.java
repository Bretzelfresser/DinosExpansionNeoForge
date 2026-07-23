package com.bretzelfresser.dinosexpansion.common.init;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

public record DinoFoodEntry(ResourceKey<EntityType<?>> dinoType, Map<ResourceKey<Item>, FoodValues> foods) {

    public static final ResourceKey<Registry<DinoFoodEntry>> DINO_FOOD_REGISTRY_KEY = ResourceKey.createRegistryKey(
            ResourceLocation.fromNamespaceAndPath(DinosExpansion.MODID, "dino_food"));

    public record FoodValues(float hungerValue, float tamingValue) {
        public static final Codec<FoodValues> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("hunger_value").forGetter(FoodValues::hungerValue),
                Codec.FLOAT.fieldOf("taming_value").forGetter(FoodValues::tamingValue)
        ).apply(instance, FoodValues::new));
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

        public Builder addFood(ResourceKey<Item> itemKey, float hunger, float taming) {
            this.foods.put(itemKey, new FoodValues(hunger, taming));
            return this;
        }

        public DinoFoodEntry build() {
            return new DinoFoodEntry(this.dinoType, Map.copyOf(this.foods));
        }
    }

    // Bootstrap method for registrations
    public static void bootstrap(BootstrapContext<DinoFoodEntry> context) {
        context.register(
                ResourceKey.create(DINO_FOOD_REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath(DinosExpansion.MODID, "test_dino")),
                new Builder(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(DinosExpansion.MODID, "test_dino")))
                        .addFood(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("minecraft", "raw_beef")), 50.0F, 0.05F)
                        .addFood(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("minecraft", "cooked_beef")), 80.0F, 0.10F)
                        .build()
        );
    }
}
