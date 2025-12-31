package net.dragonmounts.neo.data;

import net.dragonmounts.neo.common.client.renderer.block.DragonCoreRenderer;
import net.dragonmounts.neo.common.client.renderer.block.DragonHeadRenderer;
import net.dragonmounts.neo.common.init.DMBlocks;
import net.dragonmounts.neo.common.init.DMItems;
import net.dragonmounts.neo.common.init.DragonVariants;
import net.dragonmounts.neo.common.item.*;
import net.dragonmounts.neo.compat.registry.*;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.color.item.Dye;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.renderer.item.properties.numeric.UseDuration;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.BiConsumer;

import static net.dragonmounts.neo.common.DragonMountsShared.makeId;
import static net.minecraft.client.data.models.BlockModelGenerators.createSimpleBlock;
import static net.minecraft.client.data.models.model.ItemModelUtils.*;
import static net.minecraft.client.data.models.model.ModelLocationUtils.getModelLocation;
import static net.minecraft.client.data.models.model.TextureMapping.getItemTexture;
import static net.minecraft.client.data.models.model.TextureMapping.layer0;
import static net.minecraft.resources.ResourceLocation.withDefaultNamespace;

public class DMModelProvider extends FabricModelProvider {
    public static final ResourceLocation VANILLA_SKULL = withDefaultNamespace("block/skull");
    public static final ResourceLocation VANILLA_DRAGON_HEAD = withDefaultNamespace("item/dragon_head");
    public static final ModelTemplate DRAGON_SCALE_SHIELD = new ModelTemplate(
            Optional.of(makeId("item/template_shield")),
            Optional.empty(),
            TextureSlot.LAYER0
    );
    public static final ModelTemplate DRAGON_SCALE_SHIELD_BLOCKING = new ModelTemplate(
            Optional.of(makeId("item/template_shield_blocking")),
            Optional.empty(),
            TextureSlot.LAYER0
    );

    public DMModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators gen) {
        generateDragonHeads(gen, DragonVariants.BUILTIN_VALUES);
        generateBlocksWithItem(gen, BlockModelGenerators::createRotatedVariantBlock, Collections.singleton(DMBlocks.DRAGON_NEST));
        generateBlocksWithItem(gen, BlockModelGenerators::createNonTemplateModelBlock, DMBlocks.BUILTIN_DRAGON_EGGS);
        generateBlocksWithItem(gen, BlockModelGenerators::createTrivialCube, DMBlocks.BUILTIN_DRAGON_SCALE_BLOCKS);
        {
            var particle = TextureMapping.particle(makeId("block/dragon_core_break"));
            var block = DMBlocks.DRAGON_CORE.get();
            gen.blockStateOutput.accept(createSimpleBlock(block, ModelTemplates.PARTICLE_ONLY.create(block, particle, gen.modelOutput)));
            var item = block.asItem();
            gen.itemModelOutput.accept(item, ItemModelUtils.specialModel(
                    ModelTemplates.SHULKER_BOX_INVENTORY.create(item, particle, gen.modelOutput),
                    new DragonCoreRenderer.Unbaked(0.0F, Direction.SOUTH)
            ));
        }
    }

    @Override
    public void generateItemModels(ItemModelGenerators gen) {
        generateFlute(gen, DMItems.FLUTE.get());
        generateFlatItem(gen, DMItems.AMULET);
        generateFlatItem(gen, DMItems.COPPER_DRAGON_ARMOR);
        generateFlatItem(gen, DMItems.IRON_DRAGON_ARMOR);
        generateFlatItem(gen, DMItems.GOLDEN_DRAGON_ARMOR);
        generateFlatItem(gen, DMItems.EMERALD_DRAGON_ARMOR);
        generateFlatItem(gen, DMItems.DIAMOND_DRAGON_ARMOR);
        generateFlatItem(gen, DMItems.NETHERITE_DRAGON_ARMOR);
        generateFlatItem(gen, DMItems.DIAMOND_SHEARS);
        generateFlatItem(gen, DMItems.NETHERITE_SHEARS);
        gen.itemModelOutput.accept(
                DMItems.VARIATION_ORB.get(),
                plainModel(DMItems.VARIATION_ORB.key.location().withPrefix("item/"))
        );
        generateFlatItem(gen, DMItems.DRAGON_MEAT);
        generateFlatItem(gen, DMItems.COOKED_DRAGON_MEAT);
        generateSpawnEgg(gen, DMItems.AETHER_DRAGON_SPAWN_EGG, 0x06E9FA, 0x281EE7);
        generateSpawnEgg(gen, DMItems.DARK_DRAGON_SPAWN_EGG, 0x222121, 0x971B1B);
        generateSpawnEgg(gen, DMItems.ENCHANTED_DRAGON_SPAWN_EGG, 0xF30FFF, 0xD7D7D7);
        generateSpawnEgg(gen, DMItems.ENDER_DRAGON_SPAWN_EGG, 0x1D1D24, 0x900996);
        generateSpawnEgg(gen, DMItems.FIRE_DRAGON_SPAWN_EGG, 0x9F2909, 0xF7A502);
        generateSpawnEgg(gen, DMItems.FOREST_DRAGON_SPAWN_EGG, 0x28AA29, 0x024F06);
        generateSpawnEgg(gen, DMItems.ICE_DRAGON_SPAWN_EGG, 0xD7D7D7, 0xB3FFF8);
        generateSpawnEgg(gen, DMItems.MOONLIGHT_DRAGON_SPAWN_EGG, 0x002A95, 0xDAF3AF);
        generateSpawnEgg(gen, DMItems.NETHER_DRAGON_SPAWN_EGG, 0xF79C03, 0x9E4B2B);
        generateSpawnEgg(gen, DMItems.SCULK_DRAGON_SPAWN_EGG, 0x0F4649, 0x39D6E0);
        generateSpawnEgg(gen, DMItems.SKELETON_DRAGON_SPAWN_EGG, 0xD7D7D7, 0x727F82);
        generateSpawnEgg(gen, DMItems.STORM_DRAGON_SPAWN_EGG, 0x023C54, 0x0DA2C7);
        generateSpawnEgg(gen, DMItems.SUNLIGHT_DRAGON_SPAWN_EGG, 0xF07F07, 0xF2EA04);
        generateSpawnEgg(gen, DMItems.TERRA_DRAGON_SPAWN_EGG, 0x543813, 0xB3782A);
        generateSpawnEgg(gen, DMItems.WATER_DRAGON_SPAWN_EGG, 0x4F6AA6, 0x223464);
        generateSpawnEgg(gen, DMItems.WITHER_DRAGON_SPAWN_EGG, 0x839292, 0x383F40);
        generateSpawnEgg(gen, DMItems.ZOMBIE_DRAGON_SPAWN_EGG, 0x56562E, 0xA7BF2F);
        for (var type : DragonType.REGISTRY) {
            generateFlatItem(gen, type, DragonAmuletItem.class);
            generateFlatItem(gen, type, DragonEssenceItem.class);
            generateFlatItem(gen, type, DragonScalesItem.class);
            generateDragonScaleArmors(gen, type);
            generateHandheldItem(gen, type, DragonScaleAxeItem.class);
            generateDragonScaleBow(gen, type.getInstance(DragonScaleBowItem.class, null));
            generateHandheldItem(gen, type, DragonScaleHoeItem.class);
            generateHandheldItem(gen, type, DragonScalePickaxeItem.class);
            generateDragonScaleShield(gen, type.getInstance(DragonScaleShieldItem.class, null));
            generateHandheldItem(gen, type, DragonScaleShovelItem.class);
            generateHandheldItem(gen, type, DragonScaleSwordItem.class);
        }
    }

    public static void generateFlatItem(ItemModelGenerators gen, ItemHolder<?> item) {
        gen.generateFlatItem(item.get(), ModelTemplates.FLAT_ITEM);
    }

    public static void generateSpawnEgg(ItemModelGenerators gen, ItemHolder<?> item, int primaryColor, int secondaryColor) {
        gen.generateSpawnEgg(item.get(), primaryColor, secondaryColor);
    }

    public static void generateFlute(ItemModelGenerators gen, Item flute) {
        var tints = new ItemTintSource[]{constantTint(-1), new Dye(-1)};
        gen.generateBooleanDispatch(
                flute,
                isUsingItem(),
                tintedModel(getModelLocation(flute, "_playing"), tints),
                tintedModel(getModelLocation(flute), tints)
        );
    }

    /**
     * @see ItemModelGenerators#generateBow(Item)
     */
    public static void generateDragonScaleBow(ItemModelGenerators gen, @Nullable Item bow) {
        if (bow == null) return;
        gen.itemModelOutput.accept(bow, conditional(isUsingItem(), rangeSelect(
                new UseDuration(false),
                0.05F,
                plainModel(gen.createFlatItemModel(bow, "_pulling_0", ModelTemplates.BOW)),
                override(plainModel(
                        gen.createFlatItemModel(bow, "_pulling_1", ModelTemplates.BOW)
                ), 0.65F),
                override(plainModel(
                        gen.createFlatItemModel(bow, "_pulling_2", ModelTemplates.BOW)
                ), 0.9F)
        ), plainModel(gen.createFlatItemModel(bow, ModelTemplates.BOW))));
    }

    /**
     * @see ItemModelGenerators#generateShield(Item)
     */
    public static void generateDragonScaleShield(ItemModelGenerators gen, @Nullable Item shield) {
        if (shield == null) return;
        var texture = layer0(getItemTexture(shield));
        gen.generateBooleanDispatch(
                shield,
                isUsingItem(),
                plainModel(DRAGON_SCALE_SHIELD_BLOCKING.create(getModelLocation(shield, "_blocking"), texture, gen.modelOutput)),
                plainModel(DRAGON_SCALE_SHIELD.create(getModelLocation(shield), texture, gen.modelOutput))
        );
    }

    public static void generateDragonScaleArmors(ItemModelGenerators gen, DragonType type) {
        var suit = type.getInstance(DragonScaleArmorSuit.class, null);
        if (suit == null) return;
        var assets = suit.type.material.assetId();
        gen.generateTrimmableItem(suit.getHelmet(), assets, "helmet", false);
        gen.generateTrimmableItem(suit.getChestplate(), assets, "chestplate", false);
        gen.generateTrimmableItem(suit.getLeggings(), assets, "leggings", false);
        gen.generateTrimmableItem(suit.getBoots(), assets, "boots", false);
    }

    public static void generateDragonHeads(BlockModelGenerators gen, Collection<DragonVariant> variants) {
        var state = gen.blockStateOutput;
        var item = gen.itemModelOutput;
        for (DragonVariant variant : variants) {
            var head = variant.head;
            state.accept(createSimpleBlock(head.standing.get(), VANILLA_SKULL));
            state.accept(createSimpleBlock(head.wall.get(), VANILLA_SKULL));
            item.accept(head.item.get(), specialModel(VANILLA_DRAGON_HEAD, new DragonHeadRenderer.Unbaked(variant, 0.0F)));
        }
    }

    public static void generateFlatItem(ItemModelGenerators gen, DragonType type, Class<? extends Item> clazz) {
        var item = type.getInstance(clazz, null);
        if (item == null) return;
        gen.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
    }

    public static void generateHandheldItem(ItemModelGenerators gen, DragonType type, Class<? extends Item> clazz) {
        var item = type.getInstance(clazz, null);
        if (item == null) return;
        gen.generateFlatItem(item, ModelTemplates.FLAT_HANDHELD_ITEM);
    }

    public static <T extends Block> void generateBlocksWithItem(BlockModelGenerators gen, BiConsumer<BlockModelGenerators, T> consumer, Collection<BlockHolder<T>> holders) {
        for (var holder : holders) {
            var block = holder.get();
            consumer.accept(gen, block);
            gen.itemModelOutput.accept(block.asItem(), plainModel(getModelLocation(block)));
        }
    }
}
