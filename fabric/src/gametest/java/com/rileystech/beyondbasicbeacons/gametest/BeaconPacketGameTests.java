package com.rileystech.beyondbasicbeacons.gametest;

import com.rileystech.beyondbasicbeacons.access.NeighboringBeaconAccess;
import com.rileystech.beyondbasicbeacons.beacon.BeaconEffectEntry;
import com.rileystech.beyondbasicbeacons.packet.BeaconPacket;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.List;

import static com.rileystech.beyondbasicbeacons.gametest.BeaconTestSupport.placeBeacon;
import static com.rileystech.beyondbasicbeacons.gametest.BeaconTestSupport.withBeaconMenuOpen;
import static com.rileystech.beyondbasicbeacons.gametest.BeaconTestSupport.withPlayerAt;

// Server-side validation of the Save packet (BeaconPacket.onServerReceived) - the only thing stopping
// a modified client from saving an illegal beacon. Test datapack limits: range 1-64, amplifier 0-3,
// at most 4 effects, wither blacklisted, jump_boost costs 2x.
public class BeaconPacketGameTests {
    private static final BlockPos BEACON = new BlockPos(3, 1, 3);
    private static final int AVAILABLE = 100;

    private static final List<BeaconEffectEntry> ORIGINAL_EFFECTS = List.of(new BeaconEffectEntry("minecraft:haste", 0));
    private static final int ORIGINAL_RANGE = 5;

    private static NeighboringBeaconAccess beacon(GameTestHelper helper) {
        NeighboringBeaconAccess beacon = placeBeacon(helper, BEACON);
        beacon.bbb_setBaseBlockPoints(AVAILABLE);
        beacon.bbb_setEffects(ORIGINAL_EFFECTS);
        beacon.bbb_setRange(ORIGINAL_RANGE);
        beacon.bbb_setHideParticles(false);
        return beacon;
    }

    private static BeaconEffectEntry speed(int amplifier) {
        return new BeaconEffectEntry("minecraft:speed", amplifier);
    }

    private static void send(GameTestHelper helper, BeaconPacket packet) {
        withBeaconMenuOpen(helper, BEACON, packet::onServerReceived);
    }

    private static void assertApplied(GameTestHelper helper, NeighboringBeaconAccess beacon, List<BeaconEffectEntry> effects, int range, boolean hide) {
        helper.assertValueEqual(beacon.bbb_getEffects(), effects, "effects");
        helper.assertValueEqual(beacon.bbb_getRange(), range, "range");
        helper.assertValueEqual(beacon.bbb_isHideParticles(), hide, "hideParticles");
    }

    private static void assertRejected(GameTestHelper helper, NeighboringBeaconAccess beacon) {
        assertApplied(helper, beacon, ORIGINAL_EFFECTS, ORIGINAL_RANGE, false);
    }

    private static void rejects(GameTestHelper helper, List<BeaconEffectEntry> effects, int range, boolean hide) {
        NeighboringBeaconAccess beacon = beacon(helper);
        send(helper, new BeaconPacket(effects, range, hide));
        assertRejected(helper, beacon);
        helper.succeed();
    }

    private static void accepts(GameTestHelper helper, List<BeaconEffectEntry> effects, int range, boolean hide) {
        NeighboringBeaconAccess beacon = beacon(helper);
        send(helper, new BeaconPacket(effects, range, hide));
        assertApplied(helper, beacon, effects, range, hide);
        helper.succeed();
    }

    @GameTest
    public void validPacketIsSaved(GameTestHelper helper) {
        accepts(helper, List.of(speed(1), new BeaconEffectEntry("minecraft:strength", 0)), 4, true); // 36+16+25 = 77
    }

    @GameTest
    public void clearingAllEffectsIsAllowed(GameTestHelper helper) {
        accepts(helper, List.of(), 64, false);
    }

    @GameTest
    public void rangeBelowMinimumIsRejected(GameTestHelper helper) {
        rejects(helper, List.of(speed(0)), 0, false);
    }

    @GameTest
    public void rangeAboveMaximumIsRejected(GameTestHelper helper) {
        rejects(helper, List.of(), 65, false);
    }

    @GameTest
    public void amplifierAboveMaximumIsRejected(GameTestHelper helper) {
        rejects(helper, List.of(speed(4)), 1, false);
    }

    @GameTest
    public void negativeAmplifierIsRejected(GameTestHelper helper) {
        rejects(helper, List.of(speed(-1)), 1, false);
    }

    @GameTest
    public void tooManyEffectsIsRejected(GameTestHelper helper) {
        rejects(helper, Collections.nCopies(5, speed(0)), 1, false); // 5 * 4 = 20 points, affordable
    }

    @GameTest
    public void maxEffectCountIsAccepted(GameTestHelper helper) {
        accepts(helper, Collections.nCopies(4, speed(0)), 1, false);
    }

    @GameTest
    public void blacklistedEffectIsRejected(GameTestHelper helper) {
        rejects(helper, List.of(new BeaconEffectEntry("minecraft:wither", 0)), 1, false);
    }

    @GameTest
    public void unknownEffectIsRejected(GameTestHelper helper) {
        rejects(helper, List.of(new BeaconEffectEntry("minecraft:not_a_real_effect", 0)), 1, false);
    }

    @GameTest
    public void exactlyAffordableIsAccepted(GameTestHelper helper) {
        accepts(helper, List.of(speed(0)), 25, false); // 4 * 25 = 100
    }

    @GameTest
    public void overBudgetIsRejected(GameTestHelper helper) {
        rejects(helper, List.of(speed(0)), 26, false); // 4 * 26 = 104
    }

    @GameTest
    public void hidingParticlesCountsTowardsBudget(GameTestHelper helper) {
        rejects(helper, List.of(speed(0)), 19, true); // 4 * 19 + 25 = 101
    }

    @GameTest
    public void effectCostMultiplierCountsTowardsBudget(GameTestHelper helper) {
        rejects(helper, List.of(new BeaconEffectEntry("minecraft:jump_boost", 0)), 13, false); // 4 * 13 * 2.0 = 104
    }

    // Available power is currently baseBlockPoints + neighboringBeacons (marked as a placeholder in
    // BeaconScreen) - this pins the server to the same formula the client's Save button uses.
    @GameTest
    public void neighborCountAddsToAvailablePower(GameTestHelper helper) {
        NeighboringBeaconAccess beacon = beacon(helper);
        beacon.bbb_setNeighboringBeacons(4);
        send(helper, new BeaconPacket(List.of(speed(0)), 26, false)); // 104 <= 100 + 4
        assertApplied(helper, beacon, List.of(speed(0)), 26, false);
        helper.succeed();
    }

    @GameTest
    public void ignoredWithoutBeaconMenuOpen(GameTestHelper helper) {
        NeighboringBeaconAccess beacon = beacon(helper);
        withPlayerAt(helper, Vec3.atCenterOf(BEACON).add(1, 0, 0), player ->
                new BeaconPacket(List.of(speed(0)), 10, false).onServerReceived(player));
        assertRejected(helper, beacon);
        helper.succeed();
    }
}
