package com.bretzelfresser.dinosexpansion.common.entity.ai.navigation;

import com.bretzelfresser.dinosexpansion.common.entity.base.FlyingDinosaur;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SmoothFlyingPathNavigation extends FlyingPathNavigation {

    protected final FlyingDinosaur<?> dino;
    protected List<Vec3> splineWaypoints = null;
    protected int splineIndex = 0;

    public SmoothFlyingPathNavigation(FlyingDinosaur<?> dino, Level level) {
        super(dino, level);
        this.dino = dino;
    }

    @Override
    protected @Nullable Path createPath(Set<BlockPos> targets, int distance, boolean reachTarget, int maxVisitedNodes) {
        Path path = super.createPath(targets, distance, reachTarget, maxVisitedNodes);
        if (path != null && this.dino.isFlying()) {
            buildSplineFromPath(path);
        } else {
            this.splineWaypoints = null;
        }
        return path;
    }

    @Override
    public boolean moveTo(@Nullable Path path, double speed) {
        boolean success = super.moveTo(path, speed);
        if (success && path != null && this.dino.isFlying()) {
            buildSplineFromPath(path);
        } else if (!this.dino.isFlying()) {
            this.splineWaypoints = null;
        }
        return success;
    }

    @Override
    public void stop() {
        super.stop();
        this.splineWaypoints = null;
        this.splineIndex = 0;
    }

    protected void buildSplineFromPath(Path path) {
        if (path == null || path.getNodeCount() == 0) {
            this.splineWaypoints = null;
            this.splineIndex = 0;
            return;
        }

        List<Vec3> rawPoints = new ArrayList<>();
        rawPoints.add(this.dino.position());

        for (int i = path.getNextNodeIndex(); i < path.getNodeCount(); i++) {
            rawPoints.add(path.getEntityPosAtNode(this.dino, i));
        }

        if (rawPoints.size() <= 1) {
            this.splineWaypoints = rawPoints;
            this.splineIndex = 0;
            return;
        }

        // 1. Line-of-Sight Pruning (String-Pulling)
        List<Vec3> prunedKeypoints = PathStringPuller.stringPull(this.level, this.dino, rawPoints);

        // 2. Catmull-Rom Spline Interpolation
        this.splineWaypoints = CatmullRomSpline.generateSpline(prunedKeypoints, 4);
        this.splineIndex = 0;
    }

    @Override
    protected void followThePath() {
        if (this.path == null || this.path.isDone()) {
            this.splineWaypoints = null;
            return;
        }

        // When flying, follow the smooth Catmull-Rom spline waypoints
        if (this.dino.isFlying() && this.splineWaypoints != null && !this.splineWaypoints.isEmpty()) {
            if (this.splineIndex >= this.splineWaypoints.size()) {
                this.stop();
                return;
            }

            Vec3 currWay = this.splineWaypoints.get(this.splineIndex);
            this.mob.getMoveControl().setWantedPosition(currWay.x, currWay.y, currWay.z, this.speedModifier);

            double reachDistance = Math.max(1.0D, this.dino.getBbWidth() * 1.0D);
            if (this.dino.distanceToSqr(currWay) <= reachDistance * reachDistance) {
                this.splineIndex++;
                if (this.splineIndex >= this.splineWaypoints.size()) {
                    this.stop();
                }
            }
        } else {
            // Grounded mode: Fallback to standard ground node pathing (no airborne 3D splines)
            this.splineWaypoints = null;
            Vec3 currNode = this.path.getNextEntityPos(this.dino);
            this.mob.getMoveControl().setWantedPosition(currNode.x, currNode.y, currNode.z, this.speedModifier);

            double reachDistance = Math.max(1.2D, this.dino.getBbWidth() * 1.2D);
            if (this.dino.distanceToSqr(currNode) <= reachDistance * reachDistance) {
                this.path.advance();
            }
        }
    }
}
