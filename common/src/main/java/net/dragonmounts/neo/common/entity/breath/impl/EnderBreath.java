package net.dragonmounts.neo.common.entity.breath.impl;

import net.dragonmounts.neo.common.entity.breath.BreathAffectedBlock;
import net.dragonmounts.neo.common.entity.breath.DragonBreathSpec;
import net.dragonmounts.neo.common.entity.dragon.TameableDragonEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class EnderBreath extends DragonBreathSpec {
    public EnderBreath(TameableDragonEntity dragon, float damage) {
        super(dragon, damage);
    }

    @Override
    public BreathAffectedBlock affectBlock(ServerLevel level, long location, BreathAffectedBlock hit) {
        var pos = BlockPos.of(location);
        var state = level.getBlockState(pos);
        if (!state.isAir() && level.random.nextFloat() < 0.002F) {
            var cloud = createEffectCloud(level, pos, 1.6F, 750);
            cloud.setOwner(this.dragon);
            cloud.setParticle(ParticleTypes.DRAGON_BREATH);
            cloud.addEffect(new MobEffectInstance(MobEffects.HARM, 1));
            level.addFreshEntity(cloud);
        }
        return new BreathAffectedBlock(); // reset to zero
    }
}
