package com.bretzelfresser.dinosexpansion.common.entity.behaviours;

import com.bretzelfresser.dinosexpansion.common.entity.base.FlyingDinosaur;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class FlightBehaviour {

    public enum FlightState {
        FLYING,
        LANDING,
        GROUNDED,
        TAKING_OFF
    }

    protected final FlyingDinosaur<?> dino;
    protected final PathNavigation groundNavigation;
    protected final PathNavigation flyingNavigation;
    protected final MoveControl groundMoveControl;
    protected final MoveControl flyingMoveControl;

    protected FlightState flightState;
    protected @Nullable BlockPos perchTarget;
    protected int landingTicks = 0;
    protected int groundTicks = 0;
    protected int flightTicks = 0;

    public FlightBehaviour(FlyingDinosaur<?> dino) {
        this.dino = dino;
        Level level = dino.level();
        this.groundNavigation = new GroundPathNavigation(dino, level);
        this.flyingNavigation = new FlyingPathNavigation(dino, level);
        this.groundMoveControl = new MoveControl(dino);
        this.flyingMoveControl = new FlyingMoveControl(dino, 10, false);

        this.groundNavigation.setCanFloat(true);
        this.flyingNavigation.setCanFloat(true);

        if (dino.isFlying()) {
            this.flightState = FlightState.FLYING;
            dino.setNavigation(this.flyingNavigation);
            dino.setMoveControl(this.flyingMoveControl);
        } else {
            this.flightState = FlightState.GROUNDED;
            dino.setNavigation(this.groundNavigation);
            dino.setMoveControl(this.groundMoveControl);
        }
    }

    public FlightState getFlightState() {
        return this.flightState;
    }

    public @Nullable BlockPos getPerchTarget() {
        return this.perchTarget;
    }

    public PathNavigation getGroundNavigation() {
        return this.groundNavigation;
    }

    public PathNavigation getFlyingNavigation() {
        return this.flyingNavigation;
    }

    public MoveControl getGroundMoveControl() {
        return this.groundMoveControl;
    }

    public MoveControl getFlyingMoveControl() {
        return this.flyingMoveControl;
    }

    public void takeOff() {
        if (this.flightState == FlightState.FLYING && this.dino.isFlying()) {
            return;
        }
        this.dino.setFlying(true);
    }

    public void land() {
        if (this.flightState == FlightState.GROUNDED && !this.dino.isFlying()) {
            return;
        }
        this.dino.setFlying(false);
    }

    public void onTakeOff() {
        this.flightState = FlightState.FLYING;
        this.perchTarget = null;
        this.landingTicks = 0;
        this.groundTicks = 0;
        this.flightTicks = 0;
        this.dino.setNavigation(this.flyingNavigation);
        this.dino.setMoveControl(this.flyingMoveControl);
        this.flyingNavigation.stop();
        this.dino.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        this.dino.getBrain().eraseMemory(MemoryModuleType.PATH);
    }

    public void onLand() {
        this.flightState = FlightState.GROUNDED;
        this.perchTarget = null;
        this.landingTicks = 0;
        this.flightTicks = 0;
        this.groundTicks = 0;
        this.dino.setNoGravity(false);
        this.dino.setNavigation(this.groundNavigation);
        this.dino.setMoveControl(this.groundMoveControl);
        this.groundNavigation.stop();
        this.dino.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        this.dino.getBrain().eraseMemory(MemoryModuleType.PATH);
    }

    public void startLandingForSleep() {
        if (this.flightState == FlightState.GROUNDED || !this.dino.isFlying()) {
            return;
        }
        if (this.flightState == FlightState.LANDING && this.perchTarget != null) {
            return;
        }

        BlockPos perch = findLandingPerch(16, 10);
        this.flightState = FlightState.LANDING;
        this.landingTicks = 0;

        if (perch != null) {
            this.perchTarget = perch;
            Vec3 targetVec = Vec3.atBottomCenterOf(perch.above());
            this.dino.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(targetVec, 1.0F, 1));
            this.dino.getNavigation().moveTo(targetVec.x, targetVec.y, targetVec.z, 1.0D);
        }
    }

    public void startLanding() {
        if (this.flightState == FlightState.GROUNDED || !this.dino.isFlying()) {
            return;
        }
        if (this.flightState == FlightState.LANDING && this.perchTarget != null) {
            return;
        }

        BlockPos perch = findLandingPerch(16, 8);
        this.flightState = FlightState.LANDING;
        this.landingTicks = 0;

        if (perch != null) {
            this.perchTarget = perch;
            Vec3 targetVec = Vec3.atBottomCenterOf(perch.above());
            this.dino.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(targetVec, 1.0F, 1));
            this.dino.getNavigation().moveTo(targetVec.x, targetVec.y, targetVec.z, 1.0D);
        }
    }

    public @Nullable BlockPos findLandingPerch(int radiusXZ, int radiusY) {
        Level level = this.dino.level();
        BlockPos dinoPos = this.dino.blockPosition();
        BlockPos bestPerch = null;
        double bestDistSq = Double.MAX_VALUE;

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        RandomSource random = this.dino.getRandom();

        // Check random positions within radius favoring tree canopies (leaves and logs)
        for (int i = 0; i < 60; i++) {
            int dx = random.nextInt(radiusXZ * 2 + 1) - radiusXZ;
            int dy = random.nextInt(radiusY * 2 + 1) - radiusY;
            int dz = random.nextInt(radiusXZ * 2 + 1) - radiusXZ;
            cursor.set(dinoPos.getX() + dx, dinoPos.getY() + dy, dinoPos.getZ() + dz);

            if (!level.isInWorldBounds(cursor)) continue;

            BlockState state = level.getBlockState(cursor);
            if (state.is(BlockTags.LEAVES) || state.is(BlockTags.LOGS)) {
                BlockPos above = cursor.above();
                if (level.isEmptyBlock(above) && level.isEmptyBlock(above.above())) {
                    double distSq = dinoPos.distSqr(cursor);
                    if (distSq < bestDistSq) {
                        bestDistSq = distSq;
                        bestPerch = cursor.immutable();
                    }
                }
            }
        }

        if (bestPerch != null) {
            return bestPerch;
        }

        // Fallback: search downward for solid ground surface
        for (int dy = 0; dy >= -radiusY; dy--) {
            cursor.set(dinoPos.getX(), dinoPos.getY() + dy, dinoPos.getZ());
            if (level.getBlockState(cursor).isSolidRender(level, cursor)) {
                BlockPos above = cursor.above();
                if (level.isEmptyBlock(above) && level.isEmptyBlock(above.above())) {
                    return cursor.immutable();
                }
            }
        }

        return null;
    }

    public void tick() {
        if (this.dino.level().isClientSide()) {
            return;
        }

        if (this.flightState == FlightState.LANDING) {
            this.landingTicks++;

            boolean arrived = false;
            if (this.perchTarget != null) {
                BlockPos targetAir = this.perchTarget.above();
                double dx = this.dino.getX() - (targetAir.getX() + 0.5D);
                double dz = this.dino.getZ() - (targetAir.getZ() + 0.5D);
                double dy = this.dino.getY() - targetAir.getY();
                double horizDistSq = dx * dx + dz * dz;

                if (horizDistSq <= 1.5D && dy >= -0.5D && dy <= 1.5D) {
                    arrived = true;
                }
            }

            if (this.dino.onGround() || arrived) {
                land();
                return;
            }

            if (this.landingTicks > 120 && this.dino.verticalCollisionBelow) {
                land();
                return;
            }

            // Recovery if stuck landing
            if (this.landingTicks > 200) {
                this.perchTarget = null;
                this.landingTicks = 0;
                BlockPos fallback = findLandingPerch(12, 6);
                if (fallback != null) {
                    this.perchTarget = fallback;
                    Vec3 targetVec = Vec3.atBottomCenterOf(fallback.above());
                    this.dino.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(targetVec, 1.0F, 1));
                    this.dino.getNavigation().moveTo(targetVec.x, targetVec.y, targetVec.z, 1.0D);
                } else {
                    this.dino.setDeltaMovement(this.dino.getDeltaMovement().multiply(0.8D, 0.5D, 0.8D).add(0, -0.05D, 0));
                }
            }
        } else if (this.flightState == FlightState.GROUNDED) {
            this.groundTicks++;

            // If in water and awake, fly out
            if (this.dino.isInWater() && !this.dino.isSleeping() && !this.dino.isUnconscious()) {
                takeOff();
                return;
            }

            // Daytime occasional takeoff after walking/resting for a while
            if (!this.dino.isSleeping() && !this.dino.isUnconscious() && this.groundTicks > 300) {
                if (this.dino.getRandom().nextFloat() < 0.008F) {
                    takeOff();
                }
            }
        } else if (this.flightState == FlightState.FLYING) {
            this.flightTicks++;

            // Daytime occasional perch after flying for a long time
            if (!this.dino.isSleeping() && !this.dino.isUnconscious() && this.flightTicks > 800) {
                if (this.dino.getRandom().nextFloat() < 0.004F) {
                    startLanding();
                }
            }
        }
    }
}
