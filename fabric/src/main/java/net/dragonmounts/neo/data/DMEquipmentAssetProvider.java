package net.dragonmounts.neo.data;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.dragonmounts.neo.compat.registry.DragonType;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.EquipmentAsset;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public record DMEquipmentAssetProvider(PackOutput.PathProvider path) implements DataProvider {
    public static DMEquipmentAssetProvider from(FabricDataOutput output) {
        return new DMEquipmentAssetProvider(output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "equipment"));
    }

    @Override
    public @NotNull CompletableFuture<?> run(CachedOutput output) {
        var assets = new Reference2ObjectOpenHashMap<ResourceKey<EquipmentAsset>, EquipmentClientInfo>(DragonType.REGISTRY.size());
        for (var type : DragonType.REGISTRY) {
            var material = type.material;
            if (material == ArmorMaterials.ARMADILLO_SCUTE) continue;
            var key = material.assetId();
            if (assets.putIfAbsent(key, EquipmentClientInfo.builder().addHumanoidLayers(key.location()).build()) != null) {
                throw new IllegalStateException("Tried to register equipment asset twice for id: " + key);
            }
        }
        return DataProvider.saveAll(output, EquipmentClientInfo.CODEC, this.path::json, assets);
    }

    @Override
    public @NotNull String getName() {
        return "Dragon Mounts Equipment Asset Definitions";
    }
}
