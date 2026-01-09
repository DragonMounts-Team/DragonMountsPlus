package net.dragonmounts.neo.config;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static net.dragonmounts.neo.config.EntryUtil.formatName;

public abstract class ConfigEntry<T> {
    public final ModConfigSpec.ConfigValue<T> host;

    public ConfigEntry(ModConfigSpec.ConfigValue<T> host) {
        this.host = host;
    }

    public final MutableComponent getDisplayName() {
        var name = this.host.getSpec().getTranslationKey();
        if (name == null) return ComponentUtils.wrapInSquareBrackets(Component.literal(formatName(this.host)));
        return ComponentUtils.wrapInSquareBrackets(Component.translatable(name)).withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT, Component.translatable(name + ".tooltip"))
        ).withColor(ChatFormatting.GREEN));
    }

    public boolean isDefault() {
        return Objects.equals(this.host.getDefault(), forcedGet(this.host));
    }

    public abstract String getAsString();

    public abstract Tag dump();

    public abstract T load(@Nullable Tag data);

    public void set(T value) {
        this.override(value);
        try {
            this.host.set(value);
        } catch (Exception ignored) {}
    }

    public abstract void override(T value);

    public void sync() {
        this.setSaved();
        this.revert();
    }

    /// set to fallback
    public void reset() {
        this.set(this.host.getDefault());
    }

    /// set to last saved
    public abstract void revert();

    public abstract void setSaved();

    public abstract CustomPacketPayload wrap(int id);

    public abstract ArgumentType<T> getArgument();

    public abstract T parse(CommandContext<?> context, String name);

    public static <T> T forcedGet(ModConfigSpec.ConfigValue<T> entry) {
        try {
            return entry.get();
        } catch (Exception ignored) {}
        return entry.getDefault();
    }
}
