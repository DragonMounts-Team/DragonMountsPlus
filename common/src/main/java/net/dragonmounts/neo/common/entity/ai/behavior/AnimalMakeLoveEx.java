package net.dragonmounts.neo.common.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.Animal;

import java.util.Optional;

import static net.dragonmounts.neo.common.util.BrainUtil.getUnchecked;

/// @see net.minecraft.world.entity.ai.behavior.AnimalMakeLove
public class AnimalMakeLoveEx extends Behavior<Animal> {
    private final EntityType<? extends Animal> partnerType;
    private final float speedModifier;
    private final int closeEnoughDistance;
    private final int breedRange;
    private long spawnChildAtTime;

    public AnimalMakeLoveEx(
            EntityType<? extends Animal> partnerType,
            float speedModifier,
            int closeEnoughDistance,
            int breedRange
    ) {
        super(
                ImmutableMap.of(
                        MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
                        MemoryStatus.VALUE_PRESENT,
                        MemoryModuleType.BREED_TARGET,
                        MemoryStatus.VALUE_ABSENT,
                        MemoryModuleType.WALK_TARGET,
                        MemoryStatus.REGISTERED,
                        MemoryModuleType.LOOK_TARGET,
                        MemoryStatus.REGISTERED,
                        MemoryModuleType.IS_PANICKING,
                        MemoryStatus.VALUE_ABSENT
                ),
                110
        );
        this.partnerType = partnerType;
        this.speedModifier = speedModifier;
        this.closeEnoughDistance = closeEnoughDistance;
        this.breedRange = breedRange;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Animal owner) {
        return owner.onGround() && owner.isInLove() && this.findValidBreedPartner(owner).isPresent();
    }

    @Override
    protected void start(ServerLevel level, Animal entity, long gameTime) {
        Animal animal = getUnchecked(this.findValidBreedPartner(entity));
        entity.getBrain().setMemory(MemoryModuleType.BREED_TARGET, animal);
        animal.getBrain().setMemory(MemoryModuleType.BREED_TARGET, entity);
        BehaviorUtils.lockGazeAndWalkToEachOther(entity, animal, this.speedModifier, this.closeEnoughDistance);
        int i = 60 + entity.getRandom().nextInt(50);
        this.spawnChildAtTime = gameTime + (long) i;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Animal entity, long gameTime) {
        if (!this.hasBreedTargetOfRightType(entity)) return false;
        Animal animal = this.getBreedTarget(entity);
        return animal.isAlive()
                && entity.canMate(animal)
                && BehaviorUtils.entityIsVisible(entity.getBrain(), animal)
                && gameTime <= this.spawnChildAtTime
                && !entity.isPanicking()
                && !animal.isPanicking();
    }

    @Override
    protected void tick(ServerLevel level, Animal owner, long gameTime) {
        Animal animal = this.getBreedTarget(owner);
        BehaviorUtils.lockGazeAndWalkToEachOther(owner, animal, this.speedModifier, this.closeEnoughDistance);
        if (gameTime >= this.spawnChildAtTime && owner.closerThan(animal, this.breedRange)) {
            owner.spawnChildFromBreeding(level, animal);
            owner.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);
            animal.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);
        }
    }

    @Override
    protected void stop(ServerLevel level, Animal entity, long gameTime) {
        var brain = entity.getBrain();
        brain.eraseMemory(MemoryModuleType.BREED_TARGET);
        brain.eraseMemory(MemoryModuleType.WALK_TARGET);
        brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
        this.spawnChildAtTime = 0L;
    }

    private Animal getBreedTarget(Animal animal) {
        return getUnchecked(animal.getBrain().getMemory(MemoryModuleType.BREED_TARGET));
    }

    private boolean hasBreedTargetOfRightType(Animal animal) {
        var brain = animal.getBrain();
        return brain.hasMemoryValue(MemoryModuleType.BREED_TARGET) && getUnchecked(brain.getMemory(MemoryModuleType.BREED_TARGET)).getType() == this.partnerType;
    }

    private Optional<? extends Animal> findValidBreedPartner(Animal self) {
        return self.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
                .flatMap(memory -> memory.findClosest(other ->
                        other.getType() == this.partnerType && other instanceof Animal animal && self.canMate(animal) && !animal.isPanicking()
                )).map(Animal.class::cast);
    }
}
