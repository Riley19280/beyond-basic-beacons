package com.rileystech.beyondbasicbeacons.gametest;

import com.rileystech.beyondbasicbeacons.access.NeighboringBeaconAccess;
import com.rileystech.beyondbasicbeacons.data.BeaconSettingsManager;
import com.rileystech.beyondbasicbeacons.gametest.mixin.BeaconBlockEntityInvoker;
import com.rileystech.beyondbasicbeacons.menu.BeaconMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Consumer;

public final class BeaconTestSupport {
    // Must match src/gametest/resources/data/beyondbasicbeacons_gametest/beacon_settings/settings.json
    public static final BeaconSettingsManager.Settings TEST_SETTINGS = new BeaconSettingsManager.Settings(1, 64, 0, 3, 2, 2, 1, 4);

    private BeaconTestSupport() {
    }

    // Places a beacon at a structure-relative position and returns the mod's view of its block entity.
    public static NeighboringBeaconAccess placeBeacon(GameTestHelper helper, BlockPos relativePos) {
        helper.setBlock(relativePos, Blocks.BEACON);
        return access(helper, relativePos);
    }

    public static NeighboringBeaconAccess access(GameTestHelper helper, BlockPos relativePos) {
        return (NeighboringBeaconAccess) helper.getBlockEntity(relativePos, BeaconBlockEntity.class);
    }

    // Fills the (2*step+1)^2 square `step` blocks below the beacon - i.e. pyramid layer `step`.
    public static void fillLayer(GameTestHelper helper, BlockPos beaconRelative, int step, Block block) {
        int y = beaconRelative.getY() - step;
        for (int x = beaconRelative.getX() - step; x <= beaconRelative.getX() + step; x++) {
            for (int z = beaconRelative.getZ() - step; z <= beaconRelative.getZ() + step; z++) {
                helper.setBlock(new BlockPos(x, y, z), block);
            }
        }
    }

    // Runs the mod's base scan for the beacon and returns the number of pyramid levels.
    public static int updateBase(GameTestHelper helper, BlockPos beaconRelative) {
        BlockPos abs = helper.absolutePos(beaconRelative);
        return BeaconBlockEntityInvoker.bbbtest_updateBase(helper.getLevel(), abs.getX(), abs.getY(), abs.getZ());
    }

    public static void applyEffects(GameTestHelper helper, BlockPos beaconRelative, int levels) {
        BeaconBlockEntityInvoker.bbbtest_applyEffects(helper.getLevel(), helper.absolutePos(beaconRelative), levels, null, null);
    }

    // A server player standing at a structure-relative position. Removed from the server afterwards
    // so it can't leak into other tests.
    public static void withPlayerAt(GameTestHelper helper, Vec3 relativePos, Consumer<ServerPlayer> body) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        try {
            player.setPos(helper.absoluteVec(relativePos));
            body.accept(player);
        } finally {
            helper.getLevel().getServer().getPlayerList().remove(player);
        }
    }

    // Checks a menu built the way the client builds it (from the menu-open data each loader sends)
    // can find its beacon and got the server's settings and allowed effects.
    public static void assertClientMenuMatchesServer(GameTestHelper helper, BeaconMenu clientMenu, BlockPos beaconRelative) {
        helper.assertValueEqual(clientMenu.getSettings(), TEST_SETTINGS, "settings sent to client");
        List<String> allowed = clientMenu.getAllowedEffects().stream()
                .map(effect -> effect.key().identifier().toString())
                .toList();
        helper.assertTrue(allowed.contains("minecraft:speed"), "speed should be offered to the client");
        helper.assertFalse(allowed.contains("minecraft:wither"), "blacklisted wither should not be offered to the client");
        helper.assertTrue(clientMenu.getBeaconAccess().orElse(null) == access(helper, beaconRelative),
                "client menu should point at the beacon that was opened");
    }

    // A server player with this beacon's menu open, as if they had right-clicked it.
    public static void withBeaconMenuOpen(GameTestHelper helper, BlockPos beaconRelative, Consumer<ServerPlayer> body) {
        withPlayerAt(helper, Vec3.atCenterOf(beaconRelative).add(1, 0, 0), player -> {
            player.containerMenu = new BeaconMenu(1, player.getInventory(),
                    ContainerLevelAccess.create(helper.getLevel(), helper.absolutePos(beaconRelative)));
            body.accept(player);
        });
    }
}
