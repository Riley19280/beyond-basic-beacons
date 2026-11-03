package com.rileystech.beyondbasicbeacons.beacon;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BeaconPointsTest {

    private record Effect(int amplifier, double multiplier) {
    }

    private static int required(int range, boolean hideParticles, Effect... effects) {
        return BeaconPoints.requiredPoints(range, List.of(effects), Effect::amplifier, Effect::multiplier, hideParticles);
    }

    @Test
    void noEffectsCostsNothing() {
        assertEquals(0, required(50, false));
    }

    @Test
    void hidingParticlesCosts25() {
        assertEquals(25, required(50, true));
    }

    // (2 + amplifier)^2 * range * multiplier, as documented in the README
    @ParameterizedTest
    @CsvSource({
            "0, 1,   1.0, 4",
            "0, 10,  1.0, 40",
            "1, 10,  1.0, 90",
            "4, 100, 1.0, 3600",
            "0, 10,  1.5, 60",
            "1, 10,  2.0, 180",
            "0, 10,  0.0, 0",
    })
    void singleEffectFollowsFormula(int amplifier, int range, double multiplier, int expected) {
        assertEquals(expected, required(range, false, new Effect(amplifier, multiplier)));
    }

    @Test
    void multipleEffectsAreSummed() {
        // 4*10*1 + 9*10*1 + 16*10*1.5 = 40 + 90 + 240
        assertEquals(370, required(10, false, new Effect(0, 1.0), new Effect(1, 1.0), new Effect(2, 1.5)));
    }

    @Test
    void particleCostIsAddedOnTopOfEffects() {
        assertEquals(40 + 25, required(10, true, new Effect(0, 1.0)));
    }

    @Test
    void fractionalCostIsTruncatedPerEffect() {
        // 4 * 1 * 1.1 = 4.4 -> 4, twice = 8 (truncation happens per effect, not on the sum)
        assertEquals(8, required(1, false, new Effect(0, 1.1), new Effect(0, 1.1)));
    }
}
