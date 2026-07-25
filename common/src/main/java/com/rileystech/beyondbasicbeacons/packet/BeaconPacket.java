package com.rileystech.beyondbasicbeacons.packet;

import com.mojang.serialization.Codec;
import com.rileystech.beyondbasicbeacons.Constants;
import com.rileystech.beyondbasicbeacons.beacon.BeaconEffectEntry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class BeaconPacket implements ICustomPacketPayload<BeaconPacket> {
    private static final String LEVELS_TAG = "levels";
    private static final String BASE_BLOCK_POINTS_TAG = "baseBlockPoints";
    private static final String NEIGHBORING_BEACONS_TAG = "neighboringBeacons";
    private static final String EFFECTS_TAG = "effects";
    private static final Codec<List<BeaconEffectEntry>> EFFECTS_CODEC = BeaconEffectEntry.CODEC.listOf();

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

    private final int levels;
    private final int baseBlockPoints;
    private final int neighboringBeacons;
    private final List<BeaconEffectEntry> effects;

    public BeaconPacket(int levels, int baseBlockPoints, int neighboringBeacons, List<BeaconEffectEntry> effects) {
        this.levels = levels;
        this.baseBlockPoints = baseBlockPoints;
        this.neighboringBeacons = neighboringBeacons;
        this.effects = effects;
    }

    private Tag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(LEVELS_TAG, this.levels);
        tag.putInt(BASE_BLOCK_POINTS_TAG, this.baseBlockPoints);
        tag.putInt(NEIGHBORING_BEACONS_TAG, this.neighboringBeacons);
        tag.store(EFFECTS_TAG, EFFECTS_CODEC, this.effects);
        return tag;
    }

    private static BeaconPacket fromNbt(CompoundTag data) {
        int levels = data.getIntOr(LEVELS_TAG, 0);
        int baseBlockPoints = data.getIntOr(BASE_BLOCK_POINTS_TAG, 0);
        int neighboringBeacons = data.getIntOr(NEIGHBORING_BEACONS_TAG, 0);
        List<BeaconEffectEntry> effects = data.read(EFFECTS_TAG, EFFECTS_CODEC).orElseGet(List::of);
        return new BeaconPacket(levels, baseBlockPoints, neighboringBeacons, effects);
    }

    @Override
    public CustomPacketPayload.@NonNull Type<BeaconPacket> type() {
        return TYPE;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, BeaconPacket> codec() {
        return CODEC;
    }

    @Override
    public void onServerReceived() {
        Constants.LOG.info("Received BeaconPacket on the server (unexpected - this packet is only ever sent server->client)");
    }

    @Override
    public void onClientReceived() {
        // Currently unused: BeaconScreen reads the beacon's data straight from the client's own
        // local (vanilla-synced) block entity instead - see BeaconMenu.getBeaconAccess(). This
        // packet is kept around as a ready-made shape for a future client->server save flow.
        Constants.LOG.info("Received BeaconPacket on the client: levels={}, baseBlockPoints={}, neighboringBeacons={}, effects={}",
                this.levels, this.baseBlockPoints, this.neighboringBeacons, this.effects);
    }

}
