package net.dragonmounts.neo.common.entity.dragon;

import net.dragonmounts.neo.common.client.model.dragon.DragonAnimator;

public interface MouthState {
    boolean isCharging();

    float updateJawRotation(DragonAnimator animator, int duration);
}
