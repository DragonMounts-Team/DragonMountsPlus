package net.dragonmounts.neo.common.item;

import net.dragonmounts.neo.common.api.ScoreboardAccessor;
import net.dragonmounts.neo.common.entity.dragon.Relation;
import net.dragonmounts.neo.common.entity.dragon.TameableDragonEntity;
import net.dragonmounts.neo.common.init.DMDataComponents;
import net.dragonmounts.neo.common.init.DMItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static net.dragonmounts.neo.common.DragonMountsShared.ITEM_TRANSLATION_KEY_PREFIX;
import static net.dragonmounts.neo.common.component.ScoreboardInfo.applyScores;
import static net.dragonmounts.neo.common.util.EntityUtil.*;

/// @see net.minecraft.world.item.SpawnEggItem
public class AmuletItem<T extends Entity> extends Item implements EntityContainer<T> {
    public static final String TRANSLATION_KEY = ITEM_TRANSLATION_KEY_PREFIX + "dragon_amulet";
    public final Class<T> contentType;

    public AmuletItem(Class<T> contentType, Properties props) {
        super(props.stacksTo(1));
        this.contentType = contentType;
    }

    @Override
    public @Nullable Entity loadEntity(
            ServerLevel level,
            ItemStack stack,
            @Nullable Player player,
            BlockPos pos,
            EntitySpawnReason reason,
            boolean yOffset,
            boolean extraOffset
    ) {
        var data = stack.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY);
        if (data.isEmpty()) return null;
        var type = data.parseEntityType(level.registryAccess(), Registries.ENTITY_TYPE);
        if (type == null) return null;
        var entity = type.create(level, null, pos, reason, yOffset, extraOffset);
        if (entity == null) return null;
        mergeEntityData(entity, level, player, data);
        applyScores(level.getScoreboard(), stack, entity);
        return entity;
    }

    @Override
    public final Class<T> getContentType() {
        return this.contentType;
    }

    @Override
    public ItemStack saveEntity(T entity, DataComponentPatch patch) {
        var type = entity.getType();
        if (type.canSerialize()) {
            var stack = new ItemStack(this);
            stack.set(DataComponents.ENTITY_DATA, EntityContainer.simplifyData(saveWithId(entity, new CompoundTag())));
            stack.set(DMDataComponents.SCORES, ((ScoreboardAccessor) entity.level().getScoreboard()).neodragonmounts$getInfo(entity));
            stack.applyComponents(patch);
            return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof TameableDragonEntity dragon)) return InteractionResult.PASS;
        if (Relation.denyIfNotOwner(dragon, player)) return InteractionResult.FAIL;
        var amulet = dragon.getDragonType().getInstance(DragonAmuletItem.class, null);
        if (amulet == null) return InteractionResult.FAIL;
        if (!(dragon.level() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        if (!this.isEmpty(stack)) {
            var pos = dragon.blockPosition();
            var entity = this.loadEntity(level, stack, player, pos, EntitySpawnReason.BUCKET, false, false);
            if (entity != null) {
                level.addFreshEntityWithPassengers(entity);
                level.gameEvent(player, GameEvent.ENTITY_PLACE, pos);
            }
        }
        dragon.inventory.dropContents(true, 0);
        dragon.ejectPassengers();
        consumeStack(player, hand, stack, amulet.saveEntity(dragon, DataComponentPatch.EMPTY));
        player.awardStat(Stats.ITEM_USED.get(this));
        dragon.discard();
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var stack = context.getItemInHand();
        if (this.isEmpty(stack)) return InteractionResult.PASS;
        if (context.getLevel() instanceof ServerLevel level) {
            var pos = context.getClickedPos();
            var direction = context.getClickedFace();
            var spawnPos = level.getBlockState(pos).getCollisionShape(level, pos).isEmpty() ? pos : pos.relative(direction);
            var player = context.getPlayer();
            var entity = this.loadEntity(
                    level,
                    stack,
                    player,
                    spawnPos,
                    EntitySpawnReason.BUCKET,
                    true,
                    !Objects.equals(pos, spawnPos) && direction == Direction.UP
            );
            if (entity != null) {
                level.addFreshEntityWithPassengers(entity);
                level.gameEvent(player, GameEvent.ENTITY_PLACE, spawnPos);
                if (player != null) {
                    consumeStack(player, context.getHand(), stack, new ItemStack(DMItems.AMULET));
                    player.awardStat(Stats.ITEM_USED.get(this));
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (this.isEmpty(stack)) return InteractionResult.PASS;
        var hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != BlockHitResult.Type.BLOCK) return InteractionResult.PASS;
        if (!(level instanceof ServerLevel world)) return InteractionResult.SUCCESS;
        var pos = hit.getBlockPos();
        if (!(world.getBlockState(pos).getBlock() instanceof LiquidBlock)) return InteractionResult.PASS;
        if (world.mayInteract(player, pos) && player.mayUseItemAt(pos, hit.getDirection(), stack)) {
            var entity = this.loadEntity(world, stack, player, pos, EntitySpawnReason.BUCKET, false, false);
            if (entity == null) return InteractionResult.PASS;
            world.addFreshEntityWithPassengers(entity);
            consumeStack(player, hand, stack, new ItemStack(DMItems.AMULET));
            world.gameEvent(player, GameEvent.ENTITY_PLACE, pos);
            player.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public void onDestroyed(ItemEntity item) {
        var stack = item.getItem();
        if (this.isEmpty(stack)) return;
        var level = (ServerLevel) item.level();
        var entity = this.loadEntity(level, stack, null, item.getOnPos(), EntitySpawnReason.BUCKET, true, false);
        if (entity != null) {
            level.addFreshEntityWithPassengers(entity);
        }
    }
}
