package com.bretzelfresser.dinosexpansion.client.util;

import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import com.bretzelfresser.dinosexpansion.util.RayTraceUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DinoScannerCache {
    private static BaseDinoEntity targetedDino = null;
    private static long lastCheckTick = -1;

    /**
     * Gets the current dinosaur under the player's crosshair, caching the raycast calculation
     * once per game tick to ensure rendering runs smoothly without frame drops.
     */
    public static BaseDinoEntity getTargetedDinosaur(Minecraft mc, float maxDistance) {
        if (mc.level == null) return null;
        
        long currentTick = mc.level.getGameTime();
        if (currentTick != lastCheckTick) {
            lastCheckTick = currentTick;
            targetedDino = performRaycast(mc, maxDistance);
        }
        return targetedDino;
    }

    private static BaseDinoEntity performRaycast(Minecraft mc, float maxDistance) {
        if (mc.player == null) return null;
        
        EntityHitResult hit = RayTraceUtils.rayTraceEntities(mc.player, maxDistance, e -> e instanceof BaseDinoEntity);
        if (hit != null && hit.getType() == HitResult.Type.ENTITY && hit.getEntity() instanceof BaseDinoEntity dino) {
            return dino;
        }
        return null;
    }
}
