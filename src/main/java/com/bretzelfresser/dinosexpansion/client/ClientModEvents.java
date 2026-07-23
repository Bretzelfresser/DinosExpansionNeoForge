package com.bretzelfresser.dinosexpansion.client;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.common.init.ModBiomes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = DinosExpansion.MODID, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.level != null) {
            if (mc.level.getBiome(mc.player.blockPosition()).is(ModBiomes.FOGGY_SWAMP)) {
                // Shrink fog to create a dense effect
                if (event.getMode() == FogRenderer.FogMode.FOG_TERRAIN) {
                    event.setNearPlaneDistance(0f); // Starts right at the camera
                    event.setFarPlaneDistance(15f); // Ends very close, making it thick
                    event.setCanceled(true); // Cancel default fog rendering
                }
            }
        }
    }
}
