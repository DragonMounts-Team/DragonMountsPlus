package net.dragonmounts.neo.common.network.s2c;

import net.minecraft.nbt.ByteTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static net.dragonmounts.neo.common.DragonMountsShared.makeId;

public record BooleanConfigPayload(int id, boolean value) implements ConfigPayloadEntry<ByteTag> {
    public static final Type<BooleanConfigPayload> TYPE = new Type<>(makeId("bool_config"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BooleanConfigPayload> CODEC =
            CustomPacketPayload.codec(BooleanConfigPayload::encode, BooleanConfigPayload::decode);

    public static BooleanConfigPayload decode(FriendlyByteBuf buffer) {
        return new BooleanConfigPayload(buffer.readVarInt(), buffer.readBoolean());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.id).writeBoolean(this.value);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public ByteTag getAsTag() {
        return ByteTag.valueOf(this.value);
    }
}
