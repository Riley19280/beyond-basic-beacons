package com.rileystech.beyondbasicbeacons.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.NonNull;

public interface ICustomPacketPayload<T extends ICustomPacketPayload<T>> extends CustomPacketPayload {
    @Override
    CustomPacketPayload.@NonNull Type<T> type();

    StreamCodec<RegistryFriendlyByteBuf, T> codec();

    void onServerReceived();

    void onClientReceived();
}
