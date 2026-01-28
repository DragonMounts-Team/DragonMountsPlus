package net.dragonmounts.neo.config;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.dragonmounts.neo.common.network.s2c.IntegerConfigPayload;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class IntEntry extends ConfigEntry<Integer> implements IntSupplier {
    public final int fallback;
    public final int min;
    public final int max;
    protected final IntConsumer onChanged;
    protected int saved;
    protected int value;
    protected int effective;

    public IntEntry(
            String key,
            String name,
            String tooltip,
            int fallback,
            int min,
            int max,
            IntConsumer onChanged
    ) {
        super(key, name, tooltip);
        this.min = min;
        this.max = max;
        this.onChanged = onChanged;
        this.set(this.saved = this.fallback = fallback);
    }

    @Override
    public int getAsInt() {
        return this.effective;
    }

    protected void overrideImpl(int value) {
        if (value == this.effective) return;
        this.effective = value;
        if (this.onChanged == null) return;
        this.onChanged.accept(value);
    }

    @Override
    public void override(Integer wrapped) {
        this.overrideImpl(Mth.clamp(wrapped, this.min, this.max));
    }

    @Override
    public boolean set(Integer wrapped) {
        int value = Mth.clamp(wrapped, this.min, this.max);
        this.overrideImpl(value);
        if (this.value == value) return false;
        this.value = value;
        return true;
    }

    @Override
    public String getAsString() {
        return Integer.toString(this.getAsInt());
    }

    @Override
    public Tag dump() {
        return IntTag.valueOf(this.value);
    }

    @Override
    public Integer load(@Nullable Tag data) {
        return data instanceof NumericTag ? ((NumericTag) data).getAsInt() : this.fallback;
    }

    @Override
    public boolean isChanged() {
        return this.value != this.saved;
    }

    @Override
    public boolean isDefault() {
        return this.value == this.fallback;
    }

    @Override
    public void reset() {
        this.set(this.fallback);
    }

    @Override
    public void revert() {
        this.set(this.saved);
    }

    @Override
    public void setSaved() {
        this.saved = this.value;
    }

    @Override
    public CustomPacketPayload wrap(int id) {
        return new IntegerConfigPayload(id, this.getAsInt());
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
