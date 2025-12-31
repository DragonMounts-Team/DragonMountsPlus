package net.dragonmounts.neo.data;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dragonmounts.neo.common.init.DMBlocks;
import net.dragonmounts.neo.common.init.DragonVariants;
import net.dragonmounts.neo.compat.registry.BlockHolder;
import net.dragonmounts.neo.compat.registry.DragonVariant;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DMBlockLootProvider extends BlockLootSubProvider {
    private final ObjectArrayList<Block> blocks = new ObjectArrayList<>();

    public DMBlockLootProvider(HolderLookup.Provider lookup) {
        super(Stream.concat(
                DMBlocks.BUILTIN_DRAGON_EGGS.stream().map(ItemLike::asItem),
                DragonVariants.BUILTIN_VALUES.stream().map(variant -> variant.head.item.get())
        ).collect(Collectors.toSet()), FeatureFlags.REGISTRY.allFlags(), lookup);
    }

    protected void dropSelf(BlockHolder<?> block) {
        var value = block.get();
        this.dropSelf(value);
        this.blocks.add(value);
    }

    protected void dropHead(DragonVariant variant) {
        var head = variant.head;
        var value = head.standing.get();
        this.dropOther(value, head);
        this.blocks.add(value);
    }

    @Override
    public void generate() {
        this.dropSelf(DMBlocks.DRAGON_NEST);
        DMBlocks.BUILTIN_DRAGON_EGGS.forEach(this::dropSelf);
        DMBlocks.BUILTIN_DRAGON_SCALE_BLOCKS.forEach(this::dropSelf);
        DragonVariants.BUILTIN_VALUES.forEach(this::dropHead);
    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return this.blocks;
    }
}
