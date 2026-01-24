package net.dragonmounts.neo.compat.platform;

import net.dragonmounts.neo.common.capability.ArmorEffectManager.Provider;
import net.dragonmounts.neo.common.capability.ArmorEffectManagerImpl;
import net.dragonmounts.neo.common.client.ClientDragonEntity;
import net.dragonmounts.neo.common.client.ClientUtil;
import net.dragonmounts.neo.common.client.model.dragon.MouthState;
import net.dragonmounts.neo.common.component.DragonFood;
import net.dragonmounts.neo.common.entity.dragon.DragonLifeStage;
import net.dragonmounts.neo.common.entity.dragon.HatchableDragonEggEntity;
import net.dragonmounts.neo.common.network.s2c.*;
import net.dragonmounts.neo.compat.registry.CooldownCategory;
import net.dragonmounts.neo.config.ConfigEntry;
import net.dragonmounts.neo.config.S2CSyncConfigPayload;
import net.dragonmounts.neo.config.ServerConfig;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

import static net.dragonmounts.neo.config.EntryUtil.override;
import static net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver;
import static net.minecraft.util.Mth.DEG_TO_RAD;

public class ClientNetworkHandler {
    public static void send(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }

    public static void handleArmorRiposte(ArmorRipostePayload payload, ClientPlayNetworking.Context context) {
        var level = context.player().clientLevel;
        var entity = level.getEntity(payload.id());
        if (entity == null) return;
        int flag = payload.flag();
        double x = entity.getX();
        double z = entity.getZ();
        if ((flag & 0b01) == 0b01) {
            double y = entity.getY() + 0.1;
            for (int i = -30; i < 31; ++i) {
                level.addParticle(ParticleTypes.CLOUD, x, y, z, Math.sin(i), 0, Math.cos(i));
            }
            level.playLocalSound(entity, SoundEvents.GRASS_BREAK, SoundSource.PLAYERS, 0.46F, 1.0F);
        }
        if ((flag & 0b10) == 0b10) {
            double y = entity.getY() + 1;
            for (int i = -27; i < 28; ++i) {
                level.addParticle(ParticleTypes.FLAME, x, y, z, Math.sin(i) / 3, 0, Math.cos(i) / 3);
            }
            level.playLocalSound(entity, SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.46F, 1.0F);
        }
    }

    public static void handleCooldownInit(InitCooldownPayload payload, ClientPlayNetworking.Context context) {
        ArmorEffectManagerImpl.init(payload.data());
    }

    public static void handleCooldownSync(SyncCooldownPayload payload, ClientPlayNetworking.Context context) {
        var category = CooldownCategory.REGISTRY.byId(payload.id());
        if (category == null) return;
        ((Provider) context.player()).neodragonmounts$getManager().setCooldown(category, payload.cd());
    }

    public static void handleEggWobble(WobbleEggPayload payload, ClientPlayNetworking.Context context) {
        if (context.player().clientLevel.getEntity(payload.id()) instanceof HatchableDragonEggEntity egg) {
            int flag = payload.flag();
            egg.applyWobble(
                    payload.amplitude(),
                    // -0 == +0, so offset all non-negative numbers by +1
                    (flag & 0b10) == 0b10 ? -payload.axis() : payload.axis() + 1,
                    (flag & 0b01) == 0b01
            );
        }
    }

    public static void handleDragonSync(SyncDragonAgePayload payload, ClientPlayNetworking.Context context) {
        if (context.player().clientLevel.getEntity(payload.id()) instanceof ClientDragonEntity dragon) {
            dragon.setAge(payload.age());
            dragon.setLifeStage(payload.stage(), false, false);
        }
    }

    public static void handleFeedDragon(FeedDragonPayload payload, ClientPlayNetworking.Context context) {
        var level = context.player().clientLevel;
        if (level.getEntity(payload.id()) instanceof ClientDragonEntity dragon) {
            dragon.setAge(payload.age());
            dragon.setLifeStage(payload.stage(), false, false);
            var stack = payload.food();
            var food = DragonFood.getInstance(stack);
            if (food == null) return;
            if (dragon.getLifeStage() != DragonLifeStage.ADULT) {
                dragon.refreshForcedAgeTimer();
            }
            level.playLocalSound(dragon, food.majorSound().value(), SoundSource.NEUTRAL, 1F, 0.75F);
            @SuppressWarnings("SimplifyOptionalCallChains") var minor = food.minorSound().orElse(null);
            if (minor != null) {
                level.playLocalSound(dragon, minor.value(), SoundSource.NEUTRAL, 0.25F, 0.75F);
            }
            dragon.animator.transitMouthState(MouthState.EATING, true);
            dragon.animator.remainingEating = MouthState.EATING.duration;
            var particles = food.particles().orElse(stack);
            if (particles.isEmpty()) return;
            var pos = dragon.getHeadRelativeOffset(0.0F, -8.0F, 20.0F);
            var option = new ItemParticleOption(ParticleTypes.ITEM, particles);
            var random = dragon.getRandom();
            float xRot = -dragon.getXRot() * DEG_TO_RAD, yRot = -dragon.getYRot() * DEG_TO_RAD;
            double cosX = Mth.cos(xRot), sinX = Mth.sin(xRot), cosY = Mth.cos(yRot), sinY = Mth.sin(yRot);
            for (int i = 0; i < 8; ++i) {
                double x = (random.nextFloat() - 0.5) * 0.1, y = random.nextFloat() * 0.1 + 0.1;
                level.addParticle(option, pos.x, pos.y, pos.z, x * cosY + x * sinX * sinY, y * cosX + 0.05, x * sinX * cosY - x * sinY);
            }
        }
    }

    public static void handleEggSync(SyncEggAgePayload payload, ClientPlayNetworking.Context context) {
        if (context.player().clientLevel.getEntity(payload.id()) instanceof HatchableDragonEggEntity egg) {
            egg.setAge(payload.age(), false);
        }
    }

    public static void handleConfigSync(S2CSyncConfigPayload payload, ClientPlayNetworking.Context ignored) {
        if (ClientUtil.isRemoteServer()) {
            ServerConfig.INSTANCE.getEntries().forEach(ConfigEntry::reset);
            for (var config : payload.entries()) {
                var entry = ServerConfig.INSTANCE.getEntry(config.id());
                if (entry == null) continue;
                override(entry, config.value());
            }
        }
    }

    public static void handleConfig(ConfigPayloadEntry<?> payload, ClientPlayNetworking.Context ignored) {
        var entry = ServerConfig.INSTANCE.getEntry(payload.id());
        if (entry != null) {
            override(entry, payload.getAsTag());
        }
    }

    public static void initClient() {
        registerGlobalReceiver(SyncCooldownPayload.TYPE, ClientNetworkHandler::handleCooldownSync);
        registerGlobalReceiver(ArmorRipostePayload.TYPE, ClientNetworkHandler::handleArmorRiposte);
        registerGlobalReceiver(InitCooldownPayload.TYPE, ClientNetworkHandler::handleCooldownInit);
        registerGlobalReceiver(WobbleEggPayload.TYPE, ClientNetworkHandler::handleEggWobble);
        registerGlobalReceiver(SyncDragonAgePayload.TYPE, ClientNetworkHandler::handleDragonSync);
        registerGlobalReceiver(FeedDragonPayload.TYPE, ClientNetworkHandler::handleFeedDragon);
        registerGlobalReceiver(SyncEggAgePayload.TYPE, ClientNetworkHandler::handleEggSync);
        registerGlobalReceiver(S2CSyncConfigPayload.TYPE, ClientNetworkHandler::handleConfigSync);
        registerGlobalReceiver(BooleanConfigPayload.TYPE, ClientNetworkHandler::handleConfig);
        registerGlobalReceiver(DoubleConfigPayload.TYPE, ClientNetworkHandler::handleConfig);
        registerGlobalReceiver(IntegerConfigPayload.TYPE, ClientNetworkHandler::handleConfig);
    }
}
