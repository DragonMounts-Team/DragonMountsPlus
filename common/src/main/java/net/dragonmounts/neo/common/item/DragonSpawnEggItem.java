package net.dragonmounts.neo.common.item;

import net.dragonmounts.neo.common.api.DragonTypified;
import net.dragonmounts.neo.common.api.TrialSpawnerExtension;
import net.dragonmounts.neo.common.entity.dragon.HatchableDragonEggEntity;
import net.dragonmounts.neo.common.entity.dragon.TameableDragonEntity;
import net.dragonmounts.neo.common.init.DMDataComponents;
import net.dragonmounts.neo.common.init.DMEntities;
import net.dragonmounts.neo.compat.registry.DragonType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Spawner;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

import static net.dragonmounts.neo.common.DragonMountsShared.ITEM_TRANSLATION_KEY_PREFIX;
import static net.dragonmounts.neo.common.component.ScoreboardInfo.applyScores;
import static net.dragonmounts.neo.common.util.EntityUtil.mergeEntityData;
import static net.dragonmounts.neo.common.util.EntityUtil.saveWithId;

public class DragonSpawnEggItem extends SpawnEggItem implements EntityContainer<Entity>, DragonTypified {
    public static final DispenseItemBehavior DISPENSE_ITEM_BEHAVIOR = new DefaultDispenseItemBehavior() {
        @Override
        public ItemStack execute(BlockSource dispenser, ItemStack stack) {
            var direction = dispenser.state().getValue(DispenserBlock.FACING);
            try {
                var entity = ((DragonSpawnEggItem) stack.getItem()).loadEntity(
                        dispenser.level(),
                        stack,
                        null,
                        dispenser.pos().relative(direction),
                        EntitySpawnReason.DISPENSER,
                        direction != Direction.UP,
                        false
                );
                if (entity == null) return stack;
                dispenser.level().addFreshEntityWithPassengers(entity);
                if (entity instanceof Mob) {
                    ((Mob) entity).playAmbientSound();
                }
            } catch (Exception exception) {
                LOGGER.error("Error while dispensing spawn egg from dispenser at {}", dispenser.pos(), exception);
                return ItemStack.EMPTY;
            }
            stack.shrink(1);
            dispenser.level().gameEvent(null, GameEvent.ENTITY_PLACE, dispenser.pos());
            return stack;
        }
    };
    public static final String TRANSLATION_KEY = ITEM_TRANSLATION_KEY_PREFIX + "dragon_spawn_egg";
    public final TranslatableContents name;
    public final DragonType type;

    public DragonSpawnEggItem(EntityType<? extends TameableDragonEntity> defaultType, DragonType dragonType, Properties props) {
        super(defaultType, props.component(DMDataComponents.DRAGON_TYPE, dragonType));
        this.name = new TranslatableContents(TRANSLATION_KEY + ".name", null, new Object[]{MutableComponent.create(dragonType.name)});
        this.type = dragonType;
    }

    public DragonSpawnEggItem(DragonType type, Properties props) {
        this(DMEntities.TAMEABLE_DRAGON.get(), type, props);
    }

    protected void writeDragonSpec(CompoundTag root) {
        root.putString("id", DMEntities.HATCHABLE_DRAGON_EGG.key.location().toString());
        root.putString(DragonType.SERIALIZATION_KEY, this.type.identifier.toString());
        root.putBoolean(HatchableDragonEggEntity.SERIALIZATION_KEY_SPAWNER, true);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        var stack = context.getItemInHand();
        var pos = context.getClickedPos();
        var direction = context.getClickedFace();
        var state = level.getBlockState(pos);
        var random = level.getRandom();
        EntityType<?> type;
        switch (level.getBlockEntity(pos)) {
            case TrialSpawnerBlockEntity spawner:
                type = this.getType(level.registryAccess(), stack);
                if (DMEntities.TAMEABLE_DRAGON.is(type)) {
                    var entity = new CompoundTag();
                    this.writeDragonSpec(entity);
                    ((TrialSpawnerExtension) ((Object) spawner.getTrialSpawner()))
                            .neodragonmounts$overrideEntityToSpawn(level, entity);
                    spawner.setChanged();
                } else {
                    spawner.setEntityId(type, random);
                }
                break;
            case SpawnerBlockEntity spawner:
                type = this.getType(level.registryAccess(), stack);
                if (DMEntities.TAMEABLE_DRAGON.is(type)) {
                    this.writeDragonSpec(spawner.getSpawner().getOrCreateNextSpawnData(level, random, pos).entityToSpawn());
                    spawner.setChanged();
                } else {
                    spawner.setEntityId(type, random);
                }
                break;
            case Spawner spawner:
                spawner.setEntityId(this.getType(level.registryAccess(), stack), random);
                break;
            case null:
            default:
                var spawnPos = state.getCollisionShape(level, pos).isEmpty() ? pos : pos.relative(direction);
                var player = context.getPlayer();
                var entity = this.loadEntity(level, stack, player, spawnPos, EntitySpawnReason.SPAWN_ITEM_USE, true, !Objects.equals(pos, spawnPos) && direction == Direction.UP);
                if (entity != null) {
                    level.addFreshEntityWithPassengers(entity);
                    if (entity instanceof Mob) {
                        ((Mob) entity).playAmbientSound();
                    }
                    stack.shrink(1);
                    level.gameEvent(player, GameEvent.ENTITY_PLACE, spawnPos);
                }
                return InteractionResult.SUCCESS;
        }
        level.sendBlockUpdated(pos, state, state, 3);
        level.gameEvent(context.getPlayer(), GameEvent.BLOCK_CHANGE, pos);
        stack.shrink(1);
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
            var entity = this.loadEntity(world, stack, player, pos, EntitySpawnReason.SPAWN_ITEM_USE, false, false);
            if (entity == null) return InteractionResult.PASS;
            world.addFreshEntityWithPassengers(entity);
            if (entity instanceof Mob) {
                ((Mob) entity).playAmbientSound();
            }
            stack.consume(1, player);
            player.awardStat(Stats.ITEM_USED.get(this));
            world.gameEvent(player, GameEvent.ENTITY_PLACE, entity.position());
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public Optional<Mob> spawnOffspringFromSpawnEgg(Player player, Mob mob, EntityType<? extends Mob> type, ServerLevel level, Vec3 pos, ItemStack stack) {
        if (!this.spawnsEntity(level.registryAccess(), stack, type)) return Optional.empty();
        Mob neo = mob instanceof AgeableMob old ? old.getBreedOffspring(level, old) : type.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        if (neo == null) return Optional.empty();
        neo.setBaby(true);
        if (!neo.isBaby()) return Optional.empty();
        neo.moveTo(pos.x(), pos.y(), pos.z(), 0.0F, 0.0F);
        level.addFreshEntityWithPassengers(neo);
        neo.setCustomName(stack.get(DataComponents.CUSTOM_NAME));
        applyScores(level.getScoreboard(), stack, neo);
        stack.consume(1, player);
        return Optional.of(neo);
    }

    @Override
    public Component getName(ItemStack stack) {
        return MutableComponent.create(this.name);
    }

    @Override
    public final DragonType getDragonType() {
        return this.type;
    }

    public ItemStack saveEntity(TameableDragonEntity dragon) {
        return EntityContainer.saveEntityData(this, saveWithId(dragon, new CompoundTag()), DataComponentPatch.EMPTY);
    }

    @Override
    public ItemStack saveEntity(Entity entity, DataComponentPatch patch) {
        if (entity instanceof TameableDragonEntity) {
            return EntityContainer.saveEntityData(this, saveWithId(entity, new CompoundTag()), patch);
        }
        var item = SpawnEggItem.byId(entity.getType());
        return item == null ? ItemStack.EMPTY : EntityContainer.saveEntityData(item, saveWithId(entity, new CompoundTag()), patch);
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
        var type = this.getType(level.registryAccess(), stack);
        var entity = type.create(level, null, pos, reason, yOffset, extraOffset);
        if (entity == null) return null;
        if (entity instanceof TameableDragonEntity) {
            ((TameableDragonEntity) entity).overrideType(this.type, true);
        }
        mergeEntityData(entity, level, player, stack.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY));
        entity.setCustomName(stack.get(DataComponents.CUSTOM_NAME));
        applyScores(level.getScoreboard(), stack, entity);
        return entity;
    }

    @Override
    public final Class<Entity> getContentType() {
        return Entity.class;
    }

    @Override
    public boolean isEmpty(ItemStack stack) {
        return false;
    }

    /// Override the extension on NeoForge
    public DispenseItemBehavior createDispenseBehavior() {
        return DISPENSE_ITEM_BEHAVIOR;
    }
}
