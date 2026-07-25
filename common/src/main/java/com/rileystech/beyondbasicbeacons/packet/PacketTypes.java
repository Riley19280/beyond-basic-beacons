package com.rileystech.beyondbasicbeacons.packet;

import com.rileystech.beyondbasicbeacons.platform.Services;

import java.util.List;

public class PacketTypes {

    public static void register() {
        Services.NETWORKING.registerPacket(new BeaconPacket(0, 0, 0, List.of(), 0, false));
    }
}
