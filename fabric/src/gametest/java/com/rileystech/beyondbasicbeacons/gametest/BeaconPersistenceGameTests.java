package com.rileystech.beyondbasicbeacons.gametest;

import com.rileystech.beyondbasicbeacons.access.NeighboringBeaconAccess;
import com.rileystech.beyondbasicbeacons.beacon.BeaconEffectEntry;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

import static com.rileystech.beyondbasicbeacons.gametest.BeaconTestSupport.placeBeacon;

// The mod's extra beacon data surviving a world save/load, and reaching clients via the
// block entity update tag (which BeaconMenu/BeaconScreen read from on the client).
public class BeaconPersistenceGameTests {
    private static final BlockPos BEACON = new BlockPos(3, 1, 3);
    private static final List<BeaconEffectEntry> EFFECTS = List.of(
            new BeaconEffectEntry("minecraft:speed", 2),
            new BeaconEffectEntry("minecraft:regeneration", 0));

    private static NeighboringBeaconAccess configuredBeacon(GameTestHelper helper) {
        NeighboringBeaconAccess beacon = placeBeacon(helper, BEACON);
        beacon.bbb_setEffects(EFFECTS);
        beacon.bbb_setRange(37);
        beacon.bbb_setHideParticles(true);
        beacon.bbb_setBaseBlockPoints(123);
        beacon.bbb_setNeighboringBeacons(2);
        return beacon;
    }

    private static NeighboringBeaconAccess reload(GameTestHelper helper, CompoundTag tag) {
        HolderLookup.Provider registries = helper.getLevel().registryAccess();
        BlockEntity loaded = BlockEntity.loadStatic(helper.absolutePos(BEACON), Blocks.BEACON.defaultBlockState(), tag, registries);
        helper.assertTrue(loaded instanceof BeaconBlockEntity, "should load back as a beacon");
        return (NeighboringBeaconAccess) loaded;
    }

    private static void assertSameData(GameTestHelper helper, NeighboringBeaconAccess loaded) {
        helper.assertValueEqual(loaded.bbb_getEffects(), EFFECTS, "effects");
        helper.assertValueEqual(loaded.bbb_getRange(), 37, "range");
        helper.assertValueEqual(loaded.bbb_isHideParticles(), true, "hideParticles");
        helper.assertValueEqual(loaded.bbb_getBaseBlockPoints(), 123, "base block points");
        helper.assertValueEqual(loaded.bbb_getNeighboringBeacons(), 2, "neighboring beacons");
    }

    @GameTest
    public void freshBeaconHasDefaults(GameTestHelper helper) {
        NeighboringBeaconAccess beacon = placeBeacon(helper, BEACON);
        helper.assertTrue(beacon.bbb_getEffects().isEmpty(), "no effects by default");
        helper.assertValueEqual(beacon.bbb_getRange(), 1, "default range");
        helper.assertValueEqual(beacon.bbb_isHideParticles(), false, "default hideParticles");
        helper.assertValueEqual(beacon.bbb_getBaseBlockPoints(), 0, "default points");
        helper.succeed();
    }

    @GameTest
    public void dataSurvivesSaveAndLoad(GameTestHelper helper) {
        NeighboringBeaconAccess beacon = configuredBeacon(helper);
        CompoundTag saved = ((BlockEntity) beacon).saveWithFullMetadata(helper.getLevel().registryAccess());
        assertSameData(helper, reload(helper, saved));
        helper.succeed();
    }

    @GameTest
    public void dataIsSentToClients(GameTestHelper helper) {
        NeighboringBeaconAccess beacon = configuredBeacon(helper);
        CompoundTag update = ((BlockEntity) beacon).getUpdateTag(helper.getLevel().registryAccess());
        helper.assertValueEqual(update.read("effects", BeaconEffectEntry.CODEC.listOf()).orElse(List.of()), EFFECTS, "synced effects");
        helper.assertValueEqual(update.getIntOr("range", -1), 37, "synced range");
        helper.assertValueEqual(update.getBooleanOr("hideParticles", false), true, "synced hideParticles");
        helper.assertValueEqual(update.getIntOr("baseBlockPoints", -1), 123, "synced base block points");
        helper.assertValueEqual(update.getIntOr("neighboringBeacons", -1), 2, "synced neighboring beacons");
        helper.succeed();
    }

    @GameTest
    public void legacyBeaconWithoutModDataLoadsWithDefaults(GameTestHelper helper) {
        placeBeacon(helper, BEACON);
        BlockEntity blockEntity = helper.getBlockEntity(BEACON, BeaconBlockEntity.class);
        CompoundTag saved = blockEntity.saveWithFullMetadata(helper.getLevel().registryAccess());
        for (String key : List.of("effects", "range", "hideParticles", "baseBlockPoints", "neighboringBeacons")) {
            saved.remove(key);
        }
        NeighboringBeaconAccess loaded = reload(helper, saved);
        helper.assertTrue(loaded.bbb_getEffects().isEmpty(), "no effects");
        helper.assertValueEqual(loaded.bbb_getRange(), 1, "range");
        helper.succeed();
    }
}
