package com.rileystech.beyondbasicbeacons.packet;

import com.rileystech.beyondbasicbeacons.MinecraftBootstrap;
import com.rileystech.beyondbasicbeacons.beacon.BeaconEffectEntry;
import com.rileystech.beyondbasicbeacons.data.BeaconSettingsManager.Settings;
import com.rileystech.beyondbasicbeacons.menu.BeaconMenuData;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

// Client and server must agree on these byte layouts - a change to either codec that doesn't
// survive a round trip would break saving a beacon or opening its menu in multiplayer.
class NetworkCodecTest {

    @BeforeAll
    static void bootstrap() {
        MinecraftBootstrap.init();
    }

    private static RegistryFriendlyByteBuf newBuf() {
        return new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
    }

    private static <T> byte[] encode(StreamCodec<RegistryFriendlyByteBuf, T> codec, T value) {
        RegistryFriendlyByteBuf buf = newBuf();
        codec.encode(buf, value);
        return ByteBufUtil.getBytes(buf);
    }

    private static <T> T decode(StreamCodec<RegistryFriendlyByteBuf, T> codec, byte[] bytes) {
        RegistryFriendlyByteBuf buf = newBuf();
        buf.writeBytes(bytes);
        T value = codec.decode(buf);
        assertEquals(0, buf.readableBytes(), "decoder left unread bytes");
        return value;
    }

    @Test
    void beaconMenuDataRoundTrips() {
        BeaconMenuData data = new BeaconMenuData(
                new BlockPos(12, -40, 3000),
                new Settings(2, 64, 0, 3, 6, 2, 1, 8),
                List.of("minecraft:speed", "minecraft:haste"));

        BeaconMenuData decoded = decode(BeaconMenuData.STREAM_CODEC, encode(BeaconMenuData.STREAM_CODEC, data));
        assertEquals(data, decoded);
    }

    @Test
    void beaconMenuDataWithNoAllowedEffectsRoundTrips() {
        BeaconMenuData data = new BeaconMenuData(BlockPos.ZERO, Settings.DEFAULT, List.of());
        assertEquals(data, decode(BeaconMenuData.STREAM_CODEC, encode(BeaconMenuData.STREAM_CODEC, data)));
    }

    // BeaconPacket has no accessors, so compare its encoded form before and after a decode.
    @Test
    void beaconPacketRoundTrips() {
        BeaconPacket packet = new BeaconPacket(List.of(
                new BeaconEffectEntry("minecraft:speed", 1),
                new BeaconEffectEntry("minecraft:strength", 0)), 42, true);

        byte[] first = encode(BeaconPacket.CODEC, packet);
        BeaconPacket decoded = decode(BeaconPacket.CODEC, first);
        assertNotSame(packet, decoded);
        assertArrayEquals(first, encode(BeaconPacket.CODEC, decoded));
    }

    @Test
    void beaconPacketWithNoEffectsRoundTrips() {
        BeaconPacket packet = new BeaconPacket(List.of(), 1, false);
        byte[] first = encode(BeaconPacket.CODEC, packet);
        assertArrayEquals(first, encode(BeaconPacket.CODEC, decode(BeaconPacket.CODEC, first)));
    }
}
