package net.dragonmounts.neo.common.client.breath.impl;

import net.dragonmounts.neo.common.client.breath.BreathParticle;
import net.dragonmounts.neo.common.client.breath.BreathParticleFactory;
import net.dragonmounts.neo.common.entity.breath.BreathParticleOption;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;

public class AirflowBreathParticle extends BreathParticle {
    public static final BreathParticleFactory FACTORY = AirflowBreathParticle::new;
    private static final float ROLL_SPEED = Mth.PI / 3;
    protected final float rollSpeed;

    public AirflowBreathParticle(BreathParticleOption option, TextureAtlasSprite sprite, ClientLevel level, double x, double y, double z, double motionX, double motionY, double motionZ) {
        super(option, sprite, level, x, y, z, motionX, motionY, motionZ);
        this.rollSpeed = this.random.nextBoolean() ? ROLL_SPEED : -ROLL_SPEED;
    }

    @Override
    protected void tickIfAlive() {
        this.oRoll = this.roll;
        this.roll += this.rollSpeed;
    }

    @Override
    protected float getRenderSize() {
        return super.getRenderSize() * 0.625F;
    }
}
