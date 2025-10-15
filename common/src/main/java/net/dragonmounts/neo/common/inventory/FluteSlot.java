package net.dragonmounts.neo.common.inventory;

import net.dragonmounts.neo.common.capability.FluteHolder;
import net.dragonmounts.neo.common.component.FluteSound;
import net.dragonmounts.neo.common.init.DMItems;
import net.dragonmounts.neo.compat.platform.DMAttachments;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import static net.dragonmounts.neo.common.DragonMountsShared.makeId;

public class FluteSlot extends Slot {
    public static final ResourceLocation ICON = makeId("slot/flute");
    public final FluteHolder holder;
    public final DragonInventoryHandler inventory;
    public @Nullable SlotListener<? super FluteSlot> listener;
    public @Nullable String desiredName;

    public FluteSlot(
            DragonInventoryHandler handler,
            int x,
            int y
    ) {
        this(DMAttachments.getOrCreate(
                handler.player,
                DMAttachments.FLUTE_HOLDER
        ), handler, x, y);
    }

    public FluteSlot(
            FluteHolder holder,
            DragonInventoryHandler handler,
            int x,
            int y
    ) {
        super(holder, 0, x, y);
        this.holder = holder;
        this.inventory = handler;
    }

    public ItemStack takeItem(Player player) {
        var stack = this.holder.getFlute();
        this.holder.setFlute(ItemStack.EMPTY);
        this.onTake(player, stack);
        return stack;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return !stack.isEmpty() && DMItems.FLUTE.is(stack);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return 1;
    }

    @Override
    public void set(ItemStack stack) {
        if (this.listener != null) {
            this.listener.beforePlaceItem(this, stack);
        }
        super.set(stack);
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        if (this.listener != null) {
            this.listener.afterTakeItem(this, stack);
        }
        super.onTake(player, stack);
    }

    @Override
    public void setChanged() {
        var stack = this.getItem();
        if (!stack.isEmpty() && DMItems.FLUTE.is(stack)) {
            var result = stack.copy();
            FluteSound.bindFlute(result, this.inventory.dragon, this.inventory.player);
            if (this.desiredName != null && !StringUtil.isBlank(this.desiredName)) {
                if (!this.desiredName.equals(getItemName(stack))) {
                    result.set(DataComponents.CUSTOM_NAME, Component.literal(this.desiredName));
                }
            } else if (stack.has(DataComponents.CUSTOM_NAME)) {
                result.remove(DataComponents.CUSTOM_NAME);
            }
            this.holder.setFlute(result);
        }
        super.setChanged();
    }

    @Override
    public ResourceLocation getNoItemIcon() {
        return ICON;
    }

    public boolean applyName(String name) {
        var string = StringUtil.filterText(name);
        if (string.length() > 50 || string.equals(this.desiredName)) return false;
        this.desiredName = string;
        var stack = this.getItem();
        if (!stack.isEmpty()) {
            if (StringUtil.isBlank(string)) {
                stack.remove(DataComponents.CUSTOM_NAME);
            } else {
                stack.set(DataComponents.CUSTOM_NAME, Component.literal(string));
            }
        }
        this.setChanged();
        return true;
    }

    public static String getItemName(ItemStack stack) {
        return stack.getHoverName().getString();
    }
}
