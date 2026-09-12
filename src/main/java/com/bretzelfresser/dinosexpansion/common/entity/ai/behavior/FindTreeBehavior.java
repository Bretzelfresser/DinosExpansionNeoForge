package com.bretzelfresser.dinosexpansion.common.entity.ai.behavior;

import com.bretzelfresser.dinosexpansion.common.entity.base.FlyingDinosaur;
import com.bretzelfresser.dinosexpansion.common.init.ModMemoryModules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class FindTreeBehavior {

    public static <T extends FlyingDinosaur<T>> BehaviorControl<T> create() {
        return BehaviorBuilder.create(instance -> instance.group(
                instance.present(ModMemoryModules.SHOULD_SLEEP.get()),
                instance.registered(MemoryModuleType.WALK_TARGET)
        ).apply(instance, (shouldSleep, walkTarget) -> (level, dino, gameTime) -> {
            if (!dino.isFlying()) {
                return false;
            }

            // If touched down on ground or surface, land immediately
            if (dino.onGround()) {
                dino.getFlightBehaviour().land();
                walkTarget.erase();
                return true;
            }

            // Check if arrived at existing target perch
            BlockPos currentPerch = dino.getFlightBehaviour().getPerchTarget();
            if (currentPerch != null) {
                BlockPos targetAir = currentPerch.above();
                double dx = dino.getX() - (targetAir.getX() + 0.5D);
                double dz = dino.getZ() - (targetAir.getZ() + 0.5D);
                double dy = dino.getY() - targetAir.getY();
                double horizDistSq = dx * dx + dz * dz;

                if (horizDistSq <= 1.5D && dy >= -0.5D && dy <= 1.5D) {
                    dino.getFlightBehaviour().land();
                    walkTarget.erase();
                    return true;
                }

                // If already navigating to target perch, continue
                if (instance.tryGet(walkTarget).isPresent()) {
                    return true;
                }
            }

            // Find a tree or high perch
            BlockPos targetPerch = findTreePerch(level, dino, 24, 12);
            if (targetPerch != null) {
                dino.getFlightBehaviour().setPerchTarget(targetPerch);
                Vec3 targetVec = Vec3.atBottomCenterOf(targetPerch.above());
                walkTarget.set(new WalkTarget(targetVec, 1.0F, 1));
                dino.getNavigation().moveTo(targetVec.x, targetVec.y, targetVec.z, 1.0D);
                return true;
            }

            return false;
        }));
    }

    public static @Nullable BlockPos findTreePerch(ServerLevel level, FlyingDinosaur<?> dino, int radiusXZ, int radiusY) {
        BlockPos dinoPos = dino.blockPosition();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos groundCursor = new BlockPos.MutableBlockPos();
        RandomSource random = dino.getRandom();

        BlockPos bestPos = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < 80; i++) {
            int dx = random.nextInt(radiusXZ * 2 + 1) - radiusXZ;
            int dy = random.nextInt(radiusY * 2 + 1) - radiusY;
            int dz = random.nextInt(radiusXZ * 2 + 1) - radiusXZ;
            cursor.set(dinoPos.getX() + dx, dinoPos.getY() + dy, dinoPos.getZ() + dz);

            if (!level.isInWorldBounds(cursor)) continue;

            BlockState state = level.getBlockState(cursor);
            boolean isLeaves = state.is(BlockTags.LEAVES);
            boolean isLog = state.is(BlockTags.LOGS);
            boolean isSolid = state.isSolidRender(level, cursor);

            if (!isLeaves && !isLog && !isSolid) {
                continue;
            }

            // Headroom check: at least 2 air blocks above for the dinosaur to land
            BlockPos above = cursor.above();
            if (!level.isEmptyBlock(above) || !level.isEmptyBlock(above.above())) {
                continue;
            }

            // Check height above ground level below this block
            int heightAboveGround = 0;
            for (int down = 1; down <= 15; down++) {
                groundCursor.set(cursor.getX(), cursor.getY() - down, cursor.getZ());
                if (!level.isInWorldBounds(groundCursor)) break;
                BlockState belowState = level.getBlockState(groundCursor);
                if (!belowState.isAir() && !belowState.is(BlockTags.LEAVES)) {
                    heightAboveGround = down;
                    break;
                }
            }

            // Best case: leaves (+100) or logs (+50), else solid surface (+10)
            double score = 0;
            if (isLeaves) {
                score += 100.0;
            } else if (isLog) {
                score += 50.0;
            } else {
                score += 10.0;
            }

            // High up from ground bonus (up to +60)
            score += Math.min(heightAboveGround, 12) * 5.0;

            // Proximity preference
            double distSq = dinoPos.distSqr(cursor);
            score -= Math.sqrt(distSq) * 1.5;

            if (score > bestScore) {
                bestScore = score;
                bestPos = cursor.immutable();
            }
        }

        // Fallback: ground surface directly below
        if (bestPos == null) {
            for (int dy = 0; dy >= -radiusY; dy--) {
                cursor.set(dinoPos.getX(), dinoPos.getY() + dy, dinoPos.getZ());
                if (level.getBlockState(cursor).isSolidRender(level, cursor)) {
                    BlockPos above = cursor.above();
                    if (level.isEmptyBlock(above) && level.isEmptyBlock(above.above())) {
                        return cursor.immutable();
                    }
                }
            }
        }

        return bestPos;
    }
}
