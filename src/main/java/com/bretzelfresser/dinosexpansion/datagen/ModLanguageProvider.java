package com.bretzelfresser.dinosexpansion.datagen;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.common.init.ModEntities;
import com.bretzelfresser.dinosexpansion.common.init.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {
    public ModLanguageProvider(PackOutput output) {
        super(output, DinosExpansion.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        // Items
        add(ModItems.TEST_DINO_SADDLE.get(), "Test Dino Saddle");
        add(ModItems.NARCOTICS.get(), "Narcotics");

        // Entities
        add(ModEntities.TEST_DINO.get(), "Test Dinosaur");

        // Attributes
        add("attribute.name.dinosexpansion.max_torpor", "Max Torpor");
        add("attribute.name.dinosexpansion.max_hunger", "Max Hunger");

        // Creative Tabs
        add("itemGroup.dinosexpansion", "Dinos Expansion");

        // Custom Tooltips
        add("dinosexpansion.narcotic_value", "Narcotic Value: +%s Torpor");

        // Inherited Existing Manual Translations
        add("block.dinosexpansion.example_block", "Example Block");
        add("item.dinosexpansion.example_item", "Example Item");
        add("dinosexpansion.configuration.title", "Dinos Expansion Configs");
        add("dinosexpansion.configuration.section.dinosexpansion.common.toml", "Dinos Expansion Configs");
        add("dinosexpansion.configuration.section.dinosexpansion.common.toml.title", "Dinos Expansion Configs");
        add("dinosexpansion.configuration.items", "Item List");
        add("dinosexpansion.configuration.logDirtBlock", "Log Dirt Block");
        add("dinosexpansion.configuration.magicNumberIntroduction", "Magic Number Text");
        add("dinosexpansion.configuration.magicNumber", "Magic Number");
    }
}
