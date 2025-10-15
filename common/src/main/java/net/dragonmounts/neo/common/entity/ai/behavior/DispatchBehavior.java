package net.dragonmounts.neo.common.entity.ai.behavior;

import net.dragonmounts.neo.common.util.BrainUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.function.BiPredicate;

public class DispatchBehavior<E extends LivingEntity> implements BehaviorControl<E> {
    private Behavior.Status status = Behavior.Status.STOPPED;
    public final BiPredicate<ServerLevel, ? super E> condition;
    public final BehaviorControl<? super E> onSuccess;
    public final BehaviorControl<? super E> onFailure;
    private @UnknownNullability BehaviorControl<? super E> impl;

    public DispatchBehavior(
            BiPredicate<ServerLevel, ? super E> condition,
            BehaviorControl<? super E> onSuccess,
            BehaviorControl<? super E> onFailure
    ) {
        this.condition = condition;
        this.onSuccess = onSuccess;
        this.onFailure = onFailure;
    }

    private void updateBehavior(ServerLevel level, E entity, long time) {
        BehaviorControl<? super E> behavior = this.condition.test(level, entity) ? this.onSuccess : this.onFailure;
        if (this.impl == behavior) return;
        if (this.impl != null) {
            BrainUtil.stopBehavior(this.impl, level, entity, time);
        }
        this.impl = behavior;
    }

    @Override
    public Behavior.Status getStatus() {
        return this.status;
    }

    @Override
    public boolean tryStart(ServerLevel level, @NotNull E entity, long time) {
        this.status = Behavior.Status.RUNNING;
        this.updateBehavior(level, entity, time);
        BrainUtil.startBehavior(this.impl, level, entity, time);
        return true;
    }

    @Override
    public void tickOrStop(ServerLevel level, @NotNull E entity, long time) {
        this.updateBehavior(level, entity, time);
        var behavior = this.impl;
        BrainUtil.startBehavior(behavior, level, entity, time);
        if (behavior.getStatus() == Behavior.Status.RUNNING) {
            behavior.tickOrStop(level, entity, time);
            if (behavior.getStatus() == Behavior.Status.STOPPED) {
                this.doStop(level, entity, time);
            }
        } else {
            this.doStop(level, entity, time);
        }
    }

    @Override
    public void doStop(ServerLevel level, @NotNull E entity, long time) {
        this.status = Behavior.Status.STOPPED;
        BrainUtil.stopBehavior(this.impl, level, entity, time);
        this.impl = null;
    }

    @Override
    public String debugString() {
        return this.impl == null
                ? "DispatchBehavior"
                : "DispatchBehavior[" + this.impl.debugString() + "]";
    }
}
