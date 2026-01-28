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
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class IntEntry extends ConfigEntry<Integer> implements IntSupplier {
    public final int min;
    public final int max;
    protected final IntConsumer onChanged;
    protected int saved;
    protected int effective;

    public IntEntry(
            ModConfigSpec.ConfigValue<Integer> host,
            int min,
            int max,
            IntConsumer onChanged
    ) {
        super(host);
        this.min = min;
        this.max = max;
        this.onChanged = onChanged;
        this.effective = this.saved = host.getDefault();
    }

    @Override
    public int getAsInt() {
        return this.effective;
    }

    @Override
    public void override(Integer wrapped) {
        int value = Mth.clamp(wrapped, this.min, this.max);
        if (value == this.effective) return;
        this.effective = value;
        if (this.onChanged == null) return;
        this.onChanged.accept(value);
    }

    @Override
    public String getAsString() {
        return Integer.toString(this.getAsInt());
    }

    @Override
    public Tag dump() {
        return IntTag.valueOf(forcedGet(this.host));
    }

    @Override
    public Integer load(@Nullable Tag data) {
        return data instanceof NumericTag ? ((NumericTag) data).getAsInt() : this.host.getDefault();
    }

    @Override
    public void revert() {
        this.set(this.saved);
    }

    @Override
    public void setSaved() {
        this.saved = forcedGet(this.host);
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
