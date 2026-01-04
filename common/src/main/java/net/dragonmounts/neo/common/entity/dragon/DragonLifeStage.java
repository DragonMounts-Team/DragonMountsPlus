package net.dragonmounts.neo.common.entity.dragon;

import io.netty.buffer.ByteBuf;
import net.dragonmounts.neo.common.entity.breath.BreathPower;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.function.IntFunction;

import static net.dragonmounts.neo.common.DragonMountsShared.makeId;
import static net.dragonmounts.neo.common.util.TimeUtil.TICKS_PER_GAME_HOUR;

public enum DragonLifeStage implements StringRepresentable {
    HATCHLING(48 * TICKS_PER_GAME_HOUR, 0.04F, 0.09F, BreathPower.SMALL),
    INFANT(24 * TICKS_PER_GAME_HOUR, 0.10F, 0.18F, BreathPower.SMALL),
    FLEDGLING(32 * TICKS_PER_GAME_HOUR, 0.19F, 0.60F, BreathPower.SMALL),
    JUVENILE(60 * TICKS_PER_GAME_HOUR, 0.61F, 0.99F, BreathPower.MEDIUM),
    ADULT(0, 1.00F, 1.00F, BreathPower.LARGE);
    private static final IntFunction<DragonLifeStage> BY_ID = ByIdMap.continuous(DragonLifeStage::ordinal, values(), ByIdMap.OutOfBoundsStrategy.CLAMP);
    public static final ResourceLocation MODIFIER_ID = makeId("life_stage_bonus");
    public static final @SuppressWarnings("deprecation") EnumCodec<DragonLifeStage> CODEC = StringRepresentable.fromEnum(DragonLifeStage::values);
    public static final StreamCodec<ByteBuf, DragonLifeStage> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, DragonLifeStage::ordinal);
    public static final String DATA_PARAMETER_KEY = "LifeStage";
    public static final String EGG_TRANSLATION_KEY = "neodragonmounts.life_stage.egg";
    public final BreathPower power;
    public final int duration;
    public final float startSize;
    public final float endSize;
    public final String identifier;
    public final String text;

    DragonLifeStage(int duration, float startSize, float endSize, BreathPower power) {
        this.duration = duration;
        this.startSize = startSize;
        this.endSize = endSize;
        this.text = "neodragonmounts.life_stage." + (this.identifier = this.name().toLowerCase());
        this.power = power;
    }

    public AttributeModifier makeModifier(double factor, AttributeModifier.Operation operation) {
        return new AttributeModifier(MODIFIER_ID, Math.max(getSizeAverage(this), 0.1F) * factor, operation);
    }

    public boolean isOldEnough(DragonLifeStage limit) {
        return this.ordinal() > limit.ordinal();
    }

    public Component getText() {
        return Component.translatable(this.text);
    }

    @Override
    public String getSerializedName() {
        return this.identifier;
    }

    public static DragonLifeStage byId(int id) {
        var values = values();
        return id < 0 || id >= values.length ? DragonLifeStage.ADULT : values[id];
    }

    public static DragonLifeStage byName(String name) {
        return CODEC.byName(name, ADULT);
    }

    public static float getSize(DragonLifeStage stage, int age) {
        return stage.duration == 0
                ? 1.0F
                : Mth.lerp(getProgress(age, stage.duration), stage.startSize, stage.endSize);
    }

    public static float getSizeAverage(DragonLifeStage stage) {
        return 0.5F * (stage.endSize + stage.startSize);
    }

    public static float getProgress(int age, float duration) {
        return age < 0 ? 1.0F + age / duration : 1.0F - age / duration;
    }
}
