package com.rileystech.beyondbasicbeacons.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.rileystech.beyondbasicbeacons.data.BeaconSettingsManager.Settings;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static com.rileystech.beyondbasicbeacons.TestJson.decode;
import static com.rileystech.beyondbasicbeacons.TestJson.encode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BeaconSettingsManagerTest {

    private static final String FULL_SETTINGS = """
            {
              "min_range": 1,
              "max_range": 100,
              "min_amplifier": 0,
              "max_amplifier": 4,
              "max_beacon_size": 10,
              "neighboring_beacon_check_range_xz": 2,
              "neighboring_beacon_check_range_y": 1,
              "max_effects_per_beacon": 10
            }
            """;

    @AfterEach
    void reset() {
        BeaconSettingsManager.getInstance().apply(Map.of(), null, null);
    }

    @Test
    void defaultsMatchReadme() {
        assertEquals(new Settings(1, 100, 0, 4, 5, 2, 1, 10), Settings.DEFAULT);
    }

    @Test
    void decodesReadmeExample() {
        assertEquals(new Settings(1, 100, 0, 4, 10, 2, 1, 10), decode(Settings.CODEC, FULL_SETTINGS));
    }

    @Test
    void roundTrips() {
        Settings settings = new Settings(3, 64, 1, 2, 7, 4, 3, 6);
        assertEquals(settings, Settings.CODEC.parse(JsonOps.INSTANCE, encode(Settings.CODEC, settings)).getOrThrow());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "min_range", "max_range", "min_amplifier", "max_amplifier", "max_beacon_size",
            "neighboring_beacon_check_range_xz", "neighboring_beacon_check_range_y", "max_effects_per_beacon"
    })
    void everyFieldIsRequired(String field) {
        JsonObject json = JsonParser.parseString(FULL_SETTINGS).getAsJsonObject();
        assertNotNull(json.remove(field), "test fixture is missing '" + field + "'");
        assertTrue(Settings.CODEC.parse(JsonOps.INSTANCE, json).isError(), "expected error when '" + field + "' is missing");
    }

    @Test
    void usesDefaultsWhenNoDatapackProvidesSettings() {
        BeaconSettingsManager.getInstance().apply(Map.of(), null, null);
        assertEquals(Settings.DEFAULT, BeaconSettingsManager.getSettings());
    }

    @Test
    void usesLoadedSettings() {
        Settings custom = new Settings(5, 50, 0, 2, 3, 1, 1, 4);
        BeaconSettingsManager.getInstance().apply(Map.of(Identifier.parse("test:settings"), custom), null, null);
        assertEquals(custom, BeaconSettingsManager.getSettings());
    }

    @Test
    void reloadWithoutSettingsRevertsToDefaults() {
        BeaconSettingsManager.getInstance().apply(Map.of(Identifier.parse("test:settings"), new Settings(5, 50, 0, 2, 3, 1, 1, 4)), null, null);
        BeaconSettingsManager.getInstance().apply(Map.of(), null, null);
        assertEquals(Settings.DEFAULT, BeaconSettingsManager.getSettings());
    }
}
