package net.dragonmounts.neo.common.entity.breath.impl;

import net.dragonmounts.neo.common.entity.breath.BreathAffectedBlock;
import net.dragonmounts.neo.common.entity.breath.BreathAffectedEntity;
import net.dragonmounts.neo.common.entity.breath.DragonBreathSpec;
import net.dragonmounts.neo.common.entity.dragon.TameableDragonEntity;
import net.dragonmounts.neo.compat.platform.FlammableBlock;
import net.dragonmounts.neo.config.ServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT;

public class FireBreath extends DragonBreathSpec {
    public FireBreath(TameableDragonEntity dragon, float damage) {
        super(dragon, damage);
    }

    @Override
    public BreathAffectedBlock affectBlock(ServerLevel level, long location, BreathAffectedBlock hit) {
        // Flammable blocks: set fire to them once they have been exposed enough.  After sufficient exposure, destroy the
        //   block (otherwise -if it's raining, the burning block will keep going out)
        // Non-flammable blocks:
        // 1) liquids (except lava) evaporate
        // 2) If the block can be smelted (eg sand), then convert the block to the smelted version
        // 3) If the block can't be smelted then convert to lava
        var pos = BlockPos.of(location);
        var state = level.getBlockState(pos);
        if (this.litBlock(level, pos, state)) return new BreathAffectedBlock();
        boolean consume = false;
        boolean disableIgniting = !ServerConfig.INSTANCE.ignitingBreath.get();
        boolean enableSmelting = ServerConfig.INSTANCE.smeltingBreath.get();
        if (enableSmelting || !disableIgniting) {
            var random = level.random;
            float max = 0.0F;
            for (var facing : Direction.values()) {
                float density = hit.getHitDensity(facing);
                if (density > max) {
                    max = density;
                }
                if (disableIgniting
                        || random.nextFloat() < 0.0625F
                        || density < this.calcIgnitionThreshold(level, pos, state, facing)
                ) continue;
                var sideToIgnite = pos.relative(facing);
                if (level.getBlockState(sideToIgnite).isAir()) {
                    consume = true;
                    this.burnBlock(level, sideToIgnite, random);
                    //    if (densityOfThisFace >= thresholdForDestruction && state.getBlockHardness(level, pos) != -1 && DragonMountsConfig.canFireBreathAffectBlocks) {
                    //   level.setBlockToAir(pos);
                }
            }
            if (enableSmelting && max > 0.5F) {
                consume = true;
                this.smeltBlock(level, pos, state);
            }
        }
        return consume ? new BreathAffectedBlock() : hit;
    }

    @Override
    public void affectEntity(ServerLevel level, LivingEntity target, BreathAffectedEntity hit) {
        target.igniteForTicks(80);
        float damage = this.damage * hit.getHitDensity();
        if (target.isInPowderSnow || target.isFullyFrozen()) {
            damage *= 2.0F;
        } else if (target.isInWaterOrRain()) {
            damage *= 1.5F;
        }
        target.hurtServer(level, level.damageSources().mobAttack(this.dragon), damage);
    }

    protected boolean litBlock(ServerLevel level, BlockPos pos, BlockState state) {
        if (state.hasProperty(LIT) && !state.getValue(LIT)) {
            if (state.is(BlockTags.CAMPFIRES)) {
                level.levelEvent(null, 1009, pos, 0);
                level.setBlockAndUpdate(pos, state.setValue(LIT, true));
                return true;
            } else if (state.is(BlockTags.CANDLES) || state.is(BlockTags.CANDLE_CAKES)) {
                level.setBlock(pos, state.setValue(LIT, true), 11);
                return true;
            }
        }
        return false;
    }

    protected void smeltBlock(ServerLevel level, BlockPos pos, BlockState state) {
        if (state.isAir()) return;
        var input = new SingleRecipeInput(state.getCloneItemStack(level, pos, true));
        level.recipeAccess().getRecipeFor(RecipeType.SMELTING, input, level).ifPresent(holder -> {
            var stack = holder.value().assemble(input, level.registryAccess());
            if (stack.isEmpty()) return;
            if (stack.getItem() instanceof BlockItem item && item != Items.AIR) {
                level.setBlockAndUpdate(pos, item.getBlock().defaultBlockState());
            }
        });
    }

    protected void burnBlock(ServerLevel level, BlockPos sideToIgnite, RandomSource random) {
        level.setBlockAndUpdate(sideToIgnite, Blocks.FIRE.defaultBlockState());
        level.playSound(
                null,
                sideToIgnite.getX() + 0.5,
                sideToIgnite.getY() + 0.5,
                sideToIgnite.getZ() + 0.5,
                SoundEvents.FLINTANDSTEEL_USE,
                SoundSource.BLOCKS,
                1.0F,
                0.8F + random.nextFloat() * 0.4F
        );
    }

    protected float calcIgnitionThreshold(Level level, BlockPos pos, BlockState state, Direction side) {
        int flammability = FlammableBlock.getFlammability(level, pos, state, side);
        return flammability == 0 ? Float.MAX_VALUE : 15.0F / flammability;
    }
}
