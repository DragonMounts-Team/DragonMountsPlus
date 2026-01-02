package net.dragonmounts.neo.common.util;


import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerConfig;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class BlockUtil {
    public static void updateNeighborStates(Level level, BlockPos pos, BlockState state, int flag) {
        state.updateNeighbourShapes(level, pos, flag);
        level.updateNeighborsAt(pos, state.getBlock());
    }

    public static TrialSpawnerConfig overrideEntityToSpawn(TrialSpawnerConfig config, CompoundTag entity) {
        return new TrialSpawnerConfig(
                config.spawnRange(),
                config.totalMobs(),
                config.simultaneousMobs(),
                config.totalMobsAddedPerPlayer(),
                config.simultaneousMobsAddedPerPlayer(),
                config.ticksBetweenSpawn(),
                SimpleWeightedRandomList.single(new SpawnData(entity, Optional.empty(), Optional.empty())),
                config.lootTablesToEject(),
                config.itemsToDropWhenOminous()
        );
    }
}
