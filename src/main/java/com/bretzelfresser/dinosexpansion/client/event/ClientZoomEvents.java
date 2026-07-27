package com.bretzelfresser.dinosexpansion.client.event;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.client.util.ClientZoomState;
import com.bretzelfresser.dinosexpansion.common.item.ZoomItem;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.InputEvent;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = DinosExpansion.MODID, value = Dist.CLIENT)
public class ClientZoomEvents {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        ItemStack useItem = mc.player.getUseItem();
        boolean isUsingZoomItem = mc.player.isUsingItem() && useItem.getItem() instanceof ZoomItem;

        if (isUsingZoomItem) {
            ZoomItem zoomItem = (ZoomItem) useItem.getItem();
            if (!ClientZoomState.isZooming()) {
                // Smooth camera zoom
                mc.options.smoothCamera = true;
                ClientZoomState.startZoom(mc.options.getCameraType(), zoomItem.getDefaultZoom());
                if (mc.options.getCameraType() != CameraType.FIRST_PERSON) {
                    mc.options.setCameraType(CameraType.FIRST_PERSON);
                }
            }
        } else if (ClientZoomState.isZooming()) {
            ClientZoomState.stopZoom(mc);
        }
    }

    @SubscribeEvent
    public static void changeFov(ComputeFovModifierEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        if (mc.player.isUsingItem() && mc.player.getUseItem().getItem() instanceof ZoomItem) {
            event.setNewFovModifier(ClientZoomState.getCurrentZoom());
        }
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        if (mc.player.isUsingItem() && mc.player.getUseItem().getItem() instanceof ZoomItem zoomItem) {
            if (zoomItem.canScrollZoom()) {
                ClientZoomState.adjustZoom(event.getScrollDeltaY());
                event.setCanceled(true);
            }
        }
    }
}
