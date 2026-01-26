package net.dragonmounts.neo.common.client.variant;

import net.dragonmounts.neo.common.client.model.dragon.DragonModel;
import net.dragonmounts.neo.common.client.renderer.dragon.DragonRenderState;
import net.dragonmounts.neo.common.entity.breath.BreathParticleOption;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.equipment.EquipmentAsset;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import static net.dragonmounts.neo.common.DragonMountsShared.makeId;

public interface VariantAppearance {
    String TEXTURES_ROOT = "textures/entity/dragon/";
    ResourceLocation DEFAULT_CHEST = makeId(TEXTURES_ROOT + "chest.png");
    ResourceLocation DEFAULT_SADDLE = makeId(TEXTURES_ROOT + "saddle.png");
    ResourceLocation DEFAULT_DISSOLVE = makeId(TEXTURES_ROOT + "dissolve.png");

    void onReload(EntityModelSet models);

    @Contract("!null -> !null")
    @Nullable DragonModel getModel(@Nullable DragonRenderState state);

    RenderType getBase(@Nullable DragonRenderState state);

    RenderType getGlow(@Nullable DragonRenderState state);

    RenderType getDecal(DragonRenderState state);

    RenderType getGlowDecal(DragonRenderState state);

    RenderType getChest(DragonRenderState state);

    RenderType getSaddle(DragonRenderState state);

    @Nullable ResourceLocation getArmorTexture(ResourceKey<EquipmentAsset> asset);

    ResourceLocation getBodyTexture(DragonRenderState state);

    Particle createBreathParticle(
            BreathParticleOption option,
            TextureAtlas atlas,
            ClientLevel level,
            double x,
            double y,
            double z,
            double motionX,
            double motionY,
            double motionZ
    );
}
