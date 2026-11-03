package com.rileystech.beyondbasicbeacons.data;

import com.rileystech.beyondbasicbeacons.MinecraftBootstrap;
import com.rileystech.beyondbasicbeacons.data.BeaconEffectCostManager.Entries;
import com.rileystech.beyondbasicbeacons.data.BeaconEffectCostManager.Entry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.rileystech.beyondbasicbeacons.TestJson.decode;
import static com.rileystech.beyondbasicbeacons.TestJson.parse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BeaconEffectCostManagerTest {

    @BeforeAll
    static void bootstrap() {
        MinecraftBootstrap.init();
    }

    @AfterEach
    void reset() {
        load();
    }

    @SafeVarargs
    private static void load(List<Entry>... files) {
        Map<Identifier, Entries> data = new LinkedHashMap<>();
        for (int i = 0; i < files.length; i++) {
            data.put(Identifier.parse("test:costs_" + i), new Entries(files[i]));
        }
        BeaconEffectCostManager.getInstance().apply(data, null, null);
    }

    @Test
    void decodesReadmeExample() {
        Entries entries = decode(Entries.CODEC, """
                {
                  "values": [
                    { "effect": "minecraft:strength", "multiplier": 1.5 },
                    { "effect": "minecraft:regeneration", "multiplier": 2.0 }
                  ]
                }
                """);
        assertEquals(List.of(new Entry("minecraft:strength", 1.5), new Entry("minecraft:regeneration", 2.0)), entries.values());
    }

    @Test
    void valuesIsRequired() {
        assertTrue(parse(Entries.CODEC, "{}").isError());
    }

    @Test
    void unlistedEffectsDefaultToOne() {
        load();
        assertEquals(1.0, BeaconEffectCostManager.getMultiplier(MobEffects.SPEED.value()));
    }

    @Test
    void listedEffectsUseConfiguredMultiplier() {
        load(List.of(new Entry("minecraft:strength", 1.5), new Entry("minecraft:regeneration", 2.0)));
        assertEquals(1.5, BeaconEffectCostManager.getMultiplier(MobEffects.STRENGTH.value()));
        assertEquals(2.0, BeaconEffectCostManager.getMultiplier(MobEffects.REGENERATION.value()));
        assertEquals(1.0, BeaconEffectCostManager.getMultiplier(MobEffects.SPEED.value()));
    }

    @Test
    void entriesAreMergedAcrossDatapacks() {
        load(List.of(new Entry("minecraft:strength", 1.5)), List.of(new Entry("minecraft:speed", 3.0)));
        assertEquals(1.5, BeaconEffectCostManager.getMultiplier(MobEffects.STRENGTH.value()));
        assertEquals(3.0, BeaconEffectCostManager.getMultiplier(MobEffects.SPEED.value()));
    }

    @Test
    void laterEntryOverridesDuplicates() {
        load(List.of(new Entry("minecraft:strength", 1.5)), List.of(new Entry("minecraft:strength", 4.0)));
        assertEquals(4.0, BeaconEffectCostManager.getMultiplier(MobEffects.STRENGTH.value()));
    }

    @Test
    void unknownEffectIdsAreIgnored() {
        load(List.of(new Entry("minecraft:not_a_real_effect", 9.0), new Entry("minecraft:speed", 2.0)));
        assertEquals(2.0, BeaconEffectCostManager.getMultiplier(MobEffects.SPEED.value()));
    }

    @Test
    void reloadClearsPreviousValues() {
        load(List.of(new Entry("minecraft:strength", 1.5)));
        load();
        assertEquals(1.0, BeaconEffectCostManager.getMultiplier(MobEffects.STRENGTH.value()));
    }
}
