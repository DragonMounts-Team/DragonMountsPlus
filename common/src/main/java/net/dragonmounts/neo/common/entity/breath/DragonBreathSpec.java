package net.dragonmounts.neo.common.entity.breath;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.dragonmounts.neo.common.entity.dragon.DragonLifeStage;
import net.dragonmounts.neo.common.entity.dragon.ServerDragonEntity;
import net.dragonmounts.neo.common.entity.dragon.TameableDragonEntity;
import net.dragonmounts.neo.common.init.DMSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static net.dragonmounts.neo.common.entity.breath.impl.ServerBreathAdapter.*;

/**
 * Created by TGG on 5/08/2015.
 */
public abstract class DragonBreathSpec {
    public static AreaEffectCloud createEffectCloud(ServerLevel level, BlockPos pos, float radius, int duration) {
        var cloud = new AreaEffectCloud(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        cloud.setRadius(radius);
        cloud.setDuration(duration);
        cloud.setRadiusPerTick((1.0F - radius) / duration);
        return cloud;
    }

    protected final TameableDragonEntity dragon;
    public final float damage;

    public DragonBreathSpec(TameableDragonEntity dragon, float damage) {
        this.dragon = dragon;
        this.damage = damage;
    }

    public Vec3 getSpawnPosition(TameableDragonEntity dragon) {
        return dragon.getHeadRelativeOffset(0.0F, -10.0F, 24.0F);
    }

    public void collide(
            ServerDragonEntity dragon,
            boolean breathing,
            List<BreathNodeEntity> nodes,
            Long2ObjectMap<BreathAffectedBlock> affectedBlocks,
            Map<LivingEntity, BreathAffectedEntity> affectedEntities
    ) {
        if (breathing) {
            nodes.add(new BreathNodeEntity(dragon, this.getSpawnPosition(dragon)));
        }
        if (nodes.isEmpty()) return;
        var level = (ServerLevel) dragon.level();
        updateBlockAndEntityHitDensities(level, this, nodes, affectedBlocks, affectedEntities);
        implementEffectsOnBlocksTick(this, level, affectedBlocks);
        implementEffectsOnEntitiesTick(this, level, affectedEntities);
        // decay the hit densities of the affected blocks and entities (eg for flame weapon - cools down)
        Predicate<BreathEffectHandler> predicate = BreathEffectHandler::decayEffectTick;
        affectedBlocks.values().removeIf(predicate);
        affectedEntities.values().removeIf(predicate);
    }

    /**
     * if the hitDensity is high enough, manipulate the block (eg set fire to it)
     *
     * @return the updated block hit density
     */
    public abstract BreathAffectedBlock affectBlock(ServerLevel level, long location, BreathAffectedBlock hit);

    public boolean canAffect(LivingEntity entity) {
        return !this.dragon.isPassengerOfSameVehicle(entity);
    }

    public void affectEntity(ServerLevel level, LivingEntity target, BreathAffectedEntity hit) {
        target.hurtServer(level, level.damageSources().mobAttack(this.dragon), this.damage * hit.getHitDensity());
    }

    public SoundEvent getStartSound(DragonLifeStage stage) {
        return switch (stage) {
            case ADULT -> DMSounds.DRAGON_BREATH_START_ADULT;
            case JUVENILE -> DMSounds.DRAGON_BREATH_START_JUVENILE;
            default -> DMSounds.DRAGON_BREATH_START_HATCHLING;
        };
    }

    public SoundEvent getLoopSound(DragonLifeStage stage) {
        return switch (stage) {
            case ADULT -> DMSounds.DRAGON_BREATH_LOOP_ADULT;
            case JUVENILE -> DMSounds.DRAGON_BREATH_LOOP_JUVENILE;
            default -> DMSounds.DRAGON_BREATH_LOOP_HATCHLING;
        };
    }

    public SoundEvent getStopSound(DragonLifeStage stage) {
        return switch (stage) {
            case ADULT -> DMSounds.DRAGON_BREATH_STOP_ADULT;
            case JUVENILE -> DMSounds.DRAGON_BREATH_STOP_JUVENILE;
            default -> DMSounds.DRAGON_BREATH_STOP_HATCHLING;
        };
    }
}
