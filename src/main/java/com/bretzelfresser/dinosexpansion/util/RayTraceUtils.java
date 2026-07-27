package com.bretzelfresser.dinosexpansion.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

public class RayTraceUtils {

    /**
     * Raycasts entities in the look direction of the tracer.
     *
     * @param tracer  the entity whose eye position and direction we start raycasting from.
     * @param maxDist the maximum distance to trace.
     * @param filter  additional filter for target entities.
     * @return the HitResult representing the entity that was hit, or null.
     */
    public static EntityHitResult rayTraceEntities(Entity tracer, float maxDist, Predicate<Entity> filter) {
        Vec3 position = tracer.getEyePosition();
        Vec3 viewVector = tracer.getViewVector(1.0F);
        Vec3 targetPos = position.add(viewVector.x * maxDist, viewVector.y * maxDist, viewVector.z * maxDist);
        AABB searchBox = tracer.getBoundingBox().expandTowards(viewVector.scale(maxDist)).inflate(1.0D, 1.0D, 1.0D);
        
        Predicate<Entity> finalFilter = (entity) -> !entity.isSpectator() && entity.isPickable();
        if (filter != null) {
            finalFilter = finalFilter.and(filter);
        }
        
        return ProjectileUtil.getEntityHitResult(tracer.level(), tracer, position, targetPos, searchBox, finalFilter);
    }
}
