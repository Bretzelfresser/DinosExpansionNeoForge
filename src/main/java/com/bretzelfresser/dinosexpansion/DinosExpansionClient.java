package com.bretzelfresser.dinosexpansion;

import com.bretzelfresser.dinosexpansion.client.gui.DinoScreen;
import com.bretzelfresser.dinosexpansion.client.particle.SleepingParticle;
import com.bretzelfresser.dinosexpansion.client.renderer.CertosaurusRenderer;
import com.bretzelfresser.dinosexpansion.common.entity.TranquilizerArrow;
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
    static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.DINO_MENU.get(), DinoScreen::new);
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.CERATOSAURS.get(), CertosaurusRenderer::new);
        event.registerEntityRenderer(
            ModEntities.TRANQUILIZER_ARROW.get(),
            manager -> new ArrowRenderer<>(manager) {
                @Override
                public ResourceLocation getTextureLocation(TranquilizerArrow entity) {
                    return ResourceLocation.fromNamespaceAndPath(DinosExpansion.MODID, "textures/entity/arrow/tranquilizer_arrow.png");
                }
            }
        );
    }

    @SubscribeEvent
    static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.SLEEPING_PARTICLES.get(), SleepingParticle.Factory::new);
    }
}
