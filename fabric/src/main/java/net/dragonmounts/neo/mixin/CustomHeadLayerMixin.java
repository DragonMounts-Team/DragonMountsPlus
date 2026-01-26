package net.dragonmounts.neo.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.dragonmounts.neo.common.client.renderer.block.DragonHeadRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.dragonmounts.neo.common.client.renderer.block.DragonHeadRenderer.renderHead;

@Mixin(CustomHeadLayer.class)
public abstract class CustomHeadLayerMixin<S extends LivingEntityRenderState, M extends EntityModel<S> & HeadedModel> extends RenderLayer<S, M> {
    @Shadow
    @Final
    private CustomHeadLayer.Transforms transforms;

    @Inject(
            at = @At("HEAD"),
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;FF)V",
            cancellable = true
    )
    public void renderDragonHead(
            PoseStack matrices,
            MultiBufferSource buffers,
            int light,
            LivingEntityRenderState state,
            float f,
            float g,
            CallbackInfo info
    ) {
        var appearance = ((DragonHeadRenderState) state).neodragonmounts$getAppearance();
        if (appearance == null) return;
        var model = appearance.getModel(null);
        if (model == null) return;
        matrices.pushPose();
        matrices.scale(this.transforms.horizontalScale(), 1.0F, this.transforms.horizontalScale());
        var parent = this.getParentModel();
        parent.root().translateAndRotate(matrices);
        parent.getHead().translateAndRotate(matrices);
        matrices.translate(0.0F, this.transforms.skullYOffset(), 0.0F);
        matrices.scale(1.1875F, -1.1875F, -1.1875F);
        matrices.translate(-0.5, 0.0, -0.5);
        model.setupBlock(state.wornHeadAnimationPos, 180.0F, 0.75F);
        renderHead(model.head, appearance, matrices, buffers, 0.5, 0.0, 0.5, light, OverlayTexture.NO_OVERLAY);
        matrices.popPose();
        info.cancel();
    }

    private CustomHeadLayerMixin(RenderLayerParent<S, M> renderer) {
        super(renderer);
    }
}
