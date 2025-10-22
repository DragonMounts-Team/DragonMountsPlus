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
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class ServerNetworkHandler {
    public static void sendTo(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    public static void sendTracking(Entity entity, CustomPacketPayload payload) {
        for (var player : PlayerLookup.tracking(entity)) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static void sendToAll(MinecraftServer server, CustomPacketPayload payload) {
        for (var player : PlayerLookup.all(server)) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static void handleDragonRiding(ControlDragonPayload payload, ServerPlayNetworking.Context context) {
        if (context.player().level().getEntity(payload.id()) instanceof ServerDragonEntity dragon) {
            boolean[] flags = ArrayUtil.readFlags(payload.flags());
            dragon.setShiftKeyDown(flags[0]);
            dragon.setSprinting(flags[1]);
            dragon.setBreathing(flags[2]);
        }
    }

    public static void handleTeleportDragon(TeleportDragonPayload payload, ServerPlayNetworking.Context context) {
        var player = context.player();
        var dragon = FluteItem.getOrDeny(player, payload.dragon());
        if (dragon == null) return;
        dragon.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        var pos = payload.pos();
        if (!EntityUtil.teleportToAround(dragon, pos.getX(), pos.getY(), pos.getZ())) {
            player.sendSystemMessage(Component.translatable("message.neodragonmounts.flute.invalid_pos"), true);
        }
        player.level().playSound(null, player, DMSounds.FLUTE_BLOW_LONG, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    public static void handleToggleSitting(ToggleSittingByUUIDPayload payload, ServerPlayNetworking.Context context) {
        var player = context.player();
        var dragon = FluteItem.getOrDeny(player, payload.dragon());
        if (dragon == null) return;
        dragon.setOrderedToSit(!dragon.isOrderedToSit());
        player.level().playSound(null, player, DMSounds.FLUTE_BLOW_SHORT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    public static void handleToggleSitting(ToggleSittingByIDPayload payload, ServerPlayNetworking.Context context) {
        var player = context.player();
        if (player.serverLevel().getEntity(payload.dragon()) instanceof ServerDragonEntity dragon && !Relation.denyIfUntrusted(dragon, player)) {
            dragon.setOrderedToSit(!dragon.isOrderedToSit());
        }
    }

    public static void handleToggleTrust(ToggleTrustPayload payload, ServerPlayNetworking.Context context) {
        var player = context.player();
        if (player.serverLevel().getEntity(payload.dragon()) instanceof ServerDragonEntity dragon && !Relation.denyIfNotOwner(dragon, player)) {
            dragon.setTrustingAnyPlayer(!dragon.isTrustingAnyPlayer());
        }
    }

    public static void handleToggleFollowing(ToggleFollowingPayload payload, ServerPlayNetworking.Context context) {
        var player = context.player();
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

    public static void handleRenameFlute(RenameFlutePayload payload, ServerPlayNetworking.Context context) {
        if (context.player().containerMenu instanceof DragonInventoryHandler handler) {
            handler.flute.applyName(payload.name());
        }
    }
}
