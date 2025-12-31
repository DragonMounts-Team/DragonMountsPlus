package net.dragonmounts.neo.compat.platform;

import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.Nullable;

public class PlatformCompat {
    public static boolean isClientSide() {
        return FMLLoader.getDist().isClient();
    }

    public static boolean isModLoaded(String identifier) {
        return ModList.get().isLoaded(identifier);
    }

    public static boolean allowTaming(Animal animal, Player player) {
        return !EventHooks.onAnimalTame(animal, player);
    }

    public static SpawnGroupData finalizeMobSpawn(Mob mob, ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData data) {
        return EventHooks.finalizeMobSpawn(mob, level, difficulty, reason, data);
    }
}
