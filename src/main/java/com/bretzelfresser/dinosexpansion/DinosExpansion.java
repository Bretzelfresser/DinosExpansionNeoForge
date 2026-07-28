package com.bretzelfresser.dinosexpansion;

import com.bretzelfresser.dinosexpansion.common.chest.DinoChestEntry;
import com.bretzelfresser.dinosexpansion.common.command.KnockoutCommand;
import com.bretzelfresser.dinosexpansion.common.command.TameCommand;
import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import com.bretzelfresser.dinosexpansion.common.network.DinoLevelUpPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import com.bretzelfresser.dinosexpansion.common.food.DinoFoodEntry;
import com.bretzelfresser.dinosexpansion.common.init.*;
import com.bretzelfresser.dinosexpansion.config.Config;
import com.bretzelfresser.dinosexpansion.datagen.ModDatagen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(DinosExpansion.MODID)
public class DinosExpansion {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "dinosexpansion";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();


    public static ResourceLocation modLoc(String name){
        return ResourceLocation.fromNamespaceAndPath(MODID, name);
    }

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public DinosExpansion(IEventBus modEventBus, ModContainer modContainer) {
        // Register custom tree placers
        ModTreePlacers.register(modEventBus);
        // Register custom structures
        ModStructures.register(modEventBus);
        ModFeatures.FEATURE_CONFIGS.register(modEventBus);


        // Register our custom items
        ModItems.ITEMS.register(modEventBus);
        // Register custom creative tabs
        ModCreativeModeTabs.register(modEventBus);
        // Register custom attributes
        ModAttributes.ATTRIBUTES.register(modEventBus);
        // Register custom entities
        ModEntities.ENTITIES.register(modEventBus);
        // Register custom data components
        ModDataComponents.DATA_COMPONENTS.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModEnchantmentEffectComponents.ENCHANTMENT_COMPONENT_TYPES.register(modEventBus);

        ModMemoryModules.MEMORY_MODULE_TYPES.register(modEventBus);
        ModActivities.ACTIVITES.register(modEventBus);
        ModParticles.PARTICLE_TYPE.register(modEventBus);

        // Register entity attributes event listener
        modEventBus.addListener(this::registerAttributes);
        // Register default component modifier event listener
        modEventBus.addListener(this::modifyDefaultComponents);
        // Register custom datapack registry
        modEventBus.addListener(this::registerDatapackRegistries);

        // Register data generators


        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (DinosExpansion) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
        modEventBus.addListener(ModDatagen::gatherData);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        modEventBus.addListener(this::registerPayloads);
    }

    private void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(MODID).versioned("1.0");
        registrar.playToServer(
                DinoLevelUpPayload.TYPE,
                DinoLevelUpPayload.STREAM_CODEC,
                DinoLevelUpPayload::handle
        );
    }

    private void registerCommands(RegisterCommandsEvent event) {
        KnockoutCommand.register(event.getDispatcher());
        TameCommand.register(event.getDispatcher());
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.CERATOSAURS.get(), BaseDinoEntity.createDinoDefaultAttributes().build());
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("Dinos Expansion started on Server");
    }

    private void modifyDefaultComponents(ModifyDefaultComponentsEvent event) {
        event.modify(Items.ROTTEN_FLESH, builder -> builder.set(ModDataComponents.NARCOTIC_VALUE.get(), 10.0F));
        event.modify(Items.PUFFERFISH, builder -> builder.set(ModDataComponents.NARCOTIC_VALUE.get(), 30.0F));
        event.modify(Items.SPIDER_EYE, builder -> builder.set(ModDataComponents.NARCOTIC_VALUE.get(), 15.0F));
    }

    private void registerDatapackRegistries(net.neoforged.neoforge.registries.DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(DinoFoods.DINO_FOOD_REGISTRY_KEY, DinoFoodEntry.CODEC, DinoFoodEntry.CODEC);
        event.dataPackRegistry(DinoChests.DINO_CHEST_REGISTRY_KEY, DinoChestEntry.CODEC, DinoChestEntry.CODEC);
    }
}
