package net.dragonmounts.neo.common.entity.ai.sensing;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public abstract class Anger {
    public @Nullable LivingEntity target;
    public int timestamp;

    public abstract @Nullable LivingEntity updateTarget(@Nullable LivingEntity source, @Nullable LivingEntity fallback);

    public static class HurtBy extends Anger {
        @Override
        public @Nullable LivingEntity updateTarget(@Nullable LivingEntity source, @Nullable LivingEntity fallback) {
            if (source == null) return null;
            int timestamp = source.getLastHurtByMobTimestamp();
            if (this.timestamp == timestamp) return fallback;
            this.timestamp = timestamp;
            return this.target = source.getLastHurtByMob();
        }
    }

    public static class Attack extends Anger {
        @Override
        public @Nullable LivingEntity updateTarget(@Nullable LivingEntity source, @Nullable LivingEntity fallback) {
            if (source == null) return null;
            int timestamp = source.getLastHurtMobTimestamp();
            if (this.timestamp == timestamp) return fallback;
            this.timestamp = timestamp;
            return this.target = source.getLastHurtMob();
        }
    }
}
