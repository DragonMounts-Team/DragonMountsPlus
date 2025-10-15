package net.dragonmounts.neo.common.inventory;

import net.dragonmounts.neo.common.capability.FluteHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class FluteHolderImpl implements FluteHolder {
    public static FluteHolderImpl of(ItemStack flute) {
        var holder = new FluteHolderImpl();
        holder.setFlute(flute);
        return holder;
    }

    private ItemStack flute = ItemStack.EMPTY;

    @Override
    public ItemStack getFlute() {
        return this.flute;
    }

    @Override
    public void setFlute(ItemStack flute) {
        this.flute = flute;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return this.flute.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.flute;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (this.flute.isEmpty()) return ItemStack.EMPTY;
        ItemStack result = this.flute.split(amount);
        if (!result.isEmpty()) {
            this.setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = this.flute;
        this.flute = ItemStack.EMPTY;
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.flute = stack;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public void setChanged() {}

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void stopOpen(Player player) {
        if (this.flute.isEmpty()) return;
        if (!player.addItem(this.flute)) {
            player.drop(this.flute, false);
        }
        this.flute = ItemStack.EMPTY;
    }

    @Override
    public void clearContent() {
        this.setFlute(ItemStack.EMPTY);
    }
}
