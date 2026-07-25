package com.rileystech.beyondbasicbeacons.platform.services;

import com.rileystech.beyondbasicbeacons.packet.ICustomPacketPayload;
import net.minecraft.core.Registry;

public interface INetworkHelper {
    public <T extends ICustomPacketPayload<T>> void registerPacket(T packet);
}