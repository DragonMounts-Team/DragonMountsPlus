package net.dragonmounts.neo.config;

import com.google.common.collect.HashBiMap;
import net.minecraft.nbt.Tag;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;

public class EntryUtil {
    public static String formatName(ModConfigSpec.ConfigValue<?> entry) {
        return String.join(".", entry.getPath());
    }

    public static String translate(String key) {
        return "options.neodragonmounts." + key;
    }

    public static BooleanEntry config(
            ModConfigSpec.Builder builder,
            String key,
            boolean fallback,
            String desc
    ) {
        return config(builder, key, fallback, translate(key), desc);
    }

    public static BooleanEntry config(
            ModConfigSpec.Builder builder,
            String key,
            boolean fallback,
            String name,
            String desc
    ) {
        return new BooleanEntry(builder.translation(name).comment(desc).define(key, fallback));
    }

    public static DoubleEntry config(
            ModConfigSpec.Builder builder,
            String key,
            double fallback,
            double min,
            double max,
            String desc
    ) {
        return config(builder, key, fallback, min, max, desc, null);
    }

    public static DoubleEntry config(
            ModConfigSpec.Builder builder,
            String key,
            double fallback,
            double min,
            double max,
            String desc,
            DoubleConsumer onChanged
    ) {
        return new DoubleEntry(builder.translation(translate(key)).comment(desc).defineInRange(key, fallback, min, max), min, max, onChanged);
    }

    public static IntEntry config(
            ModConfigSpec.Builder builder,
            String key,
            int fallback,
            int min,
            int max,
            String desc
    ) {
        return config(builder, key, fallback, min, max, desc, (IntConsumer) null);
    }

    public static IntEntry config(
            ModConfigSpec.Builder builder,
            String key,
            int fallback,
            int min,
            int max,
            String desc,
            IntConsumer onChanged
    ) {
        return new IntEntry(builder.translation(translate(key)).comment(desc).defineInRange(key, fallback, min, max), min, max, onChanged);
    }

    public static void register(HashBiMap<ConfigEntry<?>, Integer> registry, ConfigEntry<?> entry) {
        registry.put(entry, registry.size());
    }

    public static <T> void override(ConfigEntry<T> entry, Tag data) {
        entry.override(entry.load(data));
    }

    protected static void dispatch(ModConfigEvent event, Consumer<ConfigEntry<?>> consumer) {
        switch (event.getConfig().getType()) {
            case SERVER -> ServerConfig.INSTANCE.getEntries().forEach(consumer);
            case CLIENT -> ClientConfig.INSTANCE.getEntries().forEach(consumer);
        }
    }

    public static void onLoad(ModConfigEvent event) {
        dispatch(event, ConfigEntry::sync);
    }

    public static void onUnload(ModConfigEvent.Unloading event) {
        dispatch(event, ConfigEntry::reset);
    }
}
