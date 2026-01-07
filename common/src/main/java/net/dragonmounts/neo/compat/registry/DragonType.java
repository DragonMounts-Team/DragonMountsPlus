package net.dragonmounts.neo.compat.registry;

import com.google.common.collect.ImmutableMultimap;
import com.mojang.serialization.Codec;
import net.dragonmounts.neo.common.api.DragonTypified;
import net.dragonmounts.neo.common.client.ClientDragonEntity;
import net.dragonmounts.neo.common.entity.breath.DragonBreath;
import net.dragonmounts.neo.common.entity.dragon.ServerDragonEntity;
import net.dragonmounts.neo.common.entity.dragon.TameableDragonEntity;
import net.dragonmounts.neo.compat.Dummy;
import net.minecraft.core.DefaultedMappedRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

import static net.dragonmounts.neo.common.DragonMountsShared.DRAGON_TYPE;
import static net.dragonmounts.neo.common.DragonMountsShared.makeId;

@SuppressWarnings("unused")
public class DragonType implements TooltipProvider, DragonTypified {
    public static final String SERIALIZATION_KEY = "DragonType";
    public static final ResourceLocation DEFAULT_KEY = makeId("ender");
    public static final DefaultedMappedRegistry<DragonType> REGISTRY = Dummy.get();
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

    public DragonType(ResourceLocation identifier, DragonTypeBuilder builder) {
        this.identifier = identifier;
        this.color = Dummy.get();
        this.convertible = Dummy.get();
        this.attributes = Dummy.get();
        this.sneezeParticle = Dummy.get();
        this.eggParticle = Dummy.get();
        this.scaleColor = Dummy.get();
        this.name = Dummy.get();
        this.material = Dummy.get();
        this.tier = Dummy.get();
    }

    public final ResourceLocation getId() {
        return Dummy.get();
    }

    protected String makeDescriptionId() {
        return Dummy.get();
    }

    protected ResourceLocation makeLootLocation() {
        return Dummy.get();
    }

    public final ResourceKey<LootTable> getLootTable() {
        return Dummy.get();
    }

    public MutableComponent getName() {
        return Dummy.get();
    }

    public MutableComponent getFormattedName(String pattern) {
        return Dummy.get();
    }

    public boolean isInvulnerableTo(DamageSource source) {
        return Dummy.get();
    }

    public void tickServer(ServerDragonEntity dragon) {}

    /// Do **NOT** directly access client only class here!
    public void tickClient(ClientDragonEntity dragon) {}

    public <T extends LivingEntity & DragonTypified.Mutable> void onThunderHit(T entity, LightningBolt bolt) {}

    public boolean isInHabitat(LivingEntity entity) {
        return false;
    }

    public @Nullable DragonBreath initBreath(TameableDragonEntity dragon) {
        return Dummy.get();
    }

    public SoundEvent getAmbientSound(TameableDragonEntity dragon) {
        return Dummy.get();
    }

    public SoundEvent getDeathSound(TameableDragonEntity dragon) {
        return Dummy.get();
    }

    public @Nullable SoundEvent getRoarSound(TameableDragonEntity dragon) {
        return Dummy.get();
    }

    public Vec3 locatePassenger(int index, boolean sitting) {
        return Dummy.get();
    }

    public boolean isHabitat(Block block) {
        return Dummy.get();
    }

    public boolean isHabitat(@Nullable ResourceKey<Biome> biome) {
        return Dummy.get();
    }

    public final <T> @Nullable T bindInstance(Class<T> clazz, T instance) {
        return null;
    }

    @Contract("_, !null -> !null")
    public final <T> @Nullable T getInstance(Class<T> clazz, @Nullable T fallback) {
        return fallback;
    }

    public <T> void ifPresent(Class<T> clazz, Consumer<? super T> consumer) {}

    public final <T, V> V ifPresent(Class<T> clazz, Function<? super T, V> function, V fallback) {
        return Dummy.get();
    }

    @Override
    public void addToTooltip(@NotNull Item.TooltipContext context, @NotNull Consumer<Component> consumer, @NotNull TooltipFlag flag) {}

    @Override
    public final DragonType getDragonType() {
        return this;
    }

    public static <T extends LivingEntity & DragonTypified.Mutable> void convertByLightning(T entity, DragonType type) {}
}
