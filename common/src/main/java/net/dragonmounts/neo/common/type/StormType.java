package net.dragonmounts.neo.common.type;

import net.dragonmounts.neo.common.api.DragonTypified;
import net.dragonmounts.neo.common.entity.breath.DragonBreathSpec;
import net.dragonmounts.neo.common.entity.breath.impl.StormBreath;
import net.dragonmounts.neo.common.entity.dragon.HatchableDragonEggEntity;
import net.dragonmounts.neo.common.entity.dragon.TameableDragonEntity;
import net.dragonmounts.neo.compat.registry.DragonTypeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;

import static net.dragonmounts.neo.common.util.EntityUtil.addOrMergeEffect;

public class StormType extends WaterType {
    public StormType(ResourceLocation identifier, DragonTypeBuilder builder) {
        super(identifier, builder);
    }

    @Override
    public <T extends LivingEntity & DragonTypified.Mutable> void onThunderHit(T entity, LightningBolt bolt) {
        if (entity instanceof HatchableDragonEggEntity) return;
        addOrMergeEffect(entity, MobEffects.DAMAGE_BOOST, 1200, 1, false, true, true);//35s
    }

    @Override
    public DragonBreathSpec initBreath(TameableDragonEntity dragon) {
        return new StormBreath(dragon, 0.7F);
    }
}
