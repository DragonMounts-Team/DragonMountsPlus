package net.dragonmounts.neo.common.client;

import net.dragonmounts.neo.common.client.breath.BreathSoundHandler;
import net.dragonmounts.neo.common.client.model.dragon.DragonAnimator;
import net.dragonmounts.neo.common.component.DragonFood;
import net.dragonmounts.neo.common.entity.ai.behavior.RangedAttack;
import net.dragonmounts.neo.common.entity.dragon.BuiltinMouthState;
import net.dragonmounts.neo.common.entity.dragon.DragonLifeStage;
import net.dragonmounts.neo.common.entity.dragon.TameableDragonEntity;
import net.dragonmounts.neo.common.init.DMSounds;
import net.dragonmounts.neo.common.inventory.DragonInventory;
import net.dragonmounts.neo.common.tag.DMItemTags;
import net.dragonmounts.neo.common.util.math.MathUtil;
import net.dragonmounts.neo.compat.registry.DragonType;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
@NotNullByDefault
public class ClientDragonEntity extends TameableDragonEntity {
    public final DragonAnimator animator = new DragonAnimator(this);
    public final BreathSoundHandler sound = new BreathSoundHandler();
    public int controlFlags;
    private float pendingJumpPower;
    private boolean wasOnGround;
    private @Nullable RangedAttack<? super ClientDragonEntity> rangedAttack;

    public ClientDragonEntity(EntityType<? extends TameableDragonEntity> type, Level world) {
        super(type, world, null);
    }

    @Override
    public @Nullable RangedAttack<? super ClientDragonEntity> getRangedAttack() {
        return this.rangedAttack;
    }

    @Override
    public final Vec3 getHeadRelativeOffset(float x, float y, float z) {
        return this.animator.getHeadRelativeOffset(x, y, z);
    }

    /**
     * @see #onFlap()
     */
    @Deprecated
    public void onWingsDown(float speed) {
        // play wing sounds
        this.level().playLocalSound(
                this,
                SoundEvents.ENDER_DRAGON_FLAP,
                this.getSoundSource(),
                0.8f + (this.getAgeScale() - speed),
                1.0F
        );
    }

    @Override
    public void aiStep() {
        this.wasOnGround = this.onGround();
        if (this.isDeadOrDying()) {
            this.nearestCrystal = null;
        } else {
            this.checkCrystals();
        }
        super.aiStep();
        this.animator.tick();
        var attack = this.getRangedAttack();
        if (attack != null) {
            attack.tick(this);
        }
        this.sound.update();
        if (!this.isAgeLocked()) {
            if (this.age < 0) {
                ++this.age;
            } else if (this.age > 0) {
                --this.age;
            }
        }
        var sneeze = this.getVariant().type.sneezeParticle;
        if (sneeze != null && !this.isBaby() && !this.isBreathing() && this.random.nextInt(700) == 0) {
            var level = this.level();
            var pos = this.getHeadRelativeOffset(0.0F, 4.0F, 22.0F);
            double x = pos.x, y = pos.y, z = pos.z;
            for (int i = -1; i < 1; ++i) {
                level.addParticle(sneeze, x, y + 0.5 * i, z, 0, 0.3, 0);
            }
            level.playSound(null, x, y, z, DMSounds.DRAGON_SNEEZE, SoundSource.NEUTRAL, 0.8F, 1);
        }
    }

    @Override
    protected void checkCrystals() {
        if (this.nearestCrystal != null && this.nearestCrystal.isAlive()) {
            if (this.random.nextInt(20) == 0) {
                this.nearestCrystal = this.findCrystal();
            }
        } else {
            this.nearestCrystal = this.random.nextInt(10) == 0 ? this.findCrystal() : null;
        }
    }

    @Override
    protected void applyType(DragonType type) {
        if (this.lastType == type) return;
        if (this.lastType != null) {
            this.getAttributes().removeAttributeModifiers(this.lastType.attributes);
        }
        this.getAttributes().addTransientAttributeModifiers(type.attributes);
        var attack = this.getRangedAttack();
        if (attack != null) {
            attack.onDetached(this);
        }
        this.rangedAttack = type.initRangedAttack(this);
        this.lastType = type;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        boolean notOwner = !this.isOwnedBy(player);
        if (!this.isBreathing()) {
            var food = DragonFood.getInstance(stack);
            if (food != null) {
                return (food.requiresOwner() && notOwner) || this.shouldRefuseFood(food)
                        ? InteractionResult.FAIL
                        : InteractionResult.CONSUME;
            }
        }
        if (notOwner) return InteractionResult.PASS;
        if (DragonInventory.isDragonArmor(stack)
                || DragonInventory.isDragonSaddle(stack)
                || DragonInventory.isChest(stack)
                || stack.is(DMItemTags.BATONS)
        ) return InteractionResult.CONSUME;
        var result = stack.interactLivingEntity(player, this, hand);
        return result.consumesAction() ? result : InteractionResult.CONSUME;
    }

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case ON_ATTACK -> {
                this.playSound(SoundEvents.GENERIC_EAT.value(), 1.0F, 0.7F);
                this.animator.transitMouthState(BuiltinMouthState.ATTACKING, false);
            }
            case ON_ROAR -> {
                SoundEvent sound = this.getVariant().type.getRoarSound(this);
                if (sound == null) break;
                this.playSound(sound, Mth.clamp(this.getAgeScale(), 0.3F, 0.6F), 1.0F);
                this.animator.transitMouthState(BuiltinMouthState.ROARING, false);
            }
            default -> super.handleEntityEvent(id);
        }
    }

    @Override
    public void setLifeStage(DragonLifeStage stage, boolean reset, boolean sync) {
        if (this.stage == stage) return;
        this.stage = stage;
        if (reset) {
            this.refreshAge();
        }
        this.reapplyPosition();
        this.refreshDimensions();
    }

    @Override
    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        int data = packet.getData();
        this.setLifeStage(DragonLifeStage.byId(data & 0b111), false, false);
        this.setAge(data >>> 3);
    }

    public void refreshForcedAgeTimer() {
        if (this.forcedAgeTimer <= 0) {
            this.forcedAgeTimer = 40;
        }
    }

    @Override
    protected void tickDeath() {
        ++this.deathTime;
    }

    @Override
    public void openCustomInventoryScreen(Player player) {}

    public @Nullable Vec3 locateCrystal() {
        return this.nearestCrystal == null ? null : this.nearestCrystal.position();
    }

    @Override
    protected void tickRidden(Player player, Vec3 input) {
        super.tickRidden(player, input);
        if (this.onGround() && this.isControlledByLocalInstance()) {
            // handle jump
            float power = this.pendingJumpPower;
            this.pendingJumpPower = 0.0F;
            if (power > 0.0F) {
                var motion = this.getDeltaMovement();
                this.hasImpulse = true;
                if (input.z > 0.0) {
                    float facing = this.getYRot() * MathUtil.TO_RAD_FACTOR;
                    this.setDeltaMovement(
                            motion.x - 0.4F * Mth.sin(facing) * power,
                            this.getJumpPower(power) * 2.5,
                            motion.z + 0.4F * Mth.cos(facing) * power
                    );
                } else {
                    this.setDeltaMovement(motion.x, this.getJumpPower(power) * 2.5, motion.z);
                }
            }
        }
    }

    @Override
    public boolean canJump() {
        return this.wasOnGround && super.canJump();
    }

    @Override
    public void onPlayerJump(int power) {
        this.pendingJumpPower = power >= 90 ? 1.0F : 0.4F + 0.4F * power / 90.0F;
    }
}
