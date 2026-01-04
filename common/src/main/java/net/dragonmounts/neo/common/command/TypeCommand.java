package net.dragonmounts.neo.common.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
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
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.selector.EntitySelector;
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
    public abstract static class CommandHandler<A> {
        protected abstract RequiredArgumentBuilder<CommandSourceStack, ?> input();

        protected abstract A parse(CommandContext<CommandSourceStack> context) throws CommandSyntaxException;

        protected abstract int setType(CommandContext<CommandSourceStack> context, A argument, DragonType type);

        protected abstract int getType(CommandContext<CommandSourceStack> context, A argument);

        public RequiredArgumentBuilder<CommandSourceStack, ?> generateCommand(
                ResourceArgument<DragonType> argument,
                SuggestionProvider<CommandSourceStack> suggestions,
                Predicate<CommandSourceStack> permission
        ) {
            return this.input()
                    .executes(context -> this.getType(context, this.parse(context)))
                    .then(Commands.argument("type", argument).executes(context -> this.setType(
                            context,
                            this.parse(context),
                            ResourceArgument.getResource(context, "type", DragonMountsShared.DRAGON_TYPE).value()
                    )).suggests(suggestions).requires(permission));
        }
    }

    public static class BlockHandler extends CommandHandler<BlockPos> {
        @FunctionalInterface
        public interface Getter {
            DragonType get(Block block, ServerLevel level, BlockPos pos, BlockState state);
        }

        @FunctionalInterface
        public interface Setter {
            BlockState set(Block block, ServerLevel level, BlockPos pos, BlockState state, DragonType type);
        }

        public static final Setter SETTER_DRAGON_EEG = (block, level, pos, state, type) -> type.ifPresent(HatchableDragonEggBlock.class, HatchableDragonEggBlock::defaultBlockState, state);
        public static final Setter SETTER_DRAGON_HEAD = (block, level, pos, state, type) -> {
            var variant = type.variants.draw(level.random, block == Blocks.DRAGON_HEAD ? ENDER_FEMALE : block instanceof DragonHeadBlock head ? head.variant : null);
            return variant == null ? state : variant.head.standing.defaultBlockState().setValue(ROTATION_16, state.getValue(ROTATION_16));
        };
        public static final Setter SETTER_DRAGON_HEAD_WALL = (block, level, pos, state, type) -> {
            var variant = type.variants.draw(level.random, block == Blocks.DRAGON_WALL_HEAD ? ENDER_FEMALE : block instanceof DragonHeadBlock head ? head.variant : null);
            return variant == null ? state : variant.head.wall.defaultBlockState().setValue(HORIZONTAL_FACING, state.getValue(HORIZONTAL_FACING));
        };

        private final Reference2ObjectOpenHashMap<Class<? extends Block>, Getter> getters = new Reference2ObjectOpenHashMap<>();
        private final Reference2ObjectOpenHashMap<Class<? extends Block>, Setter> setters = new Reference2ObjectOpenHashMap<>();

        @SuppressWarnings("UnusedReturnValue")
        public Getter bind(Class<? extends Block> clazz, Getter getter) {
            return getter == null ? this.getters.remove(clazz) : this.getters.put(clazz, getter);
        }

        @SuppressWarnings("UnusedReturnValue")
        public Setter bind(Class<? extends Block> clazz, Setter setter) {
            return setter == null ? this.setters.remove(clazz) : this.setters.put(clazz, setter);
        }

        @Override
        protected RequiredArgumentBuilder<CommandSourceStack, Coordinates> input() {
            return Commands.argument("pos", BlockPosArgument.blockPos());
        }

        @Override
        protected BlockPos parse(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
            return BlockPosArgument.getLoadedBlockPos(context, "pos");
        }

        @Override
        protected int setType(CommandContext<CommandSourceStack> context, BlockPos pos, DragonType type) {
            var source = context.getSource();
            var level = source.getLevel();
            var original = level.getBlockState(pos);
            var block = original.getBlock();
            var clazz = block.getClass();
            var setter = this.setters.get(clazz);
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

        @Override
        protected int getType(CommandContext<CommandSourceStack> context, BlockPos pos) {
            var source = context.getSource();
            var level = source.getLevel();
            var state = level.getBlockState(pos);
            var block = state.getBlock();
            var clazz = block.getClass();
            var getter = this.getters.get(clazz);
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
    }

    public static class EntityHandler extends CommandHandler<Entity> {
        @Override
        protected RequiredArgumentBuilder<CommandSourceStack, EntitySelector> input() {
            return Commands.argument("target", EntityArgument.entity());
        }

        @Override
        protected Entity parse(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
            return EntityArgument.getEntity(context, "target");
        }

        @Override
        protected int setType(CommandContext<CommandSourceStack> context, Entity entity, DragonType type) {
            if (entity instanceof DragonTypified.Mutable) {
                var name = entity.getDisplayName();// name may get changed after setting a new type
                ((DragonTypified.Mutable) entity).convertTo(type, false);
                context.getSource().sendSuccess(() -> Component.translatable("commands.neodragonmounts.type.entity.set", name, type.getName()), true);
                return 1;
            }
            context.getSource().sendFailure(createClassCastException(entity, DragonTypified.Mutable.class));
            return 0;
        }

        @Override
        protected int getType(CommandContext<CommandSourceStack> context, Entity entity) {
            if (entity instanceof DragonTypified) {
                context.getSource().sendSuccess(() -> Component.translatable("commands.neodragonmounts.type.entity.get", entity.getDisplayName(), ((DragonTypified) entity).getDragonType().getName()), true);
                return 1;
            }
            context.getSource().sendFailure(createClassCastException(entity, DragonTypified.class));
            return 0;
        }
    }

    public static final BlockHandler BLOCK_HANDLER = new BlockHandler();
    public static final EntityHandler ENTITY_HANDLER = new EntityHandler();

    static {
        BLOCK_HANDLER.bind(DragonEggBlock.class, (block, level, pos, state) -> DragonTypes.ENDER);
        BLOCK_HANDLER.bind(SkullBlock.class, (block, level, pos, state) -> block == Blocks.DRAGON_HEAD ? DragonTypes.ENDER : null);
        BLOCK_HANDLER.bind(WallSkullBlock.class, (block, level, pos, state) -> block == Blocks.DRAGON_WALL_HEAD ? DragonTypes.ENDER : null);
        BLOCK_HANDLER.bind(DragonEggBlock.class, BlockHandler.SETTER_DRAGON_EEG);
        BLOCK_HANDLER.bind(HatchableDragonEggBlock.class, BlockHandler.SETTER_DRAGON_EEG);
        BLOCK_HANDLER.bind(DragonHeadStandingBlock.class, BlockHandler.SETTER_DRAGON_HEAD);
        BLOCK_HANDLER.bind(SkullBlock.class, BlockHandler.SETTER_DRAGON_HEAD);
        BLOCK_HANDLER.bind(DragonHeadWallBlock.class, BlockHandler.SETTER_DRAGON_HEAD_WALL);
        BLOCK_HANDLER.bind(WallSkullBlock.class, BlockHandler.SETTER_DRAGON_HEAD_WALL);
    }

    /// @see net.minecraft.commands.SharedSuggestionProvider#filterResources(Iterable, String, Function, Consumer)
    public static LiteralArgumentBuilder<CommandSourceStack> register(CommandBuildContext context, Predicate<CommandSourceStack> permission) {
        var argument = ResourceArgument.resource(context, DragonMountsShared.DRAGON_TYPE);
        var registry = context.lookupOrThrow(DragonMountsShared.DRAGON_TYPE);
        SuggestionProvider<CommandSourceStack> suggestions = (ignored, builder) -> {
            var input = builder.getRemaining().toLowerCase(Locale.ROOT);
            boolean namespaced = input.indexOf(58) > -1;
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
        return Commands.literal("type")
                .then(Commands.literal("block").then(BLOCK_HANDLER.generateCommand(argument, suggestions, permission)))
                .then(Commands.literal("entity").then(ENTITY_HANDLER.generateCommand(argument, suggestions, permission)));
    }
}
