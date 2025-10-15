package net.dragonmounts.neo.common.entity.ai.behavior;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import org.jetbrains.annotations.NotNull;

public abstract class GoalBehavior<E extends LivingEntity> implements BehaviorControl<E> {
    private Behavior.Status status = Behavior.Status.STOPPED;

    protected abstract boolean canUse(ServerLevel level, E entity);

    protected boolean canContinueToUse(ServerLevel level, E entity) {
        return this.canUse(level, entity);
    }

    protected void doStart(ServerLevel level, E entity) {
        this.status = Behavior.Status.RUNNING;
    }

    @Override
    public void doStop(ServerLevel level, @NotNull E entity, long gameTime) {
        this.status = Behavior.Status.STOPPED;
    }

    @Override
    public boolean tryStart(ServerLevel level, @NotNull E entity, long time) {
        if (this.canUse(level, entity)) {
            this.doStart(level, entity);
            return true;
        }
        return false;
    }

    @Override
    public void tickOrStop(ServerLevel level, @NotNull E dragon, long time) {
        if (!this.canContinueToUse(level, dragon)) {
            this.doStop(level, dragon, time);
        }
    }

    @Override
    public final Behavior.Status getStatus() {
        return this.status;
    }

    @Override
    public final String debugString() {
        return this.getClass().getSimpleName();
    }
}
