package net.dragonmounts.neo.common.entity.breath.impl;

import net.dragonmounts.neo.common.entity.breath.BreathAffectedBlock;
import net.dragonmounts.neo.common.entity.breath.BreathAffectedEntity;
import net.dragonmounts.neo.common.entity.breath.DragonBreathSpec;
import net.dragonmounts.neo.common.entity.dragon.TameableDragonEntity;
import net.dragonmounts.neo.common.util.EntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;

public class WitherBreath extends DragonBreathSpec {
    public WitherBreath(TameableDragonEntity dragon, float damage) {
        super(dragon, damage);
    }

    @Override
    public BreathAffectedBlock affectBlock(ServerLevel level, long location, BreathAffectedBlock hit) {
        var pos = BlockPos.of(location);
        var state = level.getBlockState(pos);
        if (!state.isAir() && level.random.nextFloat() < 0.002F) {
            var cloud = new AreaEffectCloud(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            cloud.setOwner(this.dragon);
            cloud.setParticle(ParticleTypes.SMOKE);
            cloud.setRadius(1.4F);
            cloud.setDuration(600);
            cloud.setRadiusPerTick((1.0F - cloud.getRadius()) / cloud.getDuration());
            cloud.addEffect(new MobEffectInstance(MobEffects.WITHER, 120));
            level.addFreshEntity(cloud);
        }
        return new BreathAffectedBlock(); // reset to zero
    }

    @Override
    public void affectEntity(ServerLevel level, LivingEntity target, BreathAffectedEntity hit) {
        float density = hit.getHitDensity();
        target.hurtServer(level, level.damageSources().mobAttack(this.dragon), this.damage * density);
        EntityUtil.addOrMergeEffect(target, MobEffects.POISON, (int) (4 * density), 0, false, true, true);
    }
}
