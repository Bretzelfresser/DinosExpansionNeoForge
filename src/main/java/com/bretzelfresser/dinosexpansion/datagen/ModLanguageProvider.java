package com.bretzelfresser.dinosexpansion.datagen;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.common.init.ModEntities;
import com.bretzelfresser.dinosexpansion.common.init.ModItems;
import com.bretzelfresser.dinosexpansion.config.Config;
import net.minecraft.data.PackOutput;
import net.neoforged.fml.loading.FMLConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
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
        add(ModItems.TRANQUILIZER_ARROW.get(), "Tranquilizer Arrow");

        // Entities
        add(ModEntities.TEST_DINO.get(), "Test Dinosaur");

        // Attributes
        add("attribute.name.dinosexpansion.max_torpor", "Max Torpor");
        add("attribute.name.dinosexpansion.max_hunger", "Max Hunger");

        // Creative Tabs
        add("itemGroup.dinosexpansion", "Dinos Expansion");

        // Custom Tooltips
        add("dinosexpansion.narcotic_value", "Narcotic Value: +%s Torpor");
        add("enchantment.dinosexpansion.torpor_enchantment", "Torpor");


        addConfigValue("Torpor", "Torpor Configurations");
        addConfigValue(Config.TORPOR_CONFIG.DAMAGE_REDUCTION, "Torpor Damage Reduction");
        addConfigValue(Config.TORPOR_CONFIG.DAMAGE_SCALING, "Torpor Damage Scaling");
        addConfigValue("Dinosaurs", "Dinosaur Entity Configurations");

        addConfigValue(Config.DINOSAUR_CONFIG.BUFFERED_TORPOR_REDUCTION, "Buffered Torpor Reduction");
        addConfigValue(Config.DINOSAUR_CONFIG.MIN_BUFFERED_TORPOR_REDUCTION, "Min Buffered Torpor Reduction");


    }

    protected void addConfigValue(ModConfigSpec.ConfigValue<?> configKey, String translation){
        addConfigValue(configKey.getPath().get(configKey.getPath().size() - 1), translation);
    }

    protected void addConfigValue(String configKey, String translation){
        add(DinosExpansion.MODID + ".configuration." + configKey, translation);
    }
}
