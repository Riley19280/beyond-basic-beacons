package com.rileystech.beyondbasicbeacons.platform;

import com.rileystech.beyondbasicbeacons.Constants;
import com.rileystech.beyondbasicbeacons.packet.ICustomPacketPayload;
import com.rileystech.beyondbasicbeacons.platform.services.INetworkHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;


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

    public <T extends ICustomPacketPayload<T>> void sendToPlayer(ServerPlayer player, T packet) {
        Constants.LOG.info("Sending packet {} to player {}", packet.type().id(), player.getGameProfile().name());
        ServerPlayNetworking.send(player, packet);
    }

    public <T extends ICustomPacketPayload<T>> void sendToServer(T packet) {
        Constants.LOG.info("Sending packet {} to server", packet.type().id());
        ClientPlayNetworking.send(packet);
    }
}