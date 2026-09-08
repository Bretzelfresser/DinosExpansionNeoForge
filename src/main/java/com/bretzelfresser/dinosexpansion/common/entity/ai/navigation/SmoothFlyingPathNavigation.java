package com.bretzelfresser.dinosexpansion.common.entity.ai.navigation;

import com.bretzelfresser.dinosexpansion.common.entity.ai.control.ComposedMoveControl;
import com.bretzelfresser.dinosexpansion.common.entity.ai.control.SplineFollowMoveControl;
import com.bretzelfresser.dinosexpansion.common.entity.base.FlyingDinosaur;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
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
    protected ParametricSpline activeSpline = null;

    public SmoothFlyingPathNavigation(FlyingDinosaur<?> dino, Level level) {
        super(dino, level);
        this.dino = dino;
    }

    @Override
    protected @Nullable Path createPath(Set<BlockPos> targets, int distance, boolean reachTarget, int maxVisitedNodes) {
        Path path = super.createPath(targets, distance, reachTarget, maxVisitedNodes);
        if (path != null && this.dino.isFlying()) {
            buildSplineFromPath(path, this.speedModifier);
        } else {
            this.activeSpline = null;
            clearSplineFromMoveControl();
        }
        return path;
    }

    @Override
    public boolean moveTo(@Nullable Path path, double speed) {
        boolean success = super.moveTo(path, speed);
        if (success && path != null && this.dino.isFlying()) {
            buildSplineFromPath(path, speed);
        } else if (!this.dino.isFlying()) {
            this.activeSpline = null;
            clearSplineFromMoveControl();
        }
        return success;
    }

    @Override
    public void stop() {
        super.stop();
        this.activeSpline = null;
        clearSplineFromMoveControl();
    }

    @Override
    public boolean isDone() {
        if (this.dino.isFlying() && this.activeSpline != null) {
            return isMoveControlSplineDone() || super.isDone();
        }
        return super.isDone();
    }

    @Override
    public void tick() {
        super.tick();

        // Debug visualization: trace active spline with particles on the server
        if (this.dino.isFlying() && this.activeSpline != null && !this.activeSpline.isEmpty() && this.level instanceof ServerLevel serverLevel) {
            if (this.level.getGameTime() % 4 == 0) {
                double total = this.activeSpline.getTotalLength();
                for (double s = 0.0D; s <= total; s += 0.5D) {
                    Vec3 p = this.activeSpline.getPositionAtDistance(s);
                    serverLevel.sendParticles(DustParticleOptions.REDSTONE, p.x, p.y, p.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                }
            }
        }
    }

    protected void buildSplineFromPath(Path path, double speed) {
        if (path == null || path.getNodeCount() == 0) {
            this.activeSpline = null;
            clearSplineFromMoveControl();
            return;
        }

        List<Vec3> rawPoints = new ArrayList<>();
        rawPoints.add(this.dino.position());

        for (int i = path.getNextNodeIndex(); i < path.getNodeCount(); i++) {
            rawPoints.add(path.getEntityPosAtNode(this.dino, i));
        }

        if (rawPoints.size() <= 1) {
            this.activeSpline = null;
            clearSplineFromMoveControl();
            return;
        }

        // 1. Line-of-Sight Pruning (String-Pulling)
        List<Vec3> prunedKeypoints = PathStringPuller.stringPull(this.level, this.dino, rawPoints);

        // 2. Build Continuous Parametric Spline P(s)
        this.activeSpline = new ParametricSpline(prunedKeypoints);

        // 3. Delegate execution directly to MoveControl
        assignSplineToMoveControl(this.activeSpline, speed);
    }

    @Override
    protected void followThePath() {
        if (this.path == null || this.path.isDone()) {
            this.stop();
            return;
        }

        // When flying, delegate spline progression to closed-loop MoveControl
        if (this.dino.isFlying() && this.activeSpline != null) {
            if (isMoveControlStuck()) {
                // If dinosaur gets physically stuck against an obstacle, abort and recalculate
                this.stop();
                this.recomputePath();
                return;
            }

            if (isMoveControlSplineDone()) {
                this.stop();
                return;
            }
        } else {
            // Grounded mode: Fallback to standard ground node pathing
            clearSplineFromMoveControl();
            this.activeSpline = null;

            Vec3 currNode = this.path.getNextEntityPos(this.dino);
            this.mob.getMoveControl().setWantedPosition(currNode.x, currNode.y, currNode.z, this.speedModifier);

            double reachDistance = Math.max(1.2D, this.dino.getBbWidth() * 1.2D);
            if (this.dino.distanceToSqr(currNode) <= reachDistance * reachDistance) {
                this.path.advance();
            }
        }
    }

    protected void assignSplineToMoveControl(ParametricSpline spline, double speed) {
        if (this.mob.getMoveControl() instanceof ComposedMoveControl<?> composed) {
            composed.followSpline(spline, speed);
        } else if (this.mob.getMoveControl() instanceof SplineFollowMoveControl splineMove) {
            splineMove.followSpline(spline, speed);
        }
    }

    protected void clearSplineFromMoveControl() {
        if (this.mob.getMoveControl() instanceof ComposedMoveControl<?> composed) {
            composed.clearSpline();
        } else if (this.mob.getMoveControl() instanceof SplineFollowMoveControl splineMove) {
            splineMove.clearSpline();
        }
    }

    protected boolean isMoveControlSplineDone() {
        if (this.mob.getMoveControl() instanceof ComposedMoveControl<?> composed) {
            return composed.isSplineDone();
        } else if (this.mob.getMoveControl() instanceof SplineFollowMoveControl splineMove) {
            return splineMove.isSplineDone();
        }
        return true;
    }

    protected boolean isMoveControlStuck() {
        if (this.mob.getMoveControl() instanceof ComposedMoveControl<?> composed) {
            return composed.isStuck();
        } else if (this.mob.getMoveControl() instanceof SplineFollowMoveControl splineMove) {
            return splineMove.isStuck();
        }
        return false;
    }
}
