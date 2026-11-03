package com.rileystech.beyondbasicbeacons.gametest;

import com.rileystech.beyondbasicbeacons.access.NeighboringBeaconAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;

import static com.rileystech.beyondbasicbeacons.gametest.BeaconTestSupport.fillLayer;
import static com.rileystech.beyondbasicbeacons.gametest.BeaconTestSupport.placeBeacon;
import static com.rileystech.beyondbasicbeacons.gametest.BeaconTestSupport.updateBase;

// Pyramid scanning and power calculation (BeaconBlockEntityMixin.bbb_baseUpdating).
// Block points come from the mod's bundled datapack: iron 1, gold 2, emerald/diamond 3, netherite 10.
// The test datapack sets max_beacon_size = 2 and a neighbor check range of 2 (xz) / 1 (y).
public class BeaconBaseGameTests {
    // Room for a full 3-layer pyramid (7x7 base) inside the default 8x8x8 test structure.
    private static final BlockPos TALL_BEACON = new BlockPos(3, 3, 3);
    // A beacon on a single 3x3 layer, leaving room around it for neighbors.
    private static final BlockPos LOW_BEACON = new BlockPos(3, 1, 3);

    private static void assertBase(GameTestHelper helper, NeighboringBeaconAccess beacon, int levels, int expectedLevels,
                                   int expectedPoints, int expectedNeighbors) {
        helper.assertValueEqual(levels, expectedLevels, "levels");
        helper.assertValueEqual(beacon.bbb_getBaseBlockPoints(), expectedPoints, "base block points");
        helper.assertValueEqual(beacon.bbb_getNeighboringBeacons(), expectedNeighbors, "neighboring beacons");
    }

    @BeaconGameTest
    public void noBaseGivesNothing(GameTestHelper helper) {
        NeighboringBeaconAccess beacon = placeBeacon(helper, LOW_BEACON);
        assertBase(helper, beacon, updateBase(helper, LOW_BEACON), 0, 0, 0);
        helper.succeed();
    }

    @BeaconGameTest
    public void singleIronLayer(GameTestHelper helper) {
        fillLayer(helper, TALL_BEACON, 1, Blocks.IRON_BLOCK);
        NeighboringBeaconAccess beacon = placeBeacon(helper, TALL_BEACON);
        assertBase(helper, beacon, updateBase(helper, TALL_BEACON), 1, 9, 0);
        helper.succeed();
    }

    @BeaconGameTest
    public void mixedLayersSumTheirBlockPoints(GameTestHelper helper) {
        fillLayer(helper, TALL_BEACON, 1, Blocks.GOLD_BLOCK);    // 9 * 2
        fillLayer(helper, TALL_BEACON, 2, Blocks.DIAMOND_BLOCK); // 25 * 3
        NeighboringBeaconAccess beacon = placeBeacon(helper, TALL_BEACON);
        assertBase(helper, beacon, updateBase(helper, TALL_BEACON), 2, 18 + 75, 0);
        helper.succeed();
    }

    @BeaconGameTest
    public void netheriteIsWorthTen(GameTestHelper helper) {
        fillLayer(helper, LOW_BEACON, 1, Blocks.NETHERITE_BLOCK);
        NeighboringBeaconAccess beacon = placeBeacon(helper, LOW_BEACON);
        assertBase(helper, beacon, updateBase(helper, LOW_BEACON), 1, 90, 0);
        helper.succeed();
    }

    @BeaconGameTest
    public void nonBaseBlockBreaksTheLayer(GameTestHelper helper) {
        fillLayer(helper, LOW_BEACON, 1, Blocks.IRON_BLOCK);
        helper.setBlock(LOW_BEACON.offset(0, -1, 0), Blocks.STONE);
        NeighboringBeaconAccess beacon = placeBeacon(helper, LOW_BEACON);
        assertBase(helper, beacon, updateBase(helper, LOW_BEACON), 0, 0, 0);
        helper.succeed();
    }

    @BeaconGameTest
    public void maxBeaconSizeCapsLevelsAndPoints(GameTestHelper helper) {
        fillLayer(helper, TALL_BEACON, 1, Blocks.IRON_BLOCK);
        fillLayer(helper, TALL_BEACON, 2, Blocks.IRON_BLOCK);
        fillLayer(helper, TALL_BEACON, 3, Blocks.IRON_BLOCK); // beyond max_beacon_size = 2
        NeighboringBeaconAccess beacon = placeBeacon(helper, TALL_BEACON);
        assertBase(helper, beacon, updateBase(helper, TALL_BEACON), 2, 9 + 25, 0);
        helper.succeed();
    }

    // Only complete layers should provide power - a layer with a missing block isn't part of the
    // pyramid (it doesn't count as a level), so its blocks shouldn't count either.
    @BeaconGameTest
    public void incompleteLayerGivesNoPoints(GameTestHelper helper) {
        fillLayer(helper, TALL_BEACON, 1, Blocks.IRON_BLOCK);
        fillLayer(helper, TALL_BEACON, 2, Blocks.IRON_BLOCK);
        // The last block the scan visits in layer 2, so every other block in it is seen first.
        helper.setBlock(TALL_BEACON.offset(2, -2, 2), Blocks.AIR);
        NeighboringBeaconAccess beacon = placeBeacon(helper, TALL_BEACON);
        assertBase(helper, beacon, updateBase(helper, TALL_BEACON), 1, 9, 0);
        helper.succeed();
    }

    @BeaconGameTest
    public void oneNeighborHalvesPoints(GameTestHelper helper) {
        fillLayer(helper, LOW_BEACON, 1, Blocks.GOLD_BLOCK); // 18
        NeighboringBeaconAccess beacon = placeBeacon(helper, LOW_BEACON);
        helper.setBlock(LOW_BEACON.offset(2, 0, 0), Blocks.BEACON);
        assertBase(helper, beacon, updateBase(helper, LOW_BEACON), 1, 9, 1);
        helper.succeed();
    }

    @BeaconGameTest
    public void twoNeighborsSplitPointsThreeWays(GameTestHelper helper) {
        fillLayer(helper, LOW_BEACON, 1, Blocks.GOLD_BLOCK); // 18
        NeighboringBeaconAccess beacon = placeBeacon(helper, LOW_BEACON);
        helper.setBlock(LOW_BEACON.offset(2, 0, 0), Blocks.BEACON);
        helper.setBlock(LOW_BEACON.offset(-2, 0, -2), Blocks.BEACON);
        assertBase(helper, beacon, updateBase(helper, LOW_BEACON), 1, 6, 2);
        helper.succeed();
    }

    @BeaconGameTest
    public void splitPointsRoundDown(GameTestHelper helper) {
        fillLayer(helper, LOW_BEACON, 1, Blocks.IRON_BLOCK); // 9
        NeighboringBeaconAccess beacon = placeBeacon(helper, LOW_BEACON);
        helper.setBlock(LOW_BEACON.offset(0, 0, 2), Blocks.BEACON);
        assertBase(helper, beacon, updateBase(helper, LOW_BEACON), 1, 4, 1);
        helper.succeed();
    }

    @BeaconGameTest
    public void beaconsOutsideCheckRangeAreNotNeighbors(GameTestHelper helper) {
        fillLayer(helper, LOW_BEACON, 1, Blocks.GOLD_BLOCK); // 18
        NeighboringBeaconAccess beacon = placeBeacon(helper, LOW_BEACON);
        helper.setBlock(LOW_BEACON.offset(3, 0, 0), Blocks.BEACON);  // xz distance 3 > 2
        helper.setBlock(LOW_BEACON.offset(0, 2, 0), Blocks.BEACON);  // y distance 2 > 1
        assertBase(helper, beacon, updateBase(helper, LOW_BEACON), 1, 18, 0);
        helper.succeed();
    }

    @BeaconGameTest
    public void neighborOneBlockHigherCounts(GameTestHelper helper) {
        fillLayer(helper, LOW_BEACON, 1, Blocks.GOLD_BLOCK); // 18
        NeighboringBeaconAccess beacon = placeBeacon(helper, LOW_BEACON);
        helper.setBlock(LOW_BEACON.offset(2, 1, 2), Blocks.BEACON);
        assertBase(helper, beacon, updateBase(helper, LOW_BEACON), 1, 9, 1);
        helper.succeed();
    }

    @BeaconGameTest
    public void removingNeighborRestoresPoints(GameTestHelper helper) {
        fillLayer(helper, LOW_BEACON, 1, Blocks.GOLD_BLOCK); // 18
        NeighboringBeaconAccess beacon = placeBeacon(helper, LOW_BEACON);
        helper.setBlock(LOW_BEACON.offset(2, 0, 0), Blocks.BEACON);
        updateBase(helper, LOW_BEACON);
        helper.setBlock(LOW_BEACON.offset(2, 0, 0), Blocks.AIR);
        assertBase(helper, beacon, updateBase(helper, LOW_BEACON), 1, 18, 0);
        helper.succeed();
    }
}
