package net.dragonmounts.neo.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.WritableRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DMLootProvider extends LootTableProvider {
    public DMLootProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, Collections.emptySet(), List.of(
                new LootTableProvider.SubProviderEntry(DMBlockLootProvider::new, LootContextParamSets.BLOCK),
                new LootTableProvider.SubProviderEntry(DMChestLootProvider::new, LootContextParamSets.CHEST),
                new LootTableProvider.SubProviderEntry(DMEntityLootProvider::new, LootContextParamSets.ENTITY)
        ), registries);
    }

    @Override
    protected void validate(
            WritableRegistry<LootTable> registry,
            ValidationContext context,
            ProblemReporter.Collector collector
    ) {/* just shut up */}
}
