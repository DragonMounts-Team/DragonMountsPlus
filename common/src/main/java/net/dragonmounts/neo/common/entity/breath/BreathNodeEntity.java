package net.dragonmounts.neo.common.entity.breath;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.dragonmounts.neo.common.entity.dragon.ServerDragonEntity;
import net.dragonmounts.neo.common.init.DMEntities;
import net.dragonmounts.neo.common.util.Collision;
import net.dragonmounts.neo.common.util.EntityUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.UnknownNullability;

/**
 * Created by TGG on 31/07/2015.
 * Server side; tracks the position, motion, and collision detection of a breath node in a breath weapon stream,
 * Used with an associated BreathNode to track age, size and initial speed
 * <p>
 * Usage:
 * 1) construct using createEntityBreathNodeServer
 * 2) call onUpdate() every tick to move and collide
 * 3) various getters for intensity, radius, and recent collisions.
 */
@SuppressWarnings("UnstableApiUsage")
@NotNullByDefault
public class BreathNodeEntity extends Entity implements BreathNodeHost {
    private final BreathNode node;
    public final ReferenceOpenHashSet<LivingEntity> checked = new ReferenceOpenHashSet<>();
    private final ObjectArrayList<Collision> collisions = new ObjectArrayList<>();
    private @UnknownNullability NodeLineSegment segment;
    private boolean collided;

    public BreathNodeEntity(EntityType<?> type, Level level) {
        super(type, level);
        throw new UnsupportedOperationException();
    }

    public BreathNodeEntity(ServerDragonEntity dragon, Vec3 pos) {
        super(DMEntities.DRAGON_BREATH.get(), dragon.level());
        // don't randomise the other properties (size, age) on the server.
        this.node = new BreathNode(dragon.getLifeStage().power, null);
        this.setDeltaMovement(this.node.getRandomisedStartingMotion(dragon.getLookAngle(), this.random));
        this.setPos(pos);
    }

    public NodeLineSegment update(Long2ObjectMap<BreathAffectedBlock> hitDensity) {
        Vec3 prevPos = this.position();
        this.refreshDimensions();
        this.updateInWaterStateAndDoFluidPushing();
        EntityUtil.moveAndCollide(this, this.getDeltaMovement(), this.collisions);
        this.collided = this.horizontalCollision || this.verticalCollision;
        if (this.node.updateAge(this)) {
            this.remove(RemovalReason.DISCARDED);
        }
        return this.segment = new NodeLineSegment(
                this.random,
                prevPos,
                this.position(),
                this.getCurrentRadius(),
                this.node.getCurrentIntensity(),
                hitDensity,
                this.collisions
        );
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return super.getDimensions(pose).scale(this.node.getCurrentCollisionSize());
    }

    /**
     * Get a collection of the collisions that occurred during the last tick update
     *
     * @return returns a collection showing which parts of the entity collided with an object- eg
     * (WEST, [3,2,6]-->[3.5, 2, 6] means the west face of the entity collided; the entity tried to move to
     * x = 3, but got pushed back to x=3.5
     */
    public NodeLineSegment getSegment() {
        return this.segment;
    }

    public float getCurrentRadius() {
        return node.getCurrentDiameterOfEffect() * 0.5F;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {}

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {}

    @Override
    public boolean shouldExtinguish() {
        return this.isInWater();
    }

    @Override
    public boolean isCollided() {
        return this.collided;
    }

    @Override
    public double getMotionLengthSqr() {
        return this.getDeltaMovement().lengthSqr();
    }
}
