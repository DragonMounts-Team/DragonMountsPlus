package net.dragonmounts.neo.common.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import net.dragonmounts.neo.common.entity.dragon.ServerDragonEntity;
import net.dragonmounts.neo.common.init.DMMemories;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.NearestLivingEntitySensor;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.monster.Enemy;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;

public class DragonTargetSensor extends NearestLivingEntitySensor<ServerDragonEntity> {
    public static boolean takeIfAttackable(
            ServerLevel level,
            Brain<?> brain,
            ServerDragonEntity dragon,
            @Nullable LivingEntity owner,
            @Nullable LivingEntity target
    ) {
        if (target != null && dragon.wantsToAttack(target, owner) && Sensor.isEntityAttackable(level, dragon, target)) {
            brain.setMemory(MemoryModuleType.NEAREST_ATTACKABLE, target);
            return true;
        }
        return false;
    }

    private final Anger.HurtBy selfHurtBy = new Anger.HurtBy();
    private final Anger.HurtBy ownerHurtBy = new Anger.HurtBy();
    private final Anger.Attack ownerTarget = new Anger.Attack();

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.copyOf(Iterators.concat(super.requires().iterator(), Iterators.forArray(MemoryModuleType.NEAREST_ATTACKABLE)));
    }

    @Override
    protected void doTick(ServerLevel level, ServerDragonEntity dragon) {
        super.doTick(level, dragon);
        var brain = dragon.getBrain();
        var owner = brain.getMemory(DMMemories.FOLLOWABLE_OWNER).orElse(null);
        var current = brain.getMemory(MemoryModuleType.NEAREST_ATTACKABLE).orElse(null);
        if (takeIfAttackable(level, brain, dragon, owner, this.selfHurtBy.updateTarget(dragon, current))) return;
        if (takeIfAttackable(level, brain, dragon, owner, this.ownerHurtBy.updateTarget(owner, current))) return;
        if (takeIfAttackable(level, brain, dragon, owner, this.ownerTarget.updateTarget(owner, current))) return;
        brain.getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES)
                .stream()
                .flatMap(Collection::stream)
                .filter(target -> target instanceof Enemy && Sensor.isEntityAttackable(level, dragon, target))
                .findFirst()
                .ifPresentOrElse(
                        target -> dragon.getBrain().setMemory(MemoryModuleType.NEAREST_ATTACKABLE, target),
                        () -> dragon.getBrain().eraseMemory(MemoryModuleType.NEAREST_ATTACKABLE)
                );
    }
}