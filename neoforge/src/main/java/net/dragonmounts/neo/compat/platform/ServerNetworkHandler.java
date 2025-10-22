package net.dragonmounts.neo.compat.platform;

import net.dragonmounts.neo.common.entity.dragon.Relation;
import net.dragonmounts.neo.common.entity.dragon.ServerDragonEntity;
import net.dragonmounts.neo.common.init.DMMemories;
import net.dragonmounts.neo.common.init.DMSounds;
import net.dragonmounts.neo.common.inventory.DragonInventoryHandler;
import net.dragonmounts.neo.common.item.FluteItem;
import net.dragonmounts.neo.common.network.c2s.*;
import net.dragonmounts.neo.common.util.ArrayUtil;
import net.dragonmounts.neo.common.util.EntityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

public class ServerNetworkHandler {
    public static void sendTo(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    public static void sendTracking(Entity entity, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
    }

    public static void sendToAll(@Nullable MinecraftServer ignored, CustomPacketPayload payload) {
        PacketDistributor.sendToAllPlayers(payload);
    }

    public static void handleDragonRiding(ControlDragonPayload payload, IPayloadContext context) {
        if (context.player().level().getEntity(payload.id()) instanceof ServerDragonEntity dragon) {
            boolean[] flags = ArrayUtil.readFlags(payload.flags());
            dragon.setShiftKeyDown(flags[0]);
            dragon.setSprinting(flags[1]);
            dragon.setBreathing(flags[2]);
        }
    }

    public static void handleTeleportDragon(TeleportDragonPayload payload, IPayloadContext context) {
        var player = (ServerPlayer) context.player();
        var dragon = FluteItem.getOrDeny(player, payload.dragon());
        if (dragon == null) return;
        dragon.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        var pos = payload.pos();
        if (!EntityUtil.teleportToAround(dragon, pos.getX(), pos.getY(), pos.getZ())) {
            player.sendSystemMessage(Component.translatable("message.neodragonmounts.flute.invalid_pos"), true);
        }
        player.level().playSound(null, player, DMSounds.FLUTE_BLOW_LONG, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    public static void handleToggleSitting(ToggleSittingByUUIDPayload payload, IPayloadContext context) {
        var player = (ServerPlayer) context.player();
        var dragon = FluteItem.getOrDeny(player, payload.dragon());
        if (dragon == null) return;
        dragon.setOrderedToSit(!dragon.isOrderedToSit());
        player.level().playSound(null, player, DMSounds.FLUTE_BLOW_SHORT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    public static void handleToggleSitting(ToggleSittingByIDPayload payload, IPayloadContext context) {
        var player = (ServerPlayer) context.player();
        if (player.serverLevel().getEntity(payload.dragon()) instanceof ServerDragonEntity dragon && !Relation.denyIfUntrusted(dragon, player)) {
            dragon.setOrderedToSit(!dragon.isOrderedToSit());
        }
    }

    public static void handleToggleTrust(ToggleTrustPayload payload, IPayloadContext context) {
        var player = (ServerPlayer) context.player();
        if (player.serverLevel().getEntity(payload.dragon()) instanceof ServerDragonEntity dragon && !Relation.denyIfNotOwner(dragon, player)) {
            dragon.setTrustingAnyPlayer(!dragon.isTrustingAnyPlayer());
        }
    }

    public static void handleToggleFollowing(ToggleFollowingPayload payload, IPayloadContext context) {
        var player = (ServerPlayer) context.player();
        var dragon = FluteItem.getOrDeny(player, payload.dragon());
        if (dragon == null) return;
        var brain = dragon.getBrain();
        if (brain.hasMemoryValue(DMMemories.DISABLED_FOLLOWING_OWNER)) {
            brain.eraseMemory(DMMemories.DISABLED_FOLLOWING_OWNER);
        } else {
            brain.setMemory(DMMemories.DISABLED_FOLLOWING_OWNER, Unit.INSTANCE);
            var walk = brain.getMemory(MemoryModuleType.WALK_TARGET).orElse(null);
            if (walk != null && walk.getTarget() instanceof EntityTracker tracker && tracker.getEntity() == player) {
                brain.eraseMemory(MemoryModuleType.WALK_TARGET);
            }
        }
        player.level().playSound(null, player, DMSounds.FLUTE_BLOW_SHORT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    public static void handleRenameFlute(RenameFlutePayload payload, IPayloadContext context) {
        if (context.player().containerMenu instanceof DragonInventoryHandler handler) {
            handler.flute.applyName(payload.name());
        }
    }
}