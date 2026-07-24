package com.bretzelfresser.dinosexpansion.datagen;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.common.food.DinoFoodEntry;
import com.bretzelfresser.dinosexpansion.common.init.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ModDatagen {
    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.DIMENSION_TYPE, ModDimensionTypes::bootstrap)
            .add(Registries.NOISE_SETTINGS, ModNoiseGeneratorSettings::bootstrap)
            .add(Registries.LEVEL_STEM, ModLevelStems::bootstrap)
            .add(Registries.DENSITY_FUNCTION, ModDensityFunctions::bootstrap)
            .add(Registries.NOISE, ModNoiseParameters::bootstrap)
            .add(Registries.BIOME, ModBiomes::bootstrap)
            .add(Registries.PLACED_FEATURE, ModPlacedFeatures::generate)
            .add(Registries.CONFIGURED_FEATURE, ModConfiguredFeatures::generate)
            .add(Registries.STRUCTURE, ModStructures::bootstrapStructures)
            .add(Registries.STRUCTURE_SET, ModStructures::bootstrapStructureSets)
            .add(Registries.ENCHANTMENT, ModEnchantments::bootstrap)
            .add(DinoFoods.DINO_FOOD_REGISTRY_KEY, DinoFoods::bootstrap);

    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(
                event.includeServer(),
                new DatapackBuiltinEntriesProvider(packOutput, lookupProvider, BUILDER, Set.of(DinosExpansion.MODID))
        );

        generator.addProvider(
                event.includeServer(),
                new ModBiomeTagsProvider(packOutput, lookupProvider, event.getExistingFileHelper())
        );

        generator.addProvider(
                event.includeServer(),
                new ModRecipeProvider(packOutput, lookupProvider)
        );

        var blockTags = generator.addProvider(
                event.includeServer(),
                new ModBlockTagsProvider(packOutput, lookupProvider, event.getExistingFileHelper())
        );
        generator.addProvider(
                event.includeServer(),
                new ModItemTagsProvider(packOutput, lookupProvider, blockTags.contentsGetter(), event.getExistingFileHelper())
        );

        generator.addProvider(event.includeClient(), new ModItemModelProvider(packOutput, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new ModLanguageProvider(packOutput));
    }
}
