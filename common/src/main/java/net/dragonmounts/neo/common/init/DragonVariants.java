package net.dragonmounts.neo.common.init;

import com.google.common.collect.ImmutableList;
import net.dragonmounts.neo.common.block.DragonHeadBlock;
import net.dragonmounts.neo.common.block.DragonHeadStandingBlock;
import net.dragonmounts.neo.common.block.DragonHeadWallBlock;
import net.dragonmounts.neo.common.client.variant.VariantAppearance;
import net.dragonmounts.neo.common.client.variant.VariantAppearances;
import net.dragonmounts.neo.common.item.DragonHeadItem;
import net.dragonmounts.neo.common.util.DragonHead;
import net.dragonmounts.neo.compat.platform.PlatformCompat;
import net.dragonmounts.neo.compat.registry.BlockHolder;
import net.dragonmounts.neo.compat.registry.DragonType;
import net.dragonmounts.neo.compat.registry.DragonVariant;
import net.dragonmounts.neo.compat.registry.ItemHolder;
import net.minecraft.world.item.Rarity;

import java.util.function.Function;

import static net.dragonmounts.neo.common.DragonMountsShared.makeId;
import static net.dragonmounts.neo.common.init.DMBlocks.configureDragonHead;
import static net.dragonmounts.neo.compat.registry.BlockHolder.registerBlock;

public class DragonVariants {
    public static final ImmutableList<DragonVariant> BUILTIN_VALUES;
    public static final DragonVariant AETHER_FEMALE;
    public static final DragonVariant AETHER_MALE;
    public static final DragonVariant BREEZE;
    public static final DragonVariant DARK_FEMALE;
    public static final DragonVariant DARK_MALE;
    public static final DragonVariant ENCHANTED_FEMALE;
    public static final DragonVariant ENCHANTED_MALE;
    public static final DragonVariant ENCHANTING_TABLE;
    public static final DragonVariant ENDER_FEMALE;
    public static final DragonVariant ENDER_MALE;
    public static final DragonVariant ENDER_RARE;
    public static final DragonVariant FIRE_FEMALE;
    public static final DragonVariant FIRE_MALE;
    public static final DragonVariant BLUE_FIRE;
    public static final DragonVariant FOREST_FEMALE;
    public static final DragonVariant FOREST_MALE;
    public static final DragonVariant FOREST_DRY_FEMALE;
    public static final DragonVariant FOREST_DRY_MALE;
    public static final DragonVariant FOREST_TAIGA_FEMALE;
    public static final DragonVariant FOREST_TAIGA_MALE;
    public static final DragonVariant ICE_FEMALE;
    public static final DragonVariant ICE_MALE;
    public static final DragonVariant MOONLIGHT_FEMALE;
    public static final DragonVariant MOONLIGHT_MALE;
    public static final DragonVariant ECLIPSE;
    public static final DragonVariant NETHER_FEMALE;
    public static final DragonVariant NETHER_MALE;
    public static final DragonVariant SOUL;
    public static final DragonVariant WILD_SCULK;
    public static final DragonVariant MUTANT_SCULK;
    public static final DragonVariant HOLLOWED;
    public static final DragonVariant SKELETON;
    public static final DragonVariant STRAY;
    public static final DragonVariant BOGGED;
    public static final DragonVariant STORM_FEMALE;
    public static final DragonVariant STORM_MALE;
    public static final DragonVariant BRONZED_STORM;
    public static final DragonVariant SUNLIGHT_FEMALE;
    public static final DragonVariant SUNLIGHT_MALE;
    public static final DragonVariant AURORA;
    public static final DragonVariant TERRA_FEMALE;
    public static final DragonVariant TERRA_MALE;
    public static final DragonVariant CRYSTAL;
    public static final DragonVariant WATER_FEMALE;
    public static final DragonVariant WATER_MALE;
    public static final DragonVariant BRINE;
    public static final DragonVariant WITHER;
    public static final DragonVariant ZOMBIE;

    static BlockHolder<DragonHeadStandingBlock> registerStandingHead(DragonHead head, String name) {
        return registerBlock(name, props ->
                new DragonHeadStandingBlock(head.variant, configureDragonHead(props).overrideDescription(DragonHeadBlock.TRANSLATION_KEY))
        );
    }

    static BlockHolder<DragonHeadWallBlock> registerWallHead(DragonHead head, String name) {
        return registerBlock(name, props -> {
            var standing = head.standing.get();
            return new DragonHeadWallBlock(head.variant, configureDragonHead(props)
                    .overrideLootTable(standing.getLootTable())
                    .overrideDescription(standing.getDescriptionId())
            );
        });
    }

    static ItemHolder<DragonHeadItem> registerHeadItem(DragonHead head, String name) {
        return DMItemGroups.DRAGON_HEADS.register(name, props -> new DragonHeadItem(
                head.variant,
                head.standing.get(),
                head.wall.get(),
                props.rarity(Rarity.UNCOMMON).overrideDescription(DragonHeadBlock.TRANSLATION_KEY)
        ));
    }

    static DragonVariant make(Function<String, VariantAppearance> supplier, DragonType type, String name) {
        return new DragonVariant(type, makeId(name), supplier.apply(name), variant -> {
            var wall = variant.identifier.getPath() + "_dragon_head_wall";
            return new DragonHead(
                    variant,
                    wall.substring(0, wall.length() - 5),
                    wall,
                    DragonVariants::registerStandingHead,
                    DragonVariants::registerWallHead,
                    DragonVariants::registerHeadItem
            );
        });
    }

    static {
        Function<String, VariantAppearance> supplier = PlatformCompat.isClientSide()
                ? VariantAppearances::getBuiltinAppearance
                : ignored -> null;
        var variants = ImmutableList.<DragonVariant>builderWithExpectedSize(46);
        variants.add(AETHER_FEMALE = make(supplier, DragonTypes.AETHER, "aether_female"));
        variants.add(AETHER_MALE = make(supplier, DragonTypes.AETHER, "aether_male"));
        variants.add(BREEZE = make(supplier, DragonTypes.AETHER, "breeze"));
        variants.add(DARK_FEMALE = make(supplier, DragonTypes.DARK, "dark_female"));
        variants.add(DARK_MALE = make(supplier, DragonTypes.DARK, "dark_male"));
        variants.add(ENCHANTED_FEMALE = make(supplier, DragonTypes.ENCHANTED, "enchanted_female"));
        variants.add(ENCHANTED_MALE = make(supplier, DragonTypes.ENCHANTED, "enchanted_male"));
        variants.add(ENCHANTING_TABLE = make(supplier, DragonTypes.ENCHANTED, "enchanting_table"));
        variants.add(ENDER_FEMALE = make(supplier, DragonTypes.ENDER, "ender_female"));
        variants.add(ENDER_MALE = make(supplier, DragonTypes.ENDER, "ender_male"));
        variants.add(ENDER_RARE = make(supplier, DragonTypes.ENDER, "ender_rare"));
        variants.add(FIRE_FEMALE = make(supplier, DragonTypes.FIRE, "fire_female"));
        variants.add(FIRE_MALE = make(supplier, DragonTypes.FIRE, "fire_male"));
        variants.add(BLUE_FIRE = make(supplier, DragonTypes.FIRE, "blue_fire"));
        variants.add(FOREST_FEMALE = make(supplier, DragonTypes.FOREST, "forest_female"));
        variants.add(FOREST_MALE = make(supplier, DragonTypes.FOREST, "forest_male"));
        variants.add(FOREST_DRY_FEMALE = make(supplier, DragonTypes.FOREST, "forest_dry_female"));
        variants.add(FOREST_DRY_MALE = make(supplier, DragonTypes.FOREST, "forest_dry_male"));
        variants.add(FOREST_TAIGA_FEMALE = make(supplier, DragonTypes.FOREST, "forest_taiga_female"));
        variants.add(FOREST_TAIGA_MALE = make(supplier, DragonTypes.FOREST, "forest_taiga_male"));
        variants.add(ICE_FEMALE = make(supplier, DragonTypes.ICE, "ice_female"));
        variants.add(ICE_MALE = make(supplier, DragonTypes.ICE, "ice_male"));
        variants.add(MOONLIGHT_FEMALE = make(supplier, DragonTypes.MOONLIGHT, "moonlight_female"));
        variants.add(MOONLIGHT_MALE = make(supplier, DragonTypes.MOONLIGHT, "moonlight_male"));
        variants.add(ECLIPSE = make(supplier, DragonTypes.MOONLIGHT, "eclipse"));
        variants.add(NETHER_FEMALE = make(supplier, DragonTypes.NETHER, "nether_female"));
        variants.add(NETHER_MALE = make(supplier, DragonTypes.NETHER, "nether_male"));
        variants.add(SOUL = make(supplier, DragonTypes.NETHER, "soul"));
        variants.add(WILD_SCULK = make(supplier, DragonTypes.SCULK, "wild_sculk"));
        variants.add(MUTANT_SCULK = make(supplier, DragonTypes.SCULK, "mutant_sculk"));
        variants.add(HOLLOWED = make(supplier, DragonTypes.SCULK, "hollowed"));
        variants.add(SKELETON = make(supplier, DragonTypes.SKELETON, "skeleton"));
        variants.add(STRAY = make(supplier, DragonTypes.SKELETON, "stray"));
        variants.add(BOGGED = make(supplier, DragonTypes.SKELETON, "bogged"));
        variants.add(STORM_FEMALE = make(supplier, DragonTypes.STORM, "storm_female"));
        variants.add(STORM_MALE = make(supplier, DragonTypes.STORM, "storm_male"));
        variants.add(BRONZED_STORM = make(supplier, DragonTypes.STORM, "bronzed_storm"));
        variants.add(SUNLIGHT_FEMALE = make(supplier, DragonTypes.SUNLIGHT, "sunlight_female"));
        variants.add(SUNLIGHT_MALE = make(supplier, DragonTypes.SUNLIGHT, "sunlight_male"));
        variants.add(AURORA = make(supplier, DragonTypes.SUNLIGHT, "aurora"));
        variants.add(TERRA_FEMALE = make(supplier, DragonTypes.TERRA, "terra_female"));
        variants.add(TERRA_MALE = make(supplier, DragonTypes.TERRA, "terra_male"));
        variants.add(CRYSTAL = make(supplier, DragonTypes.TERRA, "crystal"));
        variants.add(WATER_FEMALE = make(supplier, DragonTypes.WATER, "water_female"));
        variants.add(WATER_MALE = make(supplier, DragonTypes.WATER, "water_male"));
        variants.add(BRINE = make(supplier, DragonTypes.WATER, "brine"));
        variants.add(WITHER = make(supplier, DragonTypes.WITHER, "wither"));
        variants.add(ZOMBIE = make(supplier, DragonTypes.ZOMBIE, "zombie"));
        BUILTIN_VALUES = variants.build();
    }
}
