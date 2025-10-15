package net.dragonmounts.neo.common.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

public class ArmorEffectTooltip implements ClientTooltipComponent {
    protected final Component title;
    protected final ArmorEffectDescriptor[] entries;
    protected int widthCache;
    protected int heightCache;

    public ArmorEffectTooltip(Component title, ArmorEffectDescriptor... entries) {
        this.title = title;
        this.entries = entries;
    }

    @Override
    public int getWidth(Font font) {
        int width = font.width(this.title);
        for (var entry : this.entries) {
            width = entry.getWidth(font, width);
        }
        this.heightCache = 0;
        return this.widthCache = Math.min(width, Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2);
    }

    @Override
    public int getHeight(Font font) {
        if (this.heightCache == 0) {
            int width = this.widthCache;
            int height = 1 + font.wordWrapHeight(this.title, width);
            for (var entry : this.entries) {
                height += entry.getHeight(font, width);
            }
            this.heightCache = font.lineHeight + height;
        }
        return this.heightCache;
    }

    /// @see GuiGraphics#drawWordWrap(Font, FormattedText, int, int, int, int, boolean)
    @Override
    public void renderImage(Font font, int x, int y, int width, int height, GuiGraphics graphics) {
        int line = font.lineHeight;
        for (var text : font.split(this.title, width)) {
            graphics.drawString(font, text, x, y, -1, true);
            y += line;
        }
        for (var entry : this.entries) {
            y = entry.render(font, x, y, width, graphics);
        }
    }
}
