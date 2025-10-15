package net.dragonmounts.neo.common.util;

import net.dragonmounts.neo.common.entity.ai.behavior.DispatchBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.BiPredicate;

public class BrainUtil {
    public static <E extends LivingEntity> DispatchBehavior<@NotNull E> dispatch(
            BehaviorControl<? super E> onSuccess,
            BehaviorControl<? super E> onFailure,
            BiPredicate<ServerLevel, ? super E> condition
    ) {
        return new DispatchBehavior<>(condition, onSuccess, onFailure);
    }

    public static <E extends LivingEntity> void startBehavior(
            BehaviorControl<? super E> behavior,
            ServerLevel level,
            E entity,
            long time
    ) {
        if (behavior.getStatus() == Behavior.Status.STOPPED) {
            behavior.tryStart(level, entity, time);
        }
    }

    public static <E extends LivingEntity> void stopBehavior(
            BehaviorControl<? super E> behavior,
            ServerLevel level,
            E entity,
            long time
    ) {
        if (behavior.getStatus() == Behavior.Status.RUNNING) {
            behavior.doStop(level, entity, time);
        }
    }

    @SuppressWarnings({"unchecked", "OptionalUsedAsFieldOrParameterType", "OptionalGetWithoutIsPresent"})
    public static <T> T getUnchecked(Optional<? super T> wrapped) {
        return (T) wrapped.get();
    }
}
