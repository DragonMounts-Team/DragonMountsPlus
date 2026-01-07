package net.dragonmounts.neo.config;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.dragonmounts.neo.compat.Dummy;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class IntEntry extends ConfigEntry<Integer> implements IntSupplier {
    public final int min;
    public final int max;
    protected final IntConsumer onChanged;
    protected int saved;
    protected int effective;

    public IntEntry(int min, int max, IntConsumer onChanged) {
        this.min = min;
        this.max = max;
        this.onChanged = onChanged;
    }

    @Override
    public int getAsInt() {
        return this.effective;
    }

    @Override
    public void override(Integer wrapped) {}

    @Override
    public String getAsString() {
        return Integer.toString(this.getAsInt());
    }

    @Override
    public Tag dump() {
        return Dummy.get();
    }

    @Override
    public Integer load(@Nullable Tag data) {
        return Dummy.get();
    }

    @Override
    public void revert() {}

    @Override
    public void setSaved() {}

    @Override
    public CustomPacketPayload wrap(int id) {
        return Dummy.get();
    }

    @Override
    public ArgumentType<Integer> getArgument() {
        return IntegerArgumentType.integer(this.min, this.max);
    }

    @Override
    public Integer parse(CommandContext<?> context, String name) {
        return IntegerArgumentType.getInteger(context, name);
    }
}
