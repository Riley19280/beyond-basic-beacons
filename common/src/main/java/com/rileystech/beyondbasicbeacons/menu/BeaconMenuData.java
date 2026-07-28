package com.rileystech.beyondbasicbeacons.menu;

import com.mojang.serialization.Codec;
import com.rileystech.beyondbasicbeacons.data.BeaconSettingsManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

// Carried alongside the vanilla menu-open packet (see BeaconBlockEntityFabricMixin) so the client
// gets the block's position, a snapshot of the current datapack-driven settings, and the resolved
// list of allowed effect ids, all at the moment the menu opens - settings/effect-filter data are
// server-only (PackType.SERVER_DATA reload listeners never run on a real client), so without this
// the client's own copy would always be Settings.DEFAULT / an unfiltered effect list.
public record BeaconMenuData(BlockPos pos, BeaconSettingsManager.Settings settings, List<String> allowedEffects) {
    public static final StreamCodec<RegistryFriendlyByteBuf, BeaconMenuData> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, BeaconMenuData::pos,
            ByteBufCodecs.fromCodec(BeaconSettingsManager.Settings.CODEC), BeaconMenuData::settings,
            ByteBufCodecs.fromCodec(Codec.STRING.listOf()), BeaconMenuData::allowedEffects,
            BeaconMenuData::new
    );
}
