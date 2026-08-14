package com.bretzelfresser.dinosexpansion.common.init;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.common.food.DinoFoodEntry;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import net.neoforged.neoforge.registries.DeferredItem;
import java.util.List;

public class DinoFoods {
    public static final ResourceKey<Registry<DinoFoodEntry>> DINO_FOOD_REGISTRY_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(DinosExpansion.MODID, "dino_food"));

    public static final List<ResourceKey<Item>> KIBBLE_TIERS = List.of(
            ModItems.BASIC_KIBBLE.getKey(),
            ModItems.SIMPLE_KIBBLE.getKey(),
            ModItems.REGULAR_KIBBLE.getKey(),
            ModItems.SUPERIOR_KIBBLE.getKey(),
            ModItems.EXCEPTIONAL_KIBBLE.getKey(),
            ModItems.EXTRAORDINARY_KIBBLE.getKey()
    );

    public static final ResourceKey<DinoFoodEntry> CERATOSDAURUS_FOOD = create("ceratosaurus_food");
    public static final ResourceKey<DinoFoodEntry> DIMORPHODON_FOOD = create("dimorphodon_food");


    public static ResourceKey<DinoFoodEntry> create(String name) {
        return ResourceKey.create(DINO_FOOD_REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath(DinosExpansion.MODID, name));
    }

    public static DinoFoodEntry.Builder addKibble(DinoFoodEntry.Builder builder, Holder<Item> preferredKibble) {
        return addKibble(builder, preferredKibble.getKey(), 10.0F, 0.40F);
    }

    public static DinoFoodEntry.Builder addKibble(DinoFoodEntry.Builder builder, Holder<Item> preferredKibble, float hungerValue, float tamingValue) {
        return addKibble(builder, preferredKibble.getKey(), hungerValue, tamingValue);
    }

    public static DinoFoodEntry.Builder addKibble(DinoFoodEntry.Builder builder, ResourceKey<Item> preferredKibbleKey) {
        return addKibble(builder, preferredKibbleKey, 10.0F, 0.40F);
    }

    public static DinoFoodEntry.Builder addKibble(DinoFoodEntry.Builder builder, ResourceKey<Item> preferredKibbleKey, float hungerValue, float tamingValue) {
        int index = KIBBLE_TIERS.indexOf(preferredKibbleKey);
        if (index == -1) {
            builder.addTamingFood(preferredKibbleKey, hungerValue, tamingValue);
            return builder;
        }
        for (int i = index; i < KIBBLE_TIERS.size(); i++) {
            builder.addTamingFood(KIBBLE_TIERS.get(i), hungerValue, tamingValue);
        }
        return builder;
    }

    public static void bootstrap(BootstrapContext<DinoFoodEntry> context) {
        context.register(CERATOSDAURUS_FOOD, addKibble(new DinoFoodEntry.Builder(ModEntities.CERATOSAURS.getKey())
                .addFood(Items.BEEF, 10.0F, 0.05F)
                .addFood(Items.COOKED_BEEF, 15.0F, 0.10F), ModItems.SUPERIOR_KIBBLE)
                .build()
        );
        context.register(DIMORPHODON_FOOD, addKibble(new DinoFoodEntry.Builder(ModEntities.DIMORPHODON.getKey())
                .addFood(Items.COD, 5.0F, 0.05F)
                .addFood(Items.COOKED_COD, 10.0F, 0.10F)
                .addFood(Items.SALMON, 5.0F, 0.05F)
                .addFood(Items.COOKED_SALMON, 10.0F, 0.10F)
                .addFood(Items.RABBIT, 4.0F, 0.04F)
                .addFood(Items.COOKED_RABBIT, 8.0F, 0.08F), ModItems.SIMPLE_KIBBLE)
                .build()
        );
    }
}
