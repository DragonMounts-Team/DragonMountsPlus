package net.dragonmounts.neo.compat.platform;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

public class PlatformCompat {
    public static boolean isClientSide() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    public static boolean isModLoaded(String identifier) {
        return FabricLoader.getInstance().isModLoaded(identifier);
    }

    public static boolean allowTaming(Animal animal, Player player) {
        return true;
    }

    public static SpawnGroupData finalizeMobSpawn(Mob mob, ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData data) {
        return mob.finalizeSpawn(level, difficulty, reason, data);
    }
}
