package net.dragonmounts.neo;

import net.dragonmounts.neo.common.capability.ArmorEffectManager;
import net.dragonmounts.neo.common.capability.ArmorEffectManagerImpl;
import net.dragonmounts.neo.common.command.DMCommands;
import net.dragonmounts.neo.common.init.*;
import net.dragonmounts.neo.common.network.c2s.*;
import net.dragonmounts.neo.common.network.s2c.*;
import net.dragonmounts.neo.compat.platform.DMAttachments;
import net.dragonmounts.neo.compat.platform.DMScreenHandlers;
import net.dragonmounts.neo.compat.platform.ServerNetworkHandler;
import net.dragonmounts.neo.compat.registry.DragonType;
import net.dragonmounts.neo.compat.registry.DragonVariant;
import net.dragonmounts.neo.compat.registry.RegistryHandler;
import net.dragonmounts.neo.config.S2CSyncConfigPayload;
import net.dragonmounts.neo.config.ServerConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import static net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.registerGlobalReceiver;

public class DragonMounts implements ModInitializer, ServerPlayConnectionEvents.Join, ServerPlayerEvents.CopyFrom {
    @Override
    public void onInitialize() {
        ServerConfig.init();
        DMDataComponents.init();
        DMEntities.init();
        DMItems.setup();
        DMBlocks.init();
        DMBlockEntities.init();
        DMScreenHandlers.init();
        DMItemGroups.register(RegistryHandler::registerItemCategory);
        DMAttachments.init();
        DMSounds.init();
        DMActivities.init();
        DMMemories.init();
        DMSensors.init();
        DMStructures.init();
        DMParticles.init();
        DMMobEffects.init();
        DMRecipes.init();
        initNetwork();
        EntityDataSerializers.registerSerializer(DragonType.SERIALIZER);
        EntityDataSerializers.registerSerializer(DragonVariant.SERIALIZER);
        CommandRegistrationCallback.EVENT.register(DMCommands::register);
        ServerPlayerEvents.COPY_FROM.register(this);
        AttackEntityCallback.EVENT.register(DMArmorEffects::meleeChanneling);
        ServerPlayConnectionEvents.JOIN.register(this);
    }

    static void initNetwork() {
        registerPayloads(PayloadTypeRegistry.playS2C());
        registerPayloads(PayloadTypeRegistry.playC2S());
        registerGlobalReceiver(ControlDragonPayload.TYPE, ServerNetworkHandler::handleDragonRiding);
        registerGlobalReceiver(TeleportDragonPayload.TYPE, ServerNetworkHandler::handleTeleportDragon);
        registerGlobalReceiver(ToggleSittingByUUIDPayload.TYPE, ServerNetworkHandler::handleToggleSitting);
        registerGlobalReceiver(ToggleSittingByIDPayload.TYPE, ServerNetworkHandler::handleToggleSitting);
        registerGlobalReceiver(ToggleTrustPayload.TYPE, ServerNetworkHandler::handleToggleTrust);
        registerGlobalReceiver(ToggleFollowingPayload.TYPE, ServerNetworkHandler::handleToggleFollowing);
        registerGlobalReceiver(RenameFlutePayload.TYPE, ServerNetworkHandler::handleRenameFlute);
    }

    static void registerPayloads(PayloadTypeRegistry<RegistryFriendlyByteBuf> registry) {
        registry.register(ArmorRipostePayload.TYPE, ArmorRipostePayload.CODEC);
        registry.register(FeedDragonPayload.TYPE, FeedDragonPayload.CODEC);
        registry.register(InitCooldownPayload.TYPE, InitCooldownPayload.CODEC);
        registry.register(ControlDragonPayload.TYPE, ControlDragonPayload.CODEC);
        registry.register(WobbleEggPayload.TYPE, WobbleEggPayload.CODEC);
        registry.register(SyncCooldownPayload.TYPE, SyncCooldownPayload.CODEC);
        registry.register(SyncDragonAgePayload.TYPE, SyncDragonAgePayload.CODEC);
        registry.register(SyncEggAgePayload.TYPE, SyncEggAgePayload.CODEC);
        registry.register(TeleportDragonPayload.TYPE, TeleportDragonPayload.CODEC);
        registry.register(ToggleSittingByUUIDPayload.TYPE, ToggleSittingByUUIDPayload.CODEC);
        registry.register(ToggleSittingByIDPayload.TYPE, ToggleSittingByIDPayload.CODEC);
        registry.register(ToggleTrustPayload.TYPE, ToggleTrustPayload.CODEC);
        registry.register(ToggleFollowingPayload.TYPE, ToggleFollowingPayload.CODEC);
        registry.register(RenameFlutePayload.TYPE, RenameFlutePayload.CODEC);
        registry.register(S2CSyncConfigPayload.TYPE, S2CSyncConfigPayload.CODEC);
        registry.register(BooleanConfigPayload.TYPE, BooleanConfigPayload.CODEC);
        registry.register(DoubleConfigPayload.TYPE, DoubleConfigPayload.CODEC);
        registry.register(IntegerConfigPayload.TYPE, IntegerConfigPayload.CODEC);
    }

    @Override
    public void onPlayReady(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server) {
        var player = handler.player;
        ((ArmorEffectManager.Provider) player).neodragonmounts$getManager().sendInitPacket();
        ServerConfig.INSTANCE.sync(player);
    }

    @Override
    public void copyFromPlayer(ServerPlayer player, ServerPlayer priorPlayer, boolean alive) {
        ArmorEffectManagerImpl.onPlayerClone(player, priorPlayer);
    }
}
