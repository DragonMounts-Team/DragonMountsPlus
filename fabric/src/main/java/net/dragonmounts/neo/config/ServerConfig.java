package net.dragonmounts.neo.config;

import com.google.common.collect.HashBiMap;
import com.mojang.brigadier.builder.ArgumentBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dragonmounts.neo.common.DragonMountsShared;
import net.dragonmounts.neo.common.entity.dragon.HatchableDragonEggEntity;
import net.dragonmounts.neo.common.entity.dragon.TameableDragonEntity;
import net.dragonmounts.neo.compat.platform.ServerNetworkHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

import java.util.Collection;

import static net.dragonmounts.neo.common.util.TimeUtil.TICKS_PER_GAME_HOUR;
import static net.dragonmounts.neo.config.EntryUtil.config;
import static net.dragonmounts.neo.config.EntryUtil.register;
import static net.minecraft.SharedConstants.TICKS_PER_MINUTE;

public class ServerConfig extends ConfigHolder<CommandSourceStack> {
    public static final ServerConfig INSTANCE = new ServerConfig(DragonMountsShared.NAMESPACE, "server.snbt");
    protected final HashBiMap<ConfigEntry<?>, Integer> entries;
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
    private AttributeSupplier dragonAttributes;
    private AttributeSupplier dragonEggAttributes;

    protected ServerConfig(String mod, String file) {
        super(mod, file);
        var registry = HashBiMap.<ConfigEntry<?>, Integer>create();
        register(registry, this.debug =
                config("debug", false)
        );
        register(registry, this.isEggPushable =
                config("isEggPushable", false)
        );
        register(registry, this.isEggOverridden =
                config("isEggOverridden", true)
        );
        register(registry, this.ignitingBreath =
                config("ignitingBreath", true)
        );
        register(registry, this.destructiveBreath =
                config("destructiveBreath", true)
        );
        register(registry, this.smeltingBreath =
                config("smeltingBreath", false)
        );
        register(registry, this.quenchingBreath =
                config("quenchingBreath", true)
        );
        register(registry, this.frostyBreath =
                config("frostyBreath", false)
        );
        register(registry, this.baseArmor =
                config("baseArmor", 8.0, 0.0, 30.0, this::invalidateAttributes)
        );
        register(registry, this.baseArmorToughness =
                config("baseArmorToughness", 20.0, 0.0, 20.0, this::invalidateAttributes)
        );
        register(registry, this.baseBodySize =
                config("baseBodySize", 1.0, 0.0625, 16.0, this::invalidateAttributes)
        );
        register(registry, this.baseDamage =
                config("baseDamage", 12.0, 0.0, 2048.0, this::invalidateAttributes)
        );
        register(registry, this.baseFlyingSpeed =
                config("baseFlyingSpeed", 0.25, 0.0, 1024.0, this::invalidateAttributes)
        );
        register(registry, this.baseFollowRange =
                config("baseFollowRange", 64.0, 0.0, 2048.0, this::invalidateAttributes)
        );
        register(registry, this.baseHealth =
                config("baseHealth", 90.0, 1.0, 1024.0, this::invalidateAttributes)
        );
        register(registry, this.baseJumpStrength =
                config("baseJumpStrength", 1.0, 0.0, 32.0, this::invalidateAttributes)
        );
        register(registry, this.baseKnockback =
                config("baseKnockback", 0.0, 0.0, 5.0, this::invalidateAttributes)
        );
        register(registry, this.baseKnockbackResistance =
                config("baseKnockbackResistance", 1.0, 0.0, 1.0, this::invalidateAttributes)
        );
        register(registry, this.baseMovementSpeed =
                config("baseMovementSpeed", 0.3, 0.0, 1024.0, this::invalidateAttributes)
        );
        register(registry, this.baseStepHeight =
                config("baseStepHeight", 1.25, 0.0, 10, this::invalidateAttributes)
        );
        register(registry, this.baseTemptRange =
                config("baseTemptRange", 16.0, 0.0, 2048.0, this::invalidateAttributes)
        );
        register(registry, this.baseWaterMovementEfficiency =
                config("baseWaterMovementEfficiency", 0.25, 0.0, 1.0, this::invalidateAttributes)
        );
        register(registry, this.minIncubationDuration =
                config("minIncubationDuration", 20 * TICKS_PER_MINUTE, 0, Integer.MAX_VALUE)
        );
        register(registry, this.hatchlingStageDuration =
                config("hatchlingStageDuration", 48 * TICKS_PER_GAME_HOUR, 0, Integer.MAX_VALUE)
        );
        register(registry, this.infantStageDuration =
                config("infantStageDuration", 24 * TICKS_PER_GAME_HOUR, 0, Integer.MAX_VALUE)
        );
        register(registry, this.fledglingStageDuration =
                config("fledglingStageDuration", 32 * TICKS_PER_GAME_HOUR, 0, Integer.MAX_VALUE)
        );
        register(registry, this.juvenileStageDuration =
                config("juvenileStageDuration", 60 * TICKS_PER_GAME_HOUR, 0, Integer.MAX_VALUE)
        );
        this.entries = registry;
        this.load();
    }

    public ConfigEntry<?> getEntry(int id) {
        return this.entries.inverse().get(id);
    }

    @Override
    public Collection<ConfigEntry<?>> getEntries() {
        return this.entries.keySet();
    }

    public void broadcast(MinecraftServer server, ConfigEntry<?> entry) {
        Integer id = this.entries.get(entry);
        if (id == null) return;
        ServerNetworkHandler.sendToAll(server, entry.wrap(id));
    }

    @Override
    protected <T> ArgumentBuilder<CommandSourceStack, ?> buildCommand(ConfigEntry<T> entry) {
        return Commands.literal(entry.key).executes(context -> {
            context.getSource().sendSuccess(() -> Component.translatable("commands.neodragonmounts.config.query", entry.getDisplayName(), entry.getAsString()), true);
            return 1;
        }).then(Commands.argument("value", entry.getArgument()).executes(context -> {
            if (entry.set(entry.parse(context, "value"))) {
                this.save();
                this.broadcast(context.getSource().getServer(), entry);
            }
            context.getSource().sendSuccess(() -> Component.translatable("commands.neodragonmounts.config.modify", entry.getDisplayName(), entry.getAsString()), true);
            return 1;
        }));
    }

    public AttributeSupplier getDragonAttributes() {
        var attrs = this.dragonAttributes;
        if (attrs == null) {
            this.dragonAttributes = attrs = TameableDragonEntity.createAttributes().build();
        }
        return attrs;
    }

    public AttributeSupplier getDragonEggAttributes() {
        var attrs = this.dragonEggAttributes;
        if (attrs == null) {
            this.dragonEggAttributes = attrs = HatchableDragonEggEntity.createAttributes().build();
        }
        return attrs;
    }

    public void sync(ServerPlayer player) {
        var entries = new ObjectArrayList<S2CSyncConfigPayload.Entry>();
        for (var entry : this.entries.entrySet()) {
            entries.add(S2CSyncConfigPayload.Entry.of(entry));
        }
        ServerNetworkHandler.sendTo(player, new S2CSyncConfigPayload(entries));
    }

    public void invalidateAttributes(double ignored) {
        this.dragonAttributes = null;
        this.dragonEggAttributes = null;
    }

    public static void init() {}
}
