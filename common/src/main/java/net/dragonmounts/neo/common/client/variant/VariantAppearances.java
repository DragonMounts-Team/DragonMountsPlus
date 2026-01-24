package net.dragonmounts.neo.common.client.variant;

import net.dragonmounts.neo.common.client.DMParticleSprites;
import net.dragonmounts.neo.common.client.breath.impl.*;
import net.dragonmounts.neo.common.client.model.dragon.BuiltinFactory;
import net.dragonmounts.neo.common.init.DragonArmorMaterials;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;

import static net.dragonmounts.neo.common.DragonMountsShared.makeId;
import static net.dragonmounts.neo.common.client.variant.DefaultAppearance.registerArmorTexture;
import static net.dragonmounts.neo.common.client.variant.VariantAppearance.TEXTURES_ROOT;

public class VariantAppearances {
    public static DefaultAppearance.Builder builder(BuiltinFactory model) {
        return new DefaultAppearance.Builder(model.location);
    }

    public static void registerArmorTextures(@Nullable String category, ResourceLocation folder) {
        registerArmorTexture(category, DragonArmorMaterials.COPPER.assetId(), folder.withSuffix("/copper.png"));
        registerArmorTexture(category, DragonArmorMaterials.IRON.assetId(), folder.withSuffix("/iron.png"));
        registerArmorTexture(category, DragonArmorMaterials.GOLD.assetId(), folder.withSuffix("/gold.png"));
        registerArmorTexture(category, DragonArmorMaterials.EMERALD.assetId(), folder.withSuffix("/emerald.png"));
        registerArmorTexture(category, DragonArmorMaterials.DIAMOND.assetId(), folder.withSuffix("/diamond.png"));
        registerArmorTexture(category, DragonArmorMaterials.NETHERITE.assetId(), folder.withSuffix("/netherite.png"));
    }

    public static final VariantAppearance AETHER_FEMALE;
    public static final VariantAppearance AETHER_MALE;
    public static final VariantAppearance BREEZE;
    public static final VariantAppearance DARK_FEMALE;
    public static final VariantAppearance DARK_MALE;
    public static final VariantAppearance ENCHANTED_FEMALE;
    public static final VariantAppearance ENCHANTED_MALE;
    public static final VariantAppearance ENCHANTING_TABLE;
    public static final VariantAppearance ENDER_FEMALE;
    public static final VariantAppearance ENDER_MALE;
    public static final VariantAppearance ENDER_RARE;
    public static final VariantAppearance FIRE_FEMALE;
    public static final VariantAppearance FIRE_MALE;
    public static final VariantAppearance BLUE_FIRE;
    public static final VariantAppearance FOREST_FEMALE;
    public static final VariantAppearance FOREST_MALE;
    public static final VariantAppearance FOREST_DRY_FEMALE;
    public static final VariantAppearance FOREST_DRY_MALE;
    public static final VariantAppearance FOREST_TAIGA_FEMALE;
    public static final VariantAppearance FOREST_TAIGA_MALE;
    public static final VariantAppearance ICE_FEMALE;
    public static final VariantAppearance ICE_MALE;
    public static final VariantAppearance MOONLIGHT_FEMALE;
    public static final VariantAppearance MOONLIGHT_MALE;
    public static final VariantAppearance ECLIPSE;
    public static final VariantAppearance NETHER_FEMALE;
    public static final VariantAppearance NETHER_MALE;
    public static final VariantAppearance SOUL;
    public static final VariantAppearance SKELETON;
    public static final VariantAppearance STRAY;
    public static final VariantAppearance BOGGED;
    public static final VariantAppearance STORM_FEMALE;
    public static final VariantAppearance STORM_MALE;
    public static final VariantAppearance BRONZED_STORM;
    public static final VariantAppearance SUNLIGHT_FEMALE;
    public static final VariantAppearance SUNLIGHT_MALE;
    public static final VariantAppearance AURORA;
    public static final VariantAppearance TERRA_FEMALE;
    public static final VariantAppearance TERRA_MALE;
    public static final VariantAppearance CRYSTAL;
    public static final VariantAppearance WATER_FEMALE;
    public static final VariantAppearance WATER_MALE;
    public static final VariantAppearance BRINE;
    public static final VariantAppearance WITHER;
    public static final VariantAppearance ZOMBIE;
    public static final VariantAppearance WILD_SCULK;
    public static final VariantAppearance MUTANT_SCULK;
    public static final VariantAppearance HOLLOWED;

    static {
        var builder = builder(BuiltinFactory.NORMAL)
                .withBreath(DMParticleSprites.AIRFLOW_BREATH, AirflowBreathParticle.FACTORY);
        AETHER_FEMALE = builder.build(makeId("aether/female"));
        AETHER_MALE = builder.build(makeId("aether/male"));
        BREEZE = builder.build(makeId("aether/breeze"));
    }

    static {
        var builder = builder(BuiltinFactory.NORMAL)
                .withBreath(DMParticleSprites.DARK_BREATH);
        DARK_FEMALE = builder.build(makeId("dark/female"));
        DARK_MALE = builder.build(makeId("dark/male"));
    }

    static {
        var builder = builder(BuiltinFactory.NORMAL);
        ENCHANTED_FEMALE = builder.build(makeId("enchanted/female"));
        ENCHANTED_MALE = builder.build(makeId("enchanted/male"));
        ENCHANTING_TABLE = builder.build(makeId("enchanted/enchanting_table"));
    }

    static {
        var builder = builder(BuiltinFactory.COMPAT)
                .withBreath(DMParticleSprites.ENDER_BREATH, EnderBreathParticle.FACTORY);
        ENDER_FEMALE = builder.build(makeId("ender/female"));
        ENDER_MALE = builder.build(makeId("ender/male"));
        ENDER_RARE = builder.build(makeId("ender/rare"));
    }

    static {
        var builder = builder(BuiltinFactory.NORMAL);
        FIRE_FEMALE = builder.build(makeId("fire/female"));
        FIRE_MALE = builder.build(makeId("fire/male"));
        BLUE_FIRE = builder.withBreath(DMParticleSprites.BLUE_FLAME_BREATH).build(makeId("fire/blue"));
    }

    static {
        var builder = builder(BuiltinFactory.COMPAT);
        var glow = makeId(TEXTURES_ROOT + "forest/glow.png");
        FOREST_FEMALE = builder.build(makeId(TEXTURES_ROOT + "forest/forest/female_body.png"), glow);
        FOREST_MALE = builder.build(makeId(TEXTURES_ROOT + "forest/forest/male_body.png"), glow);
        FOREST_DRY_FEMALE = builder.build(makeId(TEXTURES_ROOT + "forest/dry/female_body.png"), glow);
        FOREST_DRY_MALE = builder.build(makeId(TEXTURES_ROOT + "forest/dry/male_body.png"), glow);
        FOREST_TAIGA_FEMALE = builder.build(makeId(TEXTURES_ROOT + "forest/taiga/female_body.png"), glow);
        FOREST_TAIGA_MALE = builder.build(makeId(TEXTURES_ROOT + "forest/taiga/male_body.png"), glow);
    }

    static {
        var builder = builder(BuiltinFactory.TAIL_SCALE_INCLINED)
                .withBreath(DMParticleSprites.ICE_BREATH, IceBreathParticle.FACTORY);
        ICE_FEMALE = builder.build(makeId("ice/female"));
        ICE_MALE = builder.build(makeId("ice/male"));
    }

    static {
        var builder = builder(BuiltinFactory.NORMAL);
        MOONLIGHT_FEMALE = builder.build(makeId("moonlight/female"));
        MOONLIGHT_MALE = builder.build(makeId("moonlight/male"));
        ECLIPSE = builder.build(makeId("moonlight/eclipse"));
    }

    static {
        var builder = builder(BuiltinFactory.SCALE_SHARPENED)
                .withBreath(DMParticleSprites.NETHER_BREATH, NetherBreathParticle.FACTORY);
        NETHER_FEMALE = builder.build(makeId("nether/female"));
        NETHER_MALE = builder.build(makeId("nether/male"));
        SOUL = builder.withBreath(DMParticleSprites.SOUL_BREATH).build(makeId("nether/soul"));
    }

    static {
        var builder = builder(BuiltinFactory.TAIL_HORNED);
        STORM_FEMALE = builder.build(makeId("storm/female"));
        STORM_MALE = builder.build(makeId("storm/male"));
        BRONZED_STORM = builder.build(makeId("storm/bronzed"));
    }

    static {
        var builder = builder(BuiltinFactory.NORMAL);
        SUNLIGHT_FEMALE = builder.build(makeId("sunlight/female"));
        SUNLIGHT_MALE = builder.build(makeId("sunlight/male"));
        AURORA = builder.build(makeId("sunlight/aurora"));
    }

    static {
        var builder = builder(BuiltinFactory.NORMAL);
        TERRA_FEMALE = builder.build(makeId("terra/female"));
        TERRA_MALE = builder.build(makeId("terra/male"));
        CRYSTAL = builder.build(makeId("terra/crystal"));
    }

    static {
        var builder = builder(BuiltinFactory.SCALE_SHARPENED)
                .withBreath(DMParticleSprites.WATER_BREATH, WaterBreathParticle.FACTORY);
        WATER_FEMALE = builder.build(makeId("water/female"));
        WATER_MALE = builder.build(makeId("water/male"));
        BRINE = builder.build(makeId("water/brine"));
    }

    static {
        var builder = builder(BuiltinFactory.SKELETON).setArmorCategory("skeleton");
        SKELETON = builder.build(makeId("skeleton/normal"));
        STRAY = builder.build(makeId("skeleton/stray"));
        BOGGED = builder.build(makeId("skeleton/bogged"));
        WITHER = builder.withBreath(DMParticleSprites.WITHER_BREATH)
                .build(makeId("wither"));
    }

    static {
        ZOMBIE = builder(BuiltinFactory.COMPAT_TAIL_HORNED)
                .withBreath(DMParticleSprites.POISON_BREATH, PoisonBreathParticle.FACTORY)
                .build(makeId("zombie"));
    }

    static {
        var builder = builder(BuiltinFactory.SCULK).setArmorCategory("sculk");
        WILD_SCULK = builder.build(makeId("sculk/wild_type"));
        MUTANT_SCULK = builder.build(makeId("sculk/mutant"));
        HOLLOWED = builder.build(makeId("sculk/hollowed"));
    }

    static {
        registerArmorTextures(null, makeId("textures/entity/equipment/normal_dragon_body"));
        registerArmorTextures("sculk", makeId("textures/entity/equipment/sculk_dragon_body"));
        registerArmorTextures("skeleton", makeId("textures/entity/equipment/skeleton_dragon_body"));
    }

    public static VariantAppearance getBuiltinAppearance(String variant) {
        return switch (variant) {
            case "aether_female" -> AETHER_FEMALE;
            case "aether_male" -> AETHER_MALE;
            case "breeze" -> BREEZE;
            case "dark_female" -> DARK_FEMALE;
            case "dark_male" -> DARK_MALE;
            case "enchanted_female" -> ENCHANTED_FEMALE;
            case "enchanted_male" -> ENCHANTED_MALE;
            case "enchanting_table" -> ENCHANTING_TABLE;
            case "ender_female" -> ENDER_FEMALE;
            case "ender_male" -> ENDER_MALE;
            case "ender_rare" -> ENDER_RARE;
            case "fire_female" -> FIRE_FEMALE;
            case "fire_male" -> FIRE_MALE;
            case "blue_fire" -> BLUE_FIRE;
            case "forest_female" -> FOREST_FEMALE;
            case "forest_male" -> FOREST_MALE;
            case "forest_dry_female" -> FOREST_DRY_FEMALE;
            case "forest_dry_male" -> FOREST_DRY_MALE;
            case "forest_taiga_female" -> FOREST_TAIGA_FEMALE;
            case "forest_taiga_male" -> FOREST_TAIGA_MALE;
            case "ice_female" -> ICE_FEMALE;
            case "ice_male" -> ICE_MALE;
            case "moonlight_female" -> MOONLIGHT_FEMALE;
            case "moonlight_male" -> MOONLIGHT_MALE;
            case "eclipse" -> ECLIPSE;
            case "nether_female" -> NETHER_FEMALE;
            case "nether_male" -> NETHER_MALE;
            case "soul" -> SOUL;
            case "wild_sculk" -> WILD_SCULK;
            case "mutant_sculk" -> MUTANT_SCULK;
            case "hollowed" -> HOLLOWED;
            case "skeleton" -> SKELETON;
            case "stray" -> STRAY;
            case "bogged" -> BOGGED;
            case "storm_female" -> STORM_FEMALE;
            case "storm_male" -> STORM_MALE;
            case "bronzed_storm" -> BRONZED_STORM;
            case "sunlight_female" -> SUNLIGHT_FEMALE;
            case "sunlight_male" -> SUNLIGHT_MALE;
            case "aurora" -> AURORA;
            case "terra_female" -> TERRA_FEMALE;
            case "terra_male" -> TERRA_MALE;
            case "crystal" -> CRYSTAL;
            case "water_female" -> WATER_FEMALE;
            case "water_male" -> WATER_MALE;
            case "brine" -> BRINE;
            case "wither" -> WITHER;
            case "zombie" -> ZOMBIE;
            default -> throw new NoSuchElementException(
                    "There is no built-in variant appearance named \"" + variant + "\". Please create a custom supplier."
            );
        };
    }
}
