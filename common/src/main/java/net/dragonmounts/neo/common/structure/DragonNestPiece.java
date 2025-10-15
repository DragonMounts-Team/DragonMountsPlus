package net.dragonmounts.neo.common.structure;

import net.dragonmounts.neo.common.init.DMStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProtectedBlockProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jetbrains.annotations.NotNullByDefault;

/// @see net.minecraft.world.level.levelgen.structure.structures.RuinedPortalPiece
@SuppressWarnings("UnstableApiUsage")
@NotNullByDefault
public class DragonNestPiece extends TemplateStructurePiece {
    private static StructurePlaceSettings makeSettings(StructureTemplateManager manager, CompoundTag tag, ResourceLocation structure) {
        return makeSettings(
                Mirror.valueOf(tag.getString("Mirror")),
                Rotation.valueOf(tag.getString("Rotation")),
                getPivot(manager.getOrCreate(structure).getSize())
        );
    }

    private static StructurePlaceSettings makeSettings(
            Mirror mirror,
            Rotation rotation,
            BlockPos pivot
    ) {
        return new StructurePlaceSettings()
                .setRotation(rotation)
                .setMirror(mirror)
                .setRotationPivot(pivot)
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK)
                .addProcessor(new ProtectedBlockProcessor(BlockTags.FEATURES_CANNOT_REPLACE));
    }

    public static BlockPos getPivot(Vec3i size) {
        return new BlockPos(size.getX() / 2, 0, size.getZ() / 2);
    }

    public DragonNestPiece(
            StructureTemplateManager manager,
            BlockPos pos,
            ResourceLocation template,
            Rotation rotation,
            Mirror mirror,
            BlockPos pivot
    ) {
        super(DMStructures.DRAGON_NEST_PIECE, 0, manager, template, template.toString(), makeSettings(mirror, rotation, pivot), pos);
    }

    public DragonNestPiece(StructureTemplateManager manager, CompoundTag tag) {
        super(DMStructures.DRAGON_NEST_PIECE, tag, manager, structure -> makeSettings(manager, tag, structure));
    }

    @Override
    protected void handleDataMarker(String name, BlockPos pos, ServerLevelAccessor level, RandomSource random, BoundingBox box) {
        switch (name) {
            case "Sentry" -> {
                var shulker = EntityType.SHULKER.create(level.getLevel(), EntitySpawnReason.STRUCTURE);
                if (shulker != null) {
                    shulker.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                    level.addFreshEntity(shulker);
                }
            }
            case "Elytra" -> {
                var frame = new ItemFrame(level.getLevel(), pos, this.placeSettings.getRotation().rotate(Direction.NORTH));
                frame.setItem(new ItemStack(Items.ELYTRA), false);
                level.addFreshEntity(frame);
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        super.addAdditionalSaveData(context, tag);
        tag.putString("Rotation", this.placeSettings.getRotation().name());
        tag.putString("Mirror", this.placeSettings.getMirror().name());
    }
}
