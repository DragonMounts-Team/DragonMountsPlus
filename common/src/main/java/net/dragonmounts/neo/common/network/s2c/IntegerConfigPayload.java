package net.dragonmounts.neo.common.network.s2c;

import net.minecraft.nbt.IntTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static net.dragonmounts.neo.common.DragonMountsShared.makeId;

public record IntegerConfigPayload(int id, int value) implements ConfigPayloadEntry<IntTag> {
    public static final Type<IntegerConfigPayload> TYPE = new Type<>(makeId("int_config"));
    public static final StreamCodec<RegistryFriendlyByteBuf, IntegerConfigPayload> CODEC =
            CustomPacketPayload.codec(IntegerConfigPayload::encode, IntegerConfigPayload::decode);

    public static IntegerConfigPayload decode(FriendlyByteBuf buffer) {
        return new IntegerConfigPayload(buffer.readVarInt(), buffer.readVarInt());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.id).writeVarInt(this.value);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public IntTag getAsTag() {
        return IntTag.valueOf(this.value);
    }
}
