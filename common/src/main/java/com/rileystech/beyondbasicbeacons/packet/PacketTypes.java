package com.rileystech.beyondbasicbeacons.packet;

import com.rileystech.beyondbasicbeacons.platform.Services;

import java.util.List;

public class PacketTypes {

    public static void register() {
        Services.NETWORKING.registerPacket(new BeaconPacket(List.of(), 1, false));
    }
}
