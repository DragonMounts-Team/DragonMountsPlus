package net.dragonmounts.neo.common.util;


import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Supplier;

public class ArrayUtil {
    /// @see net.minecraft.world.ContainerHelper#removeItem(List, int, int)
    public static ItemStack removeItem(ItemStack[] stacks, int index, int amount) {
        return index >= 0 && index < stacks.length ? stacks[index].split(amount) : ItemStack.EMPTY;
    }

    /// @see net.minecraft.world.ContainerHelper#takeItem(List, int)
    public static ItemStack takeItem(ItemStack[] stacks, int index) {
        if (index >= 0 && index < stacks.length) {
            var stack = stacks[index];
            stacks[index] = ItemStack.EMPTY;
            return stack;
        }
        return ItemStack.EMPTY;
    }

    public static ListTag saveItems(HolderLookup.Provider provider, ListTag list, ItemStack[] stacks, int index) {
        for (int n = stacks.length; index < n; ++index) {
            var stack = stacks[index];
            if (stack.isEmpty()) continue;
            var tag = new CompoundTag();
            tag.putByte("Slot", (byte) index);
            list.add(stack.save(provider, tag));
        }
        return list;
    }

    public static void dropContents(Level level, double x, double y, double z, ItemStack[] stacks, int index) {
        var random = level.random;
        double width = EntityType.ITEM.getWidth(), factor = 1.0 - width, half = width / 2.0;
        x += half;
        z += half;
        for (int n = stacks.length; index < n; ++index) {
            double posX = x + random.nextDouble() * factor;
            double posY = y + random.nextDouble() * factor;
            double posZ = z + random.nextDouble() * factor;
            var stack = stacks[index];
            while (!stack.isEmpty()) {
                var item = new ItemEntity(level, posX, posY, posZ, stack.split(random.nextInt(21) + 10));
                item.setDeltaMovement(
                        random.triangle(0.0, 0.11485000171139836),
                        random.triangle(0.2, 0.11485000171139836),
                        random.triangle(0.0, 0.11485000171139836)
                );
                level.addFreshEntity(item);
            }
        }
    }

    public static <T> T[] fillArray(T[] array, Supplier<? extends T> factory) {
        for (int i = 0; i < array.length; ++i) {
            array[i] = factory.get();
        }
        return array;
    }

    /// @param flags 8 booleans at most.
    public static int compressFlags(boolean... flags) {
        int data = 0, bit = 1;
        for (boolean flag : flags) {
            data |= flag ? bit : 0;
            bit <<= 1;
        }
        return data;
    }

    /// @return an array with 8 booleans.
    public static boolean[] readFlags(int compressed) {
        boolean[] flags = new boolean[8];
        for (int i = 0, bit = 1; i < 8; ++i, bit <<= 1) {
            flags[i] = (compressed & bit) == bit;
        }
        return flags;
    }
}
