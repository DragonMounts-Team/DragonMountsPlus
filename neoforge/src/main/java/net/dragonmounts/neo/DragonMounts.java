package net.dragonmounts.neo;

import net.dragonmounts.neo.common.DragonMountsShared;
import net.dragonmounts.neo.common.capability.ArmorEffectManager;
import net.dragonmounts.neo.common.capability.ArmorEffectManagerImpl;
import net.dragonmounts.neo.common.command.DMCommands;
import net.dragonmounts.neo.common.init.*;
import net.dragonmounts.neo.common.network.c2s.*;
import net.dragonmounts.neo.common.network.s2c.*;
import net.dragonmounts.neo.compat.platform.ClientNetworkHandler;
import net.dragonmounts.neo.compat.platform.ServerNetworkHandler;
import net.dragonmounts.neo.compat.registry.EntityHolder;
import net.dragonmounts.neo.compat.registry.RegistryHandler;
import net.dragonmounts.neo.config.EntryUtil;
import net.dragonmounts.neo.config.ServerConfig;
import net.dragonmounts.neo.data.*;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.Collections;

import static net.dragonmounts.neo.common.block.HatchableDragonEggBlock.spawn;
import static net.dragonmounts.neo.common.util.EntityUtil.addOrMergeEffect;

@Mod(DragonMountsShared.NAMESPACE)
public class DragonMounts {
    public DragonMounts(IEventBus modbus, ModContainer container) {
        ServerConfig.registerConfig(container);
        modbus.addListener(DragonMounts::commonSetup);
        modbus.addListener(RegistryHandler::registerRegistries);
        modbus.addListener(RegistryHandler::registerEntries);
        modbus.addListener(DragonMounts::registerAttributes);
        modbus.addListener(DragonMounts::initNetwork);
        modbus.addListener(DragonMounts::modifyCreativeTab);
        modbus.addListener(DragonMounts::gatherClientData);
        modbus.addListener(DragonMounts::gatherServerData);
        modbus.addListener(ModConfigEvent.Loading.class, EntryUtil::onLoad);
        modbus.addListener(ModConfigEvent.Reloading.class, EntryUtil::onLoad);
        modbus.addListener(EntryUtil::onUnload);
        DMEntities.init();
        DMDataComponents.init();
        DMItems.initStatics();
        DMBlocks.init();
        DMBlockEntities.init();
        DMSounds.init();
        DMActivities.init();
        DMMemories.init();
        DMSensors.init();
        DMStructures.init();
        DMParticles.init();
        DMMobEffects.init();
        DMRecipes.init();
    }

    public static void initNetwork(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(ControlDragonPayload.TYPE, ControlDragonPayload.CODEC, ServerNetworkHandler::handleDragonRiding);
        registrar.playToServer(TeleportDragonPayload.TYPE, TeleportDragonPayload.CODEC, ServerNetworkHandler::handleTeleportDragon);
        registrar.playToServer(ToggleSittingByUUIDPayload.TYPE, ToggleSittingByUUIDPayload.CODEC, ServerNetworkHandler::handleToggleSitting);
        registrar.playToServer(ToggleSittingByIDPayload.TYPE, ToggleSittingByIDPayload.CODEC, ServerNetworkHandler::handleToggleSitting);
        registrar.playToServer(ToggleTrustPayload.TYPE, ToggleTrustPayload.CODEC, ServerNetworkHandler::handleToggleTrust);
        registrar.playToServer(ToggleFollowingPayload.TYPE, ToggleFollowingPayload.CODEC, ServerNetworkHandler::handleToggleFollowing);
        registrar.playToServer(RenameFlutePayload.TYPE, RenameFlutePayload.CODEC, ServerNetworkHandler::handleRenameFlute);
        registrar.playToClient(SyncCooldownPayload.TYPE, SyncCooldownPayload.CODEC, ClientNetworkHandler::handleCooldownSync);
        registrar.playToClient(ArmorRipostePayload.TYPE, ArmorRipostePayload.CODEC, ClientNetworkHandler::handleArmorRiposte);
        registrar.playToClient(InitCooldownPayload.TYPE, InitCooldownPayload.CODEC, ClientNetworkHandler::handleCooldownInit);
        registrar.playToClient(WobbleEggPayload.TYPE, WobbleEggPayload.CODEC, ClientNetworkHandler::handleEggWobble);
        registrar.playToClient(SyncDragonAgePayload.TYPE, SyncDragonAgePayload.CODEC, ClientNetworkHandler::handleDragonSync);
        registrar.playToClient(FeedDragonPayload.TYPE, FeedDragonPayload.CODEC, ClientNetworkHandler::handleFeedDragon);
        registrar.playToClient(SyncEggAgePayload.TYPE, SyncEggAgePayload.CODEC, ClientNetworkHandler::handleEggSync);
        registrar.playToClient(BooleanConfigPayload.TYPE, BooleanConfigPayload.CODEC, ClientNetworkHandler::handleConfig);
        registrar.playToClient(DoubleConfigPayload.TYPE, DoubleConfigPayload.CODEC, ClientNetworkHandler::handleConfig);
        registrar.playToClient(IntegerConfigPayload.TYPE, IntegerConfigPayload.CODEC, ClientNetworkHandler::handleConfig);
    }

    static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(DMItems::setup);
        var play = NeoForge.EVENT_BUS;
        play.addListener(DragonMounts::registerCommands);
        play.addListener(DragonMounts::onPlayReady);
        play.addListener(DragonMounts::onPlayerClone);
        play.addListener(DragonMounts::onDropExperience);
        play.addListener(DragonMounts::onAttackEntity);
        play.addListener(DragonMounts::onEntityHurt);
        play.addListener(DragonMounts::onPlayerInteract);
    }

    static void registerAttributes(EntityAttributeCreationEvent event) {
        EntityHolder.registerAttributes(event);
    }

    static void modifyCreativeTab(BuildCreativeModeTabContentsEvent event) {
        var tab = event.getTabKey();
        if (tab.equals(CreativeModeTabs.SPAWN_EGGS)) {
            DMItemGroups.DRAGON_SPAWN_EGGS.accept(event.getParameters(), event);
        } else if (tab.equals(CreativeModeTabs.FUNCTIONAL_BLOCKS)) {
            DMItemGroups.DRAGON_SPAWN_EGGS.accept(event.getParameters(), event);
        }
    }

    static void registerCommands(RegisterCommandsEvent event) {
        DMCommands.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
    }

    static void onPlayReady(PlayerEvent.PlayerLoggedInEvent event) {
        var player = event.getEntity();
        ((ArmorEffectManager.Provider) player).neodragonmounts$getManager().sendInitPacket();
    }

    static void onPlayerClone(PlayerEvent.Clone event) {
        ArmorEffectManagerImpl.onPlayerClone(event.getEntity(), event.getOriginal());
    }

    static void onDropExperience(LivingExperienceDropEvent event) {
        if (event.getAttackingPlayer() instanceof ArmorEffectManager.Provider provider && provider.neodragonmounts$getManager().isActive(DMArmorEffects.ENCHANTED)) {
            event.setDroppedExperience((int) Math.ceil(event.getOriginalExperience() * 1.5F));
        }
    }

    static void onAttackEntity(AttackEntityEvent event) {
        var player = event.getEntity();
        DMArmorEffects.meleeChanneling(player, player.level(), InteractionHand.MAIN_HAND, event.getTarget(), null);
    }

    static void onEntityHurt(LivingDamageEvent.Pre event) {
        var self = event.getEntity();
        if (!(self instanceof ArmorEffectManager.Provider)) return;
        var level = (ServerLevel) self.level();
        var ice = DMArmorEffects.ICE;
        var nether = DMArmorEffects.NETHER;
        var manager = ((ArmorEffectManager.Provider) self).neodragonmounts$getManager();
        var iceFlag = manager.isActive(ice) && manager.getCooldown(ice) <= 0;
        var netherFlag = manager.isActive(nether) && manager.getCooldown(nether) <= 0;
        int flag = (iceFlag ? 0b01 : 0b00) | (netherFlag ? 0b10 : 0b00);
        if (flag == 0) return;
        var entities = level.getEntities(self, self.getBoundingBox().inflate(5.0D), EntitySelector.NO_CREATIVE_OR_SPECTATOR);
        if (entities.isEmpty()) return;
        var freeze = level.damageSources().freeze();
        for (var entity : entities) {
            if (entity instanceof LivingEntity target) {
                target.knockback(0.4F, 1, 1);
                if (iceFlag) {
                    addOrMergeEffect(target, MobEffects.MOVEMENT_SLOWDOWN, 200, 1, false, true, true);
                    entity.invulnerableTime = 0;
                    entity.hurtServer(level, freeze, 1F);
                }
            } else if (iceFlag) {
                entity.invulnerableTime = 0;
                entity.hurtServer(level, freeze, 1F);
            }
            if (netherFlag) {
                int current = entity.getRemainingFireTicks();
                entity.setRemainingFireTicks(current > 0 ? current + 200 : 200);
            }
        }
        if (iceFlag) {
            manager.setCooldown(ice, ice.cooldown);
        }
        if (netherFlag) {
            manager.setCooldown(nether, nether.cooldown);
        }
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(self, new ArmorRipostePayload(self.getId(), flag));
    }

    static void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        var level = event.getLevel();
        if (!level.isClientSide && !level.dimension().equals(Level.END) && ServerConfig.INSTANCE.isEggOverridden.get()) {
            var pos = event.getPos();
            if (level.getBlockState(pos).getBlock() == Blocks.DRAGON_EGG) {
                event.setUseBlock(TriState.FALSE);
                spawn(level, pos, DragonTypes.ENDER, true);
            }
        }
    }

    public static void gatherCommonData(GatherDataEvent event) {
        event.createDatapackRegistryObjects(new RegistrySetBuilder()
                        .add(Registries.STRUCTURE, DMStructures::bootstrap)
                        .add(Registries.STRUCTURE_SET, DMStructureSets::bootstrap),
                Collections.singleton(DragonMountsShared.NAMESPACE)
        );
        event.createProvider(DMRecipeProvider.Factory::new);
        event.createProvider(DMLootProvider::new);
        event.createProvider(DMBiomeTagProvider::new);
        event.createProvider(DMEntityTagProvider::new);
        event.createProvider(DMStructureTagProvider::new);
        event.createBlockAndItemTags(DMBlockTagProvider::new, DMItemTagProvider::new);
    }

    public static void gatherClientData(GatherDataEvent.Client event) {
        event.createProvider(DMModelProvider::new);
        event.createProvider(DMEquipmentAssetProvider::from);
        gatherCommonData(event);
    }

    public static void gatherServerData(GatherDataEvent.Server event) {
        gatherCommonData(event);
    }
}
