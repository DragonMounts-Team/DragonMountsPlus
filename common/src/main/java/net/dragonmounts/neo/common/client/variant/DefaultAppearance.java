package net.dragonmounts.neo.common.client.variant;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.dragonmounts.neo.common.client.DMParticleSprites;
import net.dragonmounts.neo.common.client.breath.BreathParticleFactory;
import net.dragonmounts.neo.common.client.breath.impl.FlameBreathParticle;
import net.dragonmounts.neo.common.client.model.dragon.DragonModel;
import net.dragonmounts.neo.common.client.renderer.RenderStateAccessor;
import net.dragonmounts.neo.common.client.renderer.dragon.DragonRenderState;
import net.dragonmounts.neo.common.entity.breath.BreathParticleOption;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.equipment.EquipmentAsset;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class DefaultAppearance implements VariantAppearance {
    private static final Object2ObjectOpenHashMap<String, Map<ResourceKey<EquipmentAsset>, ResourceLocation>> ARMOR_TEXTURES = new Object2ObjectOpenHashMap<>();
    private static final Map<ResourceKey<EquipmentAsset>, ResourceLocation> DEFAULT_ARMOR_TEXTURES = getTextures(null);

    synchronized static Map<ResourceKey<EquipmentAsset>, ResourceLocation> getTextures(@Nullable String category) {
        return ARMOR_TEXTURES.computeIfAbsent(category, $ -> new Reference2ObjectOpenHashMap<>());
    }

    public synchronized static void registerArmorTexture(@Nullable String category, ResourceKey<EquipmentAsset> asset, ResourceLocation texture) {
        if (getTextures(category).put(asset, texture) != null) {
            throw new IllegalStateException("Duplicate asset: " + asset);
        }
    }

    public final ModelLayerLocation modelLocation;
    public final BreathParticleFactory factory;
    public final ResourceLocation breath;
    public final ResourceLocation body;
    public final RenderType base;
    public final RenderType decal;
    public final RenderType glow;
    public final RenderType glowDecal;
    public final RenderType chest;
    public final RenderType saddle;
    private DragonModel model;
    final Map<ResourceKey<EquipmentAsset>, ResourceLocation> armors;

    public DefaultAppearance(
            ModelLayerLocation modelLocation,
            ResourceLocation body,
            ResourceLocation glow,
            ResourceLocation breath,
            Map<ResourceKey<EquipmentAsset>, ResourceLocation> armors,
            BreathParticleFactory factory
    ) {
        this.modelLocation = modelLocation;
        this.factory = factory;
        this.breath = breath;
        this.armors = armors;
        this.body = body;
        this.base = RenderType.entityCutoutNoCull(body);
        this.decal = RenderStateAccessor.entityCutoutDecal(body, DEFAULT_DISSOLVE);
        this.glow = RenderType.entityTranslucentEmissive(glow);
        this.glowDecal = RenderStateAccessor.entityTranslucentEmissiveDecal(glow, DEFAULT_DISSOLVE);
        this.chest = RenderType.entityCutoutNoCull(DEFAULT_CHEST);
        this.saddle = RenderType.entityCutoutNoCull(DEFAULT_SADDLE);
    }

    @Override
    public void onReload(EntityModelSet models) {
        this.model = new DragonModel(models.bakeLayer(this.modelLocation));
    }

    @Override
    public DragonModel getModel(@Nullable DragonRenderState state) {
        return this.model;
    }

    @Override
    public ResourceLocation getBodyTexture(DragonRenderState state) {
        return this.body;
    }

    @Override
    public RenderType getBase(@Nullable DragonRenderState state) {
        return this.base;
    }

    @Override
    public RenderType getGlow(@Nullable DragonRenderState state) {
        return this.glow;
    }

    @Override
    public RenderType getDecal(DragonRenderState state) {
        return this.decal;
    }

    @Override
    public RenderType getGlowDecal(DragonRenderState state) {
        return this.glowDecal;
    }

    @Override
    public RenderType getChest(DragonRenderState state) {
        return this.chest;
    }

    @Override
    public RenderType getSaddle(DragonRenderState state) {
        return this.saddle;
    }

    @Override
    public @Nullable ResourceLocation getArmorTexture(ResourceKey<EquipmentAsset> asset) {
        var override = this.armors.get(asset);
        return override == null ? DEFAULT_ARMOR_TEXTURES.get(asset) : override;
    }

    @Override
    public Particle createBreathParticle(BreathParticleOption option, TextureAtlas atlas, ClientLevel level, double x, double y, double z, double motionX, double motionY, double motionZ) {
        return this.factory.createParticle(option, atlas.getSprite(this.breath), level, x, y, z, motionX, motionY, motionZ);
    }

    public static class Builder {
        public final ModelLayerLocation model;
        public BreathParticleFactory factory = FlameBreathParticle.FACTORY;
        public ResourceLocation breath = DMParticleSprites.FLAME_BREATH;
        Map<ResourceKey<EquipmentAsset>, ResourceLocation> armors = DEFAULT_ARMOR_TEXTURES;

        public Builder(ModelLayerLocation model) {
            this.model = model;
        }

        public Builder setArmorCategory(@Nullable String category) {
            this.armors = getTextures(category);
            return this;
        }

        public Builder withBreath(ResourceLocation breath) {
            this.breath = breath;
            return this;
        }

        public Builder withBreath(ResourceLocation breath, BreathParticleFactory factory) {
            this.factory = factory;
            return this.withBreath(breath);
        }

        public DefaultAppearance build(ResourceLocation folder) {
            String path = folder.getPath();
            return this.build(
                    folder.withPath(TEXTURES_ROOT + path + "/body.png"),
                    folder.withPath(TEXTURES_ROOT + path + "/glow.png")
            );
        }

        public DefaultAppearance build(ResourceLocation body, ResourceLocation glow) {
            return new DefaultAppearance(this.model, body, glow, this.breath, this.armors, this.factory);
        }
    }
}
