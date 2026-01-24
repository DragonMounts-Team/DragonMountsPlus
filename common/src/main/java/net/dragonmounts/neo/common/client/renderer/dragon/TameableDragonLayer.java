package net.dragonmounts.neo.common.client.renderer.dragon;

import com.mojang.blaze3d.vertex.PoseStack;
import net.dragonmounts.neo.common.client.model.dragon.DragonModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

import static net.minecraft.client.renderer.LightTexture.FULL_BRIGHT;
import static net.minecraft.client.renderer.RenderType.armorCutoutNoCull;
import static net.minecraft.client.renderer.entity.ItemRenderer.getArmorFoilBuffer;

public class TameableDragonLayer extends RenderLayer<DragonRenderState, DragonModel> {
    public TameableDragonLayer(RenderLayerParent<DragonRenderState, DragonModel> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack matrices, MultiBufferSource buffers, int light, DragonRenderState state, float yRot, float xRot) {
        var appearance = state.variant.appearance;
        var model = appearance.getModel();
        if (!state.isInvisible) {
            if (state.deathTime > 0) {
                int color = ARGB.color(Mth.floor(state.deathTime * 255.0F / state.maxDeathTime), -1);
                model.renderToBuffer(matrices, buffers.getBuffer(appearance.getDecal(state)), light, OverlayTexture.pack(0.0F, state.hurtTime > 0), color);
                model.renderToBuffer(matrices, buffers.getBuffer(appearance.getGlowDecal(state)), FULL_BRIGHT, OverlayTexture.NO_OVERLAY, color);
                return;
            }
            //glow
            model.renderToBuffer(matrices, buffers.getBuffer(appearance.getGlow(state)), FULL_BRIGHT, OverlayTexture.NO_OVERLAY, -1);
        }
        //saddle
        if (state.isSaddled) {
            var saddle = model.saddle;
            saddle.visible = true;
            model.renderToBuffer(matrices, buffers.getBuffer(appearance.getSaddle(state)), light, OverlayTexture.NO_OVERLAY, -1);
            saddle.visible = false;
        }
        //chest
        if (state.hasChest) {
            var chest = model.chest;
            chest.visible = true;
            matrices.pushPose();
            model.root().translateAndRotate(matrices);
            model.body.translateAndRotate(matrices);
            chest.render(matrices, buffers.getBuffer(appearance.getChest(state)), light, OverlayTexture.NO_OVERLAY, -1);
            matrices.popPose();
            chest.visible = false;
        }
        //armor
        var equippable = state.armor.get(DataComponents.EQUIPPABLE);
        if (equippable == null) return;
        var material = equippable.assetId();
        if (material.isEmpty()) return;
        var texture = appearance.getArmorTexture(material.get());
        if (texture == null) return;
        model.renderToBuffer(matrices, getArmorFoilBuffer(buffers, armorCutoutNoCull(texture), state.armor.hasFoil()), light, OverlayTexture.NO_OVERLAY, -1);
    }
}
