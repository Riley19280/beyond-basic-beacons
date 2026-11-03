package com.rileystech.beyondbasicbeacons.platform;

import com.rileystech.beyondbasicbeacons.Constants;
import com.rileystech.beyondbasicbeacons.packet.ICustomPacketPayload;
import com.rileystech.beyondbasicbeacons.platform.services.INetworkHelper;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.List;

// NeoForge only accepts payload registrations during RegisterPayloadHandlersEvent, which fires after
// mod construction - so registerPacket() queues each packet and the event handler registers them.
public class NeoForgeNetworkHelper implements INetworkHelper {
    private static final String PROTOCOL_VERSION = "1";
    private static final List<ICustomPacketPayload<?>> PENDING = new ArrayList<>();
    private static boolean registered = false;

    @Override
    public <T extends ICustomPacketPayload<T>> void registerPacket(T packet) {
        if (registered) {
            throw new IllegalStateException("Packet " + packet.type().id() + " registered after RegisterPayloadHandlersEvent");
        }
        PENDING.add(packet);
    }

    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        for (ICustomPacketPayload<?> packet : PENDING) {
            register(registrar, packet);
        }
        registered = true;
    }

    // Handlers run on the main (server/client) thread - NeoForge's default for a PayloadRegistrar.
    private static <T extends ICustomPacketPayload<T>> void register(PayloadRegistrar registrar, ICustomPacketPayload<?> packet) {
        @SuppressWarnings("unchecked")
        T typed = (T) packet;
        Constants.LOG.info("Registering packet {} {}", typed.type().id(), typed.getClass());
        registrar.playBidirectional(typed.type(), typed.codec(),
                (payload, context) -> payload.onServerReceived((ServerPlayer) context.player()),
                (payload, context) -> payload.onClientReceived());
    }

    @Override
    public <T extends ICustomPacketPayload<T>> void sendToPlayer(ServerPlayer player, T packet) {
        Constants.LOG.info("Sending packet {} to player {}", packet.type().id(), player.getGameProfile().name());
        PacketDistributor.sendToPlayer(player, packet);
    }

    // Only ever called from client code (BeaconScreen), so ClientPacketDistributor is never loaded on
    // a dedicated server.
    @Override
    public <T extends ICustomPacketPayload<T>> void sendToServer(T packet) {
        Constants.LOG.info("Sending packet {} to server", packet.type().id());
        ClientPacketDistributor.sendToServer(packet);
    }
}
