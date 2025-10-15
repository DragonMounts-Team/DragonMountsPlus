package net.dragonmounts.neo.common.entity.breath.impl;


import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.dragonmounts.neo.common.entity.breath.*;
import net.dragonmounts.neo.common.entity.dragon.ServerDragonEntity;
import net.dragonmounts.neo.common.util.math.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.LongFunction;

/**
 * Created by TGG on 30/07/2015.
 * BreathAffectedArea base class
 * Represents the area of the world (blocks, entities) affected by the breathweapon.
 * Usage:
 * (1) Construct from a BreathWeapon
 * (2) continueBreathing() once per tick whenever the dragon is breathing
 * (3) updateTick() every tick to update the area of effect, and implement breathweapon effects on the blocks &
 * entities within the area of effect
 */
public class ServerBreathHelper extends DragonBreathHelper<ServerDragonEntity> {
    private final ObjectArrayList<BreathNodeEntity> breathNodes = new ObjectArrayList<>();
    private final Long2ObjectOpenHashMap<BreathAffectedBlock> affectedBlocks = new Long2ObjectOpenHashMap<>();
    private final Reference2ObjectOpenHashMap<LivingEntity, BreathAffectedEntity> affectedEntities = new Reference2ObjectOpenHashMap<>();

    public ServerBreathHelper(ServerDragonEntity dragon) {
        super(dragon);
    }

    /**
     * updates the BreathAffectedArea, called once per tick
     */
    @Override
    public void tick() {
        ++this.tickCounter;
        var breath = this.breath;
        if (breath == null) return;
        var dragon = this.dragon;
        this.updateBreathState(dragon.isBreathing());
        breath.collide(
                dragon,
                BreathState.SUSTAIN == this.currentBreathState,
                this.breathNodes,
                this.affectedBlocks,
                this.affectedEntities
        );
    }

    public static void implementEffectsOnBlocksTick(DragonBreath breath, ServerLevel level, Long2ObjectMap<BreathAffectedBlock> affectedBlocks) {
        for (Long2ObjectMap.Entry<BreathAffectedBlock> entry : affectedBlocks.long2ObjectEntrySet()) {
            entry.setValue(breath.affectBlock(level, entry.getLongKey(), entry.getValue()));
        }
    }

    public static void implementEffectsOnEntitiesTick(DragonBreath breath, ServerLevel level, Map<LivingEntity, BreathAffectedEntity> affectedEntities) {
        for (var iterator = affectedEntities.entrySet().iterator(); iterator.hasNext(); ) {
            var entry = iterator.next();
            var target = entry.getKey();
            if (target.isRemoved()) {
                iterator.remove();
                continue;
            }
            breath.affectEntity(level, target, entry.getValue());
        }
    }

    /**
     * Models the collision of the breath nodes on the world blocks and entities:
     * Each breathnode which contacts a world block will increase the corresponding 'hit density' by an amount proportional
     * to the intensity of the node and the degree of overlap between the node and the block.
     * Likewise for the entities contacted by the breathnode
     *
     * @param world
     * @param entityBreathNodes the breathnodes in the breath weapon beam  - parallel to nodeLineSegments, must correspond 1:1
     * @param affectedBlocks    each block touched by the beam has an entry in this map.  The hitDensity (float) is increased
     *                          every time a node touches it.  blocks without an entry haven't been touched.
     * @param affectedEntities  every entity touched by the beam has an entry in this map (entityID).  The hitDensity (float)
     *                          for an entity is increased every time a node touches it.  entities without an entry haven't
     *                          been touched.
     */
    public static void updateBlockAndEntityHitDensities(
            Level world,
            DragonBreath breath,
            List<BreathNodeEntity> entityBreathNodes,
            Long2ObjectMap<BreathAffectedBlock> affectedBlocks,
            Map<LivingEntity, BreathAffectedEntity> affectedEntities
    ) {
        var fullBox = MathUtil.ZERO_AABB;
        Iterator<BreathNodeEntity> it = entityBreathNodes.iterator();
        while (it.hasNext()) {
            BreathNodeEntity entity = it.next();
            if (entity.isRemoved()) {
                it.remove();
            } else {
                fullBox = fullBox.minmax(entity.update(affectedBlocks).box);
            }
        }

        var occupiedByEntities = new Long2ObjectOpenHashMap<ObjectArrayList<LivingEntity>>();
        LongFunction<ObjectArrayList<LivingEntity>> list = ignored -> new ObjectArrayList<>();
        for (var candidate : world.getEntitiesOfClass(LivingEntity.class, fullBox, breath::canAffect)) {
            var aabb = candidate.getBoundingBox();
            for (int x = (int) aabb.minX, maxX = (int) aabb.maxX; x <= maxX; ++x) {
                for (int y = (int) aabb.minY, maxY = (int) aabb.maxY; y <= maxY; ++y) {
                    for (int z = (int) aabb.minZ, maxZ = (int) aabb.maxZ; z <= maxZ; ++z) {
                        occupiedByEntities.computeIfAbsent(BlockPos.asLong(x, y, z), list).add(candidate);
                    }
                }
            }
        }

        Function<LivingEntity, BreathAffectedEntity> fallback = ignored -> new BreathAffectedEntity();
        for (var node : entityBreathNodes) {
            var segment = node.getSegment();
            var aabb = segment.box;
            var checked = node.checked;
            for (int x = (int) aabb.minX, maxX = (int) aabb.maxX; x <= maxX; ++x) {
                for (int y = (int) aabb.minY, maxY = (int) aabb.maxY; y <= maxY; ++y) {
                    for (int z = (int) aabb.minZ, maxZ = (int) aabb.maxZ; z <= maxZ; ++z) {
                        var entitiesHere = occupiedByEntities.get(BlockPos.asLong(x, y, z));
                        if (entitiesHere == null) continue;
                        for (LivingEntity entity : entitiesHere) {
                            if (checked.add(entity)) {
                                float hitDensity = segment.collisionCheckAABB(entity.getBoundingBox());
                                if (hitDensity > 0.0) {
                                    affectedEntities.computeIfAbsent(entity, fallback)
                                            .addHitDensity(segment.unit, hitDensity);
                                }
                            }
                        }
                    }
                }
            }
            checked.clear();
        }
    }
}
