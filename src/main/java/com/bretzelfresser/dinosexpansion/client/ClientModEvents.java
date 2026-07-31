package com.bretzelfresser.dinosexpansion.client;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.DinosExpansionClient;
import com.bretzelfresser.dinosexpansion.client.key.ModKeyBindings;
import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import com.bretzelfresser.dinosexpansion.common.init.ModBiomes;
import com.bretzelfresser.dinosexpansion.common.network.OpenDinoInventoryPayload;
import com.bretzelfresser.dinosexpansion.common.network.PlayerTriggerAttackPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.network.PacketDistributor;

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

    private static long lastClickTimeStamp = 0;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getVehicle() instanceof BaseDinoEntity dino) {
            assert mc.level != null;
            if (mc.level.getGameTime() - lastClickTimeStamp > 20) {
                boolean wasClicked = false;
                while (ModKeyBindings.DINO_INVENTORY_KEY.consumeClick()) {
                    wasClicked = true;
                }
                //in case we spam it we ony execute it once
                if (wasClicked) {
                    lastClickTimeStamp = mc.level.getGameTime();
                    PacketDistributor.sendToServer(new OpenDinoInventoryPayload(dino.getId()));
                }
            }

            boolean wasAttackClicked = false;
            while (ModKeyBindings.DINO_ATTACK_KEY.consumeClick()) {
                wasAttackClicked = true;
            }
            if (wasAttackClicked) {
                PacketDistributor.sendToServer(new PlayerTriggerAttackPayload(dino.getId()));
            }
        }
    }
}
