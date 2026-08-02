package com.bretzelfresser.dinosexpansion.datagen;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.client.gui.spyglass.DinoStatTypes;
import com.bretzelfresser.dinosexpansion.client.key.ModKeyBindings;
import com.bretzelfresser.dinosexpansion.common.entity.base.DinoGender;
import com.bretzelfresser.dinosexpansion.common.init.ModAttributes;
import com.bretzelfresser.dinosexpansion.common.init.ModEnchantments;
import com.bretzelfresser.dinosexpansion.common.init.ModEntities;
import com.bretzelfresser.dinosexpansion.common.init.ModItems;
import com.bretzelfresser.dinosexpansion.config.Config;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.data.LanguageProvider;

import javax.swing.text.JTextComponent;

public class ModLanguageProvider extends LanguageProvider {
    public ModLanguageProvider(PackOutput output) {
        super(output, DinosExpansion.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {

        items();
        entities();
        attributes();

        // Creative Tabs
        add("itemGroup.dinosexpansion", "Dinos Expansion");

        enchantments();


        tooltips();

        add(DinoGender.MALE, "Male");
        add(DinoGender.FEMALE, "Female");

        configs();

        dinoStatTypes();

        generica();
        keys();


    }

    protected void keys() {
        add(ModKeyBindings.DINO_INVENTORY_KEY, "Open Dinosaur Inventory");
        add(ModKeyBindings.DINO_ATTACK_KEY, "Dinosaur Attack");
        add(ModKeyBindings.DINO_KEY_CATEGORY, "Dinos Expansion");
    }

    protected void items() {
        add(ModItems.TEST_DINO_SADDLE.get(), "Test Dino Saddle");
        add(ModItems.NARCOTICS.get(), "Narcotics");
        add(ModItems.TRANQUILIZER_ARROW.get(), "Tranquilizer Arrow");
        add(ModItems.SPYGLASS.get(), "Spyglass");
        add(ModItems.ADVANCED_SPYGLASS.get(), "Advanced Spyglass");
        add(ModItems.BASIC_KIBBLE.get(), "Basic Kibble");
        add(ModItems.SIMPLE_KIBBLE.get(), "Simple Kibble");
        add(ModItems.REGULAR_KIBBLE.get(), "Regular Kibble");
        add(ModItems.SUPERIOR_KIBBLE.get(), "Superior Kibble");
        add(ModItems.EXCEPTIONAL_KIBBLE.get(), "Exceptional Kibble");
        add(ModItems.EXTRAORDINARY_KIBBLE.get(), "Extraordinary Kibble");
    }

    protected void entities() {
        add(ModEntities.CERATOSAURS.get(), "Ceratosaurus");
    }

    protected void generica() {
        add("generic." + DinosExpansion.MODID + ".wild", "Wild");
        add("generic." + DinosExpansion.MODID + ".you", "You");
        add("generic." + DinosExpansion.MODID + ".tamed", "Tamed");
    }

    protected void tooltips() {
        add("tooltip." + DinosExpansion.MODID + ".narcotic_value", "Narcotic Value: +%s Torpor");
    }

    protected void enchantments() {
        addEnchantment(ModEnchantments.TORPOR_ENCHANTMENT, "Torpor");
    }

    protected void dinoStatTypes() {
        add("spyglass." + DinosExpansion.MODID + ".stat.race", "Race: %s");
        add("stat_type." + DinosExpansion.MODID + ".stat.bar", "%s: %s/%s");
        add("stat_type." + DinosExpansion.MODID + ".stat.label", "%s: %s");


        add(DinoStatTypes.GENDER, "Gender");
        add(DinoStatTypes.CARRYING_CAPACITY, "Carrying Capacity");
        add(DinoStatTypes.HEALTH, "Health");
        add(DinoStatTypes.LEVEL, "Level");
        add(DinoStatTypes.OWNER, "Owner");
        add(DinoStatTypes.TORPOR, "Torpor");
        add(DinoStatTypes.HUNGER, "Hunger");
        add(DinoStatTypes.TAMING_PROGRESS, "Tamed");
    }

    protected void attributes() {
        addAttribute(ModAttributes.MAX_TORPOR, "Max Torpor");
        addAttribute(ModAttributes.MAX_HUNGER, "Max Hunger");
        addAttribute(ModAttributes.CARRYING_CAPACITY, "Carrying Capacity");
        addAttribute(ModAttributes.TORPOR_WAKE_UP_THRESHOLD, "Wake Up Threshold");
        addAttribute(ModAttributes.NATURAL_REGENERATION, "Natural Regeneration");
    }

    protected void configs() {
        addConfigValue("Torpor", "Torpor Configurations");
        addConfigValue(Config.TORPOR_CONFIG.DAMAGE_REDUCTION, "Torpor Damage Reduction");
        addConfigValue(Config.TORPOR_CONFIG.DAMAGE_SCALING, "Torpor Damage Scaling");
        addConfigValue(Config.TORPOR_CONFIG.OVER_MAX_LIMIT_EFFECTIVENESS, "Over Max Limit Effectiveness");
        addConfigValue("Dinosaurs", "Dinosaur Entity Configurations");

        addConfigValue(Config.DINOSAUR_CONFIG.PERCENTAGE_BUFFERED_TORPOR_REDUCTION, "Percentage Buffered Torpor Reduction");
        addConfigValue(Config.DINOSAUR_CONFIG.FLAT_BUFFERED_TORPOR_REDUCTION, "Flat Buffered Torpor Reduction");
        addConfigValue(Config.DINOSAUR_CONFIG.MIN_LEVEL, "Spawn Minimum Level");
        addConfigValue(Config.DINOSAUR_CONFIG.MAX_LEVEL, "Spawn Maximum Level");
        addConfigValue(Config.DINOSAUR_CONFIG.AVERAGE_LEVEL, "Spawn Average Level");
        addConfigValue(Config.DINOSAUR_CONFIG.NATURAL_REGENERATION_HUNGER_THRESHOLD, "Natural Regeneration Hunger Threshold");
    }

    protected void chatMessages() {
        add("chat." + DinosExpansion.MODID + ".dino_access_denied", "You do not have access to this Dinosaur");
    }

    public void add(KeyMapping mapping, String name){
        add(mapping.getName(), name);
    }

    public void addEnchantment(ResourceKey<Enchantment> enchantment, String translation) {
        addEnchantment(enchantment.location(), translation);
    }

    public void addEnchantment(ResourceLocation enchantment, String translation) {
        add(Util.makeDescriptionId("enchantment", enchantment), translation);
    }

    public void addAttribute(Holder<Attribute> attribute, String translation) {
        addAttribute(attribute.value(), translation);
    }

    public void addAttribute(Attribute attribute, String translation) {
        add(attribute.getDescriptionId(), translation);
    }

    public void add(DinoGender gender, String translation) {
        add(gender.getTranslationKey(), translation);
    }

    public void add(DinoStatTypes statType, String translation) {
        add(statType.getLabelTranslationKey(), translation);
    }

    protected void addConfigValue(ModConfigSpec.ConfigValue<?> configKey, String translation) {
        addConfigValue(configKey.getPath().get(configKey.getPath().size() - 1), translation);
    }

    protected void addConfigValue(String configKey, String translation) {
        add(DinosExpansion.MODID + ".configuration." + configKey, translation);
    }
}
