package net.dragonmounts.neo.common.client.breath;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dragonmounts.neo.common.entity.breath.BreathNode;
import net.dragonmounts.neo.common.entity.breath.BreathNodeHost;
import net.dragonmounts.neo.common.entity.breath.BreathParticleOption;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public abstract class BreathParticle extends TextureSheetParticle implements BreathNodeHost {
    public static final float NORMAL_PARTICLE_CHANCE = 0.1F;
    public static final float SPECIAL_PARTICLE_CHANCE = 0.3F;
    public final BreathNode node;
    private boolean collided;
    private boolean inWater;
    private float lastQuadSize;

    public BreathParticle(
            BreathParticleOption option,
            TextureAtlasSprite sprite,
            ClientLevel level,
            double x,
            double y,
            double z,
            double motionX,
            double motionY,
            double motionZ
    ) {
        super(level, x, y, z);
        this.setSprite(sprite);
        this.node = new BreathNode(option.power(), this.random);
        this.lastQuadSize = this.quadSize = this.getRenderSize();
        Vec3 motion = this.node.getRandomisedStartingMotion(new Vec3(motionX, motionY, motionZ), this.random);
        this.xd = motion.x;
        this.yd = motion.y;
        this.zd = motion.z;
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    protected float getRenderSize() {
        return this.node.getCurrentRenderDiameter();
    }

    protected abstract void tickIfAlive();

    @Override
    public final float getQuadSize(float partialTicks) {
        return Mth.lerp(partialTicks, this.lastQuadSize, this.quadSize);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.lastQuadSize = this.quadSize;
        this.quadSize = this.getRenderSize();
        float size = this.node.getCurrentCollisionSize() * 0.2F;
        this.setSize(size, size);
        if (this.removed) return;
        this.move(this.xd, this.yd, this.zd);
        var pos = BlockPos.containing(this.x, this.y, this.z);
        var fluid = this.level.getFluidState(pos);
        this.inWater = fluid.is(FluidTags.WATER) && this.y < pos.getY() + fluid.getHeight(this.level, pos);
        if (this.node.updateAge(this)) {
            this.remove();
        } else {
            this.tickIfAlive();
        }
    }

    @Override
    public final void move(double x, double y, double z) {
        double motionLen = x * x + y * y + z * z;
        var movement = motionLen == 0.0 ? Vec3.ZERO : collectCollision(this, x, y, z);
        double len = movement.lengthSqr();
        if (len > 1.0E-7 || motionLen - len < 1.0E-7) {
            this.setPos(this.x + movement.x, this.y + movement.y, this.z + movement.z);
        }
        this.collided = false;
        if (Math.abs(y) > 0.0) {
            this.collided = y != movement.y;
        }
        //noinspection SuspiciousNameCombination
        if (!Mth.equal(x, movement.x)) {
            this.collided = true;
            this.xd = 0.0;
        }
        if (!Mth.equal(z, movement.z)) {
            this.collided = true;
            this.zd = 0.0;
        }
        if (y != movement.y) {
            this.yd = 0.0;
        }
    }

    public static boolean isInsideCloseToBorder(WorldBorder border, double x, double y, AABB bounds) {
        double size = Math.max(Mth.absMax(bounds.getXsize(), bounds.getZsize()), 1.0);
        return border.getDistanceToBorder(x, y) < size * 2.0 && border.isWithinBounds(x, y, size);
    }

    public static Vec3 collectCollision(BreathParticle particle, double motionX, double motionY, double motionZ) {
        var box = particle.getBoundingBox();
        var moved = box.expandTowards(motionX, motionY, motionZ);
        var candidates = particle.level.getEntityCollisions(null, moved);
        var shapes = new ObjectArrayList<VoxelShape>(candidates.size() + 16);
        shapes.addAll(candidates);
        var border = particle.level.getWorldBorder();
        if (isInsideCloseToBorder(border, particle.x, particle.y, moved)) {
            shapes.add(border.getCollisionShape());
        }
        particle.level.getBlockCollisions(null, moved).forEach(shapes::add);
        if (shapes.isEmpty()) return new Vec3(motionX, motionY, motionZ);
        if (motionY != 0.0) {
            motionY = Shapes.collide(Direction.Axis.Y, box, shapes, motionY);
            if (motionY != 0.0) {
                box = box.move(0.0, motionY, 0.0);
            }
        }

        boolean bl = Math.abs(motionX) < Math.abs(motionZ);
        if (bl && motionZ != 0.0) {
            motionZ = Shapes.collide(Direction.Axis.Z, box, shapes, motionZ);
            if (motionZ != 0.0) {
                box = box.move(0.0, 0.0, motionZ);
            }
        }

        if (motionX != 0.0) {
            motionX = Shapes.collide(Direction.Axis.X, box, shapes, motionX);
            if (!bl && motionX != 0.0) {
                box = box.move(motionX, 0.0, 0.0);
            }
        }

        if (!bl && motionZ != 0.0) {
            motionZ = Shapes.collide(Direction.Axis.Z, box, shapes, motionZ);
        }

        return new Vec3(motionX, motionY, motionZ);
    }

    @Override
    public boolean shouldExtinguish() {
        return this.inWater;
    }

    @Override
    public boolean isCollided() {
        return this.collided;
    }

    @Override
    public double getMotionLengthSqr() {
        return this.xd * this.xd + this.yd * this.yd + this.zd * this.zd;
    }
}
