package net.dragonmounts.neo.common.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.apache.commons.lang3.function.Suppliers;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class ArmorEffectDescriptor {
    protected final Component description;
    protected final BooleanSupplier predicate;
    protected final @Nullable MutableComponent trigger;
    protected final Supplier<@Nullable Component> supplier;
    private @Nullable Component cooldown;

    public ArmorEffectDescriptor(
            Component description,
            @Nullable Supplier<Component> cooldown,
            @Nullable MutableComponent trigger,
            BooleanSupplier predicate
    ) {
        this.description = description;
        this.supplier = cooldown == null ? Suppliers.nul() : cooldown;
        this.trigger = trigger;
        this.predicate = predicate;
    }

    public int getWidth(Font font, int width) {
        int measured = font.width(this.description);
        if (measured > width) {
            width = measured;
        }
        if (this.trigger != null) {
            measured = font.width(this.trigger.withStyle(
                    this.predicate.getAsBoolean() ? ChatFormatting.GREEN : ChatFormatting.GRAY
            ));
            if (measured > width) {
                width = measured;
            }
        }
        if ((this.cooldown = this.supplier.get()) != null) {
            measured = font.width(this.cooldown);
            if (measured > width) {
                width = measured;
            }
        }
        return width;
    }

    public int getHeight(Font font, int width) {
        int height = 1 + font.wordWrapHeight(this.description, width);
        if (this.trigger != null) {
            height += 1 + font.wordWrapHeight(this.trigger, width);
        }
        if (this.cooldown != null) {
            height += 1 + font.wordWrapHeight(this.cooldown, width);
        }
        return height;
    }

    public int render(Font font, int x, int y, int width, GuiGraphics graphics) {
        if (this.trigger != null) {
            y = drawString(font, x, y + 1, width, graphics, this.trigger);
        }
        y = drawString(font, x, y + 1, width, graphics, this.description);
        if (this.cooldown != null) {
            y = drawString(font, x, y + 1, width, graphics, this.cooldown);
        }
        return y;
    }

    public static int drawString(Font font, int x, int y, int width, GuiGraphics graphics, Component component) {
        int line = font.lineHeight;
        for (var text : font.split(component, width)) {
            y += line;
            graphics.drawString(font, text, x, y, -1, true);
        }
        return y;
    }
}
