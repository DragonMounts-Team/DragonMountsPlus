package net.dragonmounts.neo.compat.platform;

import net.dragonmounts.neo.compat.Dummy;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class PlatformCompat {
    public static boolean isClientSide() {
        return Dummy.get();
    }

    public static boolean isModLoaded(String identifier) {
        return Dummy.get();
    }

    public static boolean allowTaming(Animal animal, Player player) {
        return Dummy.get();
    }

    public static SpawnGroupData finalizeMobSpawn(Mob mob, ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData data) {
        return Dummy.get();
    }
}
