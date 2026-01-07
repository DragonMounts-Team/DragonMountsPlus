package net.dragonmounts.neo.compat.registry;

import com.mojang.serialization.Codec;
import net.dragonmounts.neo.common.api.DragonTypified;
import net.dragonmounts.neo.common.client.variant.VariantAppearance;
import net.dragonmounts.neo.common.util.DragonHead;
import net.minecraft.core.DefaultedMappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.registries.RegistryBuilder;
import net.neoforged.neoforge.registries.callback.AddCallback;
import net.neoforged.neoforge.registries.callback.ClearCallback;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;

import static it.unimi.dsi.fastutil.Arrays.MAX_ARRAY_SIZE;
import static net.dragonmounts.neo.common.DragonMountsShared.DRAGON_VARIANT;
import static net.dragonmounts.neo.common.DragonMountsShared.makeId;

public class DragonVariant implements DragonTypified {
    public static final String SERIALIZATION_KEY = "Variant";
    public static final ResourceLocation DEFAULT_KEY = makeId("ender_female");
    public static final DefaultedMappedRegistry<DragonVariant> REGISTRY;
    public static final Codec<DragonVariant> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, DragonVariant> STREAM_CODEC;
    public static final EntityDataSerializer<DragonVariant> SERIALIZER;

    int index = -1;// non-private to simplify nested class access
    public final DragonType type;
    public final ResourceLocation identifier;
    public final VariantAppearance appearance;
    public final DragonHead head;

    public DragonVariant(
            DragonType type,
            ResourceLocation identifier,
            VariantAppearance appearance,
            Function<DragonVariant, DragonHead> factory
    ) {
        this.type = type;
        this.identifier = identifier;
        this.appearance = appearance;
        this.head = factory.apply(this);
        type.variants.add(this);
    }

    @Override
    public final DragonType getDragonType() {
        return this.type;
    }

    /// Simplified {@link it.unimi.dsi.fastutil.objects.ReferenceArrayList}
    public static final class Manager implements DragonTypified {
        public static final int DEFAULT_INITIAL_CAPACITY = 8;
        public final DragonType type;
        private DragonVariant[] variants = {};
        private int size;

        public Manager(DragonType type) {
            this.type = type;
        }

        private void grow(int capacity) {
            if (capacity <= this.variants.length) return;
            if (this.variants.length > 0)
                capacity = (int) Math.max(Math.min((long) this.variants.length + (this.variants.length >> 1), MAX_ARRAY_SIZE), capacity);
            else if (capacity < DEFAULT_INITIAL_CAPACITY)
                capacity = DEFAULT_INITIAL_CAPACITY;
            final DragonVariant[] array = new DragonVariant[capacity];
            System.arraycopy(this.variants, 0, array, 0, size);
            this.variants = array;
            assert this.size <= this.variants.length;
        }

        @SuppressWarnings("UnusedReturnValue")
        boolean add(final DragonVariant variant) {
            if (variant.type != this.type || variant.index >= 0) return false;
            this.grow(this.size + 1);
            variant.index = this.size;
            this.variants[this.size++] = variant;
            assert this.size <= this.variants.length;
            return true;
        }

        void clear() {// non-private to simplify nested class access
            for (int i = 0; i < this.size; ++i) {
                this.variants[i].index = -1;
                this.variants[i] = null;
            }
            this.size = 0;
        }

        @Contract("!null, !null -> !null")
        public @Nullable DragonVariant draw(RandomSource random, @Nullable DragonVariant current) {
            switch (this.size) {
                case 0:
                    return current;
                case 1:
                    return this.variants[0];
            }
            if (current == null || current.type != this.type) {
                return this.variants[random.nextInt(this.size)];
            }
            if (this.size == 2) return this.variants[(current.index ^ 1) & 1];//current.index == 0 ? 1 : 0
            int index = random.nextInt(this.size - 1);
            return this.variants[index < current.index ? index : index + 1];
        }

        public int size() {
            return this.size;
        }

        @Override
        public DragonType getDragonType() {
            return this.type;
        }
    }

    static {
        @ParametersAreNonnullByDefault
        class Callback implements AddCallback<DragonVariant>, ClearCallback<DragonVariant> {
            @Override
            public void onAdd(Registry<DragonVariant> registry, int id, ResourceKey<DragonVariant> key, DragonVariant value) {
                value.type.variants.add(value);
            }

            @Override
            public void onClear(Registry<DragonVariant> registry, boolean full) {
                if (full) {
                    for (var variant : registry) {
                        variant.type.variants.clear();
                    }
                }
            }
        }
        REGISTRY = (DefaultedMappedRegistry<DragonVariant>) new RegistryBuilder<>(DRAGON_VARIANT)
                .defaultKey(DEFAULT_KEY)
                .sync(true)
                .callback(new Callback())
                .create();
        CODEC = REGISTRY.byNameCodec();
        STREAM_CODEC = ByteBufCodecs.registry(DRAGON_VARIANT);
        SERIALIZER = EntityDataSerializer.forValueType(STREAM_CODEC);
    }
}
