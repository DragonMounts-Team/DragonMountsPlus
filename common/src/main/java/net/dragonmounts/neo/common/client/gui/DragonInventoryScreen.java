package net.dragonmounts.neo.common.client.gui;

import net.dragonmounts.neo.common.client.ClientDragonEntity;
import net.dragonmounts.neo.common.inventory.DragonInventoryHandler;
import net.dragonmounts.neo.common.inventory.FluteSlot;
import net.dragonmounts.neo.common.inventory.SlotListener;
import net.dragonmounts.neo.common.network.c2s.RenameFlutePayload;
import net.dragonmounts.neo.common.network.c2s.ToggleSittingByIDPayload;
import net.dragonmounts.neo.common.network.c2s.ToggleTrustPayload;
import net.dragonmounts.neo.compat.platform.ClientNetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.UnknownNullability;

import java.util.function.Function;

import static net.dragonmounts.neo.common.DragonMountsShared.makeId;
import static net.dragonmounts.neo.common.inventory.FluteSlot.getItemName;
import static net.minecraft.client.gui.components.AbstractWidget.wrapDefaultNarrationMessage;

/**
 * @see net.minecraft.client.gui.screens.inventory.HorseInventoryScreen
 */
public class DragonInventoryScreen extends AbstractContainerScreen<DragonInventoryHandler> implements SlotListener<FluteSlot> {
    private static final ResourceLocation TEXT_FIELD_SPRITE = ResourceLocation.withDefaultNamespace("container/anvil/text_field");
    private static final ResourceLocation INVENTORY = makeId("textures/gui/dragon_inventory.png");
    private static final ResourceLocation PANEL = makeId("textures/gui/dragon_panel.png");
    private static final ResourceLocation ARMOR_SPRITE = makeId("dragon_panel/armor");
    private static final ResourceLocation HEALTH_SPRITE = makeId("dragon_panel/health");
    private static final ResourceLocation FOOD_SPRITE = makeId("dragon_panel/food");
    private static final Component TRUST_STATE = Component.translatable("button.neodragonmounts.trust_state");
    private static final Component SITTING_STATE = Component.translatable("button.neodragonmounts.sitting_state");
    private static final Component ORDER_TO_SIT = Component.translatable("button.neodragonmounts.order_to_sit");
    private static final Component ORDER_TO_STAND = Component.translatable("button.neodragonmounts.order_to_stand");
    private static final Component MESSAGE = Component.translatable("container.neodragonmounts.dragon_inventory");
    private @UnknownNullability IconToggleButton trustToggle;
    private @UnknownNullability TextToggleButton sittingToggle;
    private @UnknownNullability EditBox name;
    private @UnknownNullability String health;
    private @UnknownNullability String armor;

    public DragonInventoryScreen(DragonInventoryHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        this.imageHeight = 224;
        this.imageWidth = 324;
        this.inventoryLabelX = this.titleLabelX = 156;
        this.inventoryLabelY = 131;// 224 - 94 + 1
    }

    @Override
    protected void init() {
        super.init();
        var flute = this.menu.flute;
        int x = this.leftPos + 10, y = this.topPos;
        var name = this.name = new EditBox(this.font, x + 22, y + 12, 104, 12, MESSAGE);
        name.setCanLoseFocus(false);
        name.setTextColor(-1);
        name.setTextColorUneditable(-1);
        name.setBordered(false);
        name.setMaxLength(50);
        name.setResponder(this::onNameChanged);
        name.setValue("");
        this.addWidget(name);
        name.setEditable(flute.hasItem());
        flute.listener = this;
        this.addWidget(this.trustToggle = new IconToggleButton(x, y + 194, TRUST_STATE, this::handleToggleTrust, toggle ->
                wrapDefaultNarrationMessage(toggle.getState() ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF)
        ));
        this.addWidget(this.sittingToggle = new TextToggleButton(x + 22, y + 194, 35, SITTING_STATE, this::handleToggleSitting, toggle ->
                wrapDefaultNarrationMessage(toggle.getState() ? DragonInventoryScreen.ORDER_TO_SIT : DragonInventoryScreen.ORDER_TO_STAND)
        ));
        this.containerTick();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        var dragon = this.menu.dragon;
        this.health = String.format("%.2f/%.2f", dragon.getHealth(), dragon.getMaxHealth());
        this.armor = String.format("%.2f", dragon.getAttributeValue(Attributes.ARMOR));
        this.sittingToggle.setState(this.menu.sitting.get() != 0, ORDER_TO_STAND, ORDER_TO_SIT);
        this.trustToggle.setState(dragon.isTrustingAnyPlayer());
    }

    @Override
    protected void setInitialFocus() {
        this.setInitialFocus(this.name);
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        String string = this.name.getValue();
        this.init(minecraft, width, height);
        this.name.setValue(string);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            assert this.minecraft != null && this.minecraft.player != null;
            this.minecraft.player.closeContainer();
        }
        return this.name.keyPressed(keyCode, scanCode, modifiers) || this.name.canConsumeInput() || super.keyPressed(keyCode, scanCode, modifiers);
    }

    public void handleToggleTrust(Button ignored) {
        ClientNetworkHandler.send(new ToggleTrustPayload(this.menu.dragon.getId()));
    }

    public void handleToggleSitting(Button ignored) {
        ClientNetworkHandler.send(new ToggleSittingByIDPayload(this.menu.dragon.getId()));
    }

    private void onNameChanged(String name) {
        var slot = this.menu.flute;
        var stack = slot.getItem();
        if (stack.isEmpty()) return;
        if (!stack.has(DataComponents.CUSTOM_NAME) && name.equals(stack.getHoverName().getString())) {
            name = "";
        }
        if (slot.applyName(name)) {
            ClientNetworkHandler.send(new RenameFlutePayload(name));
        }
    }

    @Override
    public void beforePlaceItem(FluteSlot slot, ItemStack stack) {
        this.setFocused(this.name);
        if (stack.isEmpty()) {
            this.name.setEditable(false);
            this.name.setValue(slot.desiredName = "");
        } else {
            this.name.setEditable(true);
            var name = getItemName(stack);
            if (!name.equals(getItemName(slot.getItem()))) {
                this.name.setValue(slot.desiredName = name);
            }
        }
    }

    @Override
    public void afterTakeItem(FluteSlot slot, ItemStack stack) {
        this.name.setValue("");
        this.name.setEditable(false);
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float ticks) {
        super.render(graphics, x, y, ticks);
        this.name.render(graphics, x, y, ticks);
        this.trustToggle.render(graphics, x, y, ticks);
        this.sittingToggle.render(graphics, x, y, ticks);
        this.renderTooltip(graphics, x, y);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        var font = this.font;
        graphics.drawString(font, this.armor, 20, 33, 0xE99E0C, false);
        graphics.drawString(font, this.health, 20, 44, 0xE99E0C, false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float ticks, int x, int y) {
        int left = this.leftPos, top = this.topPos;
        Function<ResourceLocation, RenderType> renderer = RenderType::guiTextured;
        graphics.blit(renderer, INVENTORY, left + 148, top, 0, 0, 176, this.imageHeight, 256, 256);
        var dragon = (ClientDragonEntity) this.menu.dragon;
        if (dragon.hasChest()) {
            graphics.blit(renderer, INVENTORY, left + 148, top + 73, 0, 140, 170, 55, 256, 256);
        }
        graphics.blit(renderer, PANEL, left, top, 0, 0, 147, this.imageHeight, 256, 256);
        if (this.menu.flute.hasItem()) {
            graphics.blitSprite(renderer, TEXT_FIELD_SPRITE, left + 29, top + 8, 110, 16);
        }
        left += 10;
        graphics.blitSprite(renderer, ARMOR_SPRITE, left, top + 32, 9, 9);
        graphics.blitSprite(renderer, HEALTH_SPRITE, left, top + 43, 9, 9);
        dragon.animator.renderCrystalBeams = false;
        InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, left + 164, top + 18, left + 270, top + 70, 10, 0.25F, x, y, dragon);
        dragon.animator.renderCrystalBeams = true;
    }
}
