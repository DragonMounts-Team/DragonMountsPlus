package net.dragonmounts.neo.common.crafting;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.*;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import net.dragonmounts.neo.common.init.DMItems;
import net.dragonmounts.neo.common.init.DMRecipes;
import net.dragonmounts.neo.common.init.DragonArmorMaterials;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SmithingRecipeDisplay;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "UnstableApiUsage"})
@org.jetbrains.annotations.NotNullByDefault
public class DragonArmorUpgradeRecipe implements SmithingRecipe {
    public static List<ItemAttributeModifiers.Entry> merge(List<ItemAttributeModifiers.Entry> base, ItemLike item) {
        var component = item.asItem().components().get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (component == null) return base;
        var props = new Object2DoubleOpenHashMap<Holder<Attribute>>();
        for (var entry : component.modifiers()) {
            if (entry.slot() != EquipmentSlotGroup.BODY) continue;
            var modifier = entry.modifier();
            if (modifier.is(DMItems.DRAGON_ARMOR_MODIFIER_NAME) && modifier.operation() == AttributeModifier.Operation.ADD_VALUE) {
                props.put(entry.attribute(), modifier.amount());
            }
        }
        var builder = ImmutableList.<ItemAttributeModifiers.Entry>builderWithExpectedSize(base.size() + props.size());
        for (var entry : base) {
            if (entry.slot() == EquipmentSlotGroup.BODY && props.containsKey(entry.attribute())) {
                var modifier = entry.modifier();
                if (modifier.is(DMItems.DRAGON_ARMOR_MODIFIER_NAME)) {
                    if (modifier.operation() == AttributeModifier.Operation.ADD_VALUE) {
                        var attribute = entry.attribute();
                        double amount = props.getDouble(attribute);
                        props.removeDouble(attribute);
                        if (modifier.amount() < amount) {
                            builder.add(new ItemAttributeModifiers.Entry(
                                    attribute,
                                    new AttributeModifier(DMItems.DRAGON_ARMOR_MODIFIER_NAME, amount, AttributeModifier.Operation.ADD_VALUE),
                                    EquipmentSlotGroup.BODY
                            ));
                            continue;
                        }
                    } else {
                        props.removeDouble(entry.attribute());
                    }
                }
            }
            builder.add(entry);
        }
        for (var entry : props.object2DoubleEntrySet()) {
            builder.add(new ItemAttributeModifiers.Entry(
                    entry.getKey(),
                    new AttributeModifier(DMItems.DRAGON_ARMOR_MODIFIER_NAME, entry.getDoubleValue(), AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.BODY
            ));
        }
        return builder.build();
    }

    private final Optional<Ingredient> template = Optional.of(Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE));
    private final Optional<Ingredient> base = Optional.of(Ingredient.of(DMItems.DIAMOND_DRAGON_ARMOR));
    private final Optional<Ingredient> addition;
    private @Nullable PlacementInfo placementInfo;

    public DragonArmorUpgradeRecipe(Ingredient addition) {
        this.addition = Optional.of(addition);
    }

    @Override
    public ItemStack assemble(SmithingRecipeInput input, HolderLookup.Provider registries) {
        var stack = input.base();
        var component = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        boolean found = false;
        var builder = ImmutableList.<ItemAttributeModifiers.Entry>builder();
        for (var entry : component.modifiers()) {
            if (entry.slot() == EquipmentSlotGroup.BODY && entry.attribute().equals(Attributes.ARMOR)) {
                var modifier = entry.modifier();
                if (modifier.is(DMItems.DRAGON_ARMOR_MODIFIER_NAME) && modifier.operation() == AttributeModifier.Operation.ADD_VALUE) {
                    if (modifier.amount() < DragonArmorMaterials.NETHERITE.defense().getOrDefault(ArmorType.BODY, 0)) {
                        builder.add(new ItemAttributeModifiers.Entry(
                                Attributes.ARMOR,
                                new AttributeModifier(DMItems.DRAGON_ARMOR_MODIFIER_NAME, modifier.amount() + 1.0, AttributeModifier.Operation.ADD_VALUE),
                                EquipmentSlotGroup.BODY
                        ));
                        found = true;
                        continue;
                    } else {
                        var result = stack.transmuteCopy(DMItems.NETHERITE_DRAGON_ARMOR, 1);
                        result.set(DataComponents.ATTRIBUTE_MODIFIERS, new ItemAttributeModifiers(merge(
                                component.modifiers(),
                                DMItems.NETHERITE_DRAGON_ARMOR
                        ), component.showInTooltip()));
                        return result;
                    }
                }
            }
            builder.add(entry);
        }
        var result = stack.copy();
        result.set(DataComponents.ATTRIBUTE_MODIFIERS, new ItemAttributeModifiers(found ? builder.build() : merge(
                component.modifiers(),
                DMItems.DIAMOND_DRAGON_ARMOR
        ), component.showInTooltip()));
        return result;
    }

    @Override
    public Optional<Ingredient> templateIngredient() {
        return this.template;
    }

    @Override
    public Optional<Ingredient> baseIngredient() {
        return this.base;
    }

    @Override
    public Optional<Ingredient> additionIngredient() {
        return this.addition;
    }

    @Override
    public RecipeSerializer<DragonArmorUpgradeRecipe> getSerializer() {
        return DMRecipes.DRAGON_ARMOR_UPGRADE;
    }

    @Override
    public PlacementInfo placementInfo() {
        if (this.placementInfo == null) {
            this.placementInfo = PlacementInfo.createFromOptionals(List.of(this.template, this.base, this.addition));
        }
        return this.placementInfo;
    }

    @Override
    public List<RecipeDisplay> display() {
        return Collections.singletonList(new SmithingRecipeDisplay(
                Ingredient.optionalIngredientToDisplay(this.template),
                Ingredient.optionalIngredientToDisplay(this.base),
                Ingredient.optionalIngredientToDisplay(this.addition),
                new SlotDisplay.ItemStackSlotDisplay(new ItemStack(DMItems.NETHERITE_DRAGON_ARMOR)),
                new SlotDisplay.ItemSlotDisplay(Items.SMITHING_TABLE)
        ));
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public static class Serializer implements RecipeSerializer<DragonArmorUpgradeRecipe> {
        public static final MapCodec<DragonArmorUpgradeRecipe> CODEC = new MapCodec<>() {
            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return Stream.empty();
            }

            @Override
            public <T> RecordBuilder<T> encode(DragonArmorUpgradeRecipe input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                return prefix;
            }

            @Override
            public <T> DataResult<DragonArmorUpgradeRecipe> decode(DynamicOps<T> ops, MapLike<T> input) {
                if (ops instanceof RegistryOps<T> registries) {
                    var registry = registries.getter(Registries.ITEM);
                    if (registry.isPresent()) {
                        var items = registry.get().get(ItemTags.NETHERITE_TOOL_MATERIALS);
                        if (items.isPresent()) {
                            return DataResult.success(new DragonArmorUpgradeRecipe(Ingredient.of(items.get())));
                        }
                    }
                }
                return DataResult.error(() -> "Can't decode without registry");
            }
        };
        public static final StreamCodec<RegistryFriendlyByteBuf, DragonArmorUpgradeRecipe> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public void encode(RegistryFriendlyByteBuf buffer, DragonArmorUpgradeRecipe ignored) {}

            @Override
            public DragonArmorUpgradeRecipe decode(RegistryFriendlyByteBuf buffer) {
                return new DragonArmorUpgradeRecipe(Ingredient.of(buffer.registryAccess().lookupOrThrow(Registries.ITEM).getOrThrow(ItemTags.NETHERITE_TOOL_MATERIALS)));
            }
        };

        @Override
        public MapCodec<DragonArmorUpgradeRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, DragonArmorUpgradeRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
