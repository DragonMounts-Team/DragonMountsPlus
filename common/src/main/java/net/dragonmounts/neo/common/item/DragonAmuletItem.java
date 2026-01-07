package net.dragonmounts.neo.common.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.dragonmounts.neo.common.api.DragonTypified;
import net.dragonmounts.neo.common.api.ScoreboardAccessor;
import net.dragonmounts.neo.common.entity.dragon.ServerDragonEntity;
import net.dragonmounts.neo.common.entity.dragon.TameableDragonEntity;
import net.dragonmounts.neo.common.init.DMDataComponents;
import net.dragonmounts.neo.compat.registry.DragonType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.dragonmounts.neo.common.component.ScoreboardInfo.applyScores;
import static net.dragonmounts.neo.common.entity.dragon.TameableDragonEntity.SERIALIZATION_KEY_FLYING;
import static net.dragonmounts.neo.common.util.EntityUtil.*;

public class DragonAmuletItem extends AmuletItem<TameableDragonEntity> implements DragonTypified {
    public static final MapCodec<Component> NAME_CODEC = ComponentSerialization.CODEC.fieldOf("CustomName");
    public static final MapCodec<Float> HEALTH_CODEC = Codec.FLOAT.fieldOf("Health");
    public final DragonType type;

    public DragonAmuletItem(DragonType type, Properties props) {
        super(TameableDragonEntity.class, props.component(DMDataComponents.DRAGON_TYPE, type));
        this.type = type;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltips, TooltipFlag flag) {
        var data = stack.get(DataComponents.ENTITY_DATA);
        if (data == null) {
            tooltips.add(Component.translatable("tooltip.neodragonmounts.missing").withStyle(ChatFormatting.RED));
        } else {
            tooltips.add(Component.translatable("tooltip.neodragonmounts.type", this.type.getName()).withStyle(ChatFormatting.GRAY));
            data.read(HEALTH_CODEC).ifSuccess(health -> tooltips.add(
                    Component.translatable("tooltip.neodragonmounts.health",
                            Component.literal(Float.toString(health)).withStyle(ChatFormatting.GREEN)
                    ).withStyle(ChatFormatting.GRAY))
            );
            data.read(NAME_CODEC).ifSuccess(name -> tooltips.add(
                    Component.translatable("tooltip.neodragonmounts.custom_name", name).withStyle(ChatFormatting.GRAY))
            );
            var player = stack.get(DMDataComponents.PLAYER_NAME);
            if (player != null) {
                tooltips.add(Component.translatable("tooltip.neodragonmounts.owner_name", player).withStyle(ChatFormatting.GRAY));
            }
        }
    }

    @Override
    public ItemStack saveEntity(TameableDragonEntity entity, DataComponentPatch patch) {
        var level = entity.level();
        var stack = new ItemStack(this);
        var tag = saveWithId(entity, new CompoundTag());
        tag.remove(SERIALIZATION_KEY_FLYING);
        tag.remove("UUID");
        stack.set(DataComponents.ENTITY_DATA, EntityContainer.simplifyData(tag));
        LivingEntity owner = entity.getOwner();
        if (owner != null) {
            stack.set(DMDataComponents.PLAYER_NAME, owner.getDisplayName());
        }
        stack.set(DMDataComponents.SCORES, ((ScoreboardAccessor) level.getScoreboard()).neodragonmounts$getInfo(entity));
        stack.applyComponents(patch);
        return stack;
    }

    @Override
    public ServerDragonEntity loadEntity(
            ServerLevel world,
            ItemStack stack,
            @Nullable Player player,
            BlockPos pos,
            EntitySpawnReason reason,
            boolean yOffset,
            boolean extraOffset
    ) {
        return new ServerDragonEntity(world, (level, dragon) -> {
            finalizeSpawn(level, dragon, pos, reason, yOffset, extraOffset);
            CustomData data = stack.get(DataComponents.ENTITY_DATA);
            if (data != null) {
                mergeEntityData(dragon, level, player, data);
                dragon.convertTo(this.type, false);
            } else {
                dragon.overrideType(this.type, true);
            }
            applyScores(level.getScoreboard(), stack, dragon);
        });
    }

    @Override
    public boolean isEmpty(ItemStack stack) {
        return false;
    }

    @Override
    public DragonType getDragonType() {
        return this.type;
    }
}
