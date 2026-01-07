package net.dragonmounts.neo.common.network.s2c;

import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface ConfigPayloadEntry<T extends Tag> extends CustomPacketPayload {
    int id();

    T getAsTag();
}
