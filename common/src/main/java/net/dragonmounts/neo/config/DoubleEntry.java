package net.dragonmounts.neo.config;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.dragonmounts.neo.compat.Dummy;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.Nullable;

import java.util.function.DoubleConsumer;

public class DoubleEntry extends ConfigEntry<Double> {
    public final double min;
    public final double max;
    protected final DoubleConsumer onChanged;
    protected double saved;
    protected double effective;

    public DoubleEntry(double min, double max, DoubleConsumer onChanged) {
        this.min = min;
        this.max = max;
        this.onChanged = onChanged;
    }

    public double get() {
        return this.effective;
    }

    public float getAsFloat() {
        return (float) this.effective;
    }

    @Override
    public void override(Double wrapped) {}

    @Override
    public String getAsString() {
        return Double.toString(this.get());
    }

    @Override
    public Tag dump() {
        return Dummy.get();
    }

    @Override
    public Double load(@Nullable Tag data) {
        return Dummy.get();
    }

    @Override
    public void revert() {}

    @Override
    public void setSaved() {
    }

    @Override
    public CustomPacketPayload wrap(int id) {
        return Dummy.get();
    }

    @Override
    public ArgumentType<Double> getArgument() {
        return DoubleArgumentType.doubleArg(this.min, this.max);
    }

    @Override
    public Double parse(CommandContext<?> context, String name) {
        return DoubleArgumentType.getDouble(context, name);
    }
}
