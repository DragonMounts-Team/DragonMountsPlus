package net.dragonmounts.neo.data;

import net.dragonmounts.neo.common.init.DMStructureSets;
import net.dragonmounts.neo.common.init.DMStructures;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;

public class DMDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        var pack = generator.createPack();
        pack.addProvider(DMModelProvider::new);
        pack.addProvider(DMBiomeTagProvider::new);
        pack.addProvider(DMEntityTagProvider::new);
        pack.addProvider(DMStructureTagProvider::new);
        var block = pack.addProvider(DMBlockTagProvider::new);
        pack.addProvider((output, future) -> new DMItemTagProvider(output, future, block));
        pack.addProvider(DMRecipeProvider.Factory::new);
        pack.addProvider(DMEquipmentAssetProvider::from);
        pack.addProvider(DMDynamicProvider::new);
        pack.addProvider(DMBlockLootProvider::new);
        pack.addProvider(DMChestLootProvider::new);
        pack.addProvider(DMEntityLootProvider::new);
        //noinspection ConstantValue
        if (false) {
            pack.addProvider(StructureConvertor::stringifyStructures);
        }
        //noinspection ConstantValue
        if (false) {
            pack.addProvider(StructureConvertor::updateStructures);
        }
    }

    @Override
    public void buildRegistry(RegistrySetBuilder builder) {
        builder.add(Registries.STRUCTURE, DMStructures::bootstrap);
        builder.add(Registries.STRUCTURE_SET, DMStructureSets::bootstrap);
    }
}