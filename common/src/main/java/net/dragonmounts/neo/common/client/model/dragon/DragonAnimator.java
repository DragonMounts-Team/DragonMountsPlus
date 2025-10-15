package net.dragonmounts.neo.common.client.model.dragon;

import net.dragonmounts.neo.common.client.ClientDragonEntity;
import net.dragonmounts.neo.common.client.renderer.dragon.DragonRenderState;
import net.dragonmounts.neo.common.entity.ai.control.DragonHeadLocator;
import net.dragonmounts.neo.common.init.DragonVariants;
import net.dragonmounts.neo.common.util.CircularBuffer;
import net.dragonmounts.neo.common.util.math.Interpolation;
import net.dragonmounts.neo.common.util.math.LinearInterpolation;
import net.dragonmounts.neo.common.util.math.MathUtil;
import net.dragonmounts.neo.compat.registry.DragonVariant;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import static net.dragonmounts.neo.common.entity.dragon.DragonModelContracts.*;
import static net.dragonmounts.neo.common.util.math.Interpolation.clampedSmoothLinear;

/**
 * Animation control class to put useless reptiles in motion.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonAnimator extends DragonHeadLocator<ClientDragonEntity> {
    // constants
    public static final int JAW_SPEED = 20;
    // interpolate between folded and unfolded wing angles
    private static final float[] FOLDED_FINGER_ROT = {2.7F, 2.8F, 2.9F, 3.0F};
    private static final float[] UNFOLDED_FINGER_ROT = {0.1F, 0.9F, 1.7F, 2.5F};
    // thigh, crus, foot, toe
    private static final float[] FRONT_LEG_STAND_ROT_X = {0.8F, -1.5F, 1.3F, 0.0F};
    private static final float[] FRONT_LEG_SIT_ROT_X = {0.3F, -1.8F, 1.8F, 0.0F};
    private static final float[] HIND_LEG_STAND_ROT_X = {-0.3F, 1.5F, -0.2F, 0.0F};
    private static final float[] HIND_LEG_SIT_ROT_X = {-0.8F, 1.8F, -0.9F, 0.0F};
    // X rotation angles for walking
    // 1st dim - animation keyframe
    // 2nd dim - thigh, crus, foot, toe
    private static final float[][] FRONT_LEG_WALKING_ROT_X = {
            {0.4f, -1.4f, 1.3f, 0}, // move down and forward
            {1.2f, -1.6f, 1.3f, 0}, // move back
            {0.9f, -2.1f, 1.8f, 0.6f} // move up and forward
    };
    private static final float[][] HIND_LEG_WALKING_ROT_X = {
            {0.1f, 1.2f, -0.5f, 0}, // move back
            {-0.3f, 2.1f, -0.9f, 0.6f}, // move up and forward
            {-0.7f, 1.4f, -0.2f, 0} // move down and forward
    };

    private DragonVariant variant;
    // entity parameters
    private double prevRenderYawOffset;
    private double yawAbs;

    // timing vars
    private float cycleOfs;
    private boolean wingsDown;

    // timing interp vars
    private final LinearInterpolation animTimer = new LinearInterpolation(0.0F);
    private final LinearInterpolation groundTimer = new LinearInterpolation.Clamped(1.0F);
    private final LinearInterpolation flutterTimer = new LinearInterpolation.Clamped(0.0F);
    private final LinearInterpolation walkTimer = new LinearInterpolation.Clamped(0.0F);
    private final LinearInterpolation sitTimer = new LinearInterpolation.Clamped(0.0F);
    private final LinearInterpolation speedTimer = new LinearInterpolation.Clamped(1.0F);

    // trails
    private final CircularBuffer yTrail = new CircularBuffer(8);
    private final CircularBuffer yawTrail = new CircularBuffer(16);
    private final CircularBuffer pitchTrail = new CircularBuffer(16);

    // animation parameters
    private final Vector3f armFlutterRot = new Vector3f();
    private final Vector3f wingFlutterRot = new Vector3f();
    private final Vector3f armGlideRot = new Vector3f();
    private final Vector3f wingGlideRot = new Vector3f();
    private final Vector3f armGroundRot = new Vector3f();
    private final Vector3f wingGroundRot = new Vector3f();

    private final float[] legRotCache = new float[4];

    // state cache
    private ItemStack armor = ItemStack.EMPTY;
    private boolean saddled;
    private boolean chested;
    private @Nullable Vec3 crystal;
    private int hurtTime;
    private int maxDeathTime;
    public boolean renderCrystalBeams = true;
    private float jawRotX;
    private float lastJawRotX;
    private MouthState mouth = MouthState.IDLE;
    private int duration;
    public int remainingEating;

    public DragonAnimator(ClientDragonEntity dragon) {
        super(dragon);
        this.yawTrail.fill(0.0F);
        this.pitchTrail.fill(0.0F);
        // just to avoid nullptr :
        this.variant = DragonVariants.ENDER_FEMALE;
    }

    public void extractRenderState(DragonRenderState state, float partialTicks) {
        anim = animTimer.get(partialTicks);
        ground = groundTimer.get(partialTicks);
        flutter = flutterTimer.get(partialTicks);
        walk = walkTimer.get(partialTicks);
        sit = sitTimer.get(partialTicks);
        speed = speedTimer.get(partialTicks);

        animBase = anim * MathUtil.PI * 2;
        float baseOffset = Mth.sin(animBase - 1) + 1;
        cycleOfs = (baseOffset + 2) * baseOffset * 0.05F
                // reduce up/down amplitude
                * Mth.lerp(flutter, 0.5F, 1.0F) * Mth.lerp(ground, 1.0F, 0.5F);

        // animate head and neck
        this.calculateHeadAndNeck(state.neckSegments, state.xRot, state.yRot);
        this.animateTail(state, partialTicks);
        this.animateWing(state);
        this.animateLegs(state);

        // update state
        state.maxDeathTime = this.maxDeathTime;
        state.hurtTime = this.hurtTime;
        state.variant = this.variant;
        state.armor = this.armor;
        state.hasChest = this.chested;
        state.isSaddled = this.saddled;
        state.crystal = this.crystal;
        state.renderCrystalBeams = this.renderCrystalBeams;

        // update offset
        state.offsetY = -this.getModelOffsetY(); // flip y when rendering

        // update pitch
        state.pitch = this.getPitch() * MathUtil.TO_RAD_FACTOR;
        state.head = this.head;
        state.jawRotX = (1.0F - Mth.sin(animBase)) * 0.1F * flutter + Mth.lerp(partialTicks, this.lastJawRotX, jawRotX);
        if (state.pose == Pose.SLEEPING) {
            state.pose = Pose.SITTING;
        }
    }

    public void transitMouthState(MouthState mouth, boolean keep) {
        if (keep && mouth == this.mouth) return;
        this.mouth = mouth;
        this.duration = 0;
    }

    protected boolean updateBasicMouthRotX() {
        var mouth = this.mouth;
        if (++this.duration < mouth.turning) {
            this.jawRotX = MathUtil.clamp(this.duration * JAW_SPEED) * mouth.amplitude;
        } else if (this.duration < mouth.duration) {
            this.jawRotX = MathUtil.clamp((mouth.duration - this.duration) * JAW_SPEED) * mouth.amplitude;
        } else return false;
        return true;
    }

    protected void updateMouthRotX() {
        switch (this.mouth) {
            case IDLE -> this.jawRotX = 0.0F;
            case EATING -> {
                if (this.updateBasicMouthRotX()) break;
                if (this.remainingEating < 0) {
                    this.transitMouthState(MouthState.IDLE, false);
                } else {
                    this.duration = 0;
                }
                this.jawRotX = 0.0F;
            }
            default -> {
                if (this.updateBasicMouthRotX()) break;
                this.transitMouthState(MouthState.IDLE, false);
                this.jawRotX = 0.0F;
            }
        }
    }

    @Override
    public void tick() {
        var dragon = this.dragon;
        this.variant = dragon.getVariant();
        float health = dragon.getHealth();
        // don't move anything during death sequence
        if (dragon.getHealth() <= 0) {
            this.animTimer.sync();
            this.groundTimer.sync();
            this.flutterTimer.sync();
            this.walkTimer.sync();
            this.sitTimer.sync();
            this.relativeHealth = 0.0F;
            return;
        }
        this.relativeHealth = health / dragon.getMaxHealth();
        this.maxDeathTime = dragon.getMaxDeathTime();
        this.hurtTime = dragon.hurtTime;
        this.armor = dragon.getBodyArmorItem();
        this.chested = dragon.hasChest();
        this.saddled = dragon.isSaddled();
        this.crystal = dragon.locateCrystal();
        boolean flying = dragon.isFlying();

        float speedMax = 0.05F;
        var motion = this.dragon.getDeltaMovement();
        float speedEnt = (float) (motion.x * motion.x + motion.z * motion.z);

        // update main animation timer and depend timing speed on movement
        animTimer.add(flying
                ? 0.070F - MathUtil.clamp(speedEnt / speedMax) * 0.035F // (2 - speedMulti) * 0.035F
                : 0.035F
        );

        // update ground transition
        float ground = groundTimer.get();
        groundTimer.set(flying
                ? ground - 0.1F
                : ground * 0.95F + 0.08F
        );
        // update flutter transition
        flutterTimer.add(flying && (speedEnt < speedMax || motion.y > -0.1) ? 0.1f : -0.1f);
        // update walking and sitting transition
        if (dragon.isInSittingPose()) {
            this.walkTimer.add(-0.1F);
            this.sitTimer.set(this.sitTimer.get() * 0.95F + 0.095F);
        } else {
            this.walkTimer.add(speedEnt > 0.00005F ? 0.1F : -0.1F);
            this.sitTimer.set(this.sitTimer.get() * 0.95F - 0.095F);
        }

        // update bite opening transition and breath transitions
        this.lastJawRotX = this.jawRotX;
        switch (dragon.breathHelper.getCurrentBreathState()) {
            case IDLE -> {
                if (this.mouth == MouthState.BREATHING) {
                    this.transitMouthState(MouthState.IDLE, false);
                    this.jawRotX = 0.0F;
                    break;
                }
                this.updateMouthRotX();
            }
            case STARTING -> {
                this.transitMouthState(MouthState.BREATHING, true);
                this.updateMouthRotX();
            }
            case SUSTAIN -> {
                var mouth = this.mouth = MouthState.BREATHING;
                this.duration = mouth.turning + 2;
                this.jawRotX = mouth.amplitude;
            }
            case STOPPING -> this.updateMouthRotX();
        }
        // update speed transition
        speedTimer.add(!flying ||
                speedEnt > speedMax ||
                dragon.isHoverDisabled() ||
                dragon.getPassengers().size() > 1 ||
                dragon.isNearGround(2.0)
                ? 0.05F
                : -0.05F
        );

        // update trailers
        double yawDiff = dragon.yBodyRot - prevRenderYawOffset;
        prevRenderYawOffset = dragon.yBodyRot;

        // filter out 360 degrees wrapping
        if (yawDiff < 180 && yawDiff > -180) {
            yawAbs += yawDiff;
        }

        //yTrail.update(entity.posY - entity.getYOffset());
        yawTrail.update((float) yawAbs);
        pitchTrail.update(this.getPitch());
        --this.remainingEating;
    }

    private void animateWing(DragonRenderState state) {
        // move wings slower while sitting
        float aSpeed = this.sit > 0.0F ? 0.6F : 1.0F;
        float base = this.animBase;
        // animation speeds
        float a2 = Mth.sin(base * aSpeed * 0.5F);
        float a1 = Mth.sin(base * aSpeed * 0.35F) * a2;

        float flutter = this.flutter;
        float ground = this.ground;
        if (ground > 0.0F) {
            float swing = state.walkAnimationPos * 0.5F;
            var wing = this.wingGroundRot.set(
                    0.0F,
                    1.4f - a1 * 0.02F + Mth.sin(swing) * 0.02F * walk,
                    0.8F + a2 * Mth.sin(base * aSpeed * 0.75F) * 0.05F + Mth.cos(swing) * 0.05f * walk
            );
            this.armGroundRot.y = wing.y * -2.0F;
        }

        if (ground < 1.0F) {
            float sin = Mth.sin(base);
            float baseArmRotZ = Mth.sin(base + 2.0F) + 0.5F;
            // Hovering
            var flutterWing = this.wingFlutterRot.set(
                    0.125F - Mth.cos(base) * 0.2F,
                    0.25F,
                    sin * 0.8F + 0.1F // (sin + 0.125f) * 0.8f
            );
            this.armFlutterRot.set(
                    0.0F,
                    flutterWing.y * -2.0F,
                    baseArmRotZ * -0.75F
            );
            // Gliding
            var glideWing = this.wingGlideRot.set(
                    -0.25f - Mth.cos(base * 2.0F) * Mth.cos(base * 1.5F) * 0.04F,
                    0.25F,
                    0.35F + sin * 0.05F
            );
            this.armGlideRot.set(
                    0.0F,
                    glideWing.y * -2.0F,
                    -0.25f + baseArmRotZ * 0.05F
            );
        }
        // interpolate between fluttering and gliding, then flying and grounded
        this.wingGlideRot.lerp(this.wingFlutterRot, flutter, state.wingRot)
                .lerp(this.wingGroundRot, ground);
        this.armGlideRot.lerp(this.armFlutterRot, flutter, state.armRot)
                .lerp(this.armGroundRot, ground);

        // set wing finger angles
        float rotYOfs = a1 * 0.03f;
        float rotYMulti = 1;

        for (int i = 0; i < WING_FINGERS; ++i) {
            state.fingerRotY[i] = clampedSmoothLinear(
                    UNFOLDED_FINGER_ROT[i],
                    FOLDED_FINGER_ROT[i] + rotYOfs * rotYMulti,
                    ground
            );
            rotYMulti -= 0.2f;
        }

        // check if the wings are moving down and trigger the event
        boolean wingsDown = Mth.sin(base - 1.0F) > 0.0F;
        if (wingsDown && !this.wingsDown && flutter != 0 && !this.dragon.isInWater() && this.dragon.isFlying()) {
            this.dragon.onWingsDown(this.speed);
        }
        this.wingsDown = wingsDown;
    }

    private void animateTail(DragonRenderState state, float partialTicks) {
        var tailSegments = state.tailSegments;
        var segment = tailSegments[0];
        segment.posX = segment.posY = segment.posZ = segment.rotX = segment.rotY = segment.rotZ = 0.0F;
        float sit = this.sit;
        float base = this.animBase;
        float flutterFactor = 0.04F * Mth.lerp(this.flutter, 0.3F, 1.0F);
        float ground = this.ground;
        float speedFactor = 2.0F - 2.0F * this.speed;
        float rotXFactor = 1.0F - 0.2F * sit;
        float magicSinFactor = Mth.sin(base * 0.2F) * Mth.sin(base * 0.37F) * 0.4F;// sit = 0.8 * stand
        float sitFactor = sit - 1.0F;
        float rotYStand = 0;
        float rotXAir = 0;
        for (int i = 0; i < TAIL_SEGMENTS; ) {
            float vertMulti = (i + 1) / (float) TAIL_SEGMENTS;

            // idle
            float amp = 0.1F + i * 0.5F / TAIL_SEGMENTS;

            rotYStand = (rotYStand + Mth.sin(i * 0.45F + base * 0.5F)) * amp * 0.4F;
            rotXAir += Mth.sin(i * 0.45F + base) * flutterFactor;

            // body movement
            float limit = 80 * vertMulti;
            float yawOfs = yawTrail.getClamped(partialTicks, 0, i + 1, limit) * 2;
            float pitchOfs = pitchTrail.getClamped(partialTicks, 0, i + 1, limit) * 2;

            // interpolate between flying and grounded
            float rotX = segment.rotX = Mth.lerp(ground, rotXAir, (
                    (i - TAIL_SEGMENTS * 0.6F) * amp * 0.4F + (magicSinFactor * amp - 0.1F) * sitFactor
            ) * rotXFactor) - MathUtil.TO_RAD_FACTOR * pitchOfs + speedFactor * vertMulti;
            float rotY = segment.rotY = Mth.lerp(ground, 0.0F, Mth.lerp(// interpolate between sitting and standing
                    sit,
                    rotYStand,
                    Mth.sin(vertMulti * MathUtil.PI) * MathUtil.PI * 1.2F - 0.5F // curl to the left
            )) + yawOfs * MathUtil.TO_RAD_FACTOR;

            // update scale
            float scale = segment.scaleX = segment.scaleY = segment.scaleZ = Mth.lerp(vertMulti, 1.5F, 0.3F);
            // move next segment behind the current one
            if (++i == TAIL_SEGMENTS) return;
            float posX = segment.posX, posY = segment.posY, posZ = segment.posZ;
            float tailSize = TAIL_SIZE * scale - 0.7F;
            float cosFactor = Mth.cos(rotX) * tailSize;
            segment = tailSegments[i];
            segment.posX = posX + Mth.sin(rotY) * cosFactor;
            segment.posY = posY - Mth.sin(rotX) * tailSize;
            segment.posZ = posZ + Mth.cos(rotY) * cosFactor;
        }
    }

    private void animateLegs(DragonRenderState state) {
        float swing = state.walkAnimationPos * 0.25F / state.scale / state.ageScale;
        float sit = this.sit, walk = this.walk, ground = this.ground;
        LegPart.Pose leftFrontLeg = state.leftFrontLeg,
                rightFrontLeg = state.rightFrontLeg,
                leftHindLeg = state.leftHindLeg,
                rightHindLeg = state.rightHindLeg;
        preInterplateLegs(FRONT_LEG_STAND_ROT_X, FRONT_LEG_SIT_ROT_X, leftFrontLeg, rightFrontLeg, sit);
        preInterplateLegs(HIND_LEG_STAND_ROT_X, HIND_LEG_SIT_ROT_X, leftHindLeg, rightHindLeg, sit);
        if (walk > 0) {
            var cache = this.legRotCache;
            interplateLegWalkingRotX(leftFrontLeg, cache, FRONT_LEG_WALKING_ROT_X, swing, walk, true);
            interplateLegWalkingRotX(rightFrontLeg, cache, FRONT_LEG_WALKING_ROT_X, swing, walk, false);
            interplateLegWalkingRotX(leftHindLeg, cache, HIND_LEG_WALKING_ROT_X, swing, walk, true);
            interplateLegWalkingRotX(rightHindLeg, cache, HIND_LEG_WALKING_ROT_X, swing, walk, false);
        }
        leftFrontLeg.rotY = -(rightFrontLeg.rotY =
                interplateLegRotY(-0.25F, 0.10F, -0.1F, -0.1F, sit, walk, ground));
        leftHindLeg.rotY = -(rightHindLeg.rotY =
                interplateLegRotY(0.25F, 0.35F, 0.1F, 0.1F, sit, walk, ground));
        if (ground < 1) {
            float footOffset = cycleOfs * 0.1F;
            float frontLegRotX = 1.3F + footOffset, frontShankRotX = -(0.7F * this.speed + 0.1F + footOffset);
            float hindLegRotX = footOffset + 0.6F, hindShankRotX = footOffset + 0.8F;
            float footRotX = 0.75f + footOffset, toeRotX = footRotX * 0.5F;
            interplateFlyingLegRotX(leftFrontLeg.rotX, frontLegRotX, frontShankRotX, footRotX, toeRotX, ground);
            interplateFlyingLegRotX(rightFrontLeg.rotX, frontLegRotX, frontShankRotX, footRotX, toeRotX, ground);
            interplateFlyingLegRotX(leftHindLeg.rotX, hindLegRotX, hindShankRotX, footRotX, toeRotX, ground);
            interplateFlyingLegRotX(rightHindLeg.rotX, hindLegRotX, hindShankRotX, footRotX, toeRotX, ground);
        }
    }

    private static void preInterplateLegs(
            float[] standRotX,
            float[] sitRotX,
            LegPart.Pose left,
            LegPart.Pose right,
            float delta
    ) {
        var rotX = left.rotX;
        Interpolation.smoothLinear(standRotX, sitRotX, rotX, delta);
        // align the toes so they're always horizontal on the ground
        rotX[3] = -(rotX[0] + rotX[1] + rotX[2]);
        System.arraycopy(rotX, 0, right.rotX, 0, 4);
    }

    private static void interplateLegWalkingRotX(
            LegPart.Pose pose,
            float[] cache,
            float[][] walkingRot,
            float swing,
            float walk,
            boolean isLeft
    ) {
        // interpolate between the keyframes, based on the cycle
        Interpolation.splineArrays(swing, isLeft, cache, walkingRot);
        // align the toes so they're always horizontal on the ground
        cache[3] -= cache[0] + cache[1] + cache[2];
        if (walk >= 1.0F) {
            System.arraycopy(cache, 0, pose.rotX, 0, cache.length);
        } else {
            var rotX = pose.rotX;
            for (int i = 0; i < cache.length; ++i) {
                rotX[i] = clampedSmoothLinear(rotX[i], cache[i], walk);
            }
        }
    }

    private static float interplateLegRotY(
            float rotStand,
            float rotSit,
            float rotWalk,
            float rotFly,
            float sit,
            float walk,
            float ground
    ) {
        return clampedSmoothLinear(rotFly, clampedSmoothLinear(
                clampedSmoothLinear(rotStand, rotSit, sit), rotWalk, walk
        ), ground);
    }

    private static void interplateFlyingLegRotX(
            float[] rot,
            float flyingLeg,
            float flyingShank,
            float flyingFoot,
            float flyingToe,
            float ground
    ) {
        rot[0] = clampedSmoothLinear(flyingLeg, rot[0], ground);
        rot[1] = clampedSmoothLinear(flyingShank, rot[1], ground);
        rot[2] = clampedSmoothLinear(flyingFoot, rot[2], ground);
        rot[3] = clampedSmoothLinear(flyingToe, rot[3], ground);
    }
}
