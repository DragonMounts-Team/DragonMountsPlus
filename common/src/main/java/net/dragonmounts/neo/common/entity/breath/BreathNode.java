package net.dragonmounts.neo.common.entity.breath;

import net.dragonmounts.neo.common.util.math.MathUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by TGG on 30/07/2015.
 * BreathNode represents the age, size, and initial speed of each node in a breath weapon stream;
 * It is used with an associated Entity:
 * BreathNode tracks the age and size
 * Entity tracks the position, motion, and collision detection
 * <p>
 * updateAge() is used to keep the two synchronised
 * <p>
 * A breathnode has three characteristic diameters:
 * 1) getCurrentRenderDiameter() - the size for rendering
 * 2) getCurrentDiameterOfEffect() - the diameter that will be affected by the breath weapon node
 * 3) getCurrentAABBCollionSize() - the size used for collision detection with entities or the world
 */
public class BreathNode {
    private static final int DEFAULT_AGE_IN_TICKS = 40;
    private static final float RATIO_OF_RENDER_DIAMETER_TO_EFFECT_DIAMETER = 0.375F;
    private static final float RATIO_OF_COLLISION_DIAMETER_TO_EFFECT_DIAMETER = 0.5F;  // change to 0.5F
    private static final float INITIAL_SPEED = 1.2F; // blocks per tick at full speed
    private static final float NODE_DIAMETER_IN_BLOCKS = 2.0F;
    private static final float YOUNG_AGE = 0.25F;
    private static final float OLD_AGE = 0.75F;

    public final BreathPower power;
    public final float lifespan;
    public final float maxSize;

    public BreathNode(@NotNull BreathPower power, @Nullable RandomSource random) {
        this.power = power;
        float lifespan = DEFAULT_AGE_IN_TICKS * power.lifetime;
        float maxSize = NODE_DIAMETER_IN_BLOCKS * power.size;
        if (random != null) {
            // Randomise the maximum lifetime and the node size
            lifespan *= (float) MathUtil.getTruncatedGaussian(random, 1, AGE_VARIATION_FACTOR);
            maxSize *= (float) MathUtil.getTruncatedGaussian(random, 1, SIZE_VARIATION_FACTOR);
        }
        this.lifespan = lifespan;
        this.maxSize = maxSize;
    }

    private float ageTicks;

    private static final double SPEED_VARIATION_ABS = 0.05;  // plus or minus this amount (3 std deviations)
    private static final double AGE_VARIATION_FACTOR = 0.25;  // plus or minus this amount (3 std deviations)
    private static final double SIZE_VARIATION_FACTOR = 0.25;   // plus or minus this amount (3 std deviations)

    /**
     * Get an initial motion vector for this node, randomised around the initialDirection
     *
     * @param direction the initial side
     * @return the initial motion vector (speed and side)
     */
    public Vec3 getRandomisedStartingMotion(Vec3 direction, RandomSource random) {
        float speed = getStartingSpeed();
        Vec3 normalized = direction.normalize();

        double actualMotionX = normalized.x + MathUtil.getTruncatedGaussian(random, 0, SPEED_VARIATION_ABS);
        double actualMotionY = normalized.y + MathUtil.getTruncatedGaussian(random, 0, SPEED_VARIATION_ABS);
        double actualMotionZ = normalized.z + MathUtil.getTruncatedGaussian(random, 0, SPEED_VARIATION_ABS);
        return new Vec3(actualMotionX * speed, actualMotionY * speed, actualMotionZ * speed);
    }

    public float getStartingSpeed() {
        return this.power.speed * INITIAL_SPEED;
    }

    public float getAgeTicks() {
        return ageTicks;
    }

    /**
     * Update the age of the node based on what is happening (collisions) to the associated entity
     * Should be called once per tick
     *
     * @param host the entity associated with this node
     */
    public boolean updateAge(BreathNodeHost host) {
        if (host.shouldExtinguish() || ++ageTicks > this.lifespan) return true;

        // collision ages breath node faster
        if (host.isCollided() && (ageTicks += 5) > this.lifespan) return true;

        // slow breath nodes age very fast (they look silly when sitting still)
        final double SPEED_THRESHOLD = getStartingSpeed() * 0.25;
        if (host.getMotionLengthSqr() < SPEED_THRESHOLD * SPEED_THRESHOLD) {
            ageTicks += 20;
        }
        return ageTicks > this.lifespan;
    }

    /**
     * get the current render size (diameter) of the breathnode in blocks
     *
     * @return the rendering size (diameter) of the breathnode in blocks
     */
    public float getCurrentRenderDiameter() {
        return getCurrentDiameterOfEffect() * RATIO_OF_RENDER_DIAMETER_TO_EFFECT_DIAMETER;
    }

    /**
     * get the current width and height of the breathnode collision AABB, in blocks
     *
     * @return the width and height of the breathnode collision AABB, in blocks
     */
    public float getCurrentCollisionSize() {
        return getCurrentDiameterOfEffect() * RATIO_OF_COLLISION_DIAMETER_TO_EFFECT_DIAMETER;
    }

    /**
     * get the current size (diameter) of the area of effect of the breath node, in blocks
     *
     * @return the size (diameter) of the area of effect of the breathnode in blocks
     */
    public float getCurrentDiameterOfEffect() {
        float lifetimeFraction = getLifetimeFraction();

        float fractionOfFullSize = 1.0F;
        if (lifetimeFraction < YOUNG_AGE) {
            fractionOfFullSize = Mth.sin(lifetimeFraction / YOUNG_AGE * Mth.HALF_PI);
        }

        return this.maxSize * (MathUtil.clamp(fractionOfFullSize) * 0.8F + 0.2F);
    }


    /**
     * returns the current intensity of the node (eg for flame = how hot it is)
     *
     * @return current relative intensity - 0.0 = none, 1.0 = full
     */
    public float getCurrentIntensity() {
        float lifetimeFraction = getLifetimeFraction();

        float fractionOfFullPower = 1.0F;
        if (lifetimeFraction >= 1.0F) return 0.0F;
        if (lifetimeFraction < YOUNG_AGE) {
            fractionOfFullPower = Mth.sin(lifetimeFraction / YOUNG_AGE * Mth.HALF_PI);
        } else if (lifetimeFraction > OLD_AGE) {
            fractionOfFullPower = Mth.sin((1.0F - lifetimeFraction) / (1.0F - OLD_AGE) * Mth.HALF_PI);
        }
        return fractionOfFullPower * this.power.intensity;
    }


    public float getLifetimeFraction() {
        return Mth.clamp(this.ageTicks / this.lifespan, 0.0F, 1.0F);
    }
}

