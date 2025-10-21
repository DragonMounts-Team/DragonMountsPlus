package net.dragonmounts.neo.common.entity.breath.impl;

import net.dragonmounts.neo.common.entity.breath.BreathAffectedBlock;
import net.dragonmounts.neo.common.entity.breath.BreathAffectedEntity;
import net.dragonmounts.neo.common.entity.breath.DragonBreathSpec;
import net.dragonmounts.neo.common.entity.dragon.DragonLifeStage;
import net.dragonmounts.neo.common.entity.dragon.TameableDragonEntity;
import net.dragonmounts.neo.common.init.DMSounds;
import net.dragonmounts.neo.common.tag.DMBlockTags;
import net.dragonmounts.neo.config.ServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Created by TGG on 7/12/2015.
 */
public class AetherBreath extends DragonBreathSpec {
    public static float getDestroyDensity(ServerLevel level, BlockPos pos, BlockState state) {
        var time = state.getDestroySpeed(level, pos);
        return time * time * 400;
    }

    public AetherBreath(TameableDragonEntity dragon, float damage) {
        super(dragon, damage);
    }

    @Override
    public BreathAffectedBlock affectBlock(ServerLevel level, long location, BreathAffectedBlock hit) {
        var pos = BlockPos.of(location);
        var state = level.getBlockState(pos);
        if (AbstractCandleBlock.isLit(state)) {
            AbstractCandleBlock.extinguish(null, state, level, pos);
            return new BreathAffectedBlock();
        } else if (CampfireBlock.isLitCampfire(state)) {
            level.levelEvent(null, 1009, pos, 0);
            level.setBlockAndUpdate(pos, state.setValue(CampfireBlock.LIT, false));
            return new BreathAffectedBlock();
        } else if (ServerConfig.INSTANCE.destructiveBreath.get() && state.is(DMBlockTags.AIRFLOW_DESTRUCTIBLE)) {
            // effects- which occur after the block has been exposed for sufficient time
            // soft blocks such as sand, leaves, grass, flowers, plants, etc get blown away (destroyed)
            // blows away snow but not ice
            // shatters panes, but not glass
            // extinguish torches
            // causes fire to spread rapidly
            if (hit.getMaxHitDensity() > getDestroyDensity(level, pos, state)) {
                level.destroyBlock(pos, true, this.dragon);
                return new BreathAffectedBlock();
            }
            // TODO spread fire
        }
        return hit;
    }

    @Override
    public void affectEntity(ServerLevel level, LivingEntity target, BreathAffectedEntity hit) {
        if (target.isOnFire()) {
            target.clearFire();
        }
        TameableDragonEntity dragon = this.dragon;

        //    System.out.format("Old entity motion:[%.2f, %.2f, %.2f]\n", entity.motionX, entity.motionY, entity.motionZ);
        // push in the direction of the wind, but add a vertical upthrust as well
        final double FORCE_MULTIPLIER = 0.05;
        final double VERTICAL_FORCE_MULTIPLIER = 0.05;
        float density = hit.getHitDensity();
        var direction = hit.getHitDirection();
//        Vec3d airForceDirection = hit.getHitDensityDirection();
//        Vec3d airMotion = MathX.multiply(airForceDirection, FORCE_MULTIPLIER);
        final double WT_ENTITY = 0.05;
        final double WT_AIR = 1 - WT_ENTITY;
        target.hurtServer(level, level.damageSources().mobAttack(dragon), this.damage * density);
        target.knockback(0.1F * density, -direction.x, -direction.z);
        /*
        if (density > 1.0) {
            final double GRAVITY_OFFSET = -0.08;
            target.motionY = WT_ENTITY * (target.motionY - GRAVITY_OFFSET) + WT_AIR * VERTICAL_FORCE_MULTIPLIER * density;
        }*/
    }

    @Override
    public SoundEvent getStartSound(DragonLifeStage stage) {
        return DMSounds.DRAGON_BREATH_START_AIRFLOW;
    }

    @Override
    public SoundEvent getLoopSound(DragonLifeStage stage) {
        return DMSounds.DRAGON_BREATH_LOOP_AIRFLOW;
    }

    @Override
    public SoundEvent getStopSound(DragonLifeStage stage) {
        return DMSounds.DRAGON_BREATH_STOP_AIRFLOW;
    }
}
