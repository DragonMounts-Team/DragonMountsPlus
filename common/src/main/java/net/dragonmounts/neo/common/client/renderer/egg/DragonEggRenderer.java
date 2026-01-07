package net.dragonmounts.neo.common.client.renderer.egg;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import net.dragonmounts.neo.common.entity.dragon.HatchableDragonEggEntity;
import net.dragonmounts.neo.common.init.DMBlocks;
import net.dragonmounts.neo.config.ServerConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;

import static net.dragonmounts.neo.common.entity.dragon.HatchableDragonEggEntity.EGG_CRACK_THRESHOLD;
import static net.dragonmounts.neo.common.util.math.MathUtil.HALF_RAD_FACTOR;

/// @see net.minecraft.client.renderer.entity.FallingBlockRenderer
public class DragonEggRenderer extends EntityRenderer<HatchableDragonEggEntity, DragonEggRenderState> {
    /// Textures from 0 to 8 (inclusive) indicate unhatchable and the last one (9) indicates hatchable.
    protected final static float CRACK_PROGRESS_TO_STAGE = (ModelBakery.DESTROY_STAGE_COUNT - 1) / (1.0F - EGG_CRACK_THRESHOLD);
    protected final BlockRenderDispatcher dispatcher;

    public DragonEggRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.dispatcher = context.getBlockRenderDispatcher();
    }

    @Override
    public void extractRenderState(HatchableDragonEggEntity egg, DragonEggRenderState state, float partialTicks) {
        super.extractRenderState(egg, state, partialTicks);
        state.progress = egg.getAge() / (float) ServerConfig.INSTANCE.minIncubationDuration.getAsInt();
        state.block = egg.asBlock(DMBlocks.ENDER_DRAGON_EGG.get()).defaultBlockState();
        state.amplitude = egg.getAmplitude(partialTicks);
        if (state.amplitude != 0.0F) {
            state.axis = egg.getWobbleAxis();
            state.amplitude *= HALF_RAD_FACTOR;
        }
    }

    @Override
    public void render(DragonEggRenderState state, PoseStack matrices, MultiBufferSource buffers, int light) {
        matrices.pushPose();
        if (state.amplitude != 0.0F) {
            float sin = Mth.sin(state.amplitude);
            matrices.mulPose(new Quaternionf(
                    Mth.cos(state.axis) * sin,
                    0.0F,
                    Mth.sin(state.axis) * sin,
                    Mth.cos(state.amplitude)
            ));
        }
        matrices.translate(-0.5, 0.0, -0.5);
        if (state.progress < EGG_CRACK_THRESHOLD) {
            this.dispatcher.renderSingleBlock(state.block, matrices, buffers, light, OverlayTexture.NO_OVERLAY);
        } else {
            var generator = new SheetedDecalTextureGenerator(Minecraft.getInstance().renderBuffers().crumblingBufferSource().getBuffer(
                    ModelBakery.DESTROY_TYPES.get(Math.min((int) ((state.progress - EGG_CRACK_THRESHOLD) * CRACK_PROGRESS_TO_STAGE), 9))
            ), matrices.last(), 1.0F);
            this.dispatcher.renderSingleBlock(state.block, matrices, type -> {
                var buffer = buffers.getBuffer(type);
                return type.affectsCrumbling() ? VertexMultiConsumer.create(generator, buffer) : buffer;
            }, light, OverlayTexture.NO_OVERLAY);
        }
        super.render(state, matrices, buffers, light);
        matrices.popPose();
    }

    @Override
    public DragonEggRenderState createRenderState() {
        return new DragonEggRenderState();
    }
}
