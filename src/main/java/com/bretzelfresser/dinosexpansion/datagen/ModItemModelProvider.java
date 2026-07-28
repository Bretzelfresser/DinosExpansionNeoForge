package com.bretzelfresser.dinosexpansion.datagen;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.common.init.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, DinosExpansion.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        //basicItem(ModItems.TEST_DINO_SADDLE.get());
        basicItem(ModItems.NARCOTICS.get());
        basicItem(ModItems.TRANQUILIZER_ARROW.get());
        basicItem(ModItems.BASIC_KIBBLE.get());
        basicItem(ModItems.SIMPLE_KIBBLE.get());
        basicItem(ModItems.REGULAR_KIBBLE.get());
        basicItem(ModItems.SUPERIOR_KIBBLE.get());
        basicItem(ModItems.EXCEPTIONAL_KIBBLE.get());
        basicItem(ModItems.EXTRAORDINARY_KIBBLE.get());
    }
}
