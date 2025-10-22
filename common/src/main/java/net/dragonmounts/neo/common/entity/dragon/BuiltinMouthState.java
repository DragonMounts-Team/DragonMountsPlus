package net.dragonmounts.neo.common.entity.dragon;

import net.dragonmounts.neo.common.client.model.dragon.DragonAnimator;
import net.dragonmounts.neo.common.util.math.MathUtil;

import static net.dragonmounts.neo.common.entity.ai.behavior.DragonBreath.BREATH_START_DURATION;
import static net.dragonmounts.neo.common.entity.ai.behavior.DragonBreath.BREATH_STOP_DURATION;

public enum BuiltinMouthState implements MouthState {
    IDLE(0.0F, 0) {
        @Override
        public float updateJawRotation(DragonAnimator animator, int duration) {
            return 0.0F;
        }
    },
    ATTACKING(0.72F, 3),
    BREATHING(0.58F, BREATH_START_DURATION + BREATH_STOP_DURATION, BREATH_START_DURATION, true),
    ROARING(0.67F, 5),
    EATING(0.25F, 2) {
        @Override
        public float updateJawRotation(DragonAnimator animator, int duration) {
            if (duration < this.turning) return this.amplitude * MathUtil.clamp(duration / (float) this.turning);
            if (duration < this.duration) return this.amplitude * MathUtil.clamp(
                    (this.duration - duration) / (float) (duration - this.turning)
            );
            if (animator.remainingEating < 0) {
                animator.transitMouthState(BuiltinMouthState.IDLE, false);
            } else {
                animator.forceMouthState(this, 0);
            }
            return 0.0F;
        }
    };

    public final float amplitude;
    public final int turning;
    public final int duration;
    public final boolean charging;

    BuiltinMouthState(float amplitude, int duration, int turning, boolean charging) {
        this.amplitude = amplitude;
        this.turning = turning;
        this.duration = duration;
        this.charging = charging;
    }

    BuiltinMouthState(float amplitude, int half) {
        this(amplitude, half + half, half, false);
    }

    @Override
    public boolean isCharging() {
        return this.charging;
    }

    @Override
    public float updateJawRotation(DragonAnimator animator, int duration) {
        if (duration < this.turning) return this.amplitude * MathUtil.clamp(duration / (float) this.turning);
        if (duration >= this.duration) {
            animator.transitMouthState(BuiltinMouthState.IDLE, false);
            return 0.0F;
        }
        if (this.charging && animator.isCharging()) {
            animator.forceMouthState(this, this.turning);
            return this.amplitude;
        } else return this.amplitude * MathUtil.clamp(
                (this.duration - duration) / (float) (duration - this.turning)
        );
    }
}
