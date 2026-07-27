package com.rileystech.beyondbasicbeacons.packet;

import com.mojang.serialization.Codec;
import com.rileystech.beyondbasicbeacons.Constants;
import com.rileystech.beyondbasicbeacons.beacon.BeaconEffectEntry;
import com.rileystech.beyondbasicbeacons.beacon.BeaconPoints;
import com.rileystech.beyondbasicbeacons.data.BeaconEffectCostManager;
import com.rileystech.beyondbasicbeacons.data.BeaconEffectFilterManager;
import com.rileystech.beyondbasicbeacons.data.BeaconSettingsManager;
import com.rileystech.beyondbasicbeacons.menu.BeaconMenu;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Carries the player's edited beacon data (effects/range/hideParticles) from client to server
// when the Save button is pressed. levels/baseBlockPoints/neighboringBeacons aren't included -
// those are server-computed and the client already reads them straight from its own local block
// entity (see BeaconMenu.getBeaconAccess()), so there's nothing for this packet to carry them for.
public class BeaconPacket implements ICustomPacketPayload<BeaconPacket> {
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

    private static final String EFFECTS_TAG = "effects";
    private static final String RANGE_TAG = "range";
    private static final String HIDE_PARTICLES_TAG = "hideParticles";

    private static final Codec<List<BeaconEffectEntry>> EFFECTS_CODEC = BeaconEffectEntry.CODEC.listOf();

    private final List<BeaconEffectEntry> effects;
    private final int range;
    private final boolean hideParticles;

    public BeaconPacket(List<BeaconEffectEntry> effects, int range, boolean hideParticles) {
        this.effects = effects;
        this.range = range;
        this.hideParticles = hideParticles;
    }

    private Tag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.store(EFFECTS_TAG, EFFECTS_CODEC, this.effects);
        tag.putInt(RANGE_TAG, this.range);
        tag.putBoolean(HIDE_PARTICLES_TAG, this.hideParticles);
        return tag;
    }

    private static BeaconPacket fromNbt(CompoundTag tag) {
        List<BeaconEffectEntry> effects = tag.read(EFFECTS_TAG, EFFECTS_CODEC).orElseGet(ArrayList::new);
        int range = tag.getIntOr(RANGE_TAG, 1);
        boolean hideParticles = tag.getBooleanOr(HIDE_PARTICLES_TAG, false);
        return new BeaconPacket(effects, range, hideParticles);
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
            BeaconSettingsManager.Settings settings = BeaconSettingsManager.getSettings();
            if (this.range < settings.minRange() || this.range > settings.maxRange()
                    || this.effects.size() > settings.maxEffectsPerBeacon()
                    || this.effects.stream().anyMatch(effect -> effect.amplifier() < settings.minAmplifier() || effect.amplifier() > settings.maxAmplifier())
                    || this.effects.stream().anyMatch(effect -> !isAllowed(effect))) {
                Constants.LOG.warn("Rejecting BeaconPacket from {}: range/amplifier/effect-count/disallowed-effect (range={}, effects={})",
                        sender.getGameProfile().name(), this.range, this.effects);
                return;
            }

            int required = BeaconPoints.requiredPoints(this.range, this.effects, BeaconEffectEntry::amplifier, BeaconPacket::multiplierOf, this.hideParticles);
            int available = beacon.bbb_getBaseBlockPoints() + beacon.bbb_getNeighboringBeacons();
            if (required > available) {
                Constants.LOG.warn("Rejecting BeaconPacket from {}: required {} points but only {} available",
                        sender.getGameProfile().name(), required, available);
                return;
            }

            beacon.bbb_setEffects(this.effects);
            beacon.bbb_setRange(this.range);
            beacon.bbb_setHideParticles(this.hideParticles);
            if (beacon instanceof BlockEntity blockEntity) {
                blockEntity.setChanged();
            }
            Constants.LOG.info("Applied saved beacon data for {}", sender.getGameProfile().name());
        }, () -> Constants.LOG.info("No beacon block entity found for {}'s open menu - ignoring", sender.getGameProfile().name()));
    }

    private static Optional<MobEffect> resolveEffect(BeaconEffectEntry entry) {
        Identifier id = Identifier.parse(entry.key());
        ResourceKey<MobEffect> key = ResourceKey.create(Registries.MOB_EFFECT, id);
        return BuiltInRegistries.MOB_EFFECT.get(key).map(Holder::value);
    }

    // Unresolvable keys fail closed (not allowed) - a key that doesn't resolve to a real,
    // registered MobEffect has no business being saved regardless of blacklist/whitelist state.
    private static boolean isAllowed(BeaconEffectEntry entry) {
        return resolveEffect(entry).map(BeaconEffectFilterManager::isAllowed).orElse(false);
    }

    private static double multiplierOf(BeaconEffectEntry entry) {
        return resolveEffect(entry).map(BeaconEffectCostManager::getMultiplier).orElse(1.0);
    }

    @Override
    public void onClientReceived() {
        // Never sent to the client - this packet is only used for the client->server save flow.
        Constants.LOG.info("Received BeaconPacket on the client (unexpected): effects={}, range={}, hideParticles={}",
                this.effects, this.range, this.hideParticles);
    }

}
