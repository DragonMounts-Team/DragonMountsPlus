package net.dragonmounts.neo.compat.registry;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dragonmounts.neo.common.api.ArmorEffectSource;
import net.dragonmounts.neo.common.api.DescribedArmorEffect;
import net.dragonmounts.neo.common.api.DragonTypified;
import net.dragonmounts.neo.common.capability.ArmorEffectManager;
import net.dragonmounts.neo.common.item.DragonScaleArmorItem;
import net.dragonmounts.neo.common.util.ArmorFactory;
import net.dragonmounts.neo.common.util.ItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;

import java.util.Objects;

import static net.dragonmounts.neo.common.DragonMountsShared.ITEM_TRANSLATION_KEY_PREFIX;
import static net.dragonmounts.neo.common.DragonMountsShared.makeKey;

public final class DragonScaleArmorSuit implements DragonTypified, ArmorEffectSource {
    public static final String HELMET_TRANSLATION_KEY = ITEM_TRANSLATION_KEY_PREFIX + "dragon_scale_helmet";
    public static final String CHESTPLATE_TRANSLATION_KEY = ITEM_TRANSLATION_KEY_PREFIX + "dragon_scale_chestplate";
    public static final String LEGGINGS_TRANSLATION_KEY = ITEM_TRANSLATION_KEY_PREFIX + "dragon_scale_leggings";
    public static final String BOOTS_TRANSLATION_KEY = ITEM_TRANSLATION_KEY_PREFIX + "dragon_scale_boots";
    private static final ObjectArrayList<DragonScaleArmorSuit> SUITS = new ObjectArrayList<>();

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
        var registry = Registries.ITEM;
        var suit = new DragonScaleArmorSuit(
                type,
                effect,
                makeKey(registry, helmet),
                makeKey(registry, chestplate),
                makeKey(registry, leggings),
                makeKey(registry, boots),
                factory
        );
        type.bindInstance(DragonScaleArmorSuit.class, suit);
        SUITS.add(suit);
        group.add(suit::getHelmet);
        group.add(suit::getChestplate);
        group.add(suit::getLeggings);
        group.add(suit::getBoots);
        return suit;
    }

    static void registerEntries(Registry<Item> registry) {
        for (var suit : SUITS) {
            suit.register(registry);
        }
    }

    public final DragonType type;
    public final DescribedArmorEffect effect;
    public final ResourceKey<Item> helmet;
    public final ResourceKey<Item> chestplate;
    public final ResourceKey<Item> leggings;
    public final ResourceKey<Item> boots;
    public final ArmorFactory<DragonScaleArmorSuit, DragonScaleArmorItem> factory;
    private DragonScaleArmorItem helmetItem;
    private DragonScaleArmorItem chestplateItem;
    private DragonScaleArmorItem leggingsItem;
    private DragonScaleArmorItem bootsItem;

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
        this.factory = factory;
    }

    private void register(Registry<Item> registry) {
        var factory = this.factory;
        var key = this.helmet;
        this.helmetItem = Registry.register(registry, key, factory.makeArmor(this, ArmorType.HELMET, new Item.Properties().setId(key)));
        key = this.chestplate;
        this.chestplateItem = Registry.register(registry, key, factory.makeArmor(this, ArmorType.CHESTPLATE, new Item.Properties().setId(key)));
        key = this.leggings;
        this.leggingsItem = Registry.register(registry, key, factory.makeArmor(this, ArmorType.LEGGINGS, new Item.Properties().setId(key)));
        key = this.boots;
        this.bootsItem = Registry.register(registry, key, factory.makeArmor(this, ArmorType.BOOTS, new Item.Properties().setId(key)));
    }

    public DragonScaleArmorItem getHelmet() {
        return Objects.requireNonNull(this.helmetItem);
    }

    public DragonScaleArmorItem getChestplate() {
        return Objects.requireNonNull(this.chestplateItem);
    }

    public DragonScaleArmorItem getLeggings() {
        return Objects.requireNonNull(this.leggingsItem);
    }

    public DragonScaleArmorItem getBoots() {
        return Objects.requireNonNull(this.bootsItem);
    }

    @Override
    public DragonType getDragonType() {
        return this.type;
    }

    @Override
    public void affect(ArmorEffectManager manager, Player player, ItemStack stack) {
        if (this.effect == null) return;
        manager.addLevel(this.effect, 1);
    }

    @Override
    public ArmorEffectSourceType<?> getType() {
        return ArmorEffectSourceType.BUILTIN;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (
                other instanceof DragonScaleArmorSuit that && Objects.equals(this.type, that.type)
        );
    }

    @Override
    public int hashCode() {
        return this.type.hashCode();
    }
}
