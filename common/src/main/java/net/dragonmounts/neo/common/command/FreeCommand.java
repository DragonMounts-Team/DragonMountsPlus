package net.dragonmounts.neo.common.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Predicate;

import static net.dragonmounts.neo.common.command.DMCommands.createClassCastException;
import static net.dragonmounts.neo.common.command.DMCommands.getSingleProfileOrException;

public class FreeCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register(Predicate<CommandSourceStack> permission) {
        return Commands.literal("free").requires(permission).then(Commands.argument("targets", EntityArgument.entities()).executes(context -> {
            var targets = EntityArgument.getEntities(context, "targets");
            return free(context, targets, context.getSource().getPlayerOrException().getUUID(), targets.size() == 1);
        }).then(Commands.literal("forced").executes(context ->
                free(context, EntityArgument.getEntities(context, "targets"), null, true)
        )).then(Commands.literal("owned_by").then(Commands.argument("owner", GameProfileArgument.gameProfile()).executes(context ->
                free(context, EntityArgument.getEntities(context, "targets"), getSingleProfileOrException(context, "owner").getId(), false)
        ))));
    }

    public static int free(CommandContext<CommandSourceStack> context, Collection<? extends Entity> targets, UUID owner, boolean forced) {
        var source = context.getSource();
        Entity cache = null;
        boolean flag = true;
        int count = 0;
        for (var target : targets) {
            if (target instanceof TamableAnimal entity) {
                if (forced || (owner != null && owner.equals(entity.getOwnerUUID()))) {
                    entity.setTame(false, false);
                    entity.setOwnerUUID(null);
                    entity.setOrderedToSit(false);
                    ++count;
                }
                flag = false;
                cache = entity;
            }
        }
        if (flag) {
            if (targets.size() == 1) {
                source.sendFailure(createClassCastException(targets.iterator().next(), TamableAnimal.class));
            } else {
                source.sendFailure(Component.translatable("commands.neodragonmounts.free.multiple", count));
            }
        } else if (count == 1) {
            final var temp = cache;
            source.sendSuccess(() -> Component.translatable("commands.neodragonmounts.free.single", temp.getDisplayName()), true);
        } else {
            final var temp = count;
            source.sendSuccess(() -> Component.translatable("commands.neodragonmounts.free.multiple", temp), true);
        }
        return count;
    }
}
