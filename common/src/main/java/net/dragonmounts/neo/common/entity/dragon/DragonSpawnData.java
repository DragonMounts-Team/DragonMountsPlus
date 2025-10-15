package net.dragonmounts.neo.common.entity.dragon;

import net.minecraft.world.entity.AgeableMob;

public class DragonSpawnData extends AgeableMob.AgeableMobGroupData {
    public final DragonLifeStage stage;

    public DragonSpawnData(DragonLifeStage stage) {
        super(false);
        this.stage = stage;
    }

    @Override
    public final boolean isShouldSpawnBaby() {
        return false;
    }

    @Override
    public final float getBabySpawnChance() {
        return 0.0F;
    }
}
