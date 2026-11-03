package com.rileystech.beyondbasicbeacons.gametest;

import com.rileystech.beyondbasicbeacons.data.BeaconBaseBlockPointManager;
import com.rileystech.beyondbasicbeacons.data.BeaconEffectCostManager;
import com.rileystech.beyondbasicbeacons.data.BeaconEffectFilterManager;
import com.rileystech.beyondbasicbeacons.data.BeaconSettingsManager;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;

// Proves all four reload listeners are registered on Fabric and actually read datapacks from a
// running server: the mod's own bundled datapack plus this test mod's datapack.
public class DatapackLoadingGameTests {

    @GameTest
    public void settingsAreLoadedFromDatapack(GameTestHelper helper) {
        helper.assertValueEqual(BeaconSettingsManager.getSettings(), BeaconTestSupport.TEST_SETTINGS, "beacon settings");
        helper.succeed();
    }

    @GameTest
    public void effectFiltersAreLoadedFromDatapack(GameTestHelper helper) {
        helper.assertFalse(BeaconEffectFilterManager.isAllowed(MobEffects.WITHER.value()), "wither should be blacklisted");
        helper.assertTrue(BeaconEffectFilterManager.isAllowed(MobEffects.SPEED.value()), "speed should be allowed");
        helper.assertFalse(BeaconEffectFilterManager.getAllowedEffectIds().contains("minecraft:wither"), "allowed list sent to clients excludes wither");
        helper.succeed();
    }

    @GameTest
    public void effectCostsAreLoadedFromDatapack(GameTestHelper helper) {
        helper.assertValueEqual(BeaconEffectCostManager.getMultiplier(MobEffects.JUMP_BOOST.value()), 2.0, "jump boost multiplier");
        helper.assertValueEqual(BeaconEffectCostManager.getMultiplier(MobEffects.SPEED.value()), 1.0, "speed multiplier");
        helper.succeed();
    }

    @GameTest
    public void bundledBlockPointsAreLoaded(GameTestHelper helper) {
        var points = BeaconBaseBlockPointManager.getBlockPointMap();
        helper.assertValueEqual(points.get(Blocks.IRON_BLOCK), 1, "iron block points");
        helper.assertValueEqual(points.get(Blocks.NETHERITE_BLOCK), 10, "netherite block points");
        helper.succeed();
    }
}
