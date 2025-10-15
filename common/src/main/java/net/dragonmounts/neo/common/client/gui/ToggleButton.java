package net.dragonmounts.neo.common.client.gui;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Function;

public abstract class ToggleButton extends Button {
    protected final Function<ToggleButton, MutableComponent> narration;
    private boolean state;

    public ToggleButton(
            int x,
            int y,
            int width,
            int height,
            Component message,
            OnPress onPress,
            Function<ToggleButton, MutableComponent> narration
    ) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        this.narration = narration;
    }

    @Override
    protected MutableComponent createNarrationMessage() {
        return this.narration.apply(this);
    }

    public final void setState(boolean state) {
        this.state = state;
    }

    public final boolean getState() {
        return state;
    }
}
