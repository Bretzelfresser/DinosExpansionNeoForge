package com.bretzelfresser.dinosexpansion.client.util;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientZoomState {
    public static final float MIN_ZOOM = 0.1f;
    public static final float MAX_ZOOM = 1.5f;

    private static CameraType originalCameraType = null;
    private static float currentZoom = 1.0f;
    private static boolean isZooming = false;

    public static void startZoom(CameraType cameraType, float defaultZoom) {
        if (!isZooming) {
            originalCameraType = cameraType;
            isZooming = true;
        }
        currentZoom = defaultZoom;
    }

    public static void stopZoom(Minecraft mc) {
        if (isZooming) {
            if (originalCameraType != null) {
                mc.options.setCameraType(originalCameraType);
                originalCameraType = null;
            }
            mc.options.smoothCamera = false;
            isZooming = false;
        }
    }

    public static float getCurrentZoom() {
        return currentZoom;
    }

    public static void adjustZoom(double scrollDelta) {
        currentZoom *= (float) Math.pow(0.8f, scrollDelta);
        currentZoom = Mth.clamp(currentZoom, MIN_ZOOM, MAX_ZOOM);
    }

    public static boolean isZooming() {
        return isZooming;
    }
}
