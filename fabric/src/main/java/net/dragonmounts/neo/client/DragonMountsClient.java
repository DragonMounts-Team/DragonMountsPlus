package net.dragonmounts.neo.client;

import net.dragonmounts.neo.common.DragonMountsShared;
import net.dragonmounts.neo.common.api.DescribedArmorEffect;
import net.dragonmounts.neo.common.client.ClientDragonEntity;
import net.dragonmounts.neo.common.client.gui.DragonCoreScreen;
import net.dragonmounts.neo.common.client.gui.DragonInventoryScreen;
import net.dragonmounts.neo.common.client.model.dragon.BuiltinFactory;
import net.dragonmounts.neo.common.client.renderer.block.DragonCoreRenderer;
import net.dragonmounts.neo.common.client.renderer.block.DragonHeadRenderer;
import net.dragonmounts.neo.common.client.renderer.dragon.TameableDragonRenderer;
import net.dragonmounts.neo.common.client.renderer.egg.DragonEggRenderer;
import net.dragonmounts.neo.common.init.*;
import net.dragonmounts.neo.common.network.c2s.ControlDragonPayload;
import net.dragonmounts.neo.common.util.ArrayUtil;
import net.dragonmounts.neo.compat.platform.ClientNetworkHandler;
import net.dragonmounts.neo.compat.platform.DMScreenHandlers;
import net.dragonmounts.neo.compat.registry.DragonVariant;
import net.dragonmounts.neo.config.ClientConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.SpecialBlockRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.CreativeModeTabs;
import org.jetbrains.annotations.Nullable;

import static net.dragonmounts.neo.common.DragonMountsShared.makeId;
import static net.fabricmc.fabric.api.resource.ResourceManagerHelper.registerBuiltinResourcePack;

@Environment(EnvType.CLIENT)
public class DragonMountsClient implements
        ClientModInitializer,
        TooltipComponentCallback,
        ClientTickEvents.StartTick,
        SimpleSynchronousResourceReloadListener {
    public static final ResourceLocation MODEL_RELOADER = makeId("model_reloader");

    static void registerResourcePacks(ModContainer mod) {
        registerBuiltinResourcePack(makeId("classic_amulet"), mod, Component.translatable("resourcePack.neodragonmounts.classic_amulet.name"), ResourcePackActivationType.NORMAL);
    }

    @Override
    public void onInitializeClient() {
        ClientConfig.init();
        ClientNetworkHandler.initClient();
        DMKeyMappings.register(KeyBindingHelper::registerKeyBinding);
        TooltipComponentCallback.EVENT.register(this);
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(entries ->
                DMItemGroups.DRAGON_SPAWN_EGGS.accept(entries.getContext(), entries)
        );
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries ->
                DMItemGroups.DRAGON_EGGS.accept(entries.getContext(), entries)
        );
        MenuScreens.register(DMScreenHandlers.DRAGON_CORE, DragonCoreScreen::new);
        MenuScreens.register(DMScreenHandlers.DRAGON_INVENTORY, DragonInventoryScreen::new);
        for (var model : BuiltinFactory.values()) {
            EntityModelLayerRegistry.registerModelLayer(model.location, model::makeModel);
        }
        SpecialModelRenderers.ID_MAPPER.put(makeId("dragon_core"), DragonCoreRenderer.Unbaked.CODEC);
        SpecialModelRenderers.ID_MAPPER.put(makeId("dragon_head"), DragonHeadRenderer.Unbaked.CODEC);
        SpecialBlockRendererRegistry.register(DMBlocks.DRAGON_CORE.get(), new DragonCoreRenderer.Unbaked(0.0F, Direction.SOUTH));
        for (var variant : DragonVariants.BUILTIN_VALUES) {
            var head = variant.head;
            var renderer = new DragonHeadRenderer.Unbaked(variant, 0.0F);
            SpecialBlockRendererRegistry.register(head.standing.get(), renderer);
            SpecialBlockRendererRegistry.register(head.wall.get(), renderer);
        }
        ClientTickEvents.START_CLIENT_TICK.register(this);
        BlockEntityRenderers.register(DMBlockEntities.DRAGON_CORE.get(), DragonCoreRenderer::new);
        BlockEntityRenderers.register(DMBlockEntities.DRAGON_HEAD.get(), DragonHeadRenderer.INSTANCE);
        EntityRendererRegistry.register(DMEntities.HATCHABLE_DRAGON_EGG.get(), DragonEggRenderer::new);
        EntityRendererRegistry.register(DMEntities.TAMEABLE_DRAGON.cast(), TameableDragonRenderer::new);
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(this);
        ParticleFactoryRegistry.getInstance().register(DMParticles.DRAGON_BREATH, BreathParticleProvider::new);
        ClientCommandRegistrationCallback.EVENT.register(DMClientCommand::register);
        FabricLoader.getInstance()
                .getModContainer(DragonMountsShared.NAMESPACE)
                .ifPresent(DragonMountsClient::registerResourcePacks);
    }

    @Override
    public @Nullable ClientTooltipComponent getComponent(TooltipComponent data) {
        return data instanceof DescribedArmorEffect effect ? effect.getClientTooltip() : null;
    }

    @Override
    public void onStartTick(Minecraft client) {
        var player = client.player;
        if (player == null) return;
        if (player.getVehicle() instanceof ClientDragonEntity dragon) {
            if (player != dragon.getControllingPassenger()) return;
            int flags = ArrayUtil.compressFlags(
                    DMKeyMappings.DESCEND.isDown(),
                    client.options.keySprint.isDown(),
                    DMKeyMappings.BREATHE.isDown()
            );
            if (flags == dragon.controlFlags) return;
            dragon.controlFlags = flags;
            ClientNetworkHandler.send(new ControlDragonPayload(dragon.getId(), flags));
        }
    }

    @Override
    public ResourceLocation getFabricId() {
        return MODEL_RELOADER;
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        var models = Minecraft.getInstance().getEntityModels();
        for (var variant : DragonVariant.REGISTRY) {
            variant.appearance.onReload(models);
        }
    }
}
