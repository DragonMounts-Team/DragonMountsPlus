package net.dragonmounts.neo.common.client.breath.impl;

import net.dragonmounts.neo.common.client.ClientDragonEntity;
import net.dragonmounts.neo.common.client.breath.BreathSound;
import net.dragonmounts.neo.common.entity.ai.behavior.DragonBreath;
import net.dragonmounts.neo.common.entity.breath.BreathParticleOption;
import net.dragonmounts.neo.common.entity.breath.BreathState;
import net.dragonmounts.neo.common.entity.breath.DragonBreathSpec;
import net.dragonmounts.neo.common.entity.dragon.BuiltinMouthState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.util.Mth.lerp;

public class ClientBreathAdapter extends DragonBreath<@NotNull ClientDragonEntity> {
    private int previousTickCount = Integer.MIN_VALUE;
    private double throatX;
    private double throatY;
    private double throatZ;
    private double lookX;
    private double lookY;
    private double lookZ;

    public ClientBreathAdapter(DragonBreathSpec spec) {
        super(spec);
    }

    @Override
    public void tick(ClientDragonEntity entity) {
        ++this.tickCounter;
        var throat = this.spec.getSpawnPosition(entity);
        var stage = entity.getLifeStage();
        this.updateBreathState(entity);
        if (this.currentBreathState == BreathState.SUSTAIN) {
            /*
             * Created by TGG on 21/06/2015.
             * Used to spawn breath particles on the client side (in future: will be different for different breath weapons)
             * Spawn breath particles for this tick.  If the beam endpoints have moved, interpolate between them, unless
             * the beam stopped for a while (tickCount skipped one or more tick)
             */
            var level = entity.level();
            var look = entity.getLookAngle();
            var motion = entity.getDeltaMovement();
            double throatX = throat.x, throatY = throat.y, throatZ = throat.z,
                    lookX = look.x, lookY = look.y, lookZ = look.z,
                    motionX = motion.x, motionY = motion.y, motionZ = motion.z;
            if (this.tickCounter != previousTickCount + 1) {
                this.throatX = throatX;
                this.throatY = throatY;
                this.throatZ = throatZ;
                this.lookX = lookX;
                this.lookY = lookY;
                this.lookZ = lookZ;
            }
            var option = new BreathParticleOption(entity.getVariant(), stage.power);
            final int PARTICLES_PER_TICK = 4;
            for (int i = 0; i < PARTICLES_PER_TICK; ++i) {
                double partialTickHeadStart = i / (double) PARTICLES_PER_TICK;
                level.addParticle(
                        option,
                        lerp(partialTickHeadStart, this.throatX, throatX) + motionX,
                        lerp(partialTickHeadStart, this.throatY, throatY) + motionY,
                        lerp(partialTickHeadStart, this.throatZ, throatZ) + motionZ,
                        lerp(partialTickHeadStart, this.lookX, lookX),
                        lerp(partialTickHeadStart, this.lookY, lookY),
                        lerp(partialTickHeadStart, this.lookZ, lookZ)
                );
            }
            this.throatX = throatX;
            this.throatY = throatY;
            this.throatZ = throatZ;
            this.lookX = lookX;
            this.lookY = lookY;
            this.lookZ = lookZ;
            this.previousTickCount = this.tickCounter;
        }
    }

    @Override
    protected void onBreathStart(ClientDragonEntity dragon) {
        var spec = this.spec;
        var stage = dragon.getLifeStage();
        var start = new BreathSound.Scheduled(dragon, spec.getStartSound(stage), 25);
        start.next = new BreathSound(dragon, spec.getLoopSound(stage), true);
        dragon.sound.play(start);
    }

    @Override
    protected void onBreathStop(ClientDragonEntity dragon) {
        dragon.sound.play(new BreathSound.Scheduled(dragon, this.spec.getStopSound(dragon.getLifeStage()), 60));
    }

    @Override
    public @Nullable BuiltinMouthState getMouthState() {
        return switch (this.currentBreathState) {
            case STARTING, SUSTAIN -> BuiltinMouthState.BREATHING;
            default -> null;
        };
    }
}
