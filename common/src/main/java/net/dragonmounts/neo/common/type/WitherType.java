package net.dragonmounts.neo.common.type;

import net.dragonmounts.neo.common.client.ClientDragonEntity;
import net.dragonmounts.neo.common.client.breath.impl.ClientBreathAdapter;
import net.dragonmounts.neo.common.entity.ai.behavior.RangedAttack;
import net.dragonmounts.neo.common.entity.breath.impl.ServerBreathAdapter;
import net.dragonmounts.neo.common.entity.breath.impl.WitherBreath;
import net.dragonmounts.neo.common.entity.dragon.ServerDragonEntity;
import net.dragonmounts.neo.common.entity.dragon.TameableDragonEntity;
import net.dragonmounts.neo.common.init.DMSounds;
import net.dragonmounts.neo.compat.registry.DragonTypeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class WitherType extends SkeletonType {
    public WitherType(ResourceLocation identifier, DragonTypeBuilder builder) {
        super(identifier, builder);
    }

    @Override
    public RangedAttack<? super ServerDragonEntity> initRangedAttack(ServerDragonEntity dragon) {
        return new ServerBreathAdapter(new WitherBreath(dragon, 0.6F));
    }

    @Override
    public RangedAttack<? super ClientDragonEntity> initRangedAttack(ClientDragonEntity dragon) {
        return new ClientBreathAdapter(new WitherBreath(dragon, 0.6F));
    }

    @Override
    public SoundEvent getAmbientSound(TameableDragonEntity dragon) {
        return dragon.isBaby() ? DMSounds.DRAGON_PURR_NETHER_HATCHLING : DMSounds.DRAGON_PURR_NETHER;
    }
}
