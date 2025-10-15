package net.dragonmounts.neo.common.client.renderer.dragon;

import net.dragonmounts.neo.common.client.model.dragon.LegPart;
import net.dragonmounts.neo.common.client.renderer.block.DragonHeadRenderState;
import net.dragonmounts.neo.common.client.variant.VariantAppearance;
import net.dragonmounts.neo.common.util.ArrayUtil;
import net.dragonmounts.neo.common.util.Segment;
import net.dragonmounts.neo.compat.registry.DragonVariant;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Vector3f;

import static net.dragonmounts.neo.common.entity.dragon.DragonModelContracts.*;

public class DragonRenderState extends LivingEntityRenderState implements DragonHeadRenderState {
    public @UnknownNullability DragonVariant variant;
    public ItemStack armor = ItemStack.EMPTY;
    public @Nullable Vec3 crystal;
    public boolean renderCrystalBeams;
    public boolean isSaddled;
    public boolean hasChest;
    public int hurtTime;
    public float pitch;
    public float offsetY;
    public int maxDeathTime;
    //--------head--------
    public @UnknownNullability Segment head;
    public float jawRotX;
    //--------neck--------
    public final Segment[] neckSegments = ArrayUtil.fillArray(new Segment[NECK_SEGMENTS], Segment::new);
    //--------wing--------
    public final Vector3f wingRot = new Vector3f();
    public final Vector3f armRot = new Vector3f();
    public final float[] fingerRotY = new float[WING_FINGERS];
    //--------legs--------
    public final LegPart.Pose leftFrontLeg = new LegPart.Pose();
    public final LegPart.Pose rightFrontLeg = new LegPart.Pose();
    public final LegPart.Pose leftHindLeg = new LegPart.Pose();
    public final LegPart.Pose rightHindLeg = new LegPart.Pose();
    //--------tail--------
    public final Segment.Scalable[] tailSegments = ArrayUtil.fillArray(new Segment.Scalable[TAIL_SEGMENTS], Segment.Scalable::new);

    @Override
    public @Nullable VariantAppearance neodragonmounts$getAppearance() {
        return this.variant.appearance;
    }
}
