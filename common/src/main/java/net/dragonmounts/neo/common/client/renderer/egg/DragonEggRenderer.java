package net.dragonmounts.neo.common.client.renderer.egg;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import net.dragonmounts.neo.common.entity.dragon.HatchableDragonEggEntity;
import net.dragonmounts.neo.common.init.DMBlocks;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.RenderShape;
import org.joml.Quaternionf;

import static net.dragonmounts.neo.common.entity.dragon.HatchableDragonEggEntity.EGG_CRACK_THRESHOLD;
import static net.dragonmounts.neo.common.entity.dragon.HatchableDragonEggEntity.MIN_HATCHING_TIME;
import static net.dragonmounts.neo.common.util.math.MathUtil.HALF_RAD_FACTOR;
import static net.minecraft.client.renderer.ItemBlockRenderTypes.getMovingBlockRenderType;

/// @see net.minecraft.client.renderer.entity.FallingBlockRenderer
public class DragonEggRenderer extends EntityRenderer<HatchableDragonEggEntity, DragonEggRenderState> {
    protected final BlockRenderDispatcher dispatcher;

    public DragonEggRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.dispatcher = context.getBlockRenderDispatcher();
    }

    @Override
    public void extractRenderState(HatchableDragonEggEntity egg, DragonEggRenderState state, float partialTicks) {
        super.extractRenderState(egg, state, partialTicks);
        var pos = BlockPos.containing(egg.getX(), egg.getBoundingBox().maxY, egg.getZ());
        state.pos = pos;
        state.age = egg.getAge();
        state.level = egg.level();
        state.random = egg.level().random;
        state.biome = egg.level().getBiome(pos);
        state.block = egg.asBlock(DMBlocks.ENDER_DRAGON_EGG.get()).defaultBlockState();
        state.amplitude = egg.getAmplitude(partialTicks);
        if (state.amplitude != 0) {
            state.axis = egg.getRotationAxis();
            state.amplitude *= HALF_RAD_FACTOR;
        }
    }

    @Override
    public void render(DragonEggRenderState state, PoseStack matrices, MultiBufferSource buffers, int light) {
        var block = state.block;
        if (block.getRenderShape() != RenderShape.MODEL) return;
        var pos = state.pos;
        var level = state.level;
        var renderer = this.dispatcher.getModelRenderer();
        var model = this.dispatcher.getBlockModel(block);
        long seed = block.getSeed(pos);
        matrices.pushPose();
        matrices.translate(-0.5, 0.0, -0.5);
        if (state.amplitude != 0) {
            float sin = Mth.sin(state.amplitude);
            matrices.mulPose(new Quaternionf(
                    Mth.cos(state.axis) * sin,
                    0.0F,
                    Mth.sin(state.axis) * sin,
                    Mth.cos(state.amplitude)
            ));
        }
        renderer.tesselateBlock(level, model, block, pos, matrices, buffers.getBuffer(getMovingBlockRenderType(block)), false, state.random, seed, OverlayTexture.NO_OVERLAY);
        if (state.age >= EGG_CRACK_THRESHOLD) {
            renderer.tesselateBlock(level, model, block, pos, matrices, new SheetedDecalTextureGenerator(buffers.getBuffer(
                    ModelBakery.DESTROY_TYPES.get(Math.min((state.age - EGG_CRACK_THRESHOLD) * 90 / MIN_HATCHING_TIME, 9))
            ), matrices.last(), 1.0F), false, state.random, seed, OverlayTexture.NO_OVERLAY);
        }
        super.render(state, matrices, buffers, light);
        matrices.popPose();
    }

    @Override
    public DragonEggRenderState createRenderState() {
        return new DragonEggRenderState();
    }
}
