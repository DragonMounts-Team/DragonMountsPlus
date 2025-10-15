package net.dragonmounts.neo.common.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.dragonmounts.neo.common.block.entity.DragonCoreBlockEntity;
import net.dragonmounts.neo.common.client.model.DragonCoreModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.NotNullByDefault;

import static net.dragonmounts.neo.common.DragonMountsShared.makeId;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

/// @see net.minecraft.client.renderer.blockentity.ShulkerBoxRenderer
@SuppressWarnings("UnstableApiUsage")
@NotNullByDefault
public class DragonCoreRenderer implements BlockEntityRenderer<DragonCoreBlockEntity> {
    private static final ResourceLocation TEXTURE_LOCATION = makeId("textures/block/dragon_core.png");
    private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(TEXTURE_LOCATION);
    private final DragonCoreModel model;

    public DragonCoreRenderer(BlockEntityRendererProvider.Context context) {
        this(context.getModelSet());
    }

    public DragonCoreRenderer(EntityModelSet models) {
        this.model = new DragonCoreModel(models.bakeLayer(ModelLayers.SHULKER_BOX));
    }

    @Override
    public void render(DragonCoreBlockEntity core, float ticks, PoseStack matrices, MultiBufferSource buffers, int light, int overlay) {
        this.render(matrices, buffers, light, overlay, core.getBlockState().getValueOrElse(HORIZONTAL_FACING, Direction.SOUTH), core.getProgress(ticks));
    }

    public void render(PoseStack matrices, MultiBufferSource buffers, int light, int overlay, Direction facing, float progress) {
        matrices.pushPose();
        matrices.translate(0.5F, 0.5F, 0.5F);
        matrices.scale(0.9995F, 0.9995F, 0.9995F);
        matrices.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));
        matrices.scale(1.0F, -1.0F, -1.0F);
        matrices.translate(0.0F, -1.0F, 0.0F);
        this.model.animate(progress);
        this.model.renderToBuffer(matrices, buffers.getBuffer(RENDER_TYPE), light, overlay, -1);
        matrices.popPose();
    }

    public record Special(
            DragonCoreRenderer renderer,
            float openness,
            Direction facing
    ) implements NoDataSpecialModelRenderer {
        @Override
        public void render(ItemDisplayContext context, PoseStack matrices, MultiBufferSource buffers, int light, int overlay, boolean foil) {
            this.renderer.render(matrices, buffers, light, overlay, this.facing, this.openness);
        }
    }

    public record Unbaked(float openness, Direction facing) implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.FLOAT.optionalFieldOf("openness", 0.0F).forGetter(Unbaked::openness),
                Direction.CODEC.optionalFieldOf("facing", Direction.UP).forGetter(Unbaked::facing)
        ).apply(instance, Unbaked::new));

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(EntityModelSet models) {
            return new Special(new DragonCoreRenderer(models), this.openness, this.facing);
        }
    }

}
