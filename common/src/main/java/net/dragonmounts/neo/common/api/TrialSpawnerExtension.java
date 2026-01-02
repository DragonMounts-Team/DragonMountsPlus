package net.dragonmounts.neo.common.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public interface TrialSpawnerExtension {
    void neodragonmounts$overrideEntityToSpawn(Level level, CompoundTag entity);
}
