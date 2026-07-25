package com.rileystech.beyondbasicbeacons.packet;

import com.rileystech.beyondbasicbeacons.Constants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public class BeaconPacket  implements ICustomPacketPayload<BeaconPacket> {
    public static final Identifier BEACON_PACKET_PAYLOAD_ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "beacon_packet");

    public static final CustomPacketPayload.Type<BeaconPacket> TYPE = new CustomPacketPayload.Type<>(BEACON_PACKET_PAYLOAD_ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, BeaconPacket> CODEC = new StreamCodec<RegistryFriendlyByteBuf, BeaconPacket>() {
        public BeaconPacket decode(RegistryFriendlyByteBuf input) {
            return BeaconPacket.fromNbt(FriendlyByteBuf.readNbt(input));
        }

        public void encode(RegistryFriendlyByteBuf output, BeaconPacket packet) {
            FriendlyByteBuf.writeNbt(output, packet.toNbt());
        }
    };

    private Tag toNbt() {
        return null;
    }

    private static BeaconPacket fromNbt(CompoundTag data) {
        return new BeaconPacket();
    }


    @Override
    public CustomPacketPayload.@NonNull Type<BeaconPacket> type() {
        return TYPE;
    }

    public StreamCodec<RegistryFriendlyByteBuf, BeaconPacket> codec() {
        return CODEC;
    }

    @Override
    public void onServerReceived() {
        Constants.LOG.debug("recieved server beacon packet");
    }

    @Override
    public void onClientReceived() {
        Constants.LOG.debug("recieved client beacon packet");
    }

}
