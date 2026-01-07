package net.dragonmounts.neo.common.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.dragonmounts.neo.common.DragonMountsShared;
import net.dragonmounts.neo.common.api.DragonTypified;
import net.dragonmounts.neo.common.block.DragonHeadBlock;
import net.dragonmounts.neo.common.block.DragonHeadStandingBlock;
import net.dragonmounts.neo.common.block.DragonHeadWallBlock;
import net.dragonmounts.neo.common.block.HatchableDragonEggBlock;
import net.dragonmounts.neo.common.init.DragonTypes;
import net.dragonmounts.neo.compat.registry.DragonType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static net.dragonmounts.neo.common.command.DMCommands.createClassCastException;
import static net.dragonmounts.neo.common.init.DragonVariants.ENDER_FEMALE;
import static net.minecraft.commands.SharedSuggestionProvider.matchesSubStr;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.ROTATION_16;

public class TypeCommand {
    @FunctionalInterface
    public interface DragonTypeGetter {
        DragonType get(Block block, ServerLevel level, BlockPos pos, BlockState state);
    }

    @FunctionalInterface
    public interface DragonTypeSetter {
        BlockState set(Block block, ServerLevel level, BlockPos pos, BlockState state, DragonType type);
    }

    public static final DragonTypeSetter SETTER_DRAGON_EEG = (block, level, pos, state, type) -> type.ifPresent(HatchableDragonEggBlock.class, HatchableDragonEggBlock::defaultBlockState, state);
    public static final DragonTypeSetter SETTER_DRAGON_HEAD = (block, level, pos, state, type) -> {
        var variant = type.variants.draw(level.random, block == Blocks.DRAGON_HEAD ? ENDER_FEMALE : block instanceof DragonHeadBlock head ? head.variant : null);
        return variant == null ? state : variant.head.standing.defaultBlockState().setValue(ROTATION_16, state.getValue(ROTATION_16));
    };
    public static final DragonTypeSetter SETTER_DRAGON_HEAD_WALL = (block, level, pos, state, type) -> {
        var variant = type.variants.draw(level.random, block == Blocks.DRAGON_WALL_HEAD ? ENDER_FEMALE : block instanceof DragonHeadBlock head ? head.variant : null);
        return variant == null ? state : variant.head.wall.defaultBlockState().setValue(HORIZONTAL_FACING, state.getValue(HORIZONTAL_FACING));
    };
    private static final Reference2ObjectOpenHashMap<Class<? extends Block>, DragonTypeGetter> GETTERS = new Reference2ObjectOpenHashMap<>();
    private static final Reference2ObjectOpenHashMap<Class<? extends Block>, DragonTypeSetter> SETTERS = new Reference2ObjectOpenHashMap<>();

    @SuppressWarnings("UnusedReturnValue")
    public static DragonTypeGetter bind(Class<? extends Block> clazz, DragonTypeGetter getter) {
        return getter == null ? GETTERS.remove(clazz) : GETTERS.put(clazz, getter);
    }

    @SuppressWarnings("UnusedReturnValue")
    public static DragonTypeSetter bind(Class<? extends Block> clazz, DragonTypeSetter setter) {
        return setter == null ? SETTERS.remove(clazz) : SETTERS.put(clazz, setter);
    }

    static int getFromBlock(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, "pos");
        var source = context.getSource();
        var level = source.getLevel();
        var state = level.getBlockState(pos);
        var block = state.getBlock();
        var clazz = block.getClass();
        var getter = GETTERS.get(clazz);
        if (getter != null) {
            var type = getter.get(block, level, pos, state);
            if (type != null) {
                source.sendSuccess(() -> Component.translatable("commands.neodragonmounts.type.block.get", pos.getX(), pos.getY(), pos.getZ(), type.getName()), true);
                return 1;
            }
        }
        if (block instanceof DragonTypified) {
            source.sendSuccess(() -> Component.translatable("commands.neodragonmounts.type.block.get", pos.getX(), pos.getY(), pos.getZ(), ((DragonTypified) block).getDragonType().getName()), true);
            return 1;
        }
        source.sendFailure(createClassCastException(clazz, DragonTypified.class));
        return 0;
    }

    static int modifyBlock(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, "pos");
        DragonType type = ResourceArgument.getResource(context, "type", DragonMountsShared.DRAGON_TYPE).value();
        var source = context.getSource();
        var level = source.getLevel();
        var original = level.getBlockState(pos);
        var block = original.getBlock();
        var clazz = block.getClass();
        var setter = SETTERS.get(clazz);
        if (setter != null) {
            var state = setter.set(block, level, pos, original, type);
            if (state != original) {
                level.setBlockAndUpdate(pos, state);
                source.sendSuccess(() -> Component.translatable("commands.neodragonmounts.type.block.set", pos.getX(), pos.getY(), pos.getZ(), type.getName()), true);
                return 1;
            }
        }
        source.sendFailure(Component.literal("java.lang.NullPointerException: " + clazz.getName() + " has not bound to a handler"));
        return 0;
    }

    static int getFromEntity(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Entity entity = EntityArgument.getEntity(context, "target");
        if (entity instanceof DragonTypified) {
            context.getSource().sendSuccess(() -> Component.translatable("commands.neodragonmounts.type.entity.get", entity.getDisplayName(), ((DragonTypified) entity).getDragonType().getName()), true);
            return 1;
        }
        context.getSource().sendFailure(createClassCastException(entity, DragonTypified.class));
        return 0;
    }

    static int modifyEntity(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Entity entity = EntityArgument.getEntity(context, "target");
        if (entity instanceof DragonTypified.Mutable) {
            DragonType type = ResourceArgument.getResource(context, "type", DragonMountsShared.DRAGON_TYPE).value();
            var name = entity.getDisplayName(); // name may get changed after setting a new type
            ((DragonTypified.Mutable) entity).convertTo(type, false);
            context.getSource().sendSuccess(() -> Component.translatable("commands.neodragonmounts.type.entity.set", name, type.getName()), true);
            return 1;
        }
        context.getSource().sendFailure(createClassCastException(entity, DragonTypified.Mutable.class));
        return 0;
    }

    /// @see net.minecraft.commands.SharedSuggestionProvider#filterResources(Iterable, String, Function, Consumer)
    public static LiteralArgumentBuilder<CommandSourceStack> register(CommandBuildContext registries, Predicate<CommandSourceStack> permission) {
        var registry = registries.lookupOrThrow(DragonMountsShared.DRAGON_TYPE);
        SuggestionProvider<CommandSourceStack> suggestions = (ignored, builder) -> {
            var input = builder.getRemaining().toLowerCase(Locale.ROOT);
            boolean namespaced = input.indexOf(':') > -1;
            for (var iterator = registry.listElements().iterator(); iterator.hasNext(); ) {
                var type = iterator.next().value();
                var id = type.getId();
                if (namespaced) {
                    var string = id.toString();
                    if (matchesSubStr(input, string)) {
                        builder.suggest(string, type.getName());
                    }
                } else if (matchesSubStr(input, id.getNamespace()) || matchesSubStr(input, id.getPath())) {
                    builder.suggest(id.toString(), type.getName());
                }
            }
            return builder.buildFuture();
        };
        var argument = ResourceArgument.resource(registries, DragonMountsShared.DRAGON_TYPE);
        return Commands.literal("type").then(Commands.literal("block")
                .then(Commands.argument("pos", BlockPosArgument.blockPos()).executes(TypeCommand::getFromBlock)
                        .then(Commands.argument("type", argument).requires(permission).suggests(suggestions).executes(TypeCommand::modifyBlock)))
        ).then(Commands.literal("entity")
                .then(Commands.argument("target", EntityArgument.entity()).executes(TypeCommand::getFromEntity)
                        .then(Commands.argument("type", argument).requires(permission).suggests(suggestions).executes(TypeCommand::modifyEntity))));
    }

    static {
        bind(DragonEggBlock.class, (block, level, pos, state) -> DragonTypes.ENDER);
        bind(SkullBlock.class, (block, level, pos, state) -> block == Blocks.DRAGON_HEAD ? DragonTypes.ENDER : null);
        bind(WallSkullBlock.class, (block, level, pos, state) -> block == Blocks.DRAGON_WALL_HEAD ? DragonTypes.ENDER : null);
        bind(DragonEggBlock.class, SETTER_DRAGON_EEG);
        bind(HatchableDragonEggBlock.class, SETTER_DRAGON_EEG);
        bind(DragonHeadStandingBlock.class, SETTER_DRAGON_HEAD);
        bind(SkullBlock.class, SETTER_DRAGON_HEAD);
        bind(DragonHeadWallBlock.class, SETTER_DRAGON_HEAD_WALL);
        bind(WallSkullBlock.class, SETTER_DRAGON_HEAD_WALL);
    }
}
