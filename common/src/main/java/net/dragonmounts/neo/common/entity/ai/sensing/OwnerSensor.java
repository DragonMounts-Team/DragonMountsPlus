package net.dragonmounts.neo.common.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import net.dragonmounts.neo.common.init.DMMemories;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;

import java.util.Collections;
import java.util.Set;

/// <b>RELIES ON {@link net.minecraft.world.entity.ai.sensing.PlayerSensor}</b>
public class OwnerSensor extends Sensor<TamableAnimal> {
    @Override
    protected void doTick(ServerLevel level, TamableAnimal entity) {
        boolean found = false;
        var owner = entity.getOwner();
        var brain = entity.getBrain();
        for (var player : brain.getMemory(
                MemoryModuleType.NEAREST_PLAYERS
        ).orElse(Collections.emptyList())) {
            if (owner == player && !entity.hasPassenger(player)) {
                found = true;
                brain.setMemory(DMMemories.FOLLOWABLE_OWNER, player);
                break;
            }
        }
        if (found) return;
        brain.eraseMemory(DMMemories.FOLLOWABLE_OWNER);
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(
                MemoryModuleType.NEAREST_PLAYERS,
                DMMemories.FOLLOWABLE_OWNER
        );
    }
}
