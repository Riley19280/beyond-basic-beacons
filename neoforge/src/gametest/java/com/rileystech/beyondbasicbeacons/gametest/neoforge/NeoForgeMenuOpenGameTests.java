package com.rileystech.beyondbasicbeacons.gametest.neoforge;

import com.rileystech.beyondbasicbeacons.gametest.BeaconGameTest;
import com.rileystech.beyondbasicbeacons.menu.BeaconMenu;
import com.rileystech.beyondbasicbeacons.menu.MenuTypes;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.phys.Vec3;

import static com.rileystech.beyondbasicbeacons.gametest.BeaconTestSupport.assertClientMenuMatchesServer;
import static com.rileystech.beyondbasicbeacons.gametest.BeaconTestSupport.placeBeacon;
import static com.rileystech.beyondbasicbeacons.gametest.BeaconTestSupport.withBeaconMenuOpen;

// NeoForge-only: the menu-open data path (BeaconBlockEntityNeoForgeMixin#writeClientSideData ->
// MenuTypeHelperNeoForge's IContainerFactory -> client BeaconMenu).
public class NeoForgeMenuOpenGameTests {
    private static final BlockPos BEACON = new BlockPos(3, 1, 3);

    @BeaconGameTest
    public void menuOpenDataReachesClientMenu(GameTestHelper helper) {
        placeBeacon(helper, BEACON);
        BeaconBlockEntity beacon = helper.getBlockEntity(BEACON, BeaconBlockEntity.class);

        withBeaconMenuOpen(helper, BEACON, player -> {
            // What ServerPlayer.openMenu writes into the open-menu packet...
            RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
            beacon.writeClientSideData(player.containerMenu, buf);
            helper.assertTrue(buf.readableBytes() > 0, "beacon should write menu-open data on NeoForge");

            // ...and what the client builds from it.
            BeaconMenu clientMenu = MenuTypes.BBB_BEACON_MENU.create(1, player.getInventory(), buf);
            helper.assertValueEqual(buf.readableBytes(), 0, "unread menu-open bytes");
            assertClientMenuMatchesServer(helper, clientMenu, BEACON);
        });
        helper.succeed();
    }
}
