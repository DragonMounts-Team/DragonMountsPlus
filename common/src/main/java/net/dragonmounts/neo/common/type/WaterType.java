package net.dragonmounts.neo.common.type;

import net.dragonmounts.neo.common.api.DragonTypified;
import net.dragonmounts.neo.common.entity.breath.DragonBreathSpec;
import net.dragonmounts.neo.common.entity.breath.impl.WaterBreath;
import net.dragonmounts.neo.common.entity.dragon.TameableDragonEntity;
import net.dragonmounts.neo.common.init.DMSounds;
import net.dragonmounts.neo.common.init.DragonTypes;
import net.dragonmounts.neo.compat.registry.DragonTypeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;

public class WaterType extends BreathableType {
    public WaterType(ResourceLocation identifier, DragonTypeBuilder builder) {
        super(identifier, builder);
    }

    @Override
    public <T extends LivingEntity & DragonTypified.Mutable> void onThunderHit(T entity, LightningBolt bolt) {
        super.onThunderHit(entity, bolt);
        convertByLightning(entity, DragonTypes.STORM);
    }

    @Override
    public DragonBreathSpec initBreath(TameableDragonEntity dragon) {
        return new WaterBreath(dragon, 0.7F);
    }

    @Override
    public SoundEvent getAmbientSound(TameableDragonEntity dragon) {
        return dragon.isBaby() ? DMSounds.DRAGON_PURR_HATCHLING : DMSounds.DRAGON_AMBIENT_WATER;
    }

    @Override
    public SoundEvent getRoarSound(TameableDragonEntity dragon) {
        return dragon.isBaby() ? DMSounds.DRAGON_ROAR_HATCHLING : DMSounds.DRAGON_ROAR_WATER;
    }
}
