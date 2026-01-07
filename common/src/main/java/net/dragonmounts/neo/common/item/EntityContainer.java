package net.dragonmounts.neo.common.item;

import net.dragonmounts.neo.common.util.EntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;

import static net.dragonmounts.neo.common.entity.dragon.TameableDragonEntity.SERIALIZATION_KEY_FLYING;

public interface EntityContainer<T extends Entity> {
    static CustomData simplifyData(CompoundTag tag) {
        tag.remove("Air");
        tag.remove("DeathTime");
        tag.remove("FallDistance");
        tag.remove("FallFlying");
        tag.remove("Fire");
        tag.remove("HurtByTimestamp");
        tag.remove("HurtTime");
        tag.remove("InLove");
        tag.remove("Leash");
        tag.remove("Motion");
        tag.remove("OnGround");
        tag.remove("Passengers");
        tag.remove("PortalCooldown");
        tag.remove("Pos");
        tag.remove("Rotation");
        tag.remove("Sitting");
        tag.remove("SleepingX");
        tag.remove("SleepingY");
        tag.remove("SleepingZ");
        tag.remove("TicksFrozen");
        return CustomData.of(tag);
    }

    static ItemStack saveEntityData(Item item, CompoundTag tag, DataComponentPatch patch) {
        var stack = new ItemStack(item);
        tag.remove(SERIALIZATION_KEY_FLYING);
        tag.remove("UUID");
        stack.set(DataComponents.ENTITY_DATA, EntityContainer.simplifyData(tag));
        stack.applyComponents(patch);
        return stack;
    }

    ItemStack saveEntity(T entity, DataComponentPatch patch);

    /**
     * @see net.minecraft.world.entity.EntityType#spawn(ServerLevel, ItemStack, Player, BlockPos, EntitySpawnReason, boolean, boolean)
     * @see EntityUtil#finalizeSpawn(ServerLevel, Entity, BlockPos, EntitySpawnReason, boolean, boolean)
     */
    @Nullable
    Entity loadEntity(
            ServerLevel level,
            ItemStack stack,
            @Nullable Player player,
            BlockPos pos,
            EntitySpawnReason reason,
            boolean yOffset,
            boolean extraOffset
    );

    Class<T> getContentType();

    default boolean isEmpty(ItemStack stack) {
        return !stack.has(DataComponents.ENTITY_DATA);
    }
}
