package com.bretzelfresser.dinosexpansion;

import com.bretzelfresser.dinosexpansion.client.gui.DinoScreen;
import com.bretzelfresser.dinosexpansion.client.key.ModKeyBindings;
import com.bretzelfresser.dinosexpansion.client.particle.SleepingParticle;
import com.bretzelfresser.dinosexpansion.client.renderer.CertosaurusRenderer;
import com.bretzelfresser.dinosexpansion.common.entity.misc.TranquilizerArrow;
import com.bretzelfresser.dinosexpansion.common.init.ModEntities;
import com.bretzelfresser.dinosexpansion.common.init.ModMenus;
import com.bretzelfresser.dinosexpansion.common.init.ModParticles;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import com.bretzelfresser.dinosexpansion.client.gui.spyglass.SpyglassScannerOverlay;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

import java.lang.reflect.Modifier;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = DinosExpansion.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = DinosExpansion.MODID, value = Dist.CLIENT)
public class DinosExpansionClient {


    public DinosExpansionClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {

    }

    @SubscribeEvent
    static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        for (var field : ModKeyBindings.class.getFields()){
            if (Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers()) && KeyMapping.class.isAssignableFrom(field.getType())) {
                // 2. Retrieve the value (pass null since the field is static)
                try {
                    KeyMapping keyMapping = (KeyMapping) field.get(null);
                    if (keyMapping != null) {
                        event.register(keyMapping);
                    }else {
                        DinosExpansion.LOGGER.error("for some reason the key Mapping: {} was Null when trying to access it and convert it into a key Mapping", field.getName());
                    }
                } catch (IllegalAccessException e) {
                    DinosExpansion.LOGGER.error("cant access Key Mapping: {} for registration", field.getName());
                }


            }
        }


    }

    @SubscribeEvent
    static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.DINO_MENU.get(), DinoScreen::new);
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.CERATOSAURS.get(), CertosaurusRenderer::new);
        event.registerEntityRenderer(ModEntities.TRANQUILIZER_ARROW.get(),
                manager -> new ArrowRenderer<>(manager) {
                    @Override
                    public ResourceLocation getTextureLocation(TranquilizerArrow entity) {
                        return DinosExpansion.modLoc("textures/entity/arrow/tranquilizer_arrow.png");
                    }
                }
        );
    }

    @SubscribeEvent
    static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.SLEEPING_PARTICLES.get(), SleepingParticle.Factory::new);
    }

    @SubscribeEvent
    static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(DinosExpansion.modLoc("spyglass_scanner"), new SpyglassScannerOverlay());
    }
}
