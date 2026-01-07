package net.dragonmounts.neo.common.entity.dragon;

import io.netty.buffer.ByteBuf;
import net.dragonmounts.neo.common.entity.breath.BreathPower;
import net.dragonmounts.neo.config.ServerConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.function.IntFunction;
import java.util.function.IntSupplier;

import static net.dragonmounts.neo.common.DragonMountsShared.makeId;

public enum DragonLifeStage implements StringRepresentable {
    HATCHLING(BreathPower.SMALL, 0.04F, 0.09F, ServerConfig.INSTANCE.hatchlingStageDuration),
    INFANT(BreathPower.SMALL, 0.10F, 0.18F, ServerConfig.INSTANCE.infantStageDuration),
    FLEDGLING(BreathPower.SMALL, 0.19F, 0.60F, ServerConfig.INSTANCE.fledglingStageDuration),
    JUVENILE(BreathPower.MEDIUM, 0.61F, 0.99F, ServerConfig.INSTANCE.juvenileStageDuration),
    ADULT(BreathPower.LARGE, 1.00F, 1.00F, () -> 0);
    private static final IntFunction<DragonLifeStage> BY_ID = ByIdMap.continuous(DragonLifeStage::ordinal, values(), ByIdMap.OutOfBoundsStrategy.CLAMP);
    public static final ResourceLocation MODIFIER_ID = makeId("life_stage_bonus");
    public static final @SuppressWarnings("deprecation") EnumCodec<DragonLifeStage> CODEC = StringRepresentable.fromEnum(DragonLifeStage::values);
    public static final StreamCodec<ByteBuf, DragonLifeStage> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, DragonLifeStage::ordinal);
    public static final String SERIALIZATION_KEY = "LifeStage";
    public static final String EGG_TRANSLATION_KEY = "neodragonmounts.life_stage.egg";
    public final BreathPower power;
    public final IntSupplier duration;
    public final float startSize;
    public final float finalSize;
    public final String identifier;
    public final String text;

    DragonLifeStage(BreathPower power, float startSize, float finalSize, IntSupplier duration) {
        this.duration = duration;
        this.startSize = startSize;
        this.finalSize = finalSize;
        this.text = "neodragonmounts.life_stage." + (this.identifier = this.name().toLowerCase());
        this.power = power;
    }

    public AttributeModifier makeModifier(double factor, AttributeModifier.Operation operation) {
        return new AttributeModifier(MODIFIER_ID, Math.max(this.getAverageScale(), 0.1F) * factor, operation);
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

    public float getScale(int age) {
        int duration = this.duration.getAsInt();
        return duration == 0 ? this.finalSize : Mth.lerp(getProgress(age, duration), this.startSize, this.finalSize);
    }

    public float getAverageScale() {
        return (this.finalSize + this.startSize) * 0.5F;
    }

    public static DragonLifeStage byId(int id) {
        var values = values();
        return id < 0 || id >= values.length ? DragonLifeStage.ADULT : values[id];
    }

    public static DragonLifeStage byName(String name) {
        return CODEC.byName(name, ADULT);
    }

    public static float getProgress(int age, float duration) {
        return age < 0 ? 1.0F + age / duration : 1.0F - age / duration;
    }
}
