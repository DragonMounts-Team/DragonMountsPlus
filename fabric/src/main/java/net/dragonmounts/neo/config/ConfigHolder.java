package net.dragonmounts.neo.config;

import com.google.common.base.Charsets;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.logging.LogUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.SnbtPrinterTagVisitor;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

public abstract class ConfigHolder<S> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Path source;

    public ConfigHolder(String mod, String file) {
        this.source = FabricLoader.getInstance().getConfigDir().resolve(mod).resolve(file);
    }

    public synchronized void loadSync() {
        var source = this.source;
        if (Files.isRegularFile(source)) {
            try (BufferedReader reader = Files.newBufferedReader(source, Charsets.UTF_8)) {
                var root = TagParser.parseTag(IOUtils.toString(reader));
                for (var entry : this.getEntries()) {
                    read(entry, root.get(entry.key));
                }
            } catch (Exception exception) {
                LOGGER.error("Exception reading {}", source, exception);
            }
        }
    }

    public synchronized void saveSync() {
        var source = this.source;
        CompoundTag result = null;
        if (Files.isRegularFile(source)) {
            CompoundTag existing;
            try (BufferedReader reader = Files.newBufferedReader(source, Charsets.UTF_8)) {
                existing = TagParser.parseTag(IOUtils.toString(reader));
            } catch (Exception exception) {
                existing = null;
                LOGGER.error("Exception reading {}", source, exception);
            }
            result = collect(this, existing);
        } else if (Files.notExists(source)) {
            try {
                Files.createDirectories(source.getParent());
            } catch (Exception exception) {
                LOGGER.error("Exception creating parent of {}", source, exception);
                return;
            }
            result = collect(this, null);
        }
        if (result == null) return;
        try (BufferedWriter writer = Files.newBufferedWriter(source, Charsets.UTF_8);) {
            writer.write(new SnbtPrinterTagVisitor().visit(result));
        } catch (Exception exception) {
            LOGGER.error("Exception writing {}", source, exception);
        }
    }

    public void save() {
        Util.ioPool().execute(this::saveSync);
    }

    public void load() {
        Util.ioPool().execute(this::loadSync);
    }

    public abstract Collection<ConfigEntry<?>> getEntries();

    protected abstract <T> ArgumentBuilder<S, ?> buildCommand(ConfigEntry<T> entry);

    public <T extends ArgumentBuilder<S, T>> T appendCommands(T command) {
        for (var entry : this.getEntries()) {
            command.then(this.buildCommand(entry));
        }
        return command;
    }

    public static <T> void read(ConfigEntry<T> entry, Tag data) {
        entry.set(entry.load(data));
        entry.setSaved();
    }

    public static @Nullable CompoundTag collect(ConfigHolder<?> holder, @Nullable CompoundTag existing) {
        boolean changed = false;
        boolean full = existing == null;
        var root = full ? new CompoundTag() : existing;
        for (var entry : holder.getEntries()) {
            if (full || entry.isChanged()) {
                if (entry.isDefault()) {
                    changed = changed || root.contains(entry.key);
                    root.remove(entry.key);
                } else {
                    changed = true;
                    root.put(entry.key, entry.dump());
                }
                entry.setSaved();
            }
        }
        return changed ? root : null;
    }
}
