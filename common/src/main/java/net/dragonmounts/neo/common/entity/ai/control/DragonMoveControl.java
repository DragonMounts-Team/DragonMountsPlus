package net.dragonmounts.neo.common.entity.ai.control;

import net.dragonmounts.neo.common.entity.dragon.TameableDragonEntity;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;

public class DragonMoveControl extends MoveControl {
    public final TameableDragonEntity dragon;

    public DragonMoveControl(TameableDragonEntity dragon) {
        super(dragon);
        this.dragon = dragon;
    }

    /// @see FlyingMoveControl#tick()
    @Override
    public void tick() {
        var dragon = this.dragon;
        if (this.operation == MoveControl.Operation.MOVE_TO) {
            this.operation = MoveControl.Operation.WAIT;
            var pos = dragon.position();
            double distX = this.wantedX - pos.x, distY = this.wantedY - pos.y, distZ = this.wantedZ - pos.z;
            double squared = distX * distX + distZ * distZ;
            if (squared + distY * distY < 2.5E-7) {
                if (dragon.isFlying()) {
                    dragon.setYya(0.0F);
                }
                dragon.setZza(0.0F);
                return;
            }
            if (dragon.onGround()) {
                dragon.setYRot(this.rotlerp(
                        dragon.getYRot(),
                        (float) (Mth.atan2(distZ, distX) * Mth.RAD_TO_DEG) - 90.0F,
                        90.0F
                ));
                // invoke super.tick()
                dragon.setSpeed((float) (this.speedModifier * dragon.getAttributeValue(Attributes.MOVEMENT_SPEED)));
                var location = dragon.blockPosition();
                var state = dragon.level().getBlockState(location);
                var shape = state.getCollisionShape(dragon.level(), location);
                if (distY > dragon.maxUpStep() && squared < Math.max(1.0F, dragon.getBbWidth())
                        || !shape.isEmpty()
                        && dragon.getY() < shape.max(Direction.Axis.Y) + location.getY()
                        && !state.is(BlockTags.DOORS)
                        && !state.is(BlockTags.FENCES)
                ) {
                    dragon.getJumpControl().jump();
                    this.operation = MoveControl.Operation.JUMPING;
                } else if (distY > 0.5F) {
                    // a small jump
                    dragon.setYya(dragon.yya + 0.5F);
                }
            } else {
                // TODO: see SmoothSwimmingMoveControl
                dragon.setYRot(this.rotlerp(
                        dragon.getYRot(),
                        (float) (Mth.atan2(distZ, distX) * Mth.RAD_TO_DEG) - 90.0F,
                        30.0F
                ));
                double dist = Math.sqrt(squared);
                float speed = (float) (this.speedModifier * dragon.getAttributeValue(Attributes.FLYING_SPEED));
                dragon.setSpeed(speed);
                if (dist > Mth.EPSILON || Math.abs(distY) > Mth.EPSILON) { // adjusted order to simplify population
                    dragon.setXRot(this.rotlerp(
                            dragon.getXRot(),
                            (float) (Mth.atan2(distY, dist) * -Mth.RAD_TO_DEG),
                            85.0F
                    ));
                    dragon.setYya(distY > 0.0 ? speed : -speed);
                }
            }
        } else if (dragon.onGround()) {
            super.tick();
        } else if (this.operation == MoveControl.Operation.JUMPING) {
            this.operation = MoveControl.Operation.WAIT;
            dragon.setSpeed((float) (this.speedModifier * dragon.getAttributeValue(Attributes.FLYING_SPEED)));
        } else {
            dragon.setYya(0.0F);
            dragon.setZza(0.0F);
        }
    }
}
