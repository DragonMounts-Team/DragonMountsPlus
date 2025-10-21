package net.dragonmounts.neo.common.entity.breath.impl;

import net.dragonmounts.neo.common.entity.breath.BreathAffectedBlock;
import net.dragonmounts.neo.common.entity.breath.BreathAffectedEntity;
import net.dragonmounts.neo.common.entity.breath.DragonBreathSpec;
import net.dragonmounts.neo.common.entity.dragon.DragonLifeStage;
import net.dragonmounts.neo.common.entity.dragon.TameableDragonEntity;
import net.dragonmounts.neo.common.init.DMSounds;
import net.dragonmounts.neo.config.ServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.material.Fluids;

public class IceBreath extends DragonBreathSpec {
    public IceBreath(TameableDragonEntity dragon, float damage) {
        super(dragon, damage);
    }

    @Override
    public BreathAffectedBlock affectBlock(ServerLevel level, long location, BreathAffectedBlock hit) {
        var pos = BlockPos.of(location);
        var state = level.getBlockState(pos);
        if (state.is(Blocks.LAVA)) {
            if (!ServerConfig.INSTANCE.quenchingBreath.get()) return hit;
            level.setBlockAndUpdate(pos, (
                    level.getFluidState(pos).isSource() ? Blocks.OBSIDIAN : Blocks.COBBLESTONE
            ).defaultBlockState());
            level.levelEvent(1501, pos, 0);
        } else if (state.is(Blocks.WATER)) {
            if (!level.getFluidState(pos).is(Fluids.WATER)) return hit; // requires full water block
            level.setBlockAndUpdate(pos, (
                    ServerConfig.INSTANCE.frostyBreath.get() ? Blocks.ICE : Blocks.FROSTED_ICE
            ).defaultBlockState());
        } else if (AbstractCandleBlock.isLit(state)) {
            AbstractCandleBlock.extinguish(null, state, level, pos);
        } else if (CampfireBlock.isLitCampfire(state)) {
            level.levelEvent(null, 1009, pos, 0);
            level.setBlockAndUpdate(pos, state.setValue(CampfireBlock.LIT, false));
        } else if (state.is(BlockTags.FIRE)) {
            level.destroyBlock(pos, true, this.dragon);
        } else {
            BlockPos upper;
            if (ServerConfig.INSTANCE.frostyBreath.get() && level.getBlockState(upper = pos.above()).isAir() && (
                    state.is(BlockTags.LEAVES) || state.isFaceSturdy(level, pos, Direction.UP)
            )) {
                level.setBlockAndUpdate(upper, Blocks.SNOW.defaultBlockState());
            }
        }
        return new BreathAffectedBlock(); // reset to zero
    }

    @Override
    public void affectEntity(ServerLevel level, LivingEntity target, BreathAffectedEntity hit) {
        float density = hit.getHitDensity();
        float damage = this.damage * density;
        if (target.isOnFire()) {
            target.clearFire();
            target.playSound(SoundEvents.GENERIC_EXTINGUISH_FIRE, 1.0f, 0.0f);
            damage *= 2;
        }
        if (target.canFreeze()) {
            target.setTicksFrozen(Math.min(target.getTicksRequiredToFreeze(), target.isInPowderSnow
                    ? target.getTicksFrozen() + 1
                    : target.getTicksFrozen() + 3
            ));
        }
        target.hurtServer(level, level.damageSources().mobAttack(this.dragon), damage);
        var direction = hit.getHitDirection();
        target.knockback(0.075F * density, -direction.x, -direction.z);
    }

    @Override
    public SoundEvent getStartSound(DragonLifeStage stage) {
        return DMSounds.DRAGON_BREATH_START_ICE;
    }

    @Override
    public SoundEvent getLoopSound(DragonLifeStage stage) {
        return DMSounds.DRAGON_BREATH_LOOP_ICE;
    }

    @Override
    public SoundEvent getStopSound(DragonLifeStage stage) {
        return DMSounds.DRAGON_BREATH_STOP_ICE;
    }
}
