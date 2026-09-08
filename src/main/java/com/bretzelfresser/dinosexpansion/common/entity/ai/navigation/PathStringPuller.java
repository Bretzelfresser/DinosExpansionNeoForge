package com.bretzelfresser.dinosexpansion.common.entity.ai.navigation;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class PathStringPuller {

    /**
     * Performs string-pulling (line-of-sight pruning) on a list of 3D path waypoints.
     * Iteratively removes intermediate waypoints if direct line-of-sight is unobstructed.
     */
    public static List<Vec3> stringPull(Level level, Entity entity, List<Vec3> points) {
        if (points == null || points.size() <= 2) {
            return points != null ? new ArrayList<>(points) : new ArrayList<>();
        }

        List<Vec3> pruned = new ArrayList<>();
        pruned.add(points.get(0));

        int currentIndex = 0;
        int n = points.size();

        while (currentIndex < n - 1) {
            int furthestIndex = currentIndex + 1;

            // Greedily find furthest reachable node with clear line of sight
            for (int candidate = n - 1; candidate > currentIndex + 1; candidate--) {
                if (hasLineOfSight(level, entity, points.get(currentIndex), points.get(candidate))) {
                    furthestIndex = candidate;
                    break;
                }
            }

            pruned.add(points.get(furthestIndex));
            currentIndex = furthestIndex;
        }

        return pruned;
    }

    /**
     * Checks if a direct 3D raycast between start and end is free of block collisions,
     * including bounding box width checks for entity clearance.
     */
    public static boolean hasLineOfSight(Level level, Entity entity, Vec3 start, Vec3 end) {
        // Main center ray
        if (isBlockInPath(level, entity, start, end)) {
            return false;
        }

        // Bounding box offset checks to ensure clearance for dino width
        double radius = entity.getBbWidth() * 0.4D;
        if (radius > 0.1D) {
            Vec3 dir = end.subtract(start);
            double len = dir.length();
            if (len > 0.001D) {
                dir = dir.scale(1.0D / len);
                Vec3 perp = new Vec3(-dir.z, 0, dir.x).normalize().scale(radius);
                Vec3 up = new Vec3(0, entity.getBbHeight() * 0.4D, 0);

                if (isBlockInPath(level, entity, start.add(perp), end.add(perp))) return false;
                if (isBlockInPath(level, entity, start.subtract(perp), end.subtract(perp))) return false;
                if (isBlockInPath(level, entity, start.add(up), end.add(up))) return false;
            }
        }

        return true;
    }

    private static boolean isBlockInPath(Level level, Entity entity, Vec3 start, Vec3 end) {
        ClipContext context = new ClipContext(
                start, end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                entity
        );
        return level.clip(context).getType() != HitResult.Type.MISS;
    }
}
