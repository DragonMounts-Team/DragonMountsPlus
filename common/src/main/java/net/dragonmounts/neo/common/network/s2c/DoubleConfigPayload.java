package net.dragonmounts.neo.common.network.s2c;

import net.minecraft.nbt.DoubleTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static net.dragonmounts.neo.common.DragonMountsShared.makeId;

public record DoubleConfigPayload(int id, double value) implements ConfigPayloadEntry<DoubleTag> {
    public static final Type<DoubleConfigPayload> TYPE = new Type<>(makeId("double_config"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DoubleConfigPayload> CODEC =
            CustomPacketPayload.codec(DoubleConfigPayload::encode, DoubleConfigPayload::decode);

    public static DoubleConfigPayload decode(FriendlyByteBuf buffer) {
        return new DoubleConfigPayload(buffer.readVarInt(), buffer.readDouble());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.id).writeDouble(this.value);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public DoubleTag getAsTag() {
        return DoubleTag.valueOf(this.value);
    }
}
