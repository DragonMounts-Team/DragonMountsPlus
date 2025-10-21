package net.dragonmounts.neo.common.entity.ai.behavior;

import net.dragonmounts.neo.common.entity.dragon.MouthState;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public interface RangedAttack<T extends LivingEntity> {
    @Nullable MouthState getMouthState();

    void tick(T entity);

    void onDetached(T entity);
}
