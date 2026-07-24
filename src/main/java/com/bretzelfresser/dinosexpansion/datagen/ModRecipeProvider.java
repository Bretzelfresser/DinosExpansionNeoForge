package com.bretzelfresser.dinosexpansion.datagen;

import com.bretzelfresser.dinosexpansion.common.init.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Items;

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
    }
}
