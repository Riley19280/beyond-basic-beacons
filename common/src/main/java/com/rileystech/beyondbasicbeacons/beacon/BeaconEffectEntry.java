package com.rileystech.beyondbasicbeacons.beacon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

// A chosen beacon power: the effect's registry key plus its points. Kept outside the mixin
// package since Mixin forbids referencing anything inside the declared mixin package from
// outside the mixin transformer - BeaconPacket (a different package) needs this same shape.
public record BeaconEffectEntry(String key, int amplifier) {
    public static final Codec<BeaconEffectEntry> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("effect").forGetter(BeaconEffectEntry::key),
                    Codec.INT.fieldOf("points").forGetter(BeaconEffectEntry::amplifier)
            ).apply(instance, BeaconEffectEntry::new)
    );
}
