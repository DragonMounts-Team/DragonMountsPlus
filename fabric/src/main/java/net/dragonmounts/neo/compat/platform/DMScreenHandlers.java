package net.dragonmounts.neo.compat.platform;

import io.netty.buffer.ByteBuf;
import net.dragonmounts.neo.common.entity.dragon.TameableDragonEntity;
import net.dragonmounts.neo.common.inventory.DragonCoreHandler;
import net.dragonmounts.neo.common.inventory.DragonInventoryHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.NotNull;

import static net.dragonmounts.neo.common.client.ClientUtil.getLevel;
import static net.dragonmounts.neo.compat.registry.RegistryHandler.registerMenu;

public class DMScreenHandlers {
    public static final MenuType<DragonCoreHandler> DRAGON_CORE = registerMenu("dragon_core", DragonCoreHandler::new, BlockPos.STREAM_CODEC);
    public static final MenuType<DragonInventoryHandler> DRAGON_INVENTORY = registerMenu("dragon_inventory", DragonInventoryHandler::new, new StreamCodec<ByteBuf, TameableDragonEntity>() {
        @Override
        public @NotNull TameableDragonEntity decode(ByteBuf buffer) {
            if (getLevel().getEntity(VarInt.read(buffer)) instanceof TameableDragonEntity dragon) return dragon;
            throw new NullPointerException();
        }

        @Override
        public void encode(ByteBuf buffer, TameableDragonEntity dragon) {
            VarInt.write(buffer, dragon.getId());
        }
    });

    public static void init() {}
}
