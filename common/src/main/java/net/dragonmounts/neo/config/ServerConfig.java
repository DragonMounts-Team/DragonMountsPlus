package net.dragonmounts.neo.config;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.dragonmounts.neo.compat.Dummy;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class ServerConfig extends ConfigHolder<CommandSourceStack> {
    public static final ServerConfig INSTANCE = new ServerConfig();
    public final BooleanEntry debug;
    public final BooleanEntry isEggPushable;
    public final BooleanEntry isEggOverridden;
    public final BooleanEntry ignitingBreath;
    public final BooleanEntry destructiveBreath;
    public final BooleanEntry smeltingBreath;
    public final BooleanEntry quenchingBreath;
    public final BooleanEntry frostyBreath;
    public final DoubleEntry baseArmor;
    public final DoubleEntry baseArmorToughness;
    public final DoubleEntry baseBodySize;
    public final DoubleEntry baseDamage;
    public final DoubleEntry baseFlyingSpeed;
    public final DoubleEntry baseFollowRange;
    public final DoubleEntry baseHealth;
    public final DoubleEntry baseJumpStrength;
    public final DoubleEntry baseKnockback;
    public final DoubleEntry baseKnockbackResistance;
    public final DoubleEntry baseMovementSpeed;
    public final DoubleEntry baseStepHeight;
    public final DoubleEntry baseTemptRange;
    public final DoubleEntry baseWaterMovementEfficiency;
    public final IntEntry minIncubationDuration;
    public final IntEntry hatchlingStageDuration;
    public final IntEntry infantStageDuration;
    public final IntEntry fledglingStageDuration;
    public final IntEntry juvenileStageDuration;

    private ServerConfig() {
        this.debug = Dummy.get();
        this.isEggPushable = Dummy.get();
        this.isEggOverridden = Dummy.get();
        this.ignitingBreath = Dummy.get();
        this.destructiveBreath = Dummy.get();
        this.smeltingBreath = Dummy.get();
        this.quenchingBreath = Dummy.get();
        this.frostyBreath = Dummy.get();
        this.baseArmor = Dummy.get();
        this.baseArmorToughness = Dummy.get();
        this.baseBodySize = Dummy.get();
        this.baseDamage = Dummy.get();
        this.baseFlyingSpeed = Dummy.get();
        this.baseFollowRange = Dummy.get();
        this.baseHealth = Dummy.get();
        this.baseJumpStrength = Dummy.get();
        this.baseKnockback = Dummy.get();
        this.baseKnockbackResistance = Dummy.get();
        this.baseMovementSpeed = Dummy.get();
        this.baseStepHeight = Dummy.get();
        this.baseTemptRange = Dummy.get();
        this.baseWaterMovementEfficiency = Dummy.get();
        this.minIncubationDuration = Dummy.get();
        this.hatchlingStageDuration = Dummy.get();
        this.infantStageDuration = Dummy.get();
        this.fledglingStageDuration = Dummy.get();
        this.juvenileStageDuration = Dummy.get();
    }

    public ConfigEntry<?> getEntry(int id) {
        return Dummy.get();
    }

    @Override
    public Collection<ConfigEntry<?>> getEntries() {
        return Dummy.get();
    }

    public void broadcast(@NotNull ConfigEntry<?> entry) {}

    public AttributeSupplier getDragonAttributes() {
        return Dummy.get();
    }

    public AttributeSupplier getDragonEggAttributes() {
        return Dummy.get();
    }

    @Override
    protected <T> ArgumentBuilder<CommandSourceStack, ?> buildCommand(ConfigEntry<T> entry) {
        return Dummy.get();
    }

    public void invalidateAttributes(double ignored) {}
}
