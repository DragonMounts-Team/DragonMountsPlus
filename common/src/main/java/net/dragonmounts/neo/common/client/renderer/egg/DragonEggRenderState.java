package net.dragonmounts.neo.common.client.renderer.egg;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.UnknownNullability;

public class DragonEggRenderState extends EntityRenderState {
    public float amplitude;
    public float axis;
    public float progress;
    public @UnknownNullability BlockState block;
}