package com.rileystech.beyondbasicbeacons.beacon;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.rileystech.beyondbasicbeacons.TestJson.decode;
import static com.rileystech.beyondbasicbeacons.TestJson.encode;
import static com.rileystech.beyondbasicbeacons.TestJson.parse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BeaconEffectEntryTest {

    @Test
    void decodesSavedFormat() {
        BeaconEffectEntry entry = decode(BeaconEffectEntry.CODEC, """
                { "effect": "minecraft:speed", "points": 2 }
                """);
        assertEquals(new BeaconEffectEntry("minecraft:speed", 2), entry);
    }

    // Field names are persisted in block entity NBT and sent over the network - renaming them
    // would silently wipe every existing beacon's configuration.
    @Test
    void encodesWithStableFieldNames() {
        JsonObject json = encode(BeaconEffectEntry.CODEC, new BeaconEffectEntry("minecraft:haste", 1)).getAsJsonObject();
        assertEquals("minecraft:haste", json.get("effect").getAsString());
        assertEquals(1, json.get("points").getAsInt());
        assertEquals(2, json.size());
    }

    @Test
    void listRoundTrips() {
        List<BeaconEffectEntry> entries = List.of(
                new BeaconEffectEntry("minecraft:speed", 0),
                new BeaconEffectEntry("minecraft:strength", 4));
        var codec = BeaconEffectEntry.CODEC.listOf();
        assertEquals(entries, codec.parse(JsonOps.INSTANCE, encode(codec, entries)).getOrThrow());
    }

    @Test
    void missingFieldIsAnError() {
        assertTrue(parse(BeaconEffectEntry.CODEC, "{ \"effect\": \"minecraft:speed\" }").isError());
    }
}
