package net.dragonmounts.neo.config;

import com.google.common.collect.HashBiMap;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.dragonmounts.neo.common.entity.dragon.HatchableDragonEggEntity;
import net.dragonmounts.neo.common.entity.dragon.TameableDragonEntity;
import net.dragonmounts.neo.compat.platform.ServerNetworkHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static net.dragonmounts.neo.config.EntryUtil.config;
import static net.dragonmounts.neo.config.EntryUtil.formatName;

public class ServerConfig extends ConfigHolder<CommandSourceStack> {
    public static final ServerConfig INSTANCE = new ServerConfig();
    protected final HashBiMap<ConfigEntry<?>, Integer> entries;
    public final ModConfigSpec spec;
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
    private AttributeSupplier dragonAttributes;
    private AttributeSupplier dragonEggAttributes;

    private ServerConfig() {
        var registry = HashBiMap.<ConfigEntry<?>, Integer>create();
        var builder = new ModConfigSpec.Builder();
        EntryUtil.register(registry, this.debug =
                config(builder.worldRestart(), "debug", false, "Debug mode. You need to restart Minecraft for the change to take effect. Unless you're a developer or are told to activate it, you don't want to set this to true.")
        );
        EntryUtil.register(registry, this.isEggPushable =
                config(builder, "isEggPushable", false, "Whether an egg is pushable on collision")
        );
        EntryUtil.register(registry, this.isEggOverridden =
                config(builder, "isEggOverridden", true, "Whether interaction hook about vanilla dragon egg is enabled")
        );
        EntryUtil.register(registry, this.ignitingBreath =
                config(builder, "ignitingBreath", true, "Whether fire-like dragon breath can ignite the hit blocks")
        );
        EntryUtil.register(registry, this.destructiveBreath =
                config(builder, "destructiveBreath", true, "Whether airflow-like dragon breath can destroy the hit blocks")
        );
        EntryUtil.register(registry, this.smeltingBreath =
                config(builder, "smeltingBreath", false, "Whether fire-like dragon breath can smelt the hit blocks")
        );
        EntryUtil.register(registry, this.quenchingBreath =
                config(builder, "quenchingBreath", true, "Whether mist-like dragon breath can put out fire and solidify lava")
        );
        EntryUtil.register(registry, this.frostyBreath =
                config(builder, "frostyBreath", false, "Whether blizzard-like dragon breath can leave snow on ground")
        );
        EntryUtil.register(registry, this.baseArmor =
                config(builder, "baseArmor", 8.0, 0.0, 30.0, "The base armor of a newly spawned dragon at adulthood", this::invalidateAttributes)
        );
        EntryUtil.register(registry, this.baseArmorToughness =
                config(builder, "baseArmorToughness", 20.0, 0.0, 20.0, "The base armor toughness of a newly spawned dragon at adulthood", this::invalidateAttributes)
        );
        EntryUtil.register(registry, this.baseBodySize =
                config(builder, "baseBodySize", 1.0, 0.0625, 16.0, "The base Body Size of a newly spawned dragon at adulthood", this::invalidateAttributes)
        );
        EntryUtil.register(registry, this.baseDamage =
                config(builder, "baseDamage", 12.0, 0.0, 2048.0, "The base damage of a newly spawned dragon at adulthood", this::invalidateAttributes)
        );
        EntryUtil.register(registry, this.baseFlyingSpeed =
                config(builder, "baseFlyingSpeed", 0.25, 0.0, 1024.0, "The base flying speed of a newly spawned dragon at adulthood", this::invalidateAttributes)
        );
        EntryUtil.register(registry, this.baseFollowRange =
                config(builder, "baseFollowRange", 64.0, 0.0, 2048.0, "The base follow range of a newly spawned dragon at adulthood", this::invalidateAttributes)
        );
        EntryUtil.register(registry, this.baseHealth =
                config(builder, "baseHealth", 90.0, 1.0, 1024.0, "The base health of a newly spawned dragon at adulthood", this::invalidateAttributes)
        );
        EntryUtil.register(registry, this.baseJumpStrength =
                config(builder, "baseJumpStrength", 1.0, 0.0, 32.0, "The base jump strength of a newly spawned dragon at adulthood", this::invalidateAttributes)
        );
        EntryUtil.register(registry, this.baseKnockback =
                config(builder, "baseKnockback", 0.0, 0.0, 5.0, "The base knockback of a newly spawned dragon at adulthood", this::invalidateAttributes)
        );
        EntryUtil.register(registry, this.baseKnockbackResistance =
                config(builder, "baseKnockbackResistance", 1.0, 0.0, 1.0, "The base knockback resistance of a newly spawned dragon at adulthood", this::invalidateAttributes)
        );
        EntryUtil.register(registry, this.baseMovementSpeed =
                config(builder, "baseMovementSpeed", 0.3, 0.0, 1024.0, "The base movement speed of a newly spawned dragon at adulthood", this::invalidateAttributes)
        );
        EntryUtil.register(registry, this.baseStepHeight =
                config(builder, "baseStepHeight", 1.25, 0.0, 10, "The base step height of a newly spawned dragon at adulthood", this::invalidateAttributes)
        );
        EntryUtil.register(registry, this.baseTemptRange =
                config(builder, "baseTemptRange", 16.0, 0.0, 2048.0, "The base tempt range of a newly spawned dragon at adulthood", this::invalidateAttributes)
        );
        EntryUtil.register(registry, this.baseWaterMovementEfficiency =
                config(builder, "baseWaterMovementEfficiency", 0.25, 0.0, 1.0, "The base water movement efficiency of a newly spawned dragon at adulthood", this::invalidateAttributes)
        );
        this.entries = registry;
        this.spec = builder.build();
    }

    public ConfigEntry<?> getEntry(int id) {
        return this.entries.inverse().get(id);
    }

    @Override
    public Collection<ConfigEntry<?>> getEntries() {
        return this.entries.keySet();
    }

    public void broadcast(@NotNull ConfigEntry<?> entry) {
        Integer id = this.entries.get(entry);
        if (id == null) return;
        ServerNetworkHandler.sendToAll(null, entry.wrap(id));
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

    @Override
    protected <T> ArgumentBuilder<CommandSourceStack, ?> buildCommand(ConfigEntry<T> entry) {
        return Commands.literal(formatName(entry.host)).executes(context -> {
            context.getSource().sendSuccess(() -> Component.translatable("commands.neodragonmounts.config.query", entry.getDisplayName(), entry.getAsString()), true);
            return 1;
        }).then(Commands.argument("value", entry.getArgument()).executes(context -> {
            entry.set(entry.parse(context, "value"));
            this.spec.save();
            this.broadcast(entry);
            context.getSource().sendSuccess(() -> Component.translatable("commands.neodragonmounts.config.modify", entry.getDisplayName(), entry.getAsString()), true);
            return 1;
        }));
    }

    public void invalidateAttributes(double ignored) {
        this.dragonAttributes = null;
        this.dragonEggAttributes = null;
    }

    public void register(ModContainer mod) {
        mod.registerConfig(ModConfig.Type.SERVER, this.spec);
    }

    @Override
    public ModConfigSpec getSpec() {
        return this.spec;
    }
}
