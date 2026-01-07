package net.dragonmounts.neo.common.util;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorType;

public interface ArmorFactory<C, I extends Item> {
    I makeArmor(C context, ArmorType slot, Item.Properties props);
}
