package net.dragonmounts.neo.common.init;

import net.dragonmounts.neo.common.block.DragonCoreBlock;
import net.dragonmounts.neo.common.block.DragonScaleBlock;
import net.dragonmounts.neo.common.block.HatchableDragonEggBlock;
import net.dragonmounts.neo.common.item.*;
import net.dragonmounts.neo.compat.platform.FlammableBlock;
import net.dragonmounts.neo.compat.registry.BlockItemHolder;
import net.dragonmounts.neo.compat.registry.DragonScaleArmorSuit;
import net.dragonmounts.neo.compat.registry.DragonType;
import net.dragonmounts.neo.compat.registry.ItemHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.block.DispenserBlock;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static net.dragonmounts.neo.common.init.DMItemGroups.*;
import static net.dragonmounts.neo.compat.registry.DragonScaleArmorSuit.makeSuit;
import static net.dragonmounts.neo.compat.registry.ItemHolder.registerItem;
import static net.minecraft.resources.ResourceLocation.withDefaultNamespace;

public class DMItems {
    public static final ResourceLocation DRAGON_ARMOR_MODIFIER_NAME = withDefaultNamespace("armor." + ArmorType.BODY.getName());
    public static final BlockItemHolder<DragonCoreBlock, ?> DRAGON_CORE = BlockItemHolder.registerItem(
            DMBlocks.DRAGON_CORE,
            (block, props) -> new BlockItem(block, props.rarity(Rarity.RARE))
    );
    public static final BlockItemHolder<HatchableDragonEggBlock, ?> AETHER_DRAGON_EGG = DRAGON_EGGS.register(
            DMBlocks.AETHER_DRAGON_EGG,
            (block, props) -> makeDragonEggBlock(block, props.rarity(Rarity.UNCOMMON))
    );
    public static final BlockItemHolder<HatchableDragonEggBlock, ?> DARK_DRAGON_EGG = DRAGON_EGGS.register(
            DMBlocks.DARK_DRAGON_EGG,
            (block, props) -> makeDragonEggBlock(block, props.rarity(Rarity.UNCOMMON))
    );
    public static final BlockItemHolder<HatchableDragonEggBlock, ?> ENCHANTED_DRAGON_EGG = DRAGON_EGGS.register(
            DMBlocks.ENCHANTED_DRAGON_EGG,
            (block, props) -> makeDragonEggBlock(block, props.rarity(Rarity.UNCOMMON))
    );
    public static final BlockItemHolder<HatchableDragonEggBlock, ?> ENDER_DRAGON_EGG = DRAGON_EGGS.register(
            DMBlocks.ENDER_DRAGON_EGG,
            (block, props) -> makeDragonEggBlock(block, props.rarity(Rarity.EPIC))
    );
    public static final BlockItemHolder<HatchableDragonEggBlock, ?> FIRE_DRAGON_EGG = DRAGON_EGGS.register(
            DMBlocks.FIRE_DRAGON_EGG,
            (block, props) -> makeDragonEggBlock(block, props.rarity(Rarity.UNCOMMON))
    );
    public static final BlockItemHolder<HatchableDragonEggBlock, ?> FOREST_DRAGON_EGG = DRAGON_EGGS.register(
            DMBlocks.FOREST_DRAGON_EGG,
            (block, props) -> makeDragonEggBlock(block, props.rarity(Rarity.UNCOMMON))
    );
    public static final BlockItemHolder<HatchableDragonEggBlock, ?> ICE_DRAGON_EGG = DRAGON_EGGS.register(
            DMBlocks.ICE_DRAGON_EGG,
            (block, props) -> makeDragonEggBlock(block, props.rarity(Rarity.UNCOMMON))
    );
    public static final BlockItemHolder<HatchableDragonEggBlock, ?> MOONLIGHT_DRAGON_EGG = DRAGON_EGGS.register(
            DMBlocks.MOONLIGHT_DRAGON_EGG,
            (block, props) -> makeDragonEggBlock(block, props.rarity(Rarity.UNCOMMON))
    );
    public static final BlockItemHolder<HatchableDragonEggBlock, ?> NETHER_DRAGON_EGG = DRAGON_EGGS.register(
            DMBlocks.NETHER_DRAGON_EGG,
            (block, props) -> makeDragonEggBlock(block, props.rarity(Rarity.UNCOMMON))
    );
    public static final BlockItemHolder<HatchableDragonEggBlock, ?> SCULK_DRAGON_EGG = DRAGON_EGGS.register(
            DMBlocks.SCULK_DRAGON_EGG,
            (block, props) -> makeDragonEggBlock(block, props.rarity(Rarity.RARE).fireResistant())
    );
    public static final BlockItemHolder<HatchableDragonEggBlock, ?> SKELETON_DRAGON_EGG = DRAGON_EGGS.register(
            DMBlocks.SKELETON_DRAGON_EGG,
            (block, props) -> makeDragonEggBlock(block, props.rarity(Rarity.UNCOMMON))
    );
    public static final BlockItemHolder<HatchableDragonEggBlock, ?> STORM_DRAGON_EGG = DRAGON_EGGS.register(
            DMBlocks.STORM_DRAGON_EGG,
            (block, props) -> makeDragonEggBlock(block, props.rarity(Rarity.UNCOMMON))
    );
    public static final BlockItemHolder<HatchableDragonEggBlock, ?> SUNLIGHT_DRAGON_EGG = DRAGON_EGGS.register(
            DMBlocks.SUNLIGHT_DRAGON_EGG,
            (block, props) -> makeDragonEggBlock(block, props.rarity(Rarity.UNCOMMON))
    );
    public static final BlockItemHolder<HatchableDragonEggBlock, ?> TERRA_DRAGON_EGG = DRAGON_EGGS.register(
            DMBlocks.TERRA_DRAGON_EGG,
            (block, props) -> makeDragonEggBlock(block, props.rarity(Rarity.UNCOMMON))
    );
    public static final BlockItemHolder<HatchableDragonEggBlock, ?> WATER_DRAGON_EGG = DRAGON_EGGS.register(
            DMBlocks.WATER_DRAGON_EGG,
            (block, props) -> makeDragonEggBlock(block, props.rarity(Rarity.UNCOMMON))
    );
    public static final BlockItemHolder<HatchableDragonEggBlock, ?> WITHER_DRAGON_EGG = DRAGON_EGGS.register(
            DMBlocks.WITHER_DRAGON_EGG,
            (block, props) -> makeDragonEggBlock(block, props.rarity(Rarity.UNCOMMON))
    );
    public static final BlockItemHolder<HatchableDragonEggBlock, ?> ZOMBIE_DRAGON_EGG = DRAGON_EGGS.register(
            DMBlocks.ZOMBIE_DRAGON_EGG,
            (block, props) -> makeDragonEggBlock(block, props.rarity(Rarity.UNCOMMON))
    );
    public static final BlockItemHolder<FlammableBlock, ?> DRAGON_NEST = BLOCK_TAB.register(DMBlocks.DRAGON_NEST, BlockItem::new);
    public static final BlockItemHolder<DragonScaleBlock, ?> AETHER_DRAGON_SCALE_BLOCK = BLOCK_TAB.register(DMBlocks.AETHER_DRAGON_SCALE_BLOCK, DMItems::makeDragonScaleBlock);
    public static final BlockItemHolder<DragonScaleBlock, ?> DARK_DRAGON_SCALE_BLOCK = BLOCK_TAB.register(DMBlocks.DARK_DRAGON_SCALE_BLOCK, DMItems::makeDragonScaleBlock);
    public static final BlockItemHolder<DragonScaleBlock, ?> ENCHANTED_DRAGON_SCALE_BLOCK = BLOCK_TAB.register(DMBlocks.ENCHANTED_DRAGON_SCALE_BLOCK, DMItems::makeDragonScaleBlock);
    public static final BlockItemHolder<DragonScaleBlock, ?> ENDER_DRAGON_SCALE_BLOCK = BLOCK_TAB.register(DMBlocks.ENDER_DRAGON_SCALE_BLOCK, DMItems::makeDragonScaleBlock);
    public static final BlockItemHolder<DragonScaleBlock, ?> FIRE_DRAGON_SCALE_BLOCK = BLOCK_TAB.register(DMBlocks.FIRE_DRAGON_SCALE_BLOCK, DMItems::makeDragonScaleBlock);
    public static final BlockItemHolder<DragonScaleBlock, ?> FOREST_DRAGON_SCALE_BLOCK = BLOCK_TAB.register(DMBlocks.FOREST_DRAGON_SCALE_BLOCK, DMItems::makeDragonScaleBlock);
    public static final BlockItemHolder<DragonScaleBlock, ?> ICE_DRAGON_SCALE_BLOCK = BLOCK_TAB.register(DMBlocks.ICE_DRAGON_SCALE_BLOCK, DMItems::makeDragonScaleBlock);
    public static final BlockItemHolder<DragonScaleBlock, ?> MOONLIGHT_DRAGON_SCALE_BLOCK = BLOCK_TAB.register(DMBlocks.MOONLIGHT_DRAGON_SCALE_BLOCK, DMItems::makeDragonScaleBlock);
    public static final BlockItemHolder<DragonScaleBlock, ?> NETHER_DRAGON_SCALE_BLOCK = BLOCK_TAB.register(DMBlocks.NETHER_DRAGON_SCALE_BLOCK, DMItems::makeDragonScaleBlock);
    public static final BlockItemHolder<DragonScaleBlock, ?> SCULK_DRAGON_SCALE_BLOCK = BLOCK_TAB.register(DMBlocks.SCULK_DRAGON_SCALE_BLOCK, DMItems::makeDragonScaleBlock);
    public static final BlockItemHolder<DragonScaleBlock, ?> STORM_DRAGON_SCALE_BLOCK = BLOCK_TAB.register(DMBlocks.STORM_DRAGON_SCALE_BLOCK, DMItems::makeDragonScaleBlock);
    public static final BlockItemHolder<DragonScaleBlock, ?> SUNLIGHT_DRAGON_SCALE_BLOCK = BLOCK_TAB.register(DMBlocks.SUNLIGHT_DRAGON_SCALE_BLOCK, DMItems::makeDragonScaleBlock);
    public static final BlockItemHolder<DragonScaleBlock, ?> TERRA_DRAGON_SCALE_BLOCK = BLOCK_TAB.register(DMBlocks.TERRA_DRAGON_SCALE_BLOCK, DMItems::makeDragonScaleBlock);
    public static final BlockItemHolder<DragonScaleBlock, ?> WATER_DRAGON_SCALE_BLOCK = BLOCK_TAB.register(DMBlocks.WATER_DRAGON_SCALE_BLOCK, DMItems::makeDragonScaleBlock);
    public static final BlockItemHolder<DragonScaleBlock, ?> ZOMBIE_DRAGON_SCALE_BLOCK = BLOCK_TAB.register(DMBlocks.ZOMBIE_DRAGON_SCALE_BLOCK, DMItems::makeDragonScaleBlock);
    public static final ItemHolder<Item> DRAGON_MEAT = MISC_TAB.register("dragon_meat", props -> new Item(props.food(new FoodProperties.Builder().nutrition(3).saturationModifier(0.5F).build())));
    public static final ItemHolder<Item> COOKED_DRAGON_MEAT = MISC_TAB.register("cooked_dragon_meat", props -> new Item(props.food(new FoodProperties.Builder().nutrition(6).saturationModifier(1.5F).build())));
    public static final ItemHolder<DragonScalesItem> AETHER_DRAGON_SCALES = MISC_TAB.register("aether_dragon_scales", props -> makeDragonScales(DragonTypes.AETHER, props));
    public static final ItemHolder<DragonScalesItem> ENCHANTED_DRAGON_SCALES = MISC_TAB.register("enchanted_dragon_scales", props -> makeDragonScales(DragonTypes.ENCHANTED, props));
    public static final ItemHolder<DragonScalesItem> ENDER_DRAGON_SCALES = MISC_TAB.register("ender_dragon_scales", props -> makeDragonScales(DragonTypes.ENDER, props));
    public static final ItemHolder<DragonScalesItem> FIRE_DRAGON_SCALES = MISC_TAB.register("fire_dragon_scales", props -> makeDragonScales(DragonTypes.FIRE, props));
    public static final ItemHolder<DragonScalesItem> FOREST_DRAGON_SCALES = MISC_TAB.register("forest_dragon_scales", props -> makeDragonScales(DragonTypes.FOREST, props));
    public static final ItemHolder<DragonScalesItem> ICE_DRAGON_SCALES = MISC_TAB.register("ice_dragon_scales", props -> makeDragonScales(DragonTypes.ICE, props));
    public static final ItemHolder<DragonScalesItem> MOONLIGHT_DRAGON_SCALES = MISC_TAB.register("moonlight_dragon_scales", props -> makeDragonScales(DragonTypes.MOONLIGHT, props));
    public static final ItemHolder<DragonScalesItem> NETHER_DRAGON_SCALES = MISC_TAB.register("nether_dragon_scales", props -> makeDragonScales(DragonTypes.NETHER, props));
    public static final ItemHolder<DragonScalesItem> SCULK_DRAGON_SCALES = MISC_TAB.register("sculk_dragon_scales", props -> makeDragonScales(DragonTypes.SCULK, props.fireResistant()));
    public static final ItemHolder<DragonScalesItem> STORM_DRAGON_SCALES = MISC_TAB.register("storm_dragon_scales", props -> makeDragonScales(DragonTypes.STORM, props));
    public static final ItemHolder<DragonScalesItem> SUNLIGHT_DRAGON_SCALES = MISC_TAB.register("sunlight_dragon_scales", props -> makeDragonScales(DragonTypes.SUNLIGHT, props));
    public static final ItemHolder<DragonScalesItem> TERRA_DRAGON_SCALES = MISC_TAB.register("terra_dragon_scales", props -> makeDragonScales(DragonTypes.TERRA, props));
    public static final ItemHolder<DragonScalesItem> WATER_DRAGON_SCALES = MISC_TAB.register("water_dragon_scales", props -> makeDragonScales(DragonTypes.WATER, props));
    public static final ItemHolder<DragonScalesItem> ZOMBIE_DRAGON_SCALES = MISC_TAB.register("zombie_dragon_scales", props -> makeDragonScales(DragonTypes.ZOMBIE, props));
    public static final ItemHolder<DragonScalesItem> DARK_DRAGON_SCALES = MISC_TAB.register("dark_dragon_scales", props -> makeDragonScales(DragonTypes.DARK, props));
    // Shears
    public static final ItemHolder<TieredShearsItem> DIAMOND_SHEARS = TOOL_TAB.register("diamond_shears", props ->
            new TieredShearsItem(ToolMaterial.DIAMOND, props)
    );
    public static final ItemHolder<TieredShearsItem> NETHERITE_SHEARS = TOOL_TAB.register("netherite_shears", props ->
            new TieredShearsItem(ToolMaterial.NETHERITE, props.fireResistant())
    );
    // Flute
    public static final ItemHolder<FluteItem> FLUTE = TOOL_TAB.register("flute", props ->
            new FluteItem(props.stacksTo(1))
    );
    // Dragon Amulets
    public static final ItemHolder<AmuletItem<@NotNull Entity>> AMULET = TOOL_TAB.register("amulet", props ->
            new AmuletItem<>(Entity.class, props.overrideDescription(AmuletItem.TRANSLATION_KEY))
    );
    public static final ItemHolder<DragonAmuletItem> FOREST_DRAGON_AMULET = registerItem("forest_dragon_amulet", props -> makeDragonAmulet(DragonTypes.FOREST, props));
    public static final ItemHolder<DragonAmuletItem> FIRE_DRAGON_AMULET = registerItem("fire_dragon_amulet", props -> makeDragonAmulet(DragonTypes.FIRE, props));
    public static final ItemHolder<DragonAmuletItem> ICE_DRAGON_AMULET = registerItem("ice_dragon_amulet", props -> makeDragonAmulet(DragonTypes.ICE, props));
    public static final ItemHolder<DragonAmuletItem> WATER_DRAGON_AMULET = registerItem("water_dragon_amulet", props -> makeDragonAmulet(DragonTypes.WATER, props));
    public static final ItemHolder<DragonAmuletItem> AETHER_DRAGON_AMULET = registerItem("aether_dragon_amulet", props -> makeDragonAmulet(DragonTypes.AETHER, props));
    public static final ItemHolder<DragonAmuletItem> NETHER_DRAGON_AMULET = registerItem("nether_dragon_amulet", props -> makeDragonAmulet(DragonTypes.NETHER, props));
    public static final ItemHolder<DragonAmuletItem> ENDER_DRAGON_AMULET = registerItem("ender_dragon_amulet", props -> makeDragonAmulet(DragonTypes.ENDER, props));
    public static final ItemHolder<DragonAmuletItem> SUNLIGHT_DRAGON_AMULET = registerItem("sunlight_dragon_amulet", props -> makeDragonAmulet(DragonTypes.SUNLIGHT, props));
    public static final ItemHolder<DragonAmuletItem> ENCHANTED_DRAGON_AMULET = registerItem("enchanted_dragon_amulet", props -> makeDragonAmulet(DragonTypes.ENCHANTED, props));
    public static final ItemHolder<DragonAmuletItem> STORM_DRAGON_AMULET = registerItem("storm_dragon_amulet", props -> makeDragonAmulet(DragonTypes.STORM, props));
    public static final ItemHolder<DragonAmuletItem> TERRA_DRAGON_AMULET = registerItem("terra_dragon_amulet", props -> makeDragonAmulet(DragonTypes.TERRA, props));
    public static final ItemHolder<DragonAmuletItem> ZOMBIE_DRAGON_AMULET = registerItem("zombie_dragon_amulet", props -> makeDragonAmulet(DragonTypes.ZOMBIE, props));
    public static final ItemHolder<DragonAmuletItem> MOONLIGHT_DRAGON_AMULET = registerItem("moonlight_dragon_amulet", props -> makeDragonAmulet(DragonTypes.MOONLIGHT, props));
    public static final ItemHolder<DragonAmuletItem> SCULK_DRAGON_AMULET = registerItem("sculk_dragon_amulet", props -> makeDragonAmulet(DragonTypes.SCULK, props.fireResistant()));
    public static final ItemHolder<DragonAmuletItem> SKELETON_DRAGON_AMULET = registerItem("skeleton_dragon_amulet", props -> makeDragonAmulet(DragonTypes.SKELETON, props));
    public static final ItemHolder<DragonAmuletItem> WITHER_DRAGON_AMULET = registerItem("wither_dragon_amulet", props -> makeDragonAmulet(DragonTypes.WITHER, props));
    public static final ItemHolder<DragonAmuletItem> DARK_DRAGON_AMULET = registerItem("dark_dragon_amulet", props -> makeDragonAmulet(DragonTypes.DARK, props));
    // Dragon Essences
    public static final ItemHolder<DragonEssenceItem> FOREST_DRAGON_ESSENCE = registerItem("forest_dragon_essence", props -> makeDragonEssence(DragonTypes.FOREST, props));
    public static final ItemHolder<DragonEssenceItem> FIRE_DRAGON_ESSENCE = registerItem("fire_dragon_essence", props -> makeDragonEssence(DragonTypes.FIRE, props));
    public static final ItemHolder<DragonEssenceItem> ICE_DRAGON_ESSENCE = registerItem("ice_dragon_essence", props -> makeDragonEssence(DragonTypes.ICE, props));
    public static final ItemHolder<DragonEssenceItem> WATER_DRAGON_ESSENCE = registerItem("water_dragon_essence", props -> makeDragonEssence(DragonTypes.WATER, props));
    public static final ItemHolder<DragonEssenceItem> AETHER_DRAGON_ESSENCE = registerItem("aether_dragon_essence", props -> makeDragonEssence(DragonTypes.AETHER, props));
    public static final ItemHolder<DragonEssenceItem> NETHER_DRAGON_ESSENCE = registerItem("nether_dragon_essence", props -> makeDragonEssence(DragonTypes.NETHER, props));
    public static final ItemHolder<DragonEssenceItem> ENDER_DRAGON_ESSENCE = registerItem("ender_dragon_essence", props -> makeDragonEssence(DragonTypes.ENDER, props));
    public static final ItemHolder<DragonEssenceItem> SUNLIGHT_DRAGON_ESSENCE = registerItem("sunlight_dragon_essence", props -> makeDragonEssence(DragonTypes.SUNLIGHT, props));
    public static final ItemHolder<DragonEssenceItem> ENCHANTED_DRAGON_ESSENCE = registerItem("enchanted_dragon_essence", props -> makeDragonEssence(DragonTypes.ENCHANTED, props));
    public static final ItemHolder<DragonEssenceItem> STORM_DRAGON_ESSENCE = registerItem("storm_dragon_essence", props -> makeDragonEssence(DragonTypes.STORM, props));
    public static final ItemHolder<DragonEssenceItem> TERRA_DRAGON_ESSENCE = registerItem("terra_dragon_essence", props -> makeDragonEssence(DragonTypes.TERRA, props));
    public static final ItemHolder<DragonEssenceItem> ZOMBIE_DRAGON_ESSENCE = registerItem("zombie_dragon_essence", props -> makeDragonEssence(DragonTypes.ZOMBIE, props));
    public static final ItemHolder<DragonEssenceItem> MOONLIGHT_DRAGON_ESSENCE = registerItem("moonlight_dragon_essence", props -> makeDragonEssence(DragonTypes.MOONLIGHT, props));
    public static final ItemHolder<DragonEssenceItem> SCULK_DRAGON_ESSENCE = registerItem("sculk_dragon_essence", props -> makeDragonEssence(DragonTypes.SCULK, props.fireResistant()));
    public static final ItemHolder<DragonEssenceItem> SKELETON_DRAGON_ESSENCE = registerItem("skeleton_dragon_essence", props -> makeDragonEssence(DragonTypes.SKELETON, props));
    public static final ItemHolder<DragonEssenceItem> WITHER_DRAGON_ESSENCE = registerItem("wither_dragon_essence", props -> makeDragonEssence(DragonTypes.WITHER, props));
    public static final ItemHolder<DragonEssenceItem> DARK_DRAGON_ESSENCE = registerItem("dark_dragon_essence", props -> makeDragonEssence(DragonTypes.DARK, props));
    // Misc
    public static final ItemHolder<VariationOrbItem> VARIATION_ORB = TOOL_TAB.register("variation_orb", props ->
            new VariationOrbItem(props.stacksTo(16))
    );
    // Dragon Armors
    public static final ItemHolder<Item> COPPER_DRAGON_ARMOR = COMBAT_TAB.register("copper_dragon_armor", props -> makeDragonArmor(DragonArmorMaterials.COPPER, props));
    public static final ItemHolder<Item> IRON_DRAGON_ARMOR = COMBAT_TAB.register("iron_dragon_armor", props -> makeDragonArmor(DragonArmorMaterials.IRON, props));
    public static final ItemHolder<Item> GOLDEN_DRAGON_ARMOR = COMBAT_TAB.register("golden_dragon_armor", props -> makeDragonArmor(DragonArmorMaterials.GOLD, props));
    public static final ItemHolder<Item> EMERALD_DRAGON_ARMOR = COMBAT_TAB.register("emerald_dragon_armor", props -> makeDragonArmor(DragonArmorMaterials.EMERALD, props));
    public static final ItemHolder<Item> DIAMOND_DRAGON_ARMOR = COMBAT_TAB.register("diamond_dragon_armor", props -> makeDragonArmor(DragonArmorMaterials.DIAMOND, props));
    public static final ItemHolder<Item> NETHERITE_DRAGON_ARMOR = COMBAT_TAB.register("netherite_dragon_armor", props -> makeDragonArmor(DragonArmorMaterials.NETHERITE, props.fireResistant()));
    // Dragon Scale Swords
    public static final ItemHolder<DragonScaleSwordItem> AETHER_DRAGON_SCALE_SWORD = COMBAT_TAB.register("aether_dragon_scale_sword", props -> makeDragonScaleSword(DragonTypes.AETHER, props));
    public static final ItemHolder<DragonScaleSwordItem> WATER_DRAGON_SCALE_SWORD = COMBAT_TAB.register("water_dragon_scale_sword", props -> makeDragonScaleSword(DragonTypes.WATER, props));
    public static final ItemHolder<DragonScaleSwordItem> ICE_DRAGON_SCALE_SWORD = COMBAT_TAB.register("ice_dragon_scale_sword", props -> makeDragonScaleSword(DragonTypes.ICE, props));
    public static final ItemHolder<DragonScaleSwordItem> FIRE_DRAGON_SCALE_SWORD = COMBAT_TAB.register("fire_dragon_scale_sword", props -> makeDragonScaleSword(DragonTypes.FIRE, props));
    public static final ItemHolder<DragonScaleSwordItem> FOREST_DRAGON_SCALE_SWORD = COMBAT_TAB.register("forest_dragon_scale_sword", props -> makeDragonScaleSword(DragonTypes.FOREST, props));
    public static final ItemHolder<DragonScaleSwordItem> NETHER_DRAGON_SCALE_SWORD = COMBAT_TAB.register("nether_dragon_scale_sword", props -> makeDragonScaleSword(DragonTypes.NETHER, props));
    public static final ItemHolder<DragonScaleSwordItem> ENDER_DRAGON_SCALE_SWORD = COMBAT_TAB.register("ender_dragon_scale_sword", props -> makeDragonScaleSword(DragonTypes.ENDER, props));
    public static final ItemHolder<DragonScaleSwordItem> ENCHANTED_DRAGON_SCALE_SWORD = COMBAT_TAB.register("enchanted_dragon_scale_sword", props -> makeDragonScaleSword(DragonTypes.ENCHANTED, props));
    public static final ItemHolder<DragonScaleSwordItem> SUNLIGHT_DRAGON_SCALE_SWORD = COMBAT_TAB.register("sunlight_dragon_scale_sword", props -> makeDragonScaleSword(DragonTypes.SUNLIGHT, props));
    public static final ItemHolder<DragonScaleSwordItem> MOONLIGHT_DRAGON_SCALE_SWORD = COMBAT_TAB.register("moonlight_dragon_scale_sword", props -> makeDragonScaleSword(DragonTypes.MOONLIGHT, props));
    public static final ItemHolder<DragonScaleSwordItem> STORM_DRAGON_SCALE_SWORD = COMBAT_TAB.register("storm_dragon_scale_sword", props -> makeDragonScaleSword(DragonTypes.STORM, props));
    public static final ItemHolder<DragonScaleSwordItem> TERRA_DRAGON_SCALE_SWORD = COMBAT_TAB.register("terra_dragon_scale_sword", props -> makeDragonScaleSword(DragonTypes.TERRA, props));
    public static final ItemHolder<DragonScaleSwordItem> ZOMBIE_DRAGON_SCALE_SWORD = COMBAT_TAB.register("zombie_dragon_scale_sword", props -> makeDragonScaleSword(DragonTypes.ZOMBIE, props));
    public static final ItemHolder<DragonScaleSwordItem> SCULK_DRAGON_SCALE_SWORD = COMBAT_TAB.register("sculk_dragon_scale_sword", props -> makeDragonScaleSword(DragonTypes.SCULK, props.fireResistant()));
    public static final ItemHolder<DragonScaleSwordItem> DARK_DRAGON_SCALE_SWORD = COMBAT_TAB.register("dark_dragon_scale_sword", props -> makeDragonScaleSword(DragonTypes.DARK, props));
    // Dragon Scale Tools - Aether
    public static final ItemHolder<DragonScaleShovelItem> AETHER_DRAGON_SCALE_SHOVEL = TOOL_TAB.register("aether_dragon_scale_shovel", props -> makeDragonScaleShovel(DragonTypes.AETHER, props));
    public static final ItemHolder<DragonScalePickaxeItem> AETHER_DRAGON_SCALE_PICKAXE = TOOL_TAB.register("aether_dragon_scale_pickaxe", props -> makeDragonScalePickaxe(DragonTypes.AETHER, props));
    public static final ItemHolder<DragonScaleAxeItem> AETHER_DRAGON_SCALE_AXE = registerAxe("aether_dragon_scale_axe", props -> makeDragonScaleAxe(DragonTypes.AETHER, props));
    public static final ItemHolder<DragonScaleHoeItem> AETHER_DRAGON_SCALE_HOE = TOOL_TAB.register("aether_dragon_scale_hoe", props -> makeDragonScaleHoe(DragonTypes.AETHER, props));
    // Dragon Scale Tools - Water
    public static final ItemHolder<DragonScaleShovelItem> WATER_DRAGON_SCALE_SHOVEL = TOOL_TAB.register("water_dragon_scale_shovel", props -> makeDragonScaleShovel(DragonTypes.WATER, props));
    public static final ItemHolder<DragonScalePickaxeItem> WATER_DRAGON_SCALE_PICKAXE = TOOL_TAB.register("water_dragon_scale_pickaxe", props -> makeDragonScalePickaxe(DragonTypes.WATER, props));
    public static final ItemHolder<DragonScaleAxeItem> WATER_DRAGON_SCALE_AXE = registerAxe("water_dragon_scale_axe", props -> makeDragonScaleAxe(DragonTypes.WATER, props));
    public static final ItemHolder<DragonScaleHoeItem> WATER_DRAGON_SCALE_HOE = TOOL_TAB.register("water_dragon_scale_hoe", props -> makeDragonScaleHoe(DragonTypes.WATER, props));
    // Dragon Scale Tools - Ice
    public static final ItemHolder<DragonScaleShovelItem> ICE_DRAGON_SCALE_SHOVEL = TOOL_TAB.register("ice_dragon_scale_shovel", props -> makeDragonScaleShovel(DragonTypes.ICE, props));
    public static final ItemHolder<DragonScalePickaxeItem> ICE_DRAGON_SCALE_PICKAXE = TOOL_TAB.register("ice_dragon_scale_pickaxe", props -> makeDragonScalePickaxe(DragonTypes.ICE, props));
    public static final ItemHolder<DragonScaleAxeItem> ICE_DRAGON_SCALE_AXE = registerAxe("ice_dragon_scale_axe", props -> makeDragonScaleAxe(DragonTypes.ICE, props));
    public static final ItemHolder<DragonScaleHoeItem> ICE_DRAGON_SCALE_HOE = TOOL_TAB.register("ice_dragon_scale_hoe", props -> makeDragonScaleHoe(DragonTypes.ICE, props));
    // Dragon Scale Tools - Fire
    public static final ItemHolder<DragonScaleShovelItem> FIRE_DRAGON_SCALE_SHOVEL = TOOL_TAB.register("fire_dragon_scale_shovel", props -> makeDragonScaleShovel(DragonTypes.FIRE, props));
    public static final ItemHolder<DragonScalePickaxeItem> FIRE_DRAGON_SCALE_PICKAXE = TOOL_TAB.register("fire_dragon_scale_pickaxe", props -> makeDragonScalePickaxe(DragonTypes.FIRE, props));
    public static final ItemHolder<DragonScaleAxeItem> FIRE_DRAGON_SCALE_AXE = registerAxe("fire_dragon_scale_axe", props -> makeDragonScaleAxe(DragonTypes.FIRE, props));
    public static final ItemHolder<DragonScaleHoeItem> FIRE_DRAGON_SCALE_HOE = TOOL_TAB.register("fire_dragon_scale_hoe", props -> makeDragonScaleHoe(DragonTypes.FIRE, props));
    // Dragon Scale Tools - Forest
    public static final ItemHolder<DragonScaleShovelItem> FOREST_DRAGON_SCALE_SHOVEL = TOOL_TAB.register("forest_dragon_scale_shovel", props -> makeDragonScaleShovel(DragonTypes.FOREST, props));
    public static final ItemHolder<DragonScalePickaxeItem> FOREST_DRAGON_SCALE_PICKAXE = TOOL_TAB.register("forest_dragon_scale_pickaxe", props -> makeDragonScalePickaxe(DragonTypes.FOREST, props));
    public static final ItemHolder<DragonScaleAxeItem> FOREST_DRAGON_SCALE_AXE = registerAxe("forest_dragon_scale_axe", props -> makeDragonScaleAxe(DragonTypes.FOREST, props));
    public static final ItemHolder<DragonScaleHoeItem> FOREST_DRAGON_SCALE_HOE = TOOL_TAB.register("forest_dragon_scale_hoe", props -> makeDragonScaleHoe(DragonTypes.FOREST, props));
    // Dragon Scale Tools - Nether
    public static final ItemHolder<DragonScaleShovelItem> NETHER_DRAGON_SCALE_SHOVEL = TOOL_TAB.register("nether_dragon_scale_shovel", props -> makeDragonScaleShovel(DragonTypes.NETHER, props));
    public static final ItemHolder<DragonScalePickaxeItem> NETHER_DRAGON_SCALE_PICKAXE = TOOL_TAB.register("nether_dragon_scale_pickaxe", props -> makeDragonScalePickaxe(DragonTypes.NETHER, props));
    public static final ItemHolder<DragonScaleAxeItem> NETHER_DRAGON_SCALE_AXE = registerAxe("nether_dragon_scale_axe", props -> makeDragonScaleAxe(DragonTypes.NETHER, 6.0F, -2.9F, props));
    public static final ItemHolder<DragonScaleHoeItem> NETHER_DRAGON_SCALE_HOE = TOOL_TAB.register("nether_dragon_scale_hoe", props -> makeDragonScaleHoe(DragonTypes.NETHER, props));
    // Dragon Scale Tools - Ender
    public static final ItemHolder<DragonScaleShovelItem> ENDER_DRAGON_SCALE_SHOVEL = TOOL_TAB.register("ender_dragon_scale_shovel", props -> makeDragonScaleShovel(DragonTypes.ENDER, props));
    public static final ItemHolder<DragonScalePickaxeItem> ENDER_DRAGON_SCALE_PICKAXE = TOOL_TAB.register("ender_dragon_scale_pickaxe", props -> makeDragonScalePickaxe(DragonTypes.ENDER, props));
    public static final ItemHolder<DragonScaleAxeItem> ENDER_DRAGON_SCALE_AXE = registerAxe("ender_dragon_scale_axe", props -> makeDragonScaleAxe(DragonTypes.ENDER, 6.0F, -2.9F, props));
    public static final ItemHolder<DragonScaleHoeItem> ENDER_DRAGON_SCALE_HOE = TOOL_TAB.register("ender_dragon_scale_hoe", props -> makeDragonScaleHoe(DragonTypes.ENDER, props));
    // Dragon Scale Tools - Enchanted
    public static final ItemHolder<DragonScaleShovelItem> ENCHANTED_DRAGON_SCALE_SHOVEL = TOOL_TAB.register("enchanted_dragon_scale_shovel", props -> makeDragonScaleShovel(DragonTypes.ENCHANTED, props));
    public static final ItemHolder<DragonScalePickaxeItem> ENCHANTED_DRAGON_SCALE_PICKAXE = TOOL_TAB.register("enchanted_dragon_scale_pickaxe", props -> makeDragonScalePickaxe(DragonTypes.ENCHANTED, props));
    public static final ItemHolder<DragonScaleAxeItem> ENCHANTED_DRAGON_SCALE_AXE = registerAxe("enchanted_dragon_scale_axe", props -> makeDragonScaleAxe(DragonTypes.ENCHANTED, props));
    public static final ItemHolder<DragonScaleHoeItem> ENCHANTED_DRAGON_SCALE_HOE = TOOL_TAB.register("enchanted_dragon_scale_hoe", props -> makeDragonScaleHoe(DragonTypes.ENCHANTED, props));
    // Dragon Scale Tools - Sunlight
    public static final ItemHolder<DragonScaleShovelItem> SUNLIGHT_DRAGON_SCALE_SHOVEL = TOOL_TAB.register("sunlight_dragon_scale_shovel", props -> makeDragonScaleShovel(DragonTypes.SUNLIGHT, props));
    public static final ItemHolder<DragonScalePickaxeItem> SUNLIGHT_DRAGON_SCALE_PICKAXE = TOOL_TAB.register("sunlight_dragon_scale_pickaxe", props -> makeDragonScalePickaxe(DragonTypes.SUNLIGHT, props));
    public static final ItemHolder<DragonScaleAxeItem> SUNLIGHT_DRAGON_SCALE_AXE = registerAxe("sunlight_dragon_scale_axe", props -> makeDragonScaleAxe(DragonTypes.SUNLIGHT, props));
    public static final ItemHolder<DragonScaleHoeItem> SUNLIGHT_DRAGON_SCALE_HOE = TOOL_TAB.register("sunlight_dragon_scale_hoe", props -> makeDragonScaleHoe(DragonTypes.SUNLIGHT, props));
    // Dragon Scale Tools - Moonlight
    public static final ItemHolder<DragonScaleShovelItem> MOONLIGHT_DRAGON_SCALE_SHOVEL = TOOL_TAB.register("moonlight_dragon_scale_shovel", props -> makeDragonScaleShovel(DragonTypes.MOONLIGHT, props));
    public static final ItemHolder<DragonScalePickaxeItem> MOONLIGHT_DRAGON_SCALE_PICKAXE = TOOL_TAB.register("moonlight_dragon_scale_pickaxe", props -> makeDragonScalePickaxe(DragonTypes.MOONLIGHT, props));
    public static final ItemHolder<DragonScaleAxeItem> MOONLIGHT_DRAGON_SCALE_AXE = registerAxe("moonlight_dragon_scale_axe", props -> makeDragonScaleAxe(DragonTypes.MOONLIGHT, props));
    public static final ItemHolder<DragonScaleHoeItem> MOONLIGHT_DRAGON_SCALE_HOE = TOOL_TAB.register("moonlight_dragon_scale_hoe", props -> makeDragonScaleHoe(DragonTypes.MOONLIGHT, props));
    // Dragon Scale Tools - Storm
    public static final ItemHolder<DragonScaleShovelItem> STORM_DRAGON_SCALE_SHOVEL = TOOL_TAB.register("storm_dragon_scale_shovel", props -> makeDragonScaleShovel(DragonTypes.STORM, props));
    public static final ItemHolder<DragonScalePickaxeItem> STORM_DRAGON_SCALE_PICKAXE = TOOL_TAB.register("storm_dragon_scale_pickaxe", props -> makeDragonScalePickaxe(DragonTypes.STORM, props));
    public static final ItemHolder<DragonScaleAxeItem> STORM_DRAGON_SCALE_AXE = registerAxe("storm_dragon_scale_axe", props -> makeDragonScaleAxe(DragonTypes.STORM, props));
    public static final ItemHolder<DragonScaleHoeItem> STORM_DRAGON_SCALE_HOE = TOOL_TAB.register("storm_dragon_scale_hoe", props -> makeDragonScaleHoe(DragonTypes.STORM, props));
    // Dragon Scale Tools - Terra
    public static final ItemHolder<DragonScaleShovelItem> TERRA_DRAGON_SCALE_SHOVEL = TOOL_TAB.register("terra_dragon_scale_shovel", props -> makeDragonScaleShovel(DragonTypes.TERRA, props));
    public static final ItemHolder<DragonScalePickaxeItem> TERRA_DRAGON_SCALE_PICKAXE = TOOL_TAB.register("terra_dragon_scale_pickaxe", props -> makeDragonScalePickaxe(DragonTypes.TERRA, props));
    public static final ItemHolder<DragonScaleAxeItem> TERRA_DRAGON_SCALE_AXE = registerAxe("terra_dragon_scale_axe", props -> makeDragonScaleAxe(DragonTypes.TERRA, props));
    public static final ItemHolder<DragonScaleHoeItem> TERRA_DRAGON_SCALE_HOE = TOOL_TAB.register("terra_dragon_scale_hoe", props -> makeDragonScaleHoe(DragonTypes.TERRA, props));
    // Dragon Scale Tools - Zombie
    public static final ItemHolder<DragonScaleShovelItem> ZOMBIE_DRAGON_SCALE_SHOVEL = TOOL_TAB.register("zombie_dragon_scale_shovel", props -> makeDragonScaleShovel(DragonTypes.ZOMBIE, props));
    public static final ItemHolder<DragonScalePickaxeItem> ZOMBIE_DRAGON_SCALE_PICKAXE = TOOL_TAB.register("zombie_dragon_scale_pickaxe", props -> makeDragonScalePickaxe(DragonTypes.ZOMBIE, props));
    public static final ItemHolder<DragonScaleAxeItem> ZOMBIE_DRAGON_SCALE_AXE = registerAxe("zombie_dragon_scale_axe", props -> makeDragonScaleAxe(DragonTypes.ZOMBIE, props));
    public static final ItemHolder<DragonScaleHoeItem> ZOMBIE_DRAGON_SCALE_HOE = TOOL_TAB.register("zombie_dragon_scale_hoe", props -> makeDragonScaleHoe(DragonTypes.ZOMBIE, props));
    // Dragon Scale Tools - Sculk
    public static final ItemHolder<DragonScaleShovelItem> SCULK_DRAGON_SCALE_SHOVEL = TOOL_TAB.register("sculk_dragon_scale_shovel", props -> makeDragonScaleShovel(DragonTypes.SCULK, props.fireResistant()));
    public static final ItemHolder<DragonScalePickaxeItem> SCULK_DRAGON_SCALE_PICKAXE = TOOL_TAB.register("sculk_dragon_scale_pickaxe", props -> makeDragonScalePickaxe(DragonTypes.SCULK, props.fireResistant()));
    public static final ItemHolder<DragonScaleAxeItem> SCULK_DRAGON_SCALE_AXE = registerAxe("sculk_dragon_scale_axe", props -> makeDragonScaleAxe(DragonTypes.SCULK, props.fireResistant()));
    public static final ItemHolder<DragonScaleHoeItem> SCULK_DRAGON_SCALE_HOE = TOOL_TAB.register("sculk_dragon_scale_hoe", props -> makeDragonScaleHoe(DragonTypes.SCULK, props.fireResistant()));
    // Dragon Scale Tools - Dark
    public static final ItemHolder<DragonScaleShovelItem> DARK_DRAGON_SCALE_SHOVEL = TOOL_TAB.register("dark_dragon_scale_shovel", props -> makeDragonScaleShovel(DragonTypes.DARK, props));
    public static final ItemHolder<DragonScalePickaxeItem> DARK_DRAGON_SCALE_PICKAXE = TOOL_TAB.register("dark_dragon_scale_pickaxe", props -> makeDragonScalePickaxe(DragonTypes.DARK, props));
    public static final ItemHolder<DragonScaleAxeItem> DARK_DRAGON_SCALE_AXE = registerAxe("dark_dragon_scale_axe", props -> makeDragonScaleAxe(DragonTypes.DARK, props));
    public static final ItemHolder<DragonScaleHoeItem> DARK_DRAGON_SCALE_HOE = TOOL_TAB.register("dark_dragon_scale_hoe", props -> makeDragonScaleHoe(DragonTypes.DARK, props));
    // Dragon Scale Bows
    public static final ItemHolder<DragonScaleBowItem> AETHER_DRAGON_SCALE_BOW = COMBAT_TAB.register("aether_dragon_scale_bow", props -> makeDragonScaleBow(DragonTypes.AETHER, props));
    public static final ItemHolder<DragonScaleBowItem> WATER_DRAGON_SCALE_BOW = COMBAT_TAB.register("water_dragon_scale_bow", props -> makeDragonScaleBow(DragonTypes.WATER, props));
    public static final ItemHolder<DragonScaleBowItem> ICE_DRAGON_SCALE_BOW = COMBAT_TAB.register("ice_dragon_scale_bow", props -> makeDragonScaleBow(DragonTypes.ICE, props));
    public static final ItemHolder<DragonScaleBowItem> FIRE_DRAGON_SCALE_BOW = COMBAT_TAB.register("fire_dragon_scale_bow", props -> makeDragonScaleBow(DragonTypes.FIRE, props));
    public static final ItemHolder<DragonScaleBowItem> FOREST_DRAGON_SCALE_BOW = COMBAT_TAB.register("forest_dragon_scale_bow", props -> makeDragonScaleBow(DragonTypes.FOREST, props));
    public static final ItemHolder<DragonScaleBowItem> NETHER_DRAGON_SCALE_BOW = COMBAT_TAB.register("nether_dragon_scale_bow", props -> makeDragonScaleBow(DragonTypes.NETHER, props));
    public static final ItemHolder<DragonScaleBowItem> ENDER_DRAGON_SCALE_BOW = COMBAT_TAB.register("ender_dragon_scale_bow", props -> makeDragonScaleBow(DragonTypes.ENDER, props));
    public static final ItemHolder<DragonScaleBowItem> ENCHANTED_DRAGON_SCALE_BOW = COMBAT_TAB.register("enchanted_dragon_scale_bow", props -> makeDragonScaleBow(DragonTypes.ENCHANTED, props));
    public static final ItemHolder<DragonScaleBowItem> SUNLIGHT_DRAGON_SCALE_BOW = COMBAT_TAB.register("sunlight_dragon_scale_bow", props -> makeDragonScaleBow(DragonTypes.SUNLIGHT, props));
    public static final ItemHolder<DragonScaleBowItem> MOONLIGHT_DRAGON_SCALE_BOW = COMBAT_TAB.register("moonlight_dragon_scale_bow", props -> makeDragonScaleBow(DragonTypes.MOONLIGHT, props));
    public static final ItemHolder<DragonScaleBowItem> STORM_DRAGON_SCALE_BOW = COMBAT_TAB.register("storm_dragon_scale_bow", props -> makeDragonScaleBow(DragonTypes.STORM, props));
    public static final ItemHolder<DragonScaleBowItem> TERRA_DRAGON_SCALE_BOW = COMBAT_TAB.register("terra_dragon_scale_bow", props -> makeDragonScaleBow(DragonTypes.TERRA, props));
    public static final ItemHolder<DragonScaleBowItem> ZOMBIE_DRAGON_SCALE_BOW = COMBAT_TAB.register("zombie_dragon_scale_bow", props -> makeDragonScaleBow(DragonTypes.ZOMBIE, props));
    public static final ItemHolder<DragonScaleBowItem> SCULK_DRAGON_SCALE_BOW = COMBAT_TAB.register("sculk_dragon_scale_bow", props -> makeDragonScaleBow(DragonTypes.SCULK, props.fireResistant()));
    public static final ItemHolder<DragonScaleBowItem> DARK_DRAGON_SCALE_BOW = COMBAT_TAB.register("dark_dragon_scale_bow", props -> makeDragonScaleBow(DragonTypes.DARK, props));
    // Dragon Scale Shields
    public static final ItemHolder<DragonScaleShieldItem> AETHER_DRAGON_SCALE_SHIELD = COMBAT_TAB.register("aether_dragon_scale_shield", props -> makeDragonScaleShield(DragonTypes.AETHER, props));
    public static final ItemHolder<DragonScaleShieldItem> WATER_DRAGON_SCALE_SHIELD = COMBAT_TAB.register("water_dragon_scale_shield", props -> makeDragonScaleShield(DragonTypes.WATER, props));
    public static final ItemHolder<DragonScaleShieldItem> ICE_DRAGON_SCALE_SHIELD = COMBAT_TAB.register("ice_dragon_scale_shield", props -> makeDragonScaleShield(DragonTypes.ICE, props));
    public static final ItemHolder<DragonScaleShieldItem> FIRE_DRAGON_SCALE_SHIELD = COMBAT_TAB.register("fire_dragon_scale_shield", props -> makeDragonScaleShield(DragonTypes.FIRE, props));
    public static final ItemHolder<DragonScaleShieldItem> FOREST_DRAGON_SCALE_SHIELD = COMBAT_TAB.register("forest_dragon_scale_shield", props -> makeDragonScaleShield(DragonTypes.FOREST, props));
    public static final ItemHolder<DragonScaleShieldItem> NETHER_DRAGON_SCALE_SHIELD = COMBAT_TAB.register("nether_dragon_scale_shield", props -> makeDragonScaleShield(DragonTypes.NETHER, props));
    public static final ItemHolder<DragonScaleShieldItem> ENDER_DRAGON_SCALE_SHIELD = COMBAT_TAB.register("ender_dragon_scale_shield", props -> makeDragonScaleShield(DragonTypes.ENDER, props));
    public static final ItemHolder<DragonScaleShieldItem> ENCHANTED_DRAGON_SCALE_SHIELD = COMBAT_TAB.register("enchanted_dragon_scale_shield", props ->
            makeDragonScaleShield(DragonTypes.ENCHANTED, props.enchantable(DragonTypes.ENCHANTED.material.enchantmentValue()))
    );
    public static final ItemHolder<DragonScaleShieldItem> SUNLIGHT_DRAGON_SCALE_SHIELD = COMBAT_TAB.register("sunlight_dragon_scale_shield", props -> makeDragonScaleShield(DragonTypes.SUNLIGHT, props));
    public static final ItemHolder<DragonScaleShieldItem> MOONLIGHT_DRAGON_SCALE_SHIELD = COMBAT_TAB.register("moonlight_dragon_scale_shield", props -> makeDragonScaleShield(DragonTypes.MOONLIGHT, props));
    public static final ItemHolder<DragonScaleShieldItem> STORM_DRAGON_SCALE_SHIELD = COMBAT_TAB.register("storm_dragon_scale_shield", props -> makeDragonScaleShield(DragonTypes.STORM, props));
    public static final ItemHolder<DragonScaleShieldItem> TERRA_DRAGON_SCALE_SHIELD = COMBAT_TAB.register("terra_dragon_scale_shield", props -> makeDragonScaleShield(DragonTypes.TERRA, props));
    public static final ItemHolder<DragonScaleShieldItem> ZOMBIE_DRAGON_SCALE_SHIELD = COMBAT_TAB.register("zombie_dragon_scale_shield", props -> makeDragonScaleShield(DragonTypes.ZOMBIE, props));
    public static final ItemHolder<DragonScaleShieldItem> SCULK_DRAGON_SCALE_SHIELD = COMBAT_TAB.register("sculk_dragon_scale_shield", props -> makeDragonScaleShield(DragonTypes.SCULK, props.fireResistant()));
    public static final ItemHolder<DragonScaleShieldItem> DARK_DRAGON_SCALE_SHIELD = COMBAT_TAB.register("dark_dragon_scale_shield", props -> makeDragonScaleShield(DragonTypes.DARK, props));
    // Dragon Scale Armors
    public static final DragonScaleArmorSuit AETHER_DRAGON_SCALE_ARMORS = makeSuit(
            DragonTypes.AETHER,
            DMArmorEffects.AETHER,
            COMBAT_TAB,
            "aether_dragon_scale_helmet",
            "aether_dragon_scale_chestplate",
            "aether_dragon_scale_leggings",
            "aether_dragon_scale_boots",
            DMItems::makeDragonScaleArmor
    );
    public static final DragonScaleArmorSuit WATER_DRAGON_SCALE_ARMORS = makeSuit(
            DragonTypes.WATER,
            DMArmorEffects.WATER,
            COMBAT_TAB,
            "water_dragon_scale_helmet",
            "water_dragon_scale_chestplate",
            "water_dragon_scale_leggings",
            "water_dragon_scale_boots",
            DMItems::makeDragonScaleArmor
    );
    public static final DragonScaleArmorSuit ICE_DRAGON_SCALE_ARMORS = makeSuit(
            DragonTypes.ICE,
            DMArmorEffects.ICE,
            COMBAT_TAB,
            "ice_dragon_scale_helmet",
            "ice_dragon_scale_chestplate",
            "ice_dragon_scale_leggings",
            "ice_dragon_scale_boots",
            DMItems::makeDragonScaleArmor
    );
    public static final DragonScaleArmorSuit FIRE_DRAGON_SCALE_ARMORS = makeSuit(
            DragonTypes.FIRE,
            DMArmorEffects.FIRE,
            COMBAT_TAB,
            "fire_dragon_scale_helmet",
            "fire_dragon_scale_chestplate",
            "fire_dragon_scale_leggings",
            "fire_dragon_scale_boots",
            DMItems::makeDragonScaleArmor
    );
    public static final DragonScaleArmorSuit FOREST_DRAGON_SCALE_ARMORS = makeSuit(
            DragonTypes.FOREST,
            DMArmorEffects.FOREST,
            COMBAT_TAB,
            "forest_dragon_scale_helmet",
            "forest_dragon_scale_chestplate",
            "forest_dragon_scale_leggings",
            "forest_dragon_scale_boots",
            DMItems::makeDragonScaleArmor
    );
    public static final DragonScaleArmorSuit NETHER_DRAGON_SCALE_ARMORS = makeSuit(
            DragonTypes.NETHER,
            DMArmorEffects.NETHER,
            COMBAT_TAB,
            "nether_dragon_scale_helmet",
            "nether_dragon_scale_chestplate",
            "nether_dragon_scale_leggings",
            "nether_dragon_scale_boots",
            DMItems::makeDragonScaleArmor
    );
    public static final DragonScaleArmorSuit ENDER_DRAGON_SCALE_ARMORS = makeSuit(
            DragonTypes.ENDER,
            DMArmorEffects.ENDER,
            COMBAT_TAB,
            "ender_dragon_scale_helmet",
            "ender_dragon_scale_chestplate",
            "ender_dragon_scale_leggings",
            "ender_dragon_scale_boots",
            DMItems::makeDragonScaleArmor
    );
    public static final DragonScaleArmorSuit ENCHANTED_DRAGON_SCALE_ARMORS = makeSuit(
            DragonTypes.ENCHANTED,
            DMArmorEffects.ENCHANTED,
            COMBAT_TAB,
            "enchanted_dragon_scale_helmet",
            "enchanted_dragon_scale_chestplate",
            "enchanted_dragon_scale_leggings",
            "enchanted_dragon_scale_boots",
            DMItems::makeDragonScaleArmor
    );
    public static final DragonScaleArmorSuit SUNLIGHT_DRAGON_SCALE_ARMORS = makeSuit(
            DragonTypes.SUNLIGHT,
            DMArmorEffects.SUNLIGHT,
            COMBAT_TAB,
            "sunlight_dragon_scale_helmet",
            "sunlight_dragon_scale_chestplate",
            "sunlight_dragon_scale_leggings",
            "sunlight_dragon_scale_boots",
            DMItems::makeDragonScaleArmor
    );
    public static final DragonScaleArmorSuit MOONLIGHT_DRAGON_SCALE_ARMORS = makeSuit(
            DragonTypes.MOONLIGHT,
            DMArmorEffects.MOONLIGHT,
            COMBAT_TAB,
            "moonlight_dragon_scale_helmet",
            "moonlight_dragon_scale_chestplate",
            "moonlight_dragon_scale_leggings",
            "moonlight_dragon_scale_boots",
            DMItems::makeDragonScaleArmor
    );
    public static final DragonScaleArmorSuit STORM_DRAGON_SCALE_ARMORS = makeSuit(
            DragonTypes.STORM,
            DMArmorEffects.STORM,
            COMBAT_TAB,
            "storm_dragon_scale_helmet",
            "storm_dragon_scale_chestplate",
            "storm_dragon_scale_leggings",
            "storm_dragon_scale_boots",
            DMItems::makeDragonScaleArmor
    );
    public static final DragonScaleArmorSuit TERRA_DRAGON_SCALE_ARMORS = makeSuit(
            DragonTypes.TERRA,
            DMArmorEffects.TERRA,
            COMBAT_TAB,
            "terra_dragon_scale_helmet",
            "terra_dragon_scale_chestplate",
            "terra_dragon_scale_leggings",
            "terra_dragon_scale_boots",
            DMItems::makeDragonScaleArmor
    );
    public static final DragonScaleArmorSuit ZOMBIE_DRAGON_SCALE_ARMORS = makeSuit(
            DragonTypes.ZOMBIE,
            DMArmorEffects.ZOMBIE,
            COMBAT_TAB,
            "zombie_dragon_scale_helmet",
            "zombie_dragon_scale_chestplate",
            "zombie_dragon_scale_leggings",
            "zombie_dragon_scale_boots",
            DMItems::makeDragonScaleArmor
    );
    public static final DragonScaleArmorSuit SCULK_DRAGON_SCALE_ARMORS = makeSuit(
            DragonTypes.SCULK,
            DMArmorEffects.SCULK,
            COMBAT_TAB,
            "sculk_dragon_scale_helmet",
            "sculk_dragon_scale_chestplate",
            "sculk_dragon_scale_leggings",
            "sculk_dragon_scale_boots",
            (suit, slot, props) -> makeDragonScaleArmor(suit, slot, props.fireResistant())
    );
    public static final DragonScaleArmorSuit DARK_DRAGON_SCALE_ARMORS = makeSuit(
            DragonTypes.DARK,
            DMArmorEffects.DARK,
            COMBAT_TAB,
            "dark_dragon_scale_helmet",
            "dark_dragon_scale_chestplate",
            "dark_dragon_scale_leggings",
            "dark_dragon_scale_boots",
            DMItems::makeDragonScaleArmor
    );
    // Dragon Spawn Eggs
    public static final ItemHolder<DragonSpawnEggItem> AETHER_DRAGON_SPAWN_EGG = DRAGON_SPAWN_EGGS.register("aether_dragon_spawn_egg", props ->
            makeDragonSpawnEgg(DragonTypes.AETHER, props)
    );
    public static final ItemHolder<DragonSpawnEggItem> DARK_DRAGON_SPAWN_EGG = DRAGON_SPAWN_EGGS.register("dark_dragon_spawn_egg", props ->
            makeDragonSpawnEgg(DragonTypes.DARK, props)
    );
    public static final ItemHolder<DragonSpawnEggItem> ENCHANTED_DRAGON_SPAWN_EGG = DRAGON_SPAWN_EGGS.register("enchanted_dragon_spawn_egg", props ->
            makeDragonSpawnEgg(DragonTypes.ENCHANTED, props)
    );
    public static final ItemHolder<DragonSpawnEggItem> ENDER_DRAGON_SPAWN_EGG = DRAGON_SPAWN_EGGS.register("ender_dragon_spawn_egg", props ->
            makeDragonSpawnEgg(DragonTypes.ENDER, props)
    );
    public static final ItemHolder<DragonSpawnEggItem> FIRE_DRAGON_SPAWN_EGG = DRAGON_SPAWN_EGGS.register("fire_dragon_spawn_egg", props ->
            makeDragonSpawnEgg(DragonTypes.FIRE, props)
    );
    public static final ItemHolder<DragonSpawnEggItem> FOREST_DRAGON_SPAWN_EGG = DRAGON_SPAWN_EGGS.register("forest_dragon_spawn_egg", props ->
            makeDragonSpawnEgg(DragonTypes.FOREST, props)
    );
    public static final ItemHolder<DragonSpawnEggItem> ICE_DRAGON_SPAWN_EGG = DRAGON_SPAWN_EGGS.register("ice_dragon_spawn_egg", props ->
            makeDragonSpawnEgg(DragonTypes.ICE, props)
    );
    public static final ItemHolder<DragonSpawnEggItem> MOONLIGHT_DRAGON_SPAWN_EGG = DRAGON_SPAWN_EGGS.register("moonlight_dragon_spawn_egg", props ->
            makeDragonSpawnEgg(DragonTypes.MOONLIGHT, props)
    );
    public static final ItemHolder<DragonSpawnEggItem> NETHER_DRAGON_SPAWN_EGG = DRAGON_SPAWN_EGGS.register("nether_dragon_spawn_egg", props ->
            makeDragonSpawnEgg(DragonTypes.NETHER, props)
    );
    public static final ItemHolder<DragonSpawnEggItem> SCULK_DRAGON_SPAWN_EGG = DRAGON_SPAWN_EGGS.register("sculk_dragon_spawn_egg", props ->
            makeDragonSpawnEgg(DragonTypes.SCULK, props)
    );
    public static final ItemHolder<DragonSpawnEggItem> SKELETON_DRAGON_SPAWN_EGG = DRAGON_SPAWN_EGGS.register("skeleton_dragon_spawn_egg", props ->
            makeDragonSpawnEgg(DragonTypes.SKELETON, props)
    );
    public static final ItemHolder<DragonSpawnEggItem> STORM_DRAGON_SPAWN_EGG = DRAGON_SPAWN_EGGS.register("storm_dragon_spawn_egg", props ->
            makeDragonSpawnEgg(DragonTypes.STORM, props)
    );
    public static final ItemHolder<DragonSpawnEggItem> SUNLIGHT_DRAGON_SPAWN_EGG = DRAGON_SPAWN_EGGS.register("sunlight_dragon_spawn_egg", props ->
            makeDragonSpawnEgg(DragonTypes.SUNLIGHT, props)
    );
    public static final ItemHolder<DragonSpawnEggItem> TERRA_DRAGON_SPAWN_EGG = DRAGON_SPAWN_EGGS.register("terra_dragon_spawn_egg", props ->
            makeDragonSpawnEgg(DragonTypes.TERRA, props)
    );
    public static final ItemHolder<DragonSpawnEggItem> WATER_DRAGON_SPAWN_EGG = DRAGON_SPAWN_EGGS.register("water_dragon_spawn_egg", props ->
            makeDragonSpawnEgg(DragonTypes.WATER, props)
    );
    public static final ItemHolder<DragonSpawnEggItem> WITHER_DRAGON_SPAWN_EGG = DRAGON_SPAWN_EGGS.register("wither_dragon_spawn_egg", props ->
            makeDragonSpawnEgg(DragonTypes.WITHER, props)
    );
    public static final ItemHolder<DragonSpawnEggItem> ZOMBIE_DRAGON_SPAWN_EGG = DRAGON_SPAWN_EGGS.register("zombie_dragon_spawn_egg", props ->
            makeDragonSpawnEgg(DragonTypes.ZOMBIE, props)
    );

    static ItemHolder<DragonScaleAxeItem> registerAxe(String name, Function<Properties, DragonScaleAxeItem> factory) {
        var holder = TOOL_TAB.register(name, factory);
        COMBAT_TAB.add(holder);
        return holder;
    }

    static DragonAmuletItem makeDragonAmulet(DragonType type, Properties props) {
        var item = new DragonAmuletItem(type, props.overrideDescription(AmuletItem.TRANSLATION_KEY));
        type.bindInstance(DragonAmuletItem.class, item);
        return item;
    }

    static Item makeDragonArmor(ArmorMaterial material, Properties props) {
        var builder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(DRAGON_ARMOR_MODIFIER_NAME, material.defense().getOrDefault(ArmorType.BODY, 0), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.BODY)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(DRAGON_ARMOR_MODIFIER_NAME, material.toughness(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.BODY);
        if (material.knockbackResistance() > 0.0F) {
            builder.add(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(DRAGON_ARMOR_MODIFIER_NAME, material.knockbackResistance(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.BODY);
        }
        return new Item(props.component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.BODY)
                .setEquipSound(SoundEvents.HORSE_ARMOR)
                .setAsset(material.assetId())
                .setAllowedEntities(DMEntities.TAMEABLE_DRAGON.get())
                .setDamageOnHurt(false)
                .build()
        ).stacksTo(1).attributes(builder.build()));
    }

    static DragonEssenceItem makeDragonEssence(DragonType type, Properties props) {
        var item = new DragonEssenceItem(type, props.overrideDescription(DragonEssenceItem.TRANSLATION_KEY));
        type.bindInstance(DragonEssenceItem.class, item);
        return item;
    }

    static DragonScaleAxeItem makeDragonScaleAxe(DragonType type, float damage, float speed, Properties props) {
        var item = new DragonScaleAxeItem(type, damage, speed, props.overrideDescription(DragonScaleAxeItem.TRANSLATION_KEY));
        type.bindInstance(DragonScaleAxeItem.class, item);
        return item;
    }

    static DragonScaleAxeItem makeDragonScaleAxe(DragonType type, Properties props) {
        return makeDragonScaleAxe(type, 5.0F, -2.8F, props);
    }

    static DragonScaleBowItem makeDragonScaleBow(DragonType type, Properties props) {
        var item = new DragonScaleBowItem(type, props.overrideDescription(DragonScaleBowItem.TRANSLATION_KEY));
        type.bindInstance(DragonScaleBowItem.class, item);
        return item;
    }

    static DragonScaleHoeItem makeDragonScaleHoe(DragonType type, Properties props) {
        float damage = type.tier.attackDamageBonus();
        var item = new DragonScaleHoeItem(type, -damage, damage - 3.0F, props.overrideDescription(DragonScaleHoeItem.TRANSLATION_KEY));
        type.bindInstance(DragonScaleHoeItem.class, item);
        return item;
    }

    static DragonScalePickaxeItem makeDragonScalePickaxe(DragonType type, Properties props) {
        var item = new DragonScalePickaxeItem(type, 1.0F, -2.8F, props.overrideDescription(DragonScalePickaxeItem.TRANSLATION_KEY));
        type.bindInstance(DragonScalePickaxeItem.class, item);
        return item;
    }

    static DragonScaleArmorItem makeDragonScaleArmor(DragonScaleArmorSuit suit, ArmorType slot, Properties props) {
        if (suit.effect != null) {
            props.component(DMDataComponents.ARMOR_EFFECT_SOURCE, suit);
        }
        switch (slot) {
            case HELMET -> props.overrideDescription(DragonScaleArmorSuit.HELMET_TRANSLATION_KEY);
            case CHESTPLATE -> props.overrideDescription(DragonScaleArmorSuit.CHESTPLATE_TRANSLATION_KEY);
            case LEGGINGS -> props.overrideDescription(DragonScaleArmorSuit.LEGGINGS_TRANSLATION_KEY);
            case BOOTS -> props.overrideDescription(DragonScaleArmorSuit.BOOTS_TRANSLATION_KEY);
        }
        return new DragonScaleArmorItem(suit.type, suit.effect, slot, props);
    }

    static DragonScalesItem makeDragonScales(DragonType type, Properties props) {
        var item = new DragonScalesItem(type, props.overrideDescription(DragonScalesItem.TRANSLATION_KEY));
        type.bindInstance(DragonScalesItem.class, item);
        return item;
    }

    static DragonScaleShieldItem makeDragonScaleShield(DragonType type, Properties props) {
        var item = new DragonScaleShieldItem(type, props.overrideDescription(DragonScaleShieldItem.TRANSLATION_KEY));
        type.bindInstance(DragonScaleShieldItem.class, item);
        return item;
    }

    static DragonScaleShovelItem makeDragonScaleShovel(DragonType type, Properties props) {
        var item = new DragonScaleShovelItem(type, 1.5F, -3.0F, props.overrideDescription(DragonScaleShovelItem.TRANSLATION_KEY));
        type.bindInstance(DragonScaleShovelItem.class, item);
        return item;
    }

    static DragonScaleSwordItem makeDragonScaleSword(DragonType type, Properties props) {
        var item = new DragonScaleSwordItem(type, 3, -2.0F, props.overrideDescription(DragonScaleSwordItem.TRANSLATION_KEY));
        type.bindInstance(DragonScaleSwordItem.class, item);
        return item;
    }

    static DragonSpawnEggItem makeDragonSpawnEgg(DragonType type, Properties props) {
        var item = new DragonSpawnEggItem(type, props.overrideDescription(DragonSpawnEggItem.TRANSLATION_KEY));
        type.bindInstance(DragonSpawnEggItem.class, item);
        return item;
    }

    static BlockItem makeDragonEggBlock(HatchableDragonEggBlock block, Properties props) {
        return new BlockItem(block, props.component(DMDataComponents.DRAGON_TYPE, block.type).overrideDescription(HatchableDragonEggBlock.TRANSLATION_KEY));
    }

    static BlockItem makeDragonScaleBlock(DragonScaleBlock block, Properties props) {
        return new BlockItem(block, props.component(DMDataComponents.DRAGON_TYPE, block.type).overrideDescription(DragonScaleBlock.TRANSLATION_KEY));
    }

    /// To finish all common stuff about item registries (and trigger the static initialization by the way).
    public static void setup() {
        DRAGON_SPAWN_EGGS.items.forEach(holder -> {
            var item = holder.asItem();
            if (item instanceof DragonSpawnEggItem) {
                if (DispenserBlock.DISPENSER_REGISTRY.containsKey(item)) return;
                DispenserBlock.registerBehavior(item, ((DragonSpawnEggItem) item).createDispenseBehavior());
            }
        });
        DispenserBlock.registerBehavior(DIAMOND_SHEARS, TieredShearsItem.DISPENSE_ITEM_BEHAVIOR);
        DispenserBlock.registerBehavior(NETHERITE_SHEARS, TieredShearsItem.DISPENSE_ITEM_BEHAVIOR);
    }

    /// To trigger the static initialization only.
    public static void initStatics() {}
}