package net.dragonmounts.neo.common.item;

import net.dragonmounts.neo.common.api.DragonTypified;
import net.dragonmounts.neo.common.entity.dragon.DragonLifeStage;
import net.dragonmounts.neo.common.entity.dragon.ServerDragonEntity;
import net.dragonmounts.neo.common.entity.dragon.TameableDragonEntity;
import net.dragonmounts.neo.common.init.DMDataComponents;
import net.dragonmounts.neo.common.inventory.DragonInventory;
import net.dragonmounts.neo.compat.registry.DragonType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static net.dragonmounts.neo.common.DragonMountsShared.ITEM_TRANSLATION_KEY_PREFIX;
import static net.dragonmounts.neo.common.component.ScoreboardInfo.applyScores;
import static net.dragonmounts.neo.common.entity.dragon.TameableDragonEntity.SERIALIZATION_KEY_FLYING;
import static net.dragonmounts.neo.common.util.EntityUtil.*;

public class DragonEssenceItem extends Item implements DragonTypified, EntityContainer<TameableDragonEntity> {
    public static final String TRANSLATION_KEY = ITEM_TRANSLATION_KEY_PREFIX + "dragon_essence";

    public final DragonType type;

    public DragonEssenceItem(DragonType type, Item.Properties props) {
        super(props.stacksTo(1).component(DMDataComponents.DRAGON_TYPE, type));
        this.type = type;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel() instanceof ServerLevel level) {
            var stack = context.getItemInHand();
            var pos = context.getClickedPos();
            var direction = context.getClickedFace();
            var spawnPos = level.getBlockState(pos).getCollisionShape(level, pos).isEmpty() ? pos : pos.relative(direction);
            var player = context.getPlayer();
            level.addFreshEntityWithPassengers(this.loadEntity(
                    level,
                    stack,
                    player,
                    spawnPos,
                    EntitySpawnReason.BUCKET,
                    true,
                    !Objects.equals(pos, spawnPos) && direction == Direction.UP
            ));
            level.gameEvent(player, GameEvent.ENTITY_PLACE, spawnPos);
            stack.shrink(1);
            if (player != null) {
                player.awardStat(Stats.ITEM_USED.get(this));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        var hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != BlockHitResult.Type.BLOCK) return InteractionResult.PASS;
        if (!(level instanceof ServerLevel world)) return InteractionResult.SUCCESS;
        var pos = hit.getBlockPos();
        if (!(world.getBlockState(pos).getBlock() instanceof LiquidBlock)) return InteractionResult.PASS;
        var stack = player.getItemInHand(hand);
        if (world.mayInteract(player, pos) && player.mayUseItemAt(pos, hit.getDirection(), stack)) {
            world.addFreshEntityWithPassengers(this.loadEntity(world, stack, player, pos, EntitySpawnReason.BUCKET, false, false));
            world.gameEvent(player, GameEvent.ENTITY_PLACE, pos);
            stack.consume(1, player);
            player.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltips, TooltipFlag flag) {
        tooltips.add(this.type.getName());
    }

    @Override
    public DragonType getDragonType() {
        return this.type;
    }

    @Override
    public ItemStack saveEntity(TameableDragonEntity entity, DataComponentPatch patch) {
        var stack = new ItemStack(this);
        var tag = saveWithId(entity, new CompoundTag());
        tag.remove(SERIALIZATION_KEY_FLYING);
        tag.remove(DragonInventory.SERIALIZATION_KEY);
        tag.remove("UUID");
        tag.remove("AbsorptionAmount");
        tag.remove("Age");
        tag.remove("AgeLocked");
        tag.remove("ArmorDropChances");
        tag.remove("ArmorItems");
        tag.remove(LivingEntity.ATTRIBUTES_FIELD);
        tag.remove("Brain");
        tag.remove("ForcedAge");
        tag.remove("HandDropChances");
        tag.remove("HandItems");
        tag.remove("Health");
        tag.remove("LifeStage");
        tag.remove("LoveCause");
        tag.remove("ShearCooldown");
        tag.remove("Sitting");
        stack.set(DataComponents.ENTITY_DATA, EntityContainer.simplifyData(tag));
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
            dragon.setLifeStage(DragonLifeStage.HATCHLING, true, false);
        });
    }

    @Override
    public final Class<TameableDragonEntity> getContentType() {
        return TameableDragonEntity.class;
    }

    @Override
    public boolean isEmpty(ItemStack stack) {
        return false;
    }
}
