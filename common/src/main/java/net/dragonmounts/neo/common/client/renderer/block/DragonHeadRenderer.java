package net.dragonmounts.neo.common.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.dragonmounts.neo.common.block.DragonHeadBlock;
import net.dragonmounts.neo.common.block.entity.DragonHeadBlockEntity;
import net.dragonmounts.neo.common.client.variant.VariantAppearance;
import net.dragonmounts.neo.common.init.DragonVariants;
import net.dragonmounts.neo.common.item.DragonHeadItem;
import net.dragonmounts.neo.compat.registry.DragonVariant;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

@SuppressWarnings("UnstableApiUsage")
@NotNullByDefault
public enum DragonHeadRenderer implements BlockEntityRenderer<DragonHeadBlockEntity>, BlockEntityRendererProvider<DragonHeadBlockEntity> {
    INSTANCE;

    public static void renderHead(
            ModelPart head,
            VariantAppearance appearance,
            PoseStack matrices,
            MultiBufferSource buffers,
            double offsetX,
            double offsetY,
            double offsetZ,
            int light,
            int overlay
    ) {
        matrices.pushPose();
        matrices.translate(offsetX, offsetY, offsetZ);
        matrices.scale(-1.0F, -1.0F, 1.0F);
        head.render(matrices, buffers.getBuffer(appearance.getBase(null)), light, overlay, -1);
        head.render(matrices, buffers.getBuffer(appearance.getGlow(null)), LightTexture.FULL_BLOCK, OverlayTexture.NO_OVERLAY, -1);
        matrices.popPose();
    }

    @Override
    public void render(DragonHeadBlockEntity entity, float partialTick, PoseStack matrices, MultiBufferSource buffers, int light, int overlay) {
        var state = entity.getBlockState();
        if (state.getBlock() instanceof DragonHeadBlock head) {
            var appearance = head.variant.appearance;
            var model = appearance.getModel();
            if (model == null) return;
            model.setupBlock(entity.getAnimation(partialTick), head.getYRotation(state), 0.75F);
            if (head.isOnWall) {
                var direction = state.getValue(HORIZONTAL_FACING);
                renderHead(
                        model.head,
                        appearance,
                        matrices,
                        buffers,
                        0.5 - direction.getStepX() * 0.25,
                        0.25,
                        0.5 - direction.getStepZ() * 0.25,
                        light,
                        overlay
                );
            } else {
                renderHead(
                        model.head,
                        appearance,
                        matrices,
                        buffers,
                        0.5,
                        0.0,
                        0.5,
                        light,
                        overlay
                );
            }
        }
    }

    @Override
    public BlockEntityRenderer<DragonHeadBlockEntity> create(Context context) {
        return this;
    }

    public record Special(float animation, DragonVariant fallback) implements SpecialModelRenderer<DragonVariant> {
        @Override
        public void render(@Nullable DragonVariant variant, ItemDisplayContext context, PoseStack matrices, MultiBufferSource buffers, int light, int overlay, boolean foil) {
            if (variant == null) {
                variant = this.fallback;
            }
            var appearance = variant.appearance;
            var model = appearance.getModel();
            if (model == null) return;
            model.setupBlock(this.animation, 180.0F, 0.75F);
            renderHead(
                    model.head,
                    appearance,
                    matrices,
                    buffers,
                    0.5,
                    0.0,
                    0.5,
                    light,
                    overlay
            );
        }

        @Override
        public @Nullable DragonVariant extractArgument(ItemStack stack) {
            return stack.getItem() instanceof DragonHeadItem head ? head.variant : null;
        }
    }

    public record Unbaked(DragonVariant variant, float animation) implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                DragonVariant.CODEC.optionalFieldOf("variant", DragonVariants.ENDER_FEMALE).forGetter(Unbaked::variant),
                Codec.FLOAT.optionalFieldOf("animation", 0.0F).forGetter(Unbaked::animation)
        ).apply(instance, Unbaked::new));

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(EntityModelSet models) {
            return new Special(this.animation, this.variant);
        }
    }
}
