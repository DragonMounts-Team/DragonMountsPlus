package net.dragonmounts.neo.common.util.math;

import net.minecraft.client.model.EntityModel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;

import static net.minecraft.util.Mth.DEG_TO_RAD;

public class MathUtil {
    public static final AABB ZERO_AABB = new AABB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    public static final float HALF_RAD_FACTOR = DEG_TO_RAD * 0.5F;
    /**
     * Copy the value of {@link net.minecraft.client.model.EntityModel#MODEL_Y_OFFSET},
     * to make it accessible in server side
     */
    public static final float MOJANG_MODEL_OFFSET_Y = -EntityModel.MODEL_Y_OFFSET;
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
