package net.dragonmounts.neo.common.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.dragonmounts.neo.common.api.DragonTypified;
import net.dragonmounts.neo.common.block.entity.DragonHeadBlockEntity;
import net.dragonmounts.neo.common.init.DMBlockEntities;
import net.dragonmounts.neo.compat.registry.DragonType;
import net.dragonmounts.neo.compat.registry.DragonVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.function.BiFunction;

import static net.dragonmounts.neo.common.DragonMountsShared.BLOCK_TRANSLATION_KEY_PREFIX;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.POWERED;

/// @see net.minecraft.world.level.block.AbstractSkullBlock
public abstract class DragonHeadBlock extends BaseEntityBlock implements DragonTypified {
    protected static <T extends DragonHeadBlock> MapCodec<T> makeCodec(BiFunction<DragonVariant, Properties, T> factory) {
        return RecordCodecBuilder.mapCodec((instance) -> instance.group(DragonVariant.CODEC.fieldOf("variant").forGetter(DragonHeadBlock::getVariant), propertiesCodec()).apply(instance, factory));
    }

    public static final String TRANSLATION_KEY = BLOCK_TRANSLATION_KEY_PREFIX + "dragon_head";
    public final DragonVariant variant;
    public final boolean isOnWall;

    public DragonHeadBlock(DragonVariant variant, Properties props, boolean isOnWall) {
        super(props);
        this.variant = variant;
        this.isOnWall = isOnWall;
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
    }

    public abstract float getYRotation(BlockState state);

    @Override
    public boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    public @UnknownNullability BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
        if (!level.isClientSide) {
            boolean charged = level.hasNeighborSignal(pos);
            if (charged != state.getValue(POWERED)) {
                level.setBlock(pos, state.setValue(POWERED, charged), 2);
            }
        }
    }

    public final DragonVariant getVariant() {
        return this.variant;
    }

    @Override
    public final DragonType getDragonType() {
        return this.variant.type;
    }

    @Override
    public DragonHeadBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DragonHeadBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide && state.getBlock() instanceof DragonHeadBlock) {
            return createTickerHelper(type, DMBlockEntities.DRAGON_HEAD.get(), DragonHeadBlockEntity::animation);
        }
        return null;
    }
}
