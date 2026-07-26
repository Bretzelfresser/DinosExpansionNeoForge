package com.bretzelfresser.dinosexpansion.common.init;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.common.food.DinoFoodEntry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

public class DinoFoods {
    public static final ResourceKey<Registry<DinoFoodEntry>> DINO_FOOD_REGISTRY_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(DinosExpansion.MODID, "dino_food"));


    public static final ResourceKey<DinoFoodEntry> CERATOSDAURUS_FOOD = create("ceratosaurus_food");


    public static ResourceKey<DinoFoodEntry> create(String name) {
        return ResourceKey.create(DINO_FOOD_REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath(DinosExpansion.MODID, name));
    }

    public static void bootstrap(BootstrapContext<DinoFoodEntry> context) {


        context.register(CERATOSDAURUS_FOOD, new DinoFoodEntry.Builder(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(DinosExpansion.MODID, "test_dino")))
                .addFood(Items.BEEF, 10.0F, 0.05F)
                .addFood(Items.COOKED_BEEF, 15.0F, 0.10F)
                .build()
        );
    }
}
