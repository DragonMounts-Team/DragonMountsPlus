package net.dragonmounts.neo.common.network.s2c;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static net.dragonmounts.neo.common.DragonMountsShared.makeId;

public record WobbleEggPayload(int id, int amplitude, int axis, int flag) implements CustomPacketPayload {
    public static final Type<WobbleEggPayload> TYPE = new Type<>(makeId("wobble_egg"));
    public static final StreamCodec<RegistryFriendlyByteBuf, WobbleEggPayload> CODEC =
            CustomPacketPayload.codec(WobbleEggPayload::encode, WobbleEggPayload::decode);

    public static WobbleEggPayload decode(FriendlyByteBuf buffer) {
        return new WobbleEggPayload(buffer.readVarInt(), buffer.readByte(), Byte.toUnsignedInt(buffer.readByte()), buffer.readByte());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.id).writeByte(this.amplitude).writeByte(this.axis).writeByte(this.flag);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
