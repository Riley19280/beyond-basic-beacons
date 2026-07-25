package com.rileystech.beyondbasicbeacons.packet;

import com.rileystech.beyondbasicbeacons.platform.Services;

public class PacketTypes {

    public static void register() {
        Services.NETWORKING.registerPacket(new BeaconPacket());
    }
}
