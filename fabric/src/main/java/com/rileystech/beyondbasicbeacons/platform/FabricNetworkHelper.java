package com.rileystech.beyondbasicbeacons.platform;

import com.rileystech.beyondbasicbeacons.Constants;
import com.rileystech.beyondbasicbeacons.packet.ICustomPacketPayload;
import com.rileystech.beyondbasicbeacons.platform.services.INetworkHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;


public class FabricNetworkHelper implements INetworkHelper {

    public <T extends ICustomPacketPayload<T>> void registerPacket(T packet) {
        try {
            PayloadTypeRegistry.serverboundPlay().register(packet.type(), packet.codec());
            PayloadTypeRegistry.clientboundPlay().register(packet.type(), packet.codec());
        } catch (IllegalArgumentException e) {
            Constants.LOG.error(e.toString());
        }

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            Constants.LOG.info("Registering packet {} {} on the client", packet.type().id(), packet.getClass());

            ClientPlayNetworking.registerGlobalReceiver(packet.type(), (payload, context) -> context.client().execute(() ->
                    payload.onClientReceived()
            ));

        }

        Constants.LOG.info("Registering packet {} {} on the server", packet.type().id(), packet.getClass());
        ServerPlayNetworking.registerGlobalReceiver(packet.type(), (payload, context) -> context.server().execute(() ->
                payload.onServerReceived()
        ));

    }

//    public <T> void sendToServer(T packet, boolean ignoreCheck) {
//        PacketContainer<T> container = (PacketContainer<T>) PACKET_MAP.get(packet.getClass());
//        if (container != null) {
//            if (ignoreCheck || ClientPlayNetworking.canSend(container.type().id()))
//                ClientPlayNetworking.send(new CommonPacketWrapper<>(container, packet));
//        }
//        else
//            throw new RegistrationException(packet.getClass() + "{} packet not registered on the client, packets need to be registered on both sides!");
//
//    }
//
//    public <T> void sendToClient(T packet, ServerPlayer player, boolean ignoreCheck) {
//        PacketContainer<T> container = (PacketContainer<T>) PACKET_MAP.get(packet.getClass());
//        if (container != null) {
//            if (ignoreCheck || ServerPlayNetworking.canSend(player, container.type().id()))
//                ServerPlayNetworking.send(player, new CommonPacketWrapper<>(container, packet));
//        }
//        else
//            throw new RegistrationException(packet.getClass() + "{} packet not registered on the server, packets need to be registered on both sides!");
//    }
}