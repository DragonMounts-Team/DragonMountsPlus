package net.dragonmounts.neo.data;

import net.dragonmounts.neo.common.init.DMBlocks;
import net.dragonmounts.neo.common.init.DMItems;
import net.dragonmounts.neo.common.init.DragonVariants;
import net.dragonmounts.neo.common.item.*;
import net.dragonmounts.neo.common.tag.DMBlockTags;
import net.dragonmounts.neo.common.tag.DMItemTags;
import net.dragonmounts.neo.compat.registry.DragonScaleArmorSuit;
import net.dragonmounts.neo.compat.registry.DragonType;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class DMItemTagProvider extends FabricTagProvider.ItemTagProvider {
    public DMItemTagProvider(
            FabricDataOutput output,
            CompletableFuture<HolderLookup.Provider> provider,
            FabricTagProvider.BlockTagProvider block
    ) {
        super(output, provider, block);
    }

    protected TagAppender<Item> addToParent(FabricTagBuilder parent, TagKey<Item> child) {
        parent.addTag(child);
        return this.tag(child);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        var scales = this.getOrCreateTagBuilder(DMItemTags.DRAGON_SCALES);
        this.addToParent(scales, DMItemTags.AETHER_DRAGON_SCALES).add(DMItems.AETHER_DRAGON_SCALES.key);
        this.addToParent(scales, DMItemTags.ENCHANTED_DRAGON_SCALES).add(DMItems.ENCHANTED_DRAGON_SCALES.key);
        this.addToParent(scales, DMItemTags.ENDER_DRAGON_SCALES).add(DMItems.ENDER_DRAGON_SCALES.key);
        this.addToParent(scales, DMItemTags.FIRE_DRAGON_SCALES).add(DMItems.FIRE_DRAGON_SCALES.key);
        this.addToParent(scales, DMItemTags.FOREST_DRAGON_SCALES).add(DMItems.FOREST_DRAGON_SCALES.key);
        this.addToParent(scales, DMItemTags.ICE_DRAGON_SCALES).add(DMItems.ICE_DRAGON_SCALES.key);
        this.addToParent(scales, DMItemTags.MOONLIGHT_DRAGON_SCALES).add(DMItems.MOONLIGHT_DRAGON_SCALES.key);
        this.addToParent(scales, DMItemTags.NETHER_DRAGON_SCALES).add(DMItems.NETHER_DRAGON_SCALES.key);
        this.addToParent(scales, DMItemTags.SCULK_DRAGON_SCALES).add(DMItems.SCULK_DRAGON_SCALES.key);
        this.addToParent(scales, DMItemTags.STORM_DRAGON_SCALES).add(DMItems.STORM_DRAGON_SCALES.key);
        this.addToParent(scales, DMItemTags.SUNLIGHT_DRAGON_SCALES).add(DMItems.SUNLIGHT_DRAGON_SCALES.key);
        this.addToParent(scales, DMItemTags.TERRA_DRAGON_SCALES).add(DMItems.TERRA_DRAGON_SCALES.key);
        this.addToParent(scales, DMItemTags.WATER_DRAGON_SCALES).add(DMItems.WATER_DRAGON_SCALES.key);
        this.addToParent(scales, DMItemTags.ZOMBIE_DRAGON_SCALES).add(DMItems.ZOMBIE_DRAGON_SCALES.key);
        this.addToParent(scales, DMItemTags.DARK_DRAGON_SCALES).add(DMItems.DARK_DRAGON_SCALES.key);
        this.getOrCreateTagBuilder(DMItemTags.HARD_SHEARS)
                .add(DMItems.DIAMOND_SHEARS.key)
                .add(DMItems.NETHERITE_SHEARS.key);
        this.getOrCreateTagBuilder(ItemTags.PIGLIN_LOVED)
                .add(DMItems.GOLDEN_DRAGON_ARMOR.key);
        this.getOrCreateTagBuilder(ItemTags.PIGLIN_REPELLENTS)
                .add(DMBlocks.DRAGON_CORE.asItem());
        this.getOrCreateTagBuilder(ItemTags.MEAT)
                .add(DMItems.DRAGON_MEAT.key)
                .add(DMItems.COOKED_DRAGON_MEAT.key);
        this.getOrCreateTagBuilder(DMItemTags.BATONS)
                .forceAddTag(ConventionalItemTags.RODS)
                .add(Items.DEBUG_STICK)
                .add(Items.BONE)
                .add(Items.BAMBOO);
        this.getOrCreateTagBuilder(DMItemTags.DRAGON_SADDLES)
                .add(Items.SADDLE);
        var head = this.tag(ItemTags.HEAD_ARMOR);
        var chest = this.tag(ItemTags.CHEST_ARMOR);
        var leg = this.tag(ItemTags.LEG_ARMOR);
        var foot = this.tag(ItemTags.FOOT_ARMOR);
        Consumer<DragonScaleArmorSuit> addScaleSuit = suit -> {
            head.add(suit.helmet);
            chest.add(suit.chestplate);
            leg.add(suit.leggings);
            foot.add(suit.boots);
        };
        Consumer<Item> addToSwords = this.getOrCreateTagBuilder(ItemTags.SWORDS)::add;
        Consumer<Item> addToBows = this.getOrCreateTagBuilder(DMItemTags.DRAGON_SCALE_BOWS)::add;
        Consumer<Item> addToAxes = this.getOrCreateTagBuilder(ItemTags.AXES)::add;
        Consumer<Item> addToHoes = this.getOrCreateTagBuilder(ItemTags.HOES)::add;
        Consumer<Item> addToPickaxes = this.getOrCreateTagBuilder(ItemTags.PICKAXES)::add;
        Consumer<Item> addToShovels = this.getOrCreateTagBuilder(ItemTags.SHOVELS)::add;
        Consumer<Item> addToShields = this.getOrCreateTagBuilder(DMItemTags.DRAGON_SCALE_SHIELDS)::add;
        for (var type : DragonType.REGISTRY) {
            type.ifPresent(DragonScaleArmorSuit.class, addScaleSuit);
            type.ifPresent(DragonScaleSwordItem.class, addToSwords);
            type.ifPresent(DragonScaleBowItem.class, addToBows);
            type.ifPresent(DragonScaleAxeItem.class, addToAxes);
            type.ifPresent(DragonScaleHoeItem.class, addToHoes);
            type.ifPresent(DragonScalePickaxeItem.class, addToPickaxes);
            type.ifPresent(DragonScaleShovelItem.class, addToShovels);
            type.ifPresent(DragonScaleShieldItem.class, addToShields);
        }
        this.getOrCreateTagBuilder(DMItemTags.DRAGON_INEDIBLE)
                .add(Items.PUFFERFISH) // it is considered as food in conventional tags...
                .add(Items.PUFFERFISH_BUCKET)
                .add(Items.AXOLOTL_BUCKET)
                .add(Items.TADPOLE_BUCKET)
                .add(DMItems.DRAGON_MEAT.key)
                .add(DMItems.COOKED_DRAGON_MEAT.key);
        this.getOrCreateTagBuilder(DMItemTags.COOKED_DRAGON_FOODS)
                .forceAddTag(ConventionalItemTags.COOKED_MEAT_FOODS)
                .forceAddTag(ConventionalItemTags.COOKED_FISH_FOODS);
        this.getOrCreateTagBuilder(DMItemTags.RAW_DRAGON_FOODS)
                .forceAddTag(ConventionalItemTags.RAW_MEAT_FOODS)
                .forceAddTag(ConventionalItemTags.RAW_FISH_FOODS)
                .add(Items.COD_BUCKET)
                .add(Items.SALMON_BUCKET)
                .add(Items.TROPICAL_FISH_BUCKET);
        this.tag(ItemTags.BOW_ENCHANTABLE).addTag(DMItemTags.DRAGON_SCALE_BOWS);
        this.tag(ConventionalItemTags.BOW_TOOLS).addTag(DMItemTags.DRAGON_SCALE_BOWS);
        this.tag(ConventionalItemTags.SHEAR_TOOLS).addTag(DMItemTags.HARD_SHEARS);
        this.tag(ItemTags.MINING_ENCHANTABLE).addTag(DMItemTags.HARD_SHEARS);
        this.tag(ConventionalItemTags.SHIELD_TOOLS).addTag(DMItemTags.DRAGON_SCALE_SHIELDS);
        this.tag(ItemTags.DURABILITY_ENCHANTABLE)
                .addTag(DMItemTags.DRAGON_SCALE_BOWS)
                .addTag(DMItemTags.HARD_SHEARS)
                .addTag(DMItemTags.DRAGON_SCALE_SHIELDS);
        this.copy(DMBlockTags.DRAGON_EGGS, DMItemTags.DRAGON_EGGS);
        this.copy(DMBlockTags.DRAGON_SCALE_BLOCKS, DMItemTags.DRAGON_SCALE_BLOCKS);
        this.getOrCreateTagBuilder(ItemTags.PIGLIN_REPELLENTS).add(DMBlocks.DRAGON_CORE.asItem());
        this.tag(ItemTags.PIGLIN_LOVED).add(DMItems.GOLDEN_DRAGON_ARMOR.key);
        var skulls = this.getOrCreateTagBuilder(DMItemTags.DRAGON_HEADS).add(Items.DRAGON_HEAD);
        for (var variant : DragonVariants.BUILTIN_VALUES) {
            skulls.add(variant.head.asItem());
        }
        this.tag(ItemTags.SKULLS).addTag(DMItemTags.DRAGON_HEADS);
        this.tag(ItemTags.NOTE_BLOCK_TOP_INSTRUMENTS).addTag(DMItemTags.DRAGON_HEADS);
    }
}
