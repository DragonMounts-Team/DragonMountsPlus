package net.dragonmounts.neo.common.entity.dragon;

import com.mojang.logging.LogUtils;
import net.dragonmounts.neo.common.DragonMountsShared;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public enum Relation {
    STRANGER(false, Component.translatable("message.neodragonmounts.dragon.untamed")),
    UNTRUSTED(false, Component.translatable("message.neodragonmounts.dragon.locked")),
    TRUSTED(true, DragonMountsShared.REQUIRES_OWNER),
    OWNER(true, null);
    private static final Logger LOGGER = LogUtils.getLogger();
    public final boolean isTrusted;
    private final @Nullable Component reason;

    Relation(boolean isTrusted, @Nullable Component reason) {
        this.isTrusted = isTrusted;
        this.reason = reason;
    }

    public static boolean isOwner(TameableDragonEntity dragon, Player player) {
        return player.getUUID().equals(dragon.getOwnerUUID());
    }

    public final void onDeny(Player player) {
        if (this.reason == null) {
            LOGGER.warn("Logical Error: {} should not be denied!", player.getName());
        } else {
            player.displayClientMessage(this.reason, true);
        }
    }

    public static Relation checkRelation(TameableDragonEntity dragon, Player player) {
        if (!dragon.isTame()) return STRANGER;
        if (isOwner(dragon, player)) return OWNER;
        return dragon.isPlayerTrusted(player) ? TRUSTED : UNTRUSTED;
    }

    /// @return if the player is denied
    public static boolean denyIfUntrusted(TameableDragonEntity dragon, Player player) {
        Relation relation = checkRelation(dragon, player);
        if (relation.isTrusted) return false;
        relation.onDeny(player);
        return true;
    }

    /// @return if the player is denied
    public static boolean denyIfNotOwner(TameableDragonEntity dragon, Player player) {
        if (isOwner(dragon, player)) return false;
        player.displayClientMessage(DragonMountsShared.REQUIRES_OWNER, true);
        return true;
    }
}