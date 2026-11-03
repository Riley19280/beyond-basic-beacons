package com.rileystech.beyondbasicbeacons.data;

import com.rileystech.beyondbasicbeacons.MinecraftBootstrap;
import com.rileystech.beyondbasicbeacons.data.BeaconEffectFilterManager.Entries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.rileystech.beyondbasicbeacons.TestJson.decode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BeaconEffectFilterManagerTest {

    private static MobEffect speed;
    private static MobEffect haste;
    private static MobEffect instantDamage;

    @BeforeAll
    static void bootstrap() {
        MinecraftBootstrap.init();
        speed = MobEffects.SPEED.value();
        haste = MobEffects.HASTE.value();
        instantDamage = MobEffects.INSTANT_DAMAGE.value();
    }

    @AfterEach
    void reset() {
        load();
    }

    @SafeVarargs
    private static void load(Entries... files) {
        Map<Identifier, Entries> data = new LinkedHashMap<>();
        for (int i = 0; i < files.length; i++) {
            data.put(Identifier.parse("test:filter_" + i), files[i]);
        }
        BeaconEffectFilterManager.getInstance().apply(data, null, null);
    }

    @Test
    void bothListsAreOptional() {
        assertEquals(new Entries(List.of(), List.of()), decode(Entries.CODEC, "{}"));
    }

    @Test
    void decodesReadmeExample() {
        Entries entries = decode(Entries.CODEC, """
                { "blacklist": ["minecraft:instant_damage"], "whitelist": [] }
                """);
        assertEquals(new Entries(List.of("minecraft:instant_damage"), List.of()), entries);
    }

    @Test
    void everythingAllowedWithNoFilters() {
        load();
        assertTrue(BeaconEffectFilterManager.isAllowed(speed));
        assertTrue(BeaconEffectFilterManager.isAllowed(instantDamage));
        assertEquals(BuiltInRegistries.MOB_EFFECT.size(), BeaconEffectFilterManager.getAllowedEffectIds().size());
    }

    @Test
    void blacklistBlocksListedEffects() {
        load(new Entries(List.of("minecraft:instant_damage"), List.of()));
        assertFalse(BeaconEffectFilterManager.isAllowed(instantDamage));
        assertTrue(BeaconEffectFilterManager.isAllowed(speed));
        assertFalse(BeaconEffectFilterManager.getAllowedEffectIds().contains("minecraft:instant_damage"));
    }

    @Test
    void whitelistLimitsToListedEffects() {
        load(new Entries(List.of(), List.of("minecraft:speed", "minecraft:haste")));
        assertTrue(BeaconEffectFilterManager.isAllowed(speed));
        assertTrue(BeaconEffectFilterManager.isAllowed(haste));
        assertFalse(BeaconEffectFilterManager.isAllowed(instantDamage));
        assertEquals(List.of("minecraft:haste", "minecraft:speed"),
                BeaconEffectFilterManager.getAllowedEffectIds().stream().sorted().toList());
    }

    @Test
    void blacklistWinsOverWhitelist() {
        load(new Entries(List.of("minecraft:speed"), List.of("minecraft:speed", "minecraft:haste")));
        assertFalse(BeaconEffectFilterManager.isAllowed(speed));
        assertTrue(BeaconEffectFilterManager.isAllowed(haste));
    }

    @Test
    void listsAreMergedAcrossDatapacks() {
        load(new Entries(List.of("minecraft:instant_damage"), List.of("minecraft:speed")),
                new Entries(List.of("minecraft:haste"), List.of("minecraft:haste", "minecraft:strength")));
        assertTrue(BeaconEffectFilterManager.isAllowed(speed));
        assertTrue(BeaconEffectFilterManager.isAllowed(MobEffects.STRENGTH.value()));
        assertFalse(BeaconEffectFilterManager.isAllowed(haste));
        assertFalse(BeaconEffectFilterManager.isAllowed(instantDamage));
    }

    @Test
    void unknownEffectIdsAreIgnored() {
        load(new Entries(List.of("minecraft:not_a_real_effect"), List.of()));
        assertTrue(BeaconEffectFilterManager.isAllowed(speed));
    }

    @Test
    void reloadReplacesPreviousFilters() {
        load(new Entries(List.of("minecraft:speed"), List.of()));
        load();
        assertTrue(BeaconEffectFilterManager.isAllowed(speed));
    }
}
