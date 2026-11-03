package com.rileystech.beyondbasicbeacons.gametest;

import com.rileystech.beyondbasicbeacons.access.NeighboringBeaconAccess;
import com.rileystech.beyondbasicbeacons.beacon.BeaconEffectEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import static com.rileystech.beyondbasicbeacons.gametest.BeaconTestSupport.applyEffects;
import static com.rileystech.beyondbasicbeacons.gametest.BeaconTestSupport.placeBeacon;
import static com.rileystech.beyondbasicbeacons.gametest.BeaconTestSupport.withPlayerAt;

// Effect application (BeaconBlockEntityMixin.bbb_applyEffects).
// The test datapack blacklists minecraft:wither.
public class BeaconEffectsGameTests {
    private static final BlockPos BEACON = new BlockPos(3, 1, 3);
    private static final Vec3 NEAR = Vec3.atBottomCenterOf(BEACON).add(1, 0, 0);
    private static final int LEVELS = 1;
    private static final int EXPECTED_DURATION = (9 + LEVELS * 2) * 20;

    private static NeighboringBeaconAccess beaconWith(GameTestHelper helper, int range, boolean hideParticles, BeaconEffectEntry... effects) {
        NeighboringBeaconAccess beacon = placeBeacon(helper, BEACON);
        beacon.bbb_setEffects(List.of(effects));
        beacon.bbb_setRange(range);
        beacon.bbb_setHideParticles(hideParticles);
        return beacon;
    }

    @BeaconGameTest
    public void appliesEveryConfiguredEffect(GameTestHelper helper) {
        beaconWith(helper, 3, false,
                new BeaconEffectEntry("minecraft:speed", 1),
                new BeaconEffectEntry("minecraft:haste", 0));

        withPlayerAt(helper, NEAR, player -> {
            applyEffects(helper, BEACON, LEVELS);

            MobEffectInstance speed = player.getEffect(MobEffects.SPEED);
            MobEffectInstance haste = player.getEffect(MobEffects.HASTE);
            helper.assertTrue(speed != null, "player should have speed");
            helper.assertTrue(haste != null, "player should have haste");
            helper.assertValueEqual(speed.getAmplifier(), 1, "speed amplifier");
            helper.assertValueEqual(haste.getAmplifier(), 0, "haste amplifier");
            helper.assertValueEqual(speed.getDuration(), EXPECTED_DURATION, "duration");
            helper.assertTrue(speed.isAmbient(), "beacon effects should be ambient");
            helper.assertTrue(speed.isVisible(), "particles should be visible");
        });
        helper.succeed();
    }

    @BeaconGameTest
    public void hideParticlesHidesEffectParticles(GameTestHelper helper) {
        beaconWith(helper, 3, true, new BeaconEffectEntry("minecraft:speed", 0));
        withPlayerAt(helper, NEAR, player -> {
            applyEffects(helper, BEACON, LEVELS);
            MobEffectInstance speed = player.getEffect(MobEffects.SPEED);
            helper.assertTrue(speed != null, "player should have speed");
            helper.assertFalse(speed.isVisible(), "particles should be hidden");
        });
        helper.succeed();
    }

    @BeaconGameTest
    public void playersOutsideRangeGetNothing(GameTestHelper helper) {
        beaconWith(helper, 2, false, new BeaconEffectEntry("minecraft:speed", 0));
        // Range 2 around a 1-block beacon covers 2 blocks either side; 6 blocks away is well out.
        withPlayerAt(helper, Vec3.atBottomCenterOf(BEACON).add(6, 0, 0), farPlayer ->
                withPlayerAt(helper, NEAR, nearPlayer -> {
                    applyEffects(helper, BEACON, LEVELS);
                    helper.assertTrue(nearPlayer.hasEffect(MobEffects.SPEED), "player in range should have speed");
                    helper.assertFalse(farPlayer.hasEffect(MobEffects.SPEED), "player out of range should not have speed");
                }));
        helper.succeed();
    }

    @BeaconGameTest
    public void rangeExtendsUpwards(GameTestHelper helper) {
        beaconWith(helper, 2, false, new BeaconEffectEntry("minecraft:speed", 0));
        withPlayerAt(helper, Vec3.atBottomCenterOf(BEACON).add(0, 40, 0), player -> {
            applyEffects(helper, BEACON, LEVELS);
            helper.assertTrue(player.hasEffect(MobEffects.SPEED), "player high above the beacon should have speed");
        });
        helper.succeed();
    }

    @BeaconGameTest
    public void blacklistedEffectIsNotApplied(GameTestHelper helper) {
        beaconWith(helper, 3, false,
                new BeaconEffectEntry("minecraft:wither", 0),
                new BeaconEffectEntry("minecraft:speed", 0));
        withPlayerAt(helper, NEAR, player -> {
            applyEffects(helper, BEACON, LEVELS);
            helper.assertFalse(player.hasEffect(MobEffects.WITHER), "blacklisted wither should be skipped");
            helper.assertTrue(player.hasEffect(MobEffects.SPEED), "other effects should still apply");
        });
        helper.succeed();
    }

    @BeaconGameTest
    public void unknownEffectIsSkipped(GameTestHelper helper) {
        beaconWith(helper, 3, false,
                new BeaconEffectEntry("minecraft:not_a_real_effect", 0),
                new BeaconEffectEntry("minecraft:speed", 0));
        withPlayerAt(helper, NEAR, player -> {
            applyEffects(helper, BEACON, LEVELS);
            helper.assertTrue(player.hasEffect(MobEffects.SPEED), "known effects should still apply");
        });
        helper.succeed();
    }

    @BeaconGameTest
    public void beaconWithNoEffectsDoesNothing(GameTestHelper helper) {
        beaconWith(helper, 3, false);
        withPlayerAt(helper, NEAR, player -> {
            applyEffects(helper, BEACON, LEVELS);
            helper.assertTrue(player.getActiveEffects().isEmpty(), "player should have no effects");
        });
        helper.succeed();
    }
}
