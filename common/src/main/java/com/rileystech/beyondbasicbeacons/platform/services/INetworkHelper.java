package com.rileystech.beyondbasicbeacons.platform.services;

import com.rileystech.beyondbasicbeacons.packet.ICustomPacketPayload;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerPlayer;

public interface INetworkHelper {
    public <T extends ICustomPacketPayload<T>> void registerPacket(T packet);

    public <T extends ICustomPacketPayload<T>> void sendToPlayer(ServerPlayer player, T packet);

    public <T extends ICustomPacketPayload<T>> void sendToServer(T packet);
}