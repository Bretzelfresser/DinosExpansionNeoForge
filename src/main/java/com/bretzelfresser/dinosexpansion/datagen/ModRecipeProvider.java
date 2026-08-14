package com.bretzelfresser.dinosexpansion.datagen;

import com.bretzelfresser.dinosexpansion.common.init.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, ModItems.TRANQUILIZER_ARROW.get())
                .requires(Items.ARROW)
                .requires(ModItems.NARCOTICS.get())
                .unlockedBy("has_narcotics", has(ModItems.NARCOTICS.get()))
                .save(recipeOutput);

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.RAW_DIMORPHODON.get()), RecipeCategory.FOOD, ModItems.COOKED_DIMORPHODON.get(), 0.35F, 200)
                .unlockedBy("has_raw_dimorphodon", has(ModItems.RAW_DIMORPHODON.get()))
                .save(recipeOutput, "cooked_dimorphodon_from_smelting");

        SimpleCookingRecipeBuilder.smoking(Ingredient.of(ModItems.RAW_DIMORPHODON.get()), RecipeCategory.FOOD, ModItems.COOKED_DIMORPHODON.get(), 0.35F, 100)
                .unlockedBy("has_raw_dimorphodon", has(ModItems.RAW_DIMORPHODON.get()))
                .save(recipeOutput, "cooked_dimorphodon_from_smoking");

        SimpleCookingRecipeBuilder.campfireCooking(Ingredient.of(ModItems.RAW_DIMORPHODON.get()), RecipeCategory.FOOD, ModItems.COOKED_DIMORPHODON.get(), 0.35F, 600)
                .unlockedBy("has_raw_dimorphodon", has(ModItems.RAW_DIMORPHODON.get()))
                .save(recipeOutput, "cooked_dimorphodon_from_campfire_cooking");
    }
}
