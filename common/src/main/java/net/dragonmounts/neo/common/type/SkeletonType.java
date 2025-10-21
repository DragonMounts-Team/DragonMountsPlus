package net.dragonmounts.neo.common.type;

import net.dragonmounts.neo.common.api.DragonTypified;
import net.dragonmounts.neo.common.client.ClientDragonEntity;
import net.dragonmounts.neo.common.entity.ai.behavior.RangedAttack;
import net.dragonmounts.neo.common.entity.dragon.ServerDragonEntity;
import net.dragonmounts.neo.common.entity.dragon.TameableDragonEntity;
import net.dragonmounts.neo.common.init.DMSounds;
import net.dragonmounts.neo.common.init.DragonTypes;
import net.dragonmounts.neo.compat.registry.DragonType;
import net.dragonmounts.neo.compat.registry.DragonTypeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public class SkeletonType extends DragonType {
    public SkeletonType(ResourceLocation identifier, DragonTypeBuilder builder) {
        super(identifier, builder);
    }

    @Override
    public boolean isInHabitat(LivingEntity entity) {
        //noinspection deprecation
        return entity.getY() * 5 < entity.level().getHeight() && entity.getLightLevelDependentMagicValue() < 0.25;
    }

    @Override
    public <T extends LivingEntity & DragonTypified.Mutable> void onThunderHit(T entity, LightningBolt bolt) {
        super.onThunderHit(entity, bolt);
        convertByLightning(entity, DragonTypes.WITHER);
    }

    @Override
    public @Nullable RangedAttack<? super ServerDragonEntity> initRangedAttack(ServerDragonEntity dragon) {
        return null;
    }

    @Override
    public @Nullable RangedAttack<? super ClientDragonEntity> initRangedAttack(ClientDragonEntity dragon) {
        return null;
    }

    @Override
    public SoundEvent getAmbientSound(TameableDragonEntity dragon) {
        return dragon.isBaby() ? DMSounds.DRAGON_PURR_SKELETON_HATCHLING : DMSounds.DRAGON_PURR_SKELETON;
    }
}
