package net.dragonmounts.neo.compat.registry;

import net.dragonmounts.neo.common.api.ArmorEffectSource;
import net.dragonmounts.neo.common.api.DescribedArmorEffect;
import net.dragonmounts.neo.common.api.DragonTypified;
import net.dragonmounts.neo.common.capability.ArmorEffectManager;
import net.dragonmounts.neo.common.item.DragonScaleArmorItem;
import net.dragonmounts.neo.common.util.ArmorFactory;
import net.dragonmounts.neo.common.util.ItemGroup;
import net.dragonmounts.neo.compat.Dummy;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import static net.dragonmounts.neo.common.DragonMountsShared.ITEM_TRANSLATION_KEY_PREFIX;

public final class DragonScaleArmorSuit implements DragonTypified, ArmorEffectSource {
    public static final String HELMET_TRANSLATION_KEY = ITEM_TRANSLATION_KEY_PREFIX + "dragon_scale_helmet";
    public static final String CHESTPLATE_TRANSLATION_KEY = ITEM_TRANSLATION_KEY_PREFIX + "dragon_scale_chestplate";
    public static final String LEGGINGS_TRANSLATION_KEY = ITEM_TRANSLATION_KEY_PREFIX + "dragon_scale_leggings";
    public static final String BOOTS_TRANSLATION_KEY = ITEM_TRANSLATION_KEY_PREFIX + "dragon_scale_boots";

    public static DragonScaleArmorSuit makeSuit(
            DragonType type,
            DescribedArmorEffect effect,
            ItemGroup group,
            String helmet,
            String chestplate,
            String leggings,
            String boots,
            ArmorFactory<DragonScaleArmorSuit, DragonScaleArmorItem> factory
    ) {
        return Dummy.get();
    }

    public final DragonType type;
    public final DescribedArmorEffect effect;
    public final ResourceKey<Item> helmet;
    public final ResourceKey<Item> chestplate;
    public final ResourceKey<Item> leggings;
    public final ResourceKey<Item> boots;

    public DragonScaleArmorSuit(
            DragonType type,
            DescribedArmorEffect effect,
            ResourceKey<Item> helmet,
            ResourceKey<Item> chestplate,
            ResourceKey<Item> leggings,
            ResourceKey<Item> boots,
            ArmorFactory<DragonScaleArmorSuit, DragonScaleArmorItem> factory
    ) {
        this.type = type;
        this.effect = effect;
        this.helmet = helmet;
        this.chestplate = chestplate;
        this.leggings = leggings;
        this.boots = boots;
    }

    public DragonScaleArmorItem getHelmet() {
        return Dummy.get();
    }

    public DragonScaleArmorItem getChestplate() {
        return Dummy.get();
    }

    public DragonScaleArmorItem getLeggings() {
        return Dummy.get();
    }

    public DragonScaleArmorItem getBoots() {
        return Dummy.get();
    }

    @Override
    public DragonType getDragonType() {
        return this.type;
    }

    @Override
    public void affect(ArmorEffectManager manager, Player player, ItemStack stack) {}

    @Override
    public ArmorEffectSourceType<?> getType() {
        return ArmorEffectSourceType.BUILTIN;
    }
}
