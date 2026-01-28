package net.dragonmounts.neo.config;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.dragonmounts.neo.common.DragonMountsShared;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.List;

import static net.dragonmounts.neo.config.EntryUtil.config;

public class ClientConfig extends ConfigHolder<FabricClientCommandSource> {
    public static final ClientConfig INSTANCE = new ClientConfig(DragonMountsShared.NAMESPACE, "client.snbt");
    protected final List<ConfigEntry<?>> entries;
    public final BooleanEntry debug;
    public final DoubleEntry cameraDistance;
    public final DoubleEntry cameraOffset;
    public final BooleanEntry convergePitchAngle;
    public final BooleanEntry convergeYawAngle;
    public final BooleanEntry hoverState;
    public final BooleanEntry toggleDescending;
    public final BooleanEntry toggleBreathing;
    public final BooleanEntry pauseOnFluting;

    protected ClientConfig(String mod, String file) {
        super(mod, file);
        this.entries = List.of(
                this.debug = config("debug", false),
                this.cameraDistance = config("cameraDistance", 20.0, 0.0, 64.0),
                this.cameraOffset = config("cameraOffset", 0.0, -32.0, 32.0),
                this.convergePitchAngle = config("convergePitchAngle", true),
                this.convergeYawAngle = config("convergeYawAngle", true),
                this.hoverState = config("hoverState", true),
                this.toggleDescending = config("toggleDescending", false, "key.neodragonmounts.descend"),
                this.toggleBreathing = config("toggleBreathing", false, "key.neodragonmounts.breathe"),
                this.pauseOnFluting = config("pauseOnFluting", true)
        );
        this.load();
    }

    @Override
    public Collection<ConfigEntry<?>> getEntries() {
        return this.entries;
    }

    @Override
    protected <T> ArgumentBuilder<FabricClientCommandSource, ?> buildCommand(ConfigEntry<T> entry) {
        return ClientCommandManager.literal(entry.key).executes(context -> {
            context.getSource().sendFeedback(Component.translatable("commands.neodragonmounts.config.query", entry.getDisplayName(), entry.getAsString()));
            return 1;
        }).then(ClientCommandManager.argument("value", entry.getArgument()).executes(context -> {
            if (entry.set(entry.parse(context, "value"))) {
                this.save();
            }
            context.getSource().sendFeedback(Component.translatable("commands.neodragonmounts.config.modify", entry.getDisplayName(), entry.getAsString()));
            return 1;
        }));
    }

    public static void init() {}
}
