package net.dragonmounts.neo.common.entity.ai.control;

import net.dragonmounts.neo.common.entity.dragon.DragonModelContracts;
import net.dragonmounts.neo.common.entity.dragon.TameableDragonEntity;
import net.dragonmounts.neo.common.util.Segment;
import net.dragonmounts.neo.common.util.math.MathUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import static net.dragonmounts.neo.common.entity.dragon.DragonModelContracts.NECK_SEGMENTS;
import static net.dragonmounts.neo.common.util.math.Interpolation.clampedSmoothLinear;
import static net.minecraft.util.Mth.DEG_TO_RAD;

public class DragonHeadLocator<T extends TameableDragonEntity> {
    public static final float ANIMATION_NECK_SIZE = 0.6F * DragonModelContracts.NECK_SIZE - 1.4F;
    public final T dragon;
    protected final Segment head = new Segment();
    // entity parameters
    protected float relativeHealth;
    protected float speed = 1.0F;
    // timing vars
    protected float anim;
    protected float animBase;
    protected float ground = 1.0F;
    protected float flutter;
    protected float walk;
    protected float sit;

    public DragonHeadLocator(T dragon) {
        this.dragon = dragon;
    }

    public void tick() {
        // server side impl:
        var dragon = this.dragon;
        // don't move anything during death sequence
        if (dragon.deathTime > 0) return;
        this.relativeHealth = dragon.getHealth() / dragon.getMaxHealth();
        float speedMax = 0.05f;
        var motion = dragon.getDeltaMovement();
        float speedEnt = (float) (motion.x * motion.x + motion.z * motion.z);
        float speedMulti = MathUtil.clamp(speedEnt / speedMax);
        // update main animation timer, depend timing speed on movement
        boolean flying = dragon.isFlying();
        this.anim += flying ? 0.070F - speedMulti * 0.035F : 0.035F;
        this.animBase = this.anim * Mth.TWO_PI;
        // update ground transition
        this.ground = MathUtil.clamp(flying ? this.ground - 0.1F : this.ground * 0.95F + 0.08F);
        // update flutter transition
        this.flutter = MathUtil.clamp(flying && (speedEnt < speedMax || motion.y > -0.1)
                ? this.flutter + 0.1F
                : this.flutter - 0.1F
        );
        // update walking and sitting transition
        if (dragon.isInSittingPose()) {
            this.walk = MathUtil.clamp(this.walk - 0.1F);
            this.sit = MathUtil.clamp((this.sit + 0.1F) * 0.95F);
        } else {
            this.walk = MathUtil.clamp(this.walk + 0.1F);
            this.sit = MathUtil.clamp((this.sit - 0.1F) * 0.95F);
        }
        // update speed transition
        this.speed = MathUtil.clamp(!flying ||
                speedEnt > speedMax ||
                dragon.isHoverDisabled() ||
                dragon.getPassengers().size() > 1 ||
                dragon.isNearGround(2.0)
                ? this.speed + 0.05F
                : this.speed - 0.05F
        );
    }

    public void calculateHeadAndNeck(Segment[] necks, float lookRotX, float lookRotY) {
        var dragon = this.dragon;
        var head = this.head;
        var segment = necks[0];
        segment.posX = 0.0F;
        segment.posY = 14.0F;
        segment.posZ = -8.0F;
        float lastRotY = segment.rotX = segment.rotY = segment.rotZ = 0.0F;
        float rotXFactor = 0.15F
                // basic up/down movement
                * Mth.lerp(this.sit, 1.0F, 0.2F)
                // reduce rotation when on ground
                * clampedSmoothLinear(1.0F, 0.5F, this.walk);
        if (!dragon.isBreathing()) {
            rotXFactor *= Mth.lerp(this.flutter, 0.2F, 1.0F);
        }
        float healthFactor = this.relativeHealth * this.ground;
        float speed = this.speed;
        float rotYFactor = lookRotY * DEG_TO_RAD * speed;
        float speedFactor = 1.0F - speed;
        float base = this.animBase;
        for (int i = 0; i < NECK_SEGMENTS; ) {
            float posX = segment.posX, posY = segment.posY, posZ = segment.posZ;
            float vertMulti = (i + 1) / (float) NECK_SEGMENTS;
            float rotX = segment.rotX = Mth.cos(i * 0.45F + base)
                    * rotXFactor
                    // flex neck down when hovering
                    + speedFactor * vertMulti
                    // lower neck on low health
                    - Mth.lerp(healthFactor, 0.0F, Mth.sin(vertMulti * Mth.PI * 0.9F) * 0.63F);
            // use looking yaw
            lastRotY = segment.rotY = rotYFactor * vertMulti;
            segment = (++i < NECK_SEGMENTS) ? necks[i] : head;
            // move next segment behind the current one
            float factor = Mth.cos(rotX) * ANIMATION_NECK_SIZE;
            segment.posX = posX - Mth.sin(lastRotY) * factor;
            segment.posY = posY + Mth.sin(rotX) * ANIMATION_NECK_SIZE;
            segment.posZ = posZ - Mth.cos(lastRotY) * factor;
        }
        //final float HEAD_TILT_DURING_BREATH = -0.1F;
        head.rotX = lookRotX * DEG_TO_RAD + speedFactor; // + breath * HEAD_TILT_DURING_BREATH
        head.rotY = lastRotY;
        head.rotZ = 0.0F;
    }

    public Vec3 getHeadRelativeOffset(float x, float y, float z) {
        final float scale = this.dragon.getAdjustedSize();
        final float modelScale = scale * MathUtil.MOJANG_MODEL_SCALE;
        final float headScale = modelScale * DragonModelContracts.MAGICAL_HEAD_SCALE;
        var head = this.head;
        var pos = this.dragon.position();
        return new Vec3(x * headScale, y * headScale, -z * headScale)
                .xRot(head.rotX)
                .yRot(-head.rotY)
                .add(-head.posX * modelScale, -head.posY * modelScale, head.posZ * modelScale)
                .xRot(-DEG_TO_RAD * this.getPitch())
                .add(0.0, 0.0, -1.5F * scale)
                .yRot(Mth.PI - DEG_TO_RAD * this.dragon.yBodyRot)
                .add(pos.x, pos.y + scale * (this.getModelOffsetY() + MathUtil.MOJANG_MODEL_OFFSET_Y), pos.z);
    }

    public final float getModelOffsetY() {
        return 1.5F - 0.6F * this.sit;
    }

    public float getPitch() {
        return clampedSmoothLinear(60F, 0F, this.speed);
    }
}
