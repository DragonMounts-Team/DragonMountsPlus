package net.dragonmounts.neo.common.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dragonmounts.neo.common.entity.dragon.TameableDragonEntity;
import net.dragonmounts.neo.common.init.DMItems;
import net.dragonmounts.neo.common.item.DragonAmuletItem;
import net.dragonmounts.neo.common.item.DragonEssenceItem;
import net.dragonmounts.neo.common.item.DragonSpawnEggItem;
import net.dragonmounts.neo.common.item.EntityContainer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

import static net.dragonmounts.neo.common.command.DMCommands.createClassCastException;
import static net.dragonmounts.neo.common.entity.dragon.TameableDragonEntity.SERIALIZATION_KEY_FLYING;
import static net.dragonmounts.neo.common.util.EntityUtil.saveWithId;

public class SaveCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandBuildContext builder, Predicate<CommandSourceStack> permission) {
        return Commands.literal("save").requires(permission).then(Commands.argument("target", EntityArgument.entity())
                .then(Commands.literal("amulet").executes(context -> saveAmulet(context, EntityArgument.getEntity(context, "target"))))
                .then(Commands.literal("essence").executes(context -> saveEssence(context, EntityArgument.getEntity(context, "target"))))
                .then(Commands.literal("spawn_egg").executes(context -> saveSpawnEgg(context, EntityArgument.getEntity(context, "target"))))
                .then(Commands.literal("container").then(Commands.argument("item", ItemArgument.item(builder)).executes(context ->
                        save(context, ItemArgument.getItem(context, "item"), EntityArgument.getEntity(context, "target"))
                )))
        );
    }

    public static int saveAmulet(CommandContext<CommandSourceStack> context, Entity target) throws CommandSyntaxException {
        var source = context.getSource();
        if (target instanceof TameableDragonEntity dragon) {
            var amulet = dragon.getDragonType().getInstance(DragonAmuletItem.class, null);
            if (amulet != null) return give(source, amulet.saveEntity(dragon, DataComponentPatch.EMPTY));
        }
        var stack = DMItems.AMULET.get().saveEntity(target, DataComponentPatch.EMPTY);
        return stack.isEmpty()
                ? fail(source, target, "commands.neodragonmounts.save.no_spawn_egg")
                : give(source, stack);
    }

    public static int saveEssence(CommandContext<CommandSourceStack> context, Entity target) throws CommandSyntaxException {
        var source = context.getSource();
        if (target instanceof TameableDragonEntity dragon) {
            var essence = dragon.getDragonType().getInstance(DragonEssenceItem.class, null);
            if (essence != null) return give(source, essence.saveEntity(dragon, DataComponentPatch.EMPTY));
        }
        source.sendFailure(createClassCastException(target, TameableDragonEntity.class));
        return 0;
    }

    public static int saveSpawnEgg(CommandContext<CommandSourceStack> context, Entity target) throws CommandSyntaxException {
        var source = context.getSource();
        if (target instanceof TameableDragonEntity dragon) {
            var spawnEgg = dragon.getDragonType().getInstance(DragonSpawnEggItem.class, null);
            if (spawnEgg != null) return give(source, spawnEgg.saveEntity(dragon));
        }
        var type = target.getType();
        if (type.canSerialize()) {
            var item = SpawnEggItem.byId(type);
            return item == null
                    ? fail(source, target, "commands.neodragonmounts.save.no_spawn_egg")
                    : give(source, EntityContainer.saveEntityData(item, saveWithId(target, new CompoundTag()), DataComponentPatch.EMPTY));
        }
        return fail(source, target, "commands.neodragonmounts.save.cannot_serialize");
    }

    public static int save(CommandContext<CommandSourceStack> context, ItemInput input, Entity target) throws CommandSyntaxException {
        var source = context.getSource();
        var item = input.getItem();
        if (item instanceof EntityContainer<?>) {
            var stack = saveContainer((EntityContainer<?>) item, input, target);
            if (stack != null) return stack.isEmpty()
                    ? fail(source, target, "commands.neodragonmounts.save.cannot_serialize")
                    : give(source, stack);
        }
        if (target.getType().canSerialize()) {
            var stack = input.createItemStack(1, false);
            var tag = saveWithId(target, new CompoundTag());
            tag.remove(SERIALIZATION_KEY_FLYING);
            tag.remove("UUID");
            stack.set(DataComponents.ENTITY_DATA, EntityContainer.simplifyData(tag));
            return give(source, stack);
        }
        return fail(source, target, "commands.neodragonmounts.save.cannot_serialize");
    }

    public static <T extends Entity> @Nullable ItemStack saveContainer(EntityContainer<T> container, ItemInput input, Entity target) throws CommandSyntaxException {
        var clazz = container.getContentType();
        return clazz.isInstance(target) ? container.saveEntity(
                clazz.cast(target),
                input.createItemStack(1, false).getComponentsPatch()
        ) : null;
    }

    public static int give(CommandSourceStack source, ItemStack stack) throws CommandSyntaxException {
        var player = source.getPlayerOrException();
        var name = stack.getDisplayName();
        int count = stack.getCount();
        if (!player.getInventory().add(stack)) {
            var item = player.drop(stack, false);
            if (item != null) {
                item.setNoPickUpDelay();
                item.setTarget(player.getUUID());
            }
        }
        source.sendSuccess(() -> Component.translatable("commands.give.success.single", count, name, player.getDisplayName()), true);
        return 1;
    }

    public static int fail(CommandSourceStack source, Entity target, String reason) {
        source.sendFailure(Component.translatable(reason, target.getDisplayName()));
        return 0;
    }
}
