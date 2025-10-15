package net.dragonmounts.neo.common.entity.ai.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

import static net.minecraft.world.entity.ai.memory.MemoryModuleType.*;

/// @see net.minecraft.world.entity.ai.behavior.TryFindLand
public class TryFindGround<E extends PathfinderMob & FlyingAnimal> extends OneShot<E> {
    private static final int COOLDOWN_TICKS = 120;
    public final Predicate<E> predicate;
    public final int width;
    public final int height;
    public final float speedModifier;
    private long lastTry;

    public TryFindGround(int width, int height, float speedModifier, Predicate<E> predicate) {
        this.width = width;
        this.height = height;
        this.speedModifier = speedModifier;
        this.lastTry = -COOLDOWN_TICKS - 1;
        this.predicate = predicate;
    }

    @Override
    public boolean trigger(ServerLevel level, @NotNull E entity, long time) {
        if (this.predicate.test(entity)) return false;
        var brain = entity.getBrain();
        if (brain.hasMemoryValue(WALK_TARGET) || brain.hasMemoryValue(ATTACK_TARGET)) return false;
        var current = entity.blockPosition();
        var below = current.below();
        if (level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
            entity.setDeltaMovement(entity.getDeltaMovement().add(0.0, -0.98, 0.0));
        }
        if (time - this.lastTry < COOLDOWN_TICKS) return true;
        this.lastTry = time;
        var down = new BlockPos.MutableBlockPos();
        var context = CollisionContext.of(entity);
        for (var pos : BlockPos.withinManhattan(current, this.width, this.height, this.width)) {
            if (pos.getX() != current.getX() || pos.getZ() != current.getZ()) {
                var state = level.getBlockState(pos);
                if (!state.is(Blocks.WATER) &&
                        level.getFluidState(pos).isEmpty() &&
                        state.getCollisionShape(level, pos, context).isEmpty() &&
                        level.getBlockState(
                                down.setWithOffset(pos, Direction.DOWN)
                        ).isFaceSturdy(level, down, Direction.UP)
                ) {
                    var target = new BlockPosTracker(pos.immutable());
                    brain.setMemory(LOOK_TARGET, target);
                    brain.setMemory(WALK_TARGET, new WalkTarget(target, this.speedModifier, 1));
                    return true;
                }
            }
        }
        int targetY = Math.max(level.getMinY() + level.getLogicalHeight() / 4, level.getSeaLevel());
        int distance = current.getY() - targetY;
        if (distance > 32) {
            targetY = current.getY() - 32;
        } else if (distance < -32) {
            targetY = current.getY() + 32;
        }
        var target = new BlockPosTracker(new BlockPos(current.getX(), targetY, current.getZ()));
        brain.setMemory(LOOK_TARGET, target);
        brain.setMemory(WALK_TARGET, new WalkTarget(target, this.speedModifier, 8));
        return true;
    }
}
