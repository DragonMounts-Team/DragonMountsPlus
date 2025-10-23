package net.dragonmounts.neo.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.function.BiConsumer;

import static net.dragonmounts.neo.common.DragonMountsShared.makeKey;
import static net.minecraft.resources.ResourceLocation.withDefaultNamespace;
import static net.minecraft.world.level.storage.loot.LootPool.lootPool;
import static net.minecraft.world.level.storage.loot.LootTable.lootTable;
import static net.minecraft.world.level.storage.loot.entries.LootItem.lootTableItem;
import static net.minecraft.world.level.storage.loot.entries.NestedLootTable.lootTableReference;
import static net.minecraft.world.level.storage.loot.functions.SetItemCountFunction.setCount;
import static net.minecraft.world.level.storage.loot.providers.number.UniformGenerator.between;

public class DMChestLootProvider implements LootTableSubProvider {
    public DMChestLootProvider(HolderLookup.Provider ignored) {}

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        output.accept(makeKey(Registries.LOOT_TABLE, "chests/undead_dragon_nest"), lootTable()
                .withPool(lootPool().add(lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, withDefaultNamespace("chests/nether_bridge")))))
                .withPool(lootPool().setRolls(between(2.0F, 4.0F))
                        .add(lootTableItem(Items.DIAMOND).setWeight(2).apply(setCount(between(1.0F, 3.0F))))
                        .add(lootTableItem(Items.GOLD_INGOT).setWeight(3).apply(setCount(between(2.0F, 6.0F))))
                        .add(lootTableItem(Items.OBSIDIAN).setWeight(5).apply(setCount(between(3.0F, 5.0F)))))
                .withPool(lootPool().add(lootTableItem(Items.NAME_TAG)))
        );
        output.accept(makeKey(Registries.LOOT_TABLE, "chests/water_dragon_nest"), lootTable()
                .withPool(lootPool().add(lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, withDefaultNamespace("chests/simple_dungeon")))))
                .withPool(lootPool().setRolls(between(1.0F, 2.0F)).add(lootTableItem(Items.GOLD_INGOT).setWeight(3).apply(setCount(between(6.0F, 8.0F)))))
        );
    }
}
