package net.dragonmounts.neo.compat.registry;

import com.google.common.collect.ImmutableMultimap;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.dragonmounts.neo.common.api.DragonTypified;
import net.dragonmounts.neo.common.client.ClientDragonEntity;
import net.dragonmounts.neo.common.entity.breath.DragonBreath;
import net.dragonmounts.neo.common.entity.breath.impl.FireBreath;
import net.dragonmounts.neo.common.entity.dragon.HatchableDragonEggEntity;
import net.dragonmounts.neo.common.entity.dragon.ServerDragonEntity;
import net.dragonmounts.neo.common.entity.dragon.TameableDragonEntity;
import net.dragonmounts.neo.common.init.DMSounds;
import net.minecraft.Util;
import net.minecraft.core.DefaultedMappedRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.RegistryBuilder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.dragonmounts.neo.common.DragonMountsShared.DRAGON_TYPE;
import static net.dragonmounts.neo.common.DragonMountsShared.makeId;
import static net.dragonmounts.neo.common.util.EntityUtil.addOrMergeEffect;

public class DragonType implements TooltipProvider, DragonTypified {
    public static final String SERIALIZATION_KEY = "DragonType";
    public static final ResourceLocation DEFAULT_KEY = makeId("ender");
    public static final DefaultedMappedRegistry<DragonType> REGISTRY
            = (DefaultedMappedRegistry<DragonType>) new RegistryBuilder<>(DRAGON_TYPE).sync(true).defaultKey(DEFAULT_KEY).create();
    public static final Codec<DragonType> CODEC = REGISTRY.byNameCodec();
    public static final StreamCodec<RegistryFriendlyByteBuf, DragonType> STREAM_CODEC = ByteBufCodecs.registry(DRAGON_TYPE);
    public static final EntityDataSerializer<DragonType> SERIALIZER = EntityDataSerializer.forValueType(STREAM_CODEC);
    public final int color;
    public final boolean convertible;
    public final ResourceLocation identifier;
    public final ImmutableMultimap<Holder<Attribute>, AttributeModifier> attributes;
    public final ParticleOptions sneezeParticle;
    public final ParticleOptions eggParticle;
    public final MapColor scaleColor;
    public final DragonVariant.Manager variants = new DragonVariant.Manager(this);
    public final TranslatableContents name;
    public final ArmorMaterial material;
    public final ToolMaterial tier;
    private final Reference2ObjectOpenHashMap<Class<?>, Object> map = new Reference2ObjectOpenHashMap<>();
    private final Style style;
    private final Set<ResourceKey<DamageType>> immunities;
    private final Set<Block> blocks;
    private final Set<ResourceKey<Biome>> biomes;
    private ResourceKey<LootTable> lootTable;

    public DragonType(ResourceLocation identifier, DragonTypeBuilder builder) {
        this.identifier = identifier;
        this.color = builder.color;
        this.convertible = builder.convertible;
        this.style = Style.EMPTY.withColor(TextColor.fromRgb(this.color));
        this.attributes = builder.attributes.build();
        this.immunities = builder.immunities.build();
        this.blocks = builder.blocks.build();
        this.biomes = builder.biomes.build();
        this.sneezeParticle = builder.sneezeParticle;
        this.eggParticle = builder.eggParticle;
        this.scaleColor = builder.scaleColor;
        this.name = new TranslatableContents(this.makeDescriptionId(), null, TranslatableContents.NO_ARGS);
        this.material = builder.material == null
                ? ArmorMaterials.ARMADILLO_SCUTE
                : builder.material.build(builder.scales, ResourceKey.create(EquipmentAssets.ROOT_ID, identifier.withSuffix("_dragon_scale")));
        this.tier = builder.tier == null ? null : builder.tier.build(builder.scales);
    }

    public final ResourceLocation getId() {
        return this.identifier;
    }

    protected String makeDescriptionId() {
        return Util.makeDescriptionId("dragon_type", this.identifier);
    }

    protected ResourceLocation makeLootLocation() {
        return this.identifier.withPath("entities/dragon/" + this.identifier.getPath());
    }

    public final ResourceKey<LootTable> getLootTable() {
        return this.lootTable == null
                ? this.lootTable = ResourceKey.create(Registries.LOOT_TABLE, this.makeLootLocation())
                : this.lootTable;
    }

    public MutableComponent getName() {
        return MutableComponent.create(this.name).setStyle(this.style);
    }

    public MutableComponent getFormattedName(String pattern) {
        return Component.translatable(pattern, MutableComponent.create(this.name));
    }

    public boolean isInvulnerableTo(DamageSource source) {
        return source.typeHolder().is(this.immunities::contains);
    }

    public void tickServer(ServerDragonEntity dragon) {}

    /// Do **NOT** directly access client only class here!
    public void tickClient(ClientDragonEntity dragon) {}

    public <T extends LivingEntity & DragonTypified.Mutable> void onThunderHit(T entity, LightningBolt bolt) {
        if (entity instanceof HatchableDragonEggEntity) return;
        addOrMergeEffect(entity, MobEffects.DAMAGE_BOOST, 700, 0, false, true, true);//35s
    }

    public boolean isInHabitat(LivingEntity entity) {
        return false;
    }

    public @Nullable DragonBreath initBreath(TameableDragonEntity dragon) {
        return new FireBreath(dragon, 0.7F);
    }

    public SoundEvent getAmbientSound(TameableDragonEntity dragon) {
        return dragon.isBaby()
                ? DMSounds.DRAGON_PURR_HATCHLING
                : dragon.getRandom().nextFloat() < 0.33F
                ? DMSounds.DRAGON_PURR
                : DMSounds.DRAGON_AMBIENT;
    }

    public SoundEvent getDeathSound(TameableDragonEntity dragon) {
        return DMSounds.DRAGON_DEATH;
    }

    public @Nullable SoundEvent getRoarSound(TameableDragonEntity dragon) {
        return dragon.isBaby() ? DMSounds.DRAGON_ROAR_HATCHLING : DMSounds.DRAGON_ROAR;
    }

    public Vec3 locatePassenger(int index, boolean sitting) {
        return switch (index) {
            case 1 -> new Vec3(6.5, sitting ? 26.5 : 44.0, -10);
            case 2 -> new Vec3(-6.5, sitting ? 26.5 : 44.0, -10);
            case 3 -> new Vec3(12.0, sitting ? 10.5 : 28.0, -6.0);
            case 4 -> new Vec3(-12.0, sitting ? 10.5 : 28.0, -6.0);
            default -> new Vec3(0.0, sitting ? 29.0 : 46.5, 20.0);
        };
    }

    public boolean isHabitat(Block block) {
        return this.blocks.contains(block);
    }

    public boolean isHabitat(@Nullable ResourceKey<Biome> biome) {
        return this.biomes.contains(biome);
    }

    @SuppressWarnings("UnusedReturnValue")
    public final <T> @Nullable T bindInstance(Class<T> clazz, T instance) {
        return clazz.cast(this.map.put(clazz, instance));
    }

    @Contract("_, !null -> !null")
    public final <T> @Nullable T getInstance(Class<T> clazz, T fallback) {
        return clazz.cast(this.map.getOrDefault(clazz, fallback));
    }

    public <T> void ifPresent(Class<T> clazz, Consumer<? super T> consumer) {
        var value = this.map.get(clazz);
        if (value != null) {
            consumer.accept(clazz.cast(value));
        }
    }

    public final <T, V> V ifPresent(Class<T> clazz, Function<? super T, V> function, V fallback) {
        var value = this.map.get(clazz);
        return value == null ? fallback : function.apply(clazz.cast(value));
    }

    @Override
    public void addToTooltip(@NotNull Item.TooltipContext context, Consumer<Component> consumer, @NotNull TooltipFlag flag) {
        consumer.accept(this.getName());
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (
                other instanceof DragonType that && Objects.equals(this.identifier, that.identifier)
        );
    }

    @Override
    public int hashCode() {
        return this.identifier.hashCode();
    }

    @Override
    public final DragonType getDragonType() {
        return this;
    }

    public static <T extends LivingEntity & DragonTypified.Mutable> void convertByLightning(T entity, DragonType type) {
        entity.convertTo(type, false);
        entity.playSound(SoundEvents.END_PORTAL_SPAWN, 2, 1);
        entity.playSound(SoundEvents.PORTAL_TRIGGER, 2, 1);
    }
}
