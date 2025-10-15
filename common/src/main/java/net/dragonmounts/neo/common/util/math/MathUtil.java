package net.dragonmounts.neo.common.util.math;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;

public class MathUtil {
    public static final AABB ZERO_AABB = new AABB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    public static final float PI = (float) Math.PI;
    public static final float TO_RAD_FACTOR = PI / 180F;
    public static final float HALF_RAD_FACTOR = TO_RAD_FACTOR / 2F;
    /**
     * Copy the value of {@link net.minecraft.client.model.EntityModel#MODEL_Y_OFFSET},
     * to make it accessible in server side
     */
    public static final float MOJANG_MODEL_OFFSET_Y = 1.501F;
    /**
     * Found in 1.12.2 {@code RenderLivingBase::prepareScale}
     */
    public static final float MOJANG_MODEL_SCALE = 0.0625F;

    public static float clamp(float value) {
        return Mth.clamp(value, 0F, 1F);
    }

    /**
     * return a random value from a truncated gaussian distribution with
     * mean and standard deviation = threeSigma/3
     * distribution is truncated to +/- threeSigma.
     *
     * @param mean       the mean of the distribution
     * @param threeSigma three times the standard deviation of the distribution
     */
    public static double getTruncatedGaussian(RandomSource random, double mean, double threeSigma) {
        return mean + Mth.clamp(
                random.nextGaussian(),
                -3.0,
                +3.0
        ) * threeSigma / 3.0;
    }
}
