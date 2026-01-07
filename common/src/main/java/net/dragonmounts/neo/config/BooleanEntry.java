package net.dragonmounts.neo.config;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.dragonmounts.neo.compat.Dummy;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.Nullable;

public class BooleanEntry extends ConfigEntry<Boolean> {
    protected boolean saved;
    protected boolean effective;

    public boolean get() {
        return this.effective;
    }

    @Override
    public void override(Boolean value) {
        this.effective = value;
    }

    @Override
    public String getAsString() {
        return Boolean.toString(this.get());
    }

    @Override
    public Tag dump() {
        return Dummy.get();
    }

    @Override
    public Boolean load(@Nullable Tag data) {
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
    public ArgumentType<Boolean> getArgument() {
        return BoolArgumentType.bool();
    }

    @Override
    public Boolean parse(CommandContext<?> context, String name) {
        return BoolArgumentType.getBool(context, name);
    }
}
