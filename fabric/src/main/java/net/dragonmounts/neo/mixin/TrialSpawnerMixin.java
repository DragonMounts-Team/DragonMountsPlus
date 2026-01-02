package net.dragonmounts.neo.mixin;

import net.dragonmounts.neo.common.api.TrialSpawnerExtension;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerConfig;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerData;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import static net.dragonmounts.neo.common.util.BlockUtil.overrideEntityToSpawn;

@Mixin(TrialSpawner.class)
public abstract class TrialSpawnerMixin implements TrialSpawnerExtension {
    @Shadow
    @Final
    private TrialSpawnerData data;

    @Shadow
    private Holder<TrialSpawnerConfig> normalConfig;

    @Shadow
    private Holder<TrialSpawnerConfig> ominousConfig;

    @Shadow
    public abstract void setState(Level level, TrialSpawnerState state);

    @Override
    public void neodragonmounts$overrideEntityToSpawn(Level level, CompoundTag entity) {
        this.data.reset();
        this.normalConfig = Holder.direct(overrideEntityToSpawn(this.normalConfig.value(), entity));
        this.ominousConfig = Holder.direct(overrideEntityToSpawn(this.ominousConfig.value(), entity));
        this.setState(level, TrialSpawnerState.INACTIVE);
    }

    private TrialSpawnerMixin() {}
}
