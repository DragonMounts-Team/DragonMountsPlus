package net.dragonmounts.neo.common.entity.dragon;

import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public enum Relation {
    STRANGER(false, "message.neodragonmounts.dragon.untamed"),
    UNTRUSTED(false, "message.neodragonmounts.dragon.locked"),
    TRUSTED(true, "message.neodragonmounts.dragon.not_owner"),
    OWNER(true, null);
    private static final Logger LOGGER = LogUtils.getLogger();
    public final boolean isTrusted;
    private final @Nullable Component reason;

    Relation(boolean isTrusted, @Nullable String reason) {
        this.isTrusted = isTrusted;
        this.reason = reason == null ? null : Component.translatable(reason);
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
        if (player.getUUID().equals(dragon.getOwnerUUID())) return OWNER;
        return dragon.isPlayerTrusted(player) ? TRUSTED : UNTRUSTED;
    }

    /// @return if the player is denied
    public static boolean denyIfNotOwner(TameableDragonEntity dragon, Player player) {
        Relation relation = checkRelation(dragon, player);
        if (OWNER == relation) return false;
        relation.onDeny(player);
        return true;
    }
}