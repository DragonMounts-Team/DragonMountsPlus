package net.dragonmounts.neo.common.item;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import net.dragonmounts.neo.common.entity.dragon.Relation;
import net.dragonmounts.neo.common.entity.dragon.TameableDragonEntity;
import net.dragonmounts.neo.common.util.ShearsDispenseItemBehaviorEx;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;

import static net.dragonmounts.neo.common.util.EntityUtil.getSlotForHand;
import static net.minecraft.world.level.block.BeehiveBlock.HONEY_LEVEL;
import static net.minecraft.world.level.block.BeehiveBlock.dropHoneycomb;

public class TieredShearsItem extends ShearsItem {
    public static final DispenseItemBehavior DISPENSE_ITEM_BEHAVIOR = new ShearsDispenseItemBehaviorEx();

    public static Tool createToolProperties(ToolMaterial tier) {
        var props = ShearsItem.createToolProperties();
        var vanilla = props.rules();
        float factor = tier.speed() / ToolMaterial.IRON.speed();
        var list = ImmutableList.<Tool.Rule>builderWithExpectedSize(vanilla.size());
        for (var rule : vanilla) {
            list.add(new Tool.Rule(rule.blocks(), rule.speed().map(speed -> speed * factor), rule.correctForDrops()));
        }
        return new Tool(list.build(), props.defaultMiningSpeed(), props.damagePerBlock());
    }

    protected final ToolMaterial tier;

    public TieredShearsItem(ToolMaterial tier, Properties props) {
        super(props.durability((int) (tier.durability() * 0.952F))
                .component(DataComponents.TOOL, createToolProperties(tier))
                .repairable(tier.repairItems())
                .enchantable(tier.enchantmentValue())
        );
        this.tier = tier;
    }

    public ToolMaterial getTier() {
        return this.tier;
    }

    /**
     * @see BeehiveBlock
     * @see PumpkinBlock
     */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        var result = super.useOn(context);
        if (result.consumesAction()) return result;
        var player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var state = level.getBlockState(pos);
        var stack = context.getItemInHand();
        switch (state.getBlock()) {
            case BeehiveBlock beehive:
                int content = state.getValue(HONEY_LEVEL);
                if (content < 5) return InteractionResult.TRY_WITH_EMPTY_HAND;
                level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BEEHIVE_SHEAR, SoundSource.BLOCKS, 1.0F, 1.0F);
                dropHoneycomb(level, pos);
                stack.hurtAndBreak(1, player, getSlotForHand(context.getHand()));
                level.gameEvent(player, GameEvent.SHEAR, pos);
                if (!level.isClientSide) {
                    player.awardStat(Stats.ITEM_USED.get(this));
                }
                if (CampfireBlock.isSmokeyPos(level, pos)) {
                    beehive.resetHoneyLevel(level, state, pos);
                    return InteractionResult.SUCCESS;
                }
                if (level.getBlockEntity(pos) instanceof BeehiveBlockEntity entity && !entity.isEmpty()) {
                    var aabb = new AABB(pos).inflate(8.0, 6.0, 8.0);
                    var bees = level.getEntitiesOfClass(Bee.class, aabb, Predicates.alwaysTrue());
                    if (!bees.isEmpty()) {
                        var players = level.getEntitiesOfClass(Player.class, aabb);
                        if (!players.isEmpty()) {
                            for (var bee : bees) {
                                if (bee.getTarget() == null) {
                                    bee.setTarget(Util.getRandom(players, level.random));
                                }
                            }
                        }
                    }
                }
                beehive.releaseBeesAndResetHoneyLevel(level, state, pos, player, BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
                return InteractionResult.SUCCESS;
            case PumpkinBlock ignored:
                var direction = context.getClickedFace();
                direction = direction.getAxis() == Direction.Axis.Y ? player.getDirection().getOpposite() : direction;
                level.playSound(null, pos, SoundEvents.PUMPKIN_CARVE, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.setBlock(pos, Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(CarvedPumpkinBlock.FACING, direction), 11);
                var item = new ItemEntity(
                        level,
                        pos.getX() + 0.5 + direction.getStepX() * 0.65,
                        pos.getY() + 0.1,
                        pos.getZ() + 0.5 + direction.getStepZ() * 0.65,
                        new ItemStack(Items.PUMPKIN_SEEDS, 4)
                );
                item.setDeltaMovement(
                        0.05 * direction.getStepX() + level.random.nextDouble() * 0.02, 0.05, 0.05 * direction.getStepZ() + level.random.nextDouble() * 0.02
                );
                level.addFreshEntity(item);
                stack.hurtAndBreak(1, player, getSlotForHand(context.getHand()));
                level.gameEvent(player, GameEvent.SHEAR, pos);
                player.awardStat(Stats.ITEM_USED.get(this));
                return InteractionResult.SUCCESS;
            default:
                return InteractionResult.PASS;
        }
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        switch (entity) {
            case TameableDragonEntity dragon:
                if (player instanceof ServerPlayer $player) {
                    var level = $player.serverLevel();
                    if (dragon.isOwnedBy($player) && dragon.readyForShearing(level, stack) && dragon.shear(level, $player, stack, dragon.blockPosition(), SoundSource.PLAYERS)) {
                        stack.hurtAndBreak(20, level, $player, item -> $player.onEquippedItemBroken(item, getSlotForHand(hand)));
                        return InteractionResult.SUCCESS;
                    }
                    return InteractionResult.FAIL;
                }
                return Relation.denyIfNotOwner(dragon, player) ? InteractionResult.FAIL : InteractionResult.SUCCESS;
            case Wolf wolf:
                ItemStack armor;
                if (wolf.isOwnedBy(player) && wolf.isWearingBodyArmor() && (!EnchantmentHelper.has(
                        armor = wolf.getBodyArmorItem(), EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE
                ) || player.isCreative())) {
                    stack.hurtAndBreak(1, player, getSlotForHand(hand));
                    wolf.playSound(SoundEvents.ARMOR_UNEQUIP_WOLF);
                    wolf.setBodyArmorItem(ItemStack.EMPTY);
                    if (wolf.level() instanceof ServerLevel level) {
                        wolf.spawnAtLocation(level, armor);
                    }
                    return InteractionResult.SUCCESS;
                }
                return super.interactLivingEntity(stack, player, entity, hand);
            case Shearable shearable:
                if (shearable.readyForShearing()) {
                    if (entity.level() instanceof ServerLevel level) {
                        shearable.shear(level, SoundSource.PLAYERS, stack);
                        entity.gameEvent(GameEvent.SHEAR, player);
                        stack.hurtAndBreak(1, player, getSlotForHand(hand));
                    }
                    return InteractionResult.SUCCESS;
                }
                return super.interactLivingEntity(stack, player, entity, hand);
            default:
                return super.interactLivingEntity(stack, player, entity, hand);
        }
    }
}
