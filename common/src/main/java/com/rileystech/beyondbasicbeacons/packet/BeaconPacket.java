package com.rileystech.beyondbasicbeacons.packet;

import com.mojang.serialization.Codec;
import com.rileystech.beyondbasicbeacons.Constants;
import com.rileystech.beyondbasicbeacons.access.NeighboringBeaconAccess;
import com.rileystech.beyondbasicbeacons.beacon.BeaconEffectEntry;
import com.rileystech.beyondbasicbeacons.menu.BeaconMenu;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class BeaconPacket implements ICustomPacketPayload<BeaconPacket> {
    private static final String LEVELS_TAG = "levels";
    private static final String BASE_BLOCK_POINTS_TAG = "baseBlockPoints";
    private static final String NEIGHBORING_BEACONS_TAG = "neighboringBeacons";
    private static final String EFFECTS_TAG = "effects";
    private static final String RANGE_TAG = "range";
    private static final String HIDE_PARTICLES_TAG = "hideParticles";
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
    private final int range;
    private final boolean hideParticles;

    public BeaconPacket(int levels, int baseBlockPoints, int neighboringBeacons, List<BeaconEffectEntry> effects, int range, boolean hideParticles) {
        this.levels = levels;
        this.baseBlockPoints = baseBlockPoints;
        this.neighboringBeacons = neighboringBeacons;
        this.effects = effects;
        this.range = range;
        this.hideParticles = hideParticles;
    }

    private Tag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(LEVELS_TAG, this.levels);
        tag.putInt(BASE_BLOCK_POINTS_TAG, this.baseBlockPoints);
        tag.putInt(NEIGHBORING_BEACONS_TAG, this.neighboringBeacons);
        tag.store(EFFECTS_TAG, EFFECTS_CODEC, this.effects);
        tag.putInt(RANGE_TAG, this.range);
        tag.putBoolean(HIDE_PARTICLES_TAG, this.hideParticles);
        return tag;
    }

    private static BeaconPacket fromNbt(CompoundTag data) {
        int levels = data.getIntOr(LEVELS_TAG, 0);
        int baseBlockPoints = data.getIntOr(BASE_BLOCK_POINTS_TAG, 0);
        int neighboringBeacons = data.getIntOr(NEIGHBORING_BEACONS_TAG, 0);
        List<BeaconEffectEntry> effects = data.read(EFFECTS_TAG, EFFECTS_CODEC).orElseGet(List::of);
        int range = data.getIntOr(RANGE_TAG, 0);
        boolean hideParticles = data.getBooleanOr(HIDE_PARTICLES_TAG, false);
        return new BeaconPacket(levels, baseBlockPoints, neighboringBeacons, effects, range, hideParticles);
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
    public void onServerReceived(ServerPlayer sender) {
        Constants.LOG.info("Received BeaconPacket on the server from {}: effects={}, range={}, hideParticles={}",
                sender.getGameProfile().name(), this.effects, this.range, this.hideParticles);

        if (!(sender.containerMenu instanceof BeaconMenu menu)) {
            Constants.LOG.info("Player {} does not have a BeaconMenu open - ignoring", sender.getGameProfile().name());
            return;
        }

        menu.getBeaconAccess().ifPresentOrElse(beacon -> {
            beacon.bbb_setEffects(this.effects);
            beacon.bbb_setRange(this.range);
            beacon.bbb_setHideParticles(this.hideParticles);
            if (beacon instanceof BlockEntity blockEntity) {
                blockEntity.setChanged();

                // setChanged() only marks the chunk dirty for disk saving - it does NOT notify
                // nearby clients on its own. sendBlockUpdated is what actually pushes the block
                // entity's updated data to them (same old/current state since the block itself
                // hasn't changed, just its entity data).
                Level level = blockEntity.getLevel();
                if (level != null) {
                    BlockState state = blockEntity.getBlockState();
                    level.sendBlockUpdated(blockEntity.getBlockPos(), state, state, Block.UPDATE_CLIENTS);
                }
            }
            Constants.LOG.info("Applied saved beacon data for {}", sender.getGameProfile().name());
        }, () -> Constants.LOG.info("No beacon block entity found for {}'s open menu - ignoring", sender.getGameProfile().name()));
    }

    @Override
    public void onClientReceived() {
        // Currently unused: BeaconScreen reads the beacon's data straight from the client's own
        // local (vanilla-synced) block entity instead - see BeaconMenu.getBeaconAccess().
        Constants.LOG.info("Received BeaconPacket on the client: levels={}, baseBlockPoints={}, neighboringBeacons={}, effects={}, range={}, hideParticles={}",
                this.levels, this.baseBlockPoints, this.neighboringBeacons, this.effects, this.range, this.hideParticles);
    }

}
