package net.dragonmounts.neo.common.type;

import net.dragonmounts.neo.common.entity.breath.DragonBreathSpec;
import net.dragonmounts.neo.common.entity.breath.impl.ZombieBreath;
import net.dragonmounts.neo.common.entity.dragon.TameableDragonEntity;
import net.dragonmounts.neo.common.init.DMSounds;
import net.dragonmounts.neo.compat.registry.DragonTypeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class ZombieType extends BreathableType {
    public ZombieType(ResourceLocation identifier, DragonTypeBuilder builder) {
        super(identifier, builder);
    }

    @Override
    public DragonBreathSpec initBreath(TameableDragonEntity dragon) {
        return new ZombieBreath(dragon, 0.6F);
    }

    @Override
    public SoundEvent getAmbientSound(TameableDragonEntity dragon) {
        return dragon.isBaby() ? DMSounds.DRAGON_PURR_HATCHLING : DMSounds.DRAGON_PURR_ZOMBIE;
    }

    @Override
    public SoundEvent getDeathSound(TameableDragonEntity dragon) {
        return DMSounds.DRAGON_DEATH_ZOMBIE;
    }
}
