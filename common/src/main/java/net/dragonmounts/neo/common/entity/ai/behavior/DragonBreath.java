package net.dragonmounts.neo.common.entity.ai.behavior;

import net.dragonmounts.neo.common.entity.breath.BreathState;
import net.dragonmounts.neo.common.entity.breath.DragonBreathSpec;
import net.dragonmounts.neo.common.entity.dragon.TameableDragonEntity;

/**
 * Created by TGG on 8/07/2015.
 * Responsible for
 * - retrieving the player's selected target (based on player's input from Dragon Orb item)
 * - synchronising the player-selected target between server AI and client copy - using datawatcher
 * - rendering the breath weapon on the client
 * - performing the effects of the weapon on the server (eg burning blocks, causing damage)
 * The selection of an actual target (typically - based on the player desired target), navigation of dragon to the appropriate range,
 * turning the dragon to face the target, is done by targeting AI.
 * DragonBreathHelper is also responsible for
 * - tracking the current breath state (IDLE, STARTING, SUSTAINED BREATHING, STOPPING)
 * - sound effects
 * - adding delays for jaw open / breathing start
 * - interrupting the beam when the dragon is facing the wrong way / the angle of the beam mismatches the head angle
 * Usage:
 * 1) Create instance, providing the parent dragon entity and a datawatcher index to use for breathing
 * 2) call onLivingUpdate(), onDeath(), onDeathUpdate(), readFromNBT() and writeFromNBT() from the corresponding
 * parent entity methods
 * 3a) The AI task responsible for targeting should call getPlayerSelectedTarget() to find out what the player wants
 * the dragon to target.
 * 3b) Once the target is in range and the dragon is facing the correct side, the AI should use setBreathingTarget()
 * to commence breathing at the target
 * 4) getCurrentBreathState() and getBreathStateFractionComplete() should be called by animation routines for
 * the dragon during breath weapon (eg jaw opening)
 */
public abstract class DragonBreath<T extends TameableDragonEntity> implements RangedAttack<T> {
    public static final int BREATH_START_DURATION = 5; // ticks
    public static final int BREATH_STOP_DURATION = 5; // ticks
    public final DragonBreathSpec spec;
    protected BreathState currentBreathState = BreathState.IDLE;
    protected int transitionStartTick;
    protected int tickCounter = 0;

    public DragonBreath(DragonBreathSpec spec) {
        this.spec = spec;
    }

    protected void updateBreathState(T dragon) {
        switch (currentBreathState) {
            case IDLE -> {
                if (dragon.isBreathing()) {
                    transitionStartTick = tickCounter;
                    currentBreathState = BreathState.STARTING;
                }
            }
            case STARTING -> {
                if (tickCounter - transitionStartTick >= BREATH_START_DURATION) {
                    transitionStartTick = tickCounter;
                    if (dragon.isBreathing()) {
                        currentBreathState = BreathState.SUSTAIN;
                        this.onBreathStart(dragon);
                    } else {
                        currentBreathState = BreathState.STOPPING;
                        this.onBreathStop(dragon);
                    }
                }
            }
            case SUSTAIN -> {
                if (!dragon.isBreathing()) {
                    transitionStartTick = tickCounter;
                    currentBreathState = BreathState.STOPPING;
                    this.onBreathStop(dragon);
                }
            }
            case STOPPING -> {
                if (tickCounter - transitionStartTick >= BREATH_STOP_DURATION) {
                    currentBreathState = BreathState.IDLE;
                }
            }
        }
    }

    protected void onBreathStart(T dragon) {}

    protected void onBreathStop(T dragon) {}

    @Override
    public void onDetached(T entity) {
        this.onBreathStop(entity);
    }
}