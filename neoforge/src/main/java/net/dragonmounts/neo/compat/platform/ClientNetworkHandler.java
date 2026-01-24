package net.dragonmounts.neo.compat.platform;

import net.dragonmounts.neo.common.capability.ArmorEffectManager;
import net.dragonmounts.neo.common.capability.ArmorEffectManagerImpl;
import net.dragonmounts.neo.common.client.ClientDragonEntity;
import net.dragonmounts.neo.common.client.model.dragon.MouthState;
import net.dragonmounts.neo.common.component.DragonFood;
import net.dragonmounts.neo.common.entity.dragon.DragonLifeStage;
import net.dragonmounts.neo.common.entity.dragon.HatchableDragonEggEntity;
import net.dragonmounts.neo.common.network.s2c.*;
import net.dragonmounts.neo.compat.registry.CooldownCategory;
import net.dragonmounts.neo.config.ServerConfig;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static net.dragonmounts.neo.config.EntryUtil.override;
import static net.minecraft.util.Mth.DEG_TO_RAD;

public class ClientNetworkHandler {
    public static void send(CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }

    public static void handleArmorRiposte(ArmorRipostePayload payload, IPayloadContext context) {
        var level = ((LocalPlayer) context.player()).clientLevel;
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

    public static void handleCooldownInit(InitCooldownPayload payload, @SuppressWarnings("unused") IPayloadContext context) {
        ArmorEffectManagerImpl.init(payload.data());
    }

    public static void handleCooldownSync(SyncCooldownPayload payload, IPayloadContext context) {
        var category = CooldownCategory.REGISTRY.byId(payload.id());
        if (category == null) return;
        ((ArmorEffectManager.Provider) context.player()).neodragonmounts$getManager().setCooldown(category, payload.cd());
    }

    public static void handleEggWobble(WobbleEggPayload payload, IPayloadContext context) {
        if (((LocalPlayer) context.player()).clientLevel.getEntity(payload.id()) instanceof HatchableDragonEggEntity egg) {
            int flag = payload.flag();
            egg.applyWobble(
                    payload.amplitude(),
                    // -0 == +0, so offset all non-negative numbers by +1
                    (flag & 0b10) == 0b10 ? -payload.axis() : payload.axis() + 1,
                    (flag & 0b01) == 0b01
            );
        }
    }

    public static void handleDragonSync(SyncDragonAgePayload payload, IPayloadContext context) {
        if (((LocalPlayer) context.player()).clientLevel.getEntity(payload.id()) instanceof ClientDragonEntity dragon) {
            dragon.setAge(payload.age());
            dragon.setLifeStage(payload.stage(), false, false);
        }
    }

    public static void handleFeedDragon(FeedDragonPayload payload, IPayloadContext context) {
        var level = ((LocalPlayer) context.player()).clientLevel;
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

    public static void handleEggSync(SyncEggAgePayload payload, IPayloadContext context) {
        if (((LocalPlayer) context.player()).clientLevel.getEntity(payload.id()) instanceof HatchableDragonEggEntity egg) {
            egg.setAge(payload.age(), false);
        }
    }

    public static void handleConfig(ConfigPayloadEntry<?> payload, IPayloadContext ignored) {
        var entry = ServerConfig.INSTANCE.getEntry(payload.id());
        if (entry != null) {
            override(entry, payload.getAsTag());
        }
    }
}