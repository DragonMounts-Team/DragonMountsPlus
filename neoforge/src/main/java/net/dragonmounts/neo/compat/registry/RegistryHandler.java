package net.dragonmounts.neo.compat.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dragonmounts.neo.common.DragonMountsShared;
import net.dragonmounts.neo.common.init.DMItemGroups;
import net.dragonmounts.neo.common.init.DragonVariants;
import net.dragonmounts.neo.compat.platform.DMAttachments;
import net.dragonmounts.neo.compat.platform.DMScreenHandlers;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static net.dragonmounts.neo.common.DragonMountsShared.makeId;
import static net.minecraft.resources.ResourceLocation.withDefaultNamespace;

public class RegistryHandler {
    public static Activity registerActivity(String name) {
        return register(ACTIVITIES, makeId(name), new Activity(name));
    }

    public static <T extends ArmorEffect> T registerArmorEffect(ResourceLocation identifier, T effect) {
        if (effect instanceof CooldownCategory category) {
            COOLDOWN_CATEGORIES.add(category);
        }
        return register(ARMOR_EFFECTS, identifier, effect);
    }

    public static <T> MemoryModuleType<T> registerSensoryMemory(String name) {
        return register(MEMORIES, makeId(name), new MemoryModuleType<>(Optional.empty()));
    }

    public static MemoryModuleType<Unit> registerMemory(String name) {
        return registerMemory(name, Unit.CODEC);
    }

    public static <T> MemoryModuleType<T> registerMemory(String name, Codec<T> codec) {
        return register(MEMORIES, makeId(name), new MemoryModuleType<>(Optional.of(codec)));
    }

    public static <T extends ParticleOptions> ParticleType<T> registerParticle(
            String name,
            boolean overrideLimiter,
            MapCodec<T> codec,
            StreamCodec<? super RegistryFriendlyByteBuf, T> packetCodec
    ) {
        return register(PARTICLES, makeId(name), new ParticleType<T>(overrideLimiter) {
            @Override
            public MapCodec<T> codec() {
                return codec;
            }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec() {
                return packetCodec;
            }
        });
    }

    public static <S extends RecipeSerializer<T>, T extends Recipe<?>> S registerRecipe(ResourceLocation identifier, S serializer) {
        return register(RECIPES, identifier, serializer);
    }

    public static <T extends Sensor<?>> SensorType<T> registerSensor(String name, Supplier<T> factory) {
        return register(SENSORS, makeId(name), new SensorType<>(factory));
    }

    public static <T extends SoundEvent> T registerSound(ResourceLocation identifier, T sound) {
        return register(SOUNDS, identifier, sound);
    }

    public static <S extends Structure> StructureType<S> registerStructure(String name, StructureType<S> structure) {
        return register(STRUCTURES, makeId(name), structure);
    }

    public static StructurePieceType registerStructure(String name, StructurePieceType.StructureTemplateType piece) {
        return register(STRUCTURE_PIECES, makeId(name), piece);
    }

    public static <T extends ArmorEffect> T registerArmorEffect(String name, Function<ResourceLocation, T> factory) {
        var identifier = makeId(name);
        return registerArmorEffect(identifier, factory.apply(identifier));
    }

    public static <T> DataComponentType<T> registerComponent(String name, UnaryOperator<DataComponentType.Builder<T>> operator) {
        return register(COMPONENTS, makeId(name), operator.apply(new DataComponentType.Builder<>()).build());
    }

    public static <T extends ConsumeEffect> ConsumeEffect.Type<T> registerConsumeEffect(String name, MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> network) {
        return register(CONSUMERS, makeId(name), new ConsumeEffect.Type<>(codec, network));
    }

    //---------------------------------------- impl ----------------------------------------
    private static final ObjectArrayList<Consumer<RegisterEvent.RegisterHelper<Activity>>> ACTIVITIES = new ObjectArrayList<>();
    private static final ObjectArrayList<Consumer<RegisterEvent.RegisterHelper<ArmorEffect>>> ARMOR_EFFECTS = new ObjectArrayList<>();
    private static final ObjectArrayList<Consumer<RegisterEvent.RegisterHelper<DataComponentType<?>>>> COMPONENTS = new ObjectArrayList<>();
    private static final ObjectArrayList<Consumer<RegisterEvent.RegisterHelper<ConsumeEffect.Type<?>>>> CONSUMERS = new ObjectArrayList<>();
    private static final ObjectArrayList<Consumer<RegisterEvent.RegisterHelper<MemoryModuleType<?>>>> MEMORIES = new ObjectArrayList<>();
    private static final ObjectArrayList<Consumer<RegisterEvent.RegisterHelper<ParticleType<?>>>> PARTICLES = new ObjectArrayList<>();
    private static final ObjectArrayList<Consumer<RegisterEvent.RegisterHelper<RecipeSerializer<?>>>> RECIPES = new ObjectArrayList<>();
    private static final ObjectArrayList<Consumer<RegisterEvent.RegisterHelper<SensorType<?>>>> SENSORS = new ObjectArrayList<>();
    private static final ObjectArrayList<Consumer<RegisterEvent.RegisterHelper<SoundEvent>>> SOUNDS = new ObjectArrayList<>();
    private static final ObjectArrayList<Consumer<RegisterEvent.RegisterHelper<StructureType<?>>>> STRUCTURES = new ObjectArrayList<>();
    private static final ObjectArrayList<Consumer<RegisterEvent.RegisterHelper<StructurePieceType>>> STRUCTURE_PIECES = new ObjectArrayList<>();
    private static final ObjectArrayList<CooldownCategory> COOLDOWN_CATEGORIES = new ObjectArrayList<>();

    private static <T, V extends T> V register(List<Consumer<RegisterEvent.RegisterHelper<T>>> holder, ResourceLocation key, V value) {
        holder.add(registry -> registry.register(key, value));
        return value;
    }

    public static void registerRegistries(NewRegistryEvent event) {
        event.register(ArmorEffect.REGISTRY);
        event.register(ArmorEffectSourceType.REGISTRY);
        event.register(CooldownCategory.REGISTRY);
        event.register(DragonType.REGISTRY);
        event.register(DragonVariant.REGISTRY);
    }

    public static void registerEntries(RegisterEvent event) {
        if (register(event, Registries.BLOCK, BlockHolder::registerEntries)) return;
        if (register(event, Registries.BLOCK_ENTITY_TYPE, BlockEntityHolder::registerEntries)) return;
        if (register(event, Registries.ENTITY_TYPE, EntityHolder::registerEntries)) return;
        if (register(event, Registries.ITEM, ItemHolder::registerEntries)) return;
        if (register(event, Registries.MOB_EFFECT, EffectHolder::registerEntries)) return;
        event.register(Registries.ACTIVITY, registry -> {
            for (var value : RegistryHandler.ACTIVITIES) {
                value.accept(registry);
            }
        });
        event.register(Registries.CONSUME_EFFECT_TYPE, registry -> {
            for (var value : RegistryHandler.CONSUMERS) {
                value.accept(registry);
            }
        });
        event.register(Registries.DATA_COMPONENT_TYPE, registry -> {
            for (var value : RegistryHandler.COMPONENTS) {
                value.accept(registry);
            }
        });
        event.register(Registries.CREATIVE_MODE_TAB, registry -> DMItemGroups.register((category, title, icon) ->
                registry.register(category.key, CreativeModeTab.builder().title(Component.translatable(title)).icon(icon).displayItems(category).build())
        ));
        event.register(Registries.MENU, DMScreenHandlers::register);
        event.register(Registries.PARTICLE_TYPE, registry -> {
            for (var value : RegistryHandler.PARTICLES) {
                value.accept(registry);
            }
        });
        event.register(Registries.RECIPE_SERIALIZER, registry -> {
            for (var value : RegistryHandler.RECIPES) {
                value.accept(registry);
            }
        });
        event.register(Registries.SOUND_EVENT, registry -> {
            for (var value : RegistryHandler.SOUNDS) {
                value.accept(registry);
            }
        });
        event.register(Registries.STRUCTURE_TYPE, registry -> {
            for (var value : RegistryHandler.STRUCTURES) {
                value.accept(registry);
            }
        });
        event.register(Registries.STRUCTURE_PIECE, registry -> {
            for (var value : RegistryHandler.STRUCTURE_PIECES) {
                value.accept(registry);
            }
        });
        event.register(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, registry ->
                registry.register(makeId("flute_holder"), DMAttachments.FLUTE_HOLDER)
        );
        event.register(NeoForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, registry -> {
            registry.register(makeId("dragon_type"), DragonType.SERIALIZER);
            registry.register(makeId("dragon_variant"), DragonVariant.SERIALIZER);
        });
        event.register(DragonMountsShared.ARMOR_EFFECT, registry -> {
            for (var value : RegistryHandler.ARMOR_EFFECTS) {
                value.accept(registry);
            }
        });
        event.register(DragonMountsShared.ARMOR_EFFECT_SOURCE, registry -> {
            registry.register(withDefaultNamespace("component"), ArmorEffectSourceType.COMPONENT);
            registry.register(makeId("builtin"), ArmorEffectSourceType.BUILTIN);
        });
        event.register(DragonMountsShared.COOLDOWN_CATEGORY, registry -> {
            for (var category : RegistryHandler.COOLDOWN_CATEGORIES) {
                registry.register(category.identifier, category);
            }
        });
        event.register(DragonMountsShared.DRAGON_TYPE, DragonTypeBuilder::register);
        event.register(DragonMountsShared.DRAGON_VARIANT, registry -> {
            for (var variant : DragonVariants.BUILTIN_VALUES) {
                registry.register(variant.identifier, variant);
            }
        });
    }

    static <T> boolean register(RegisterEvent event, ResourceKey<? extends Registry<T>> key, Consumer<Registry<T>> action) {
        var registry = event.getRegistry(key);
        if (registry == null) return false;
        action.accept(registry);
        return true;
    }
}
