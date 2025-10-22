package net.dragonmounts.neo.common;

import net.dragonmounts.neo.compat.registry.*;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import static net.minecraft.resources.ResourceKey.createRegistryKey;

public class DragonMountsShared {
    public static final String NAMESPACE = "neodragonmounts";
    public static final String BLOCK_TRANSLATION_KEY_PREFIX = "block." + NAMESPACE + '.';
    public static final String ITEM_TRANSLATION_KEY_PREFIX = "item." + NAMESPACE + '.';
    private static final ResourceLocation ROOT = ResourceLocation.fromNamespaceAndPath(NAMESPACE, "root");
    public static final ResourceKey<Registry<ArmorEffect>> ARMOR_EFFECT = createRegistryKey(makeId("armor_effect"));
    public static final ResourceKey<Registry<ArmorEffectSourceType<?>>> ARMOR_EFFECT_SOURCE = createRegistryKey(makeId("armor_effect_source"));
    public static final ResourceKey<Registry<DragonType>> DRAGON_TYPE = createRegistryKey(makeId("dragon_type"));
    public static final ResourceKey<Registry<DragonVariant>> DRAGON_VARIANT = createRegistryKey(makeId("dragon_variant"));
    public static final ResourceKey<Registry<CooldownCategory>> COOLDOWN_CATEGORY = createRegistryKey(makeId("cooldown_category"));
    public static final Component REQUIRES_OWNER = Component.translatable("message.neodragonmounts.dragon.requires_owner");

    /// to skip namespace checking
    public static ResourceLocation makeId(String name) {
        return ROOT.withPath(name);
    }

    /// to skip namespace checking
    public static <T> ResourceKey<T> makeKey(ResourceKey<? extends Registry<T>> registry, String name) {
        return ResourceKey.create(registry, ROOT.withPath(name));
    }
}
