package net.dragonmounts.neo.common.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.dragonmounts.neo.common.component.impl.ContorlGrowthConsumeEffect;
import net.dragonmounts.neo.common.init.DMDataComponents;
import net.dragonmounts.neo.common.tag.DMItemTags;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record DragonFood(
        int age,
        float health,
        float tamingProbability,
        boolean requiresOwner,
        boolean canAlwaysFeed,
        Holder<SoundEvent> majorSound,
        Optional<Holder<SoundEvent>> minorSound,
        Optional<ItemStack> particles,
        List<ConsumeEffect> effects
) {
    public static final DragonFood RAW_MEAT = new DragonFood(1500, 2.0F, 0.25F, false, false, SoundEvents.GENERIC_EAT, Optional.empty(), Optional.empty(), Collections.emptyList());
    public static final DragonFood COOKED_MEAT = new DragonFood(2500, 3.0F, 0.375F, false, false, SoundEvents.GENERIC_EAT, Optional.empty(), Optional.empty(), Collections.emptyList());
    public static final Codec<DragonFood> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("age", 0).forGetter(DragonFood::age),
            Codec.FLOAT.optionalFieldOf("health", 0.0F).forGetter(DragonFood::health),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("taming_probability", 0.25F).forGetter(DragonFood::tamingProbability),
            Codec.BOOL.optionalFieldOf("requires_owner", false).forGetter(DragonFood::requiresOwner),
            Codec.BOOL.optionalFieldOf("can_always_feed", false).forGetter(DragonFood::canAlwaysFeed),
            SoundEvent.CODEC.optionalFieldOf("major_sound", SoundEvents.GENERIC_EAT).forGetter(DragonFood::majorSound),
            SoundEvent.CODEC.optionalFieldOf("minor_sound").forGetter(DragonFood::minorSound),
            ItemStack.CODEC.optionalFieldOf("override_particles").forGetter(DragonFood::particles),
            ConsumeEffect.CODEC.listOf().optionalFieldOf("side_effects", Collections.emptyList()).forGetter(DragonFood::effects)
    ).apply(instance, DragonFood::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, DragonFood> STREAM_CODEC = StreamCodec.ofMember(DragonFood::encode, DragonFood::decode);

    public static DragonFood decode(RegistryFriendlyByteBuf buffer) {
        return new DragonFood(
                0,
                0.0F,
                0.0F,
                false,
                buffer.readBoolean(),
                SoundEvent.STREAM_CODEC.decode(buffer),
                buffer.readBoolean()
                        ? Optional.of(SoundEvent.STREAM_CODEC.decode(buffer))
                        : Optional.empty(),
                buffer.readBoolean()
                        ? Optional.of(ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer))
                        : Optional.empty(),
                Collections.emptyList()
        );
    }

    public void encode(RegistryFriendlyByteBuf buffer) {
        buffer.writeBoolean(this.canAlwaysFeed);
        SoundEvent.STREAM_CODEC.encode(buffer, this.majorSound);
        Runnable writeNull = () -> buffer.writeBoolean(false);
        this.minorSound.ifPresentOrElse(sound -> {
            buffer.writeBoolean(true);
            SoundEvent.STREAM_CODEC.encode(buffer, sound);
        }, writeNull);
        this.particles.ifPresentOrElse(stack -> {
            buffer.writeBoolean(true);
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, stack);
        }, writeNull);
    }

    private static final Reference2ObjectOpenHashMap<Item, @Nullable DragonFood> FALLBACKS = new Reference2ObjectOpenHashMap<>();

    public static void setFallback(Item item, DragonFood fallback) {
        FALLBACKS.put(item, fallback);
    }

    public static boolean isDragonFood(ItemStack stack) {
        if (stack.getComponents().has(DMDataComponents.DRAGON_FOOD)) return true;
        var item = stack.getItem();
        if (FALLBACKS.containsKey(item)) return true;
        @SuppressWarnings("deprecation") var holder = item.builtInRegistryHolder();
        return !holder.is(DMItemTags.DRAGON_INEDIBLE) && (
                holder.is(DMItemTags.COOKED_DRAGON_FOODS) || holder.is(DMItemTags.RAW_DRAGON_FOODS)
        );
    }

    public static @Nullable DragonFood getInstance(ItemStack stack) {
        var instance = stack.get(DMDataComponents.DRAGON_FOOD);
        if (instance != null) return instance;
        var item = stack.getItem();
        instance = FALLBACKS.get(item);
        if (instance != null) return instance;
        @SuppressWarnings("deprecation") var holder = item.builtInRegistryHolder();
        if (holder.is(DMItemTags.DRAGON_INEDIBLE)) return null;
        if (holder.is(DMItemTags.COOKED_DRAGON_FOODS)) return COOKED_MEAT;
        if (holder.is(DMItemTags.RAW_DRAGON_FOODS)) return RAW_MEAT;
        return null;
    }

    static {
        var minorDrink = Optional.<Holder<SoundEvent>>of(SoundEvents.GENERIC_DRINK);
        var emptyEffects = Collections.<ConsumeEffect>emptyList();
        setFallback(Items.HONEY_BOTTLE, new DragonFood(
                100,
                1.0F,
                0.25F,
                true,
                true,
                SoundEvents.HONEY_DRINK,
                Optional.empty(),
                Optional.of(ItemStack.EMPTY),
                Collections.singletonList(new ContorlGrowthConsumeEffect(true))
        ));
        setFallback(Items.POISONOUS_POTATO, new DragonFood(
                100,
                -1.0F,
                0.0F,
                true,
                true,
                SoundEvents.GENERIC_EAT,
                Optional.empty(),
                Optional.empty(),
                Collections.singletonList(new ContorlGrowthConsumeEffect(false))
        ));
        setFallback(Items.COD_BUCKET, new DragonFood(
                1500,
                2.0F,
                0.25F,
                false,
                false,
                SoundEvents.GENERIC_EAT,
                minorDrink,
                Optional.of(new ItemStack(Items.COD)),
                emptyEffects
        ));
        setFallback(Items.SALMON_BUCKET, new DragonFood(
                1500,
                2.0F,
                0.25F,
                false,
                false,
                SoundEvents.GENERIC_EAT,
                minorDrink,
                Optional.of(new ItemStack(Items.SALMON)),
                emptyEffects
        ));
        setFallback(Items.TROPICAL_FISH_BUCKET, new DragonFood(
                1500,
                2.0F,
                0.25F,
                false,
                false,
                SoundEvents.GENERIC_EAT,
                minorDrink,
                Optional.of(new ItemStack(Items.TROPICAL_FISH)),
                emptyEffects
        ));
        setFallback(Items.RABBIT_STEW, new DragonFood(
                2500,
                3.0F,
                0.375F,
                false,
                false,
                SoundEvents.GENERIC_EAT,
                minorDrink,
                Optional.empty(),
                emptyEffects
        ));
    }
}
