package com.rileystech.beyondbasicbeacons.beacon;

import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

// Shared "required points" formula, used both by the client (BeaconScreenData, to gate the Save
// button/color the required-points text) and the server (BeaconPacket, to reject an edit that
// exceeds the beacon's available points even if a modified client sends one anyway). Generic over
// the effect type since the client's list holds EffectSelection (Holder<MobEffect> + points)
// while the server's holds BeaconEffectEntry (registry key string + points) - amplifierOf/multiplierOf
// let each caller pass its own effects list directly instead of pre-extracting into throwaway lists.
public class BeaconPoints {
    private BeaconPoints() {
    }

    public static <T> int requiredPoints(int range, List<T> effects, ToIntFunction<T> amplifierOf, ToDoubleFunction<T> multiplierOf, boolean hideParticles) {
        int required = hideParticles ? 25 : 0;
        for (T effect : effects) {
            required += (int) (Math.pow(2 + amplifierOf.applyAsInt(effect), 2) * range * multiplierOf.applyAsDouble(effect));
        }
        return required;
    }
}
