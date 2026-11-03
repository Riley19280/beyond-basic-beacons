package com.rileystech.beyondbasicbeacons.gametest.fabric;

import com.rileystech.beyondbasicbeacons.gametest.BeaconGameTest;
import com.rileystech.beyondbasicbeacons.menu.BeaconMenu;
import com.rileystech.beyondbasicbeacons.menu.BeaconMenuData;
import com.rileystech.beyondbasicbeacons.menu.MenuTypes;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.phys.Vec3;

import static com.rileystech.beyondbasicbeacons.gametest.BeaconTestSupport.assertClientMenuMatchesServer;
import static com.rileystech.beyondbasicbeacons.gametest.BeaconTestSupport.placeBeacon;
import static com.rileystech.beyondbasicbeacons.gametest.BeaconTestSupport.withPlayerAt;

// Fabric-only: the menu-open data path (BeaconBlockEntityFabricMixin -> ExtendedMenuType codec -> client BeaconMenu).
public class FabricMenuOpenGameTests {
    private static final BlockPos BEACON = new BlockPos(3, 1, 3);

    @SuppressWarnings("unchecked")
    @BeaconGameTest
    public void menuOpenDataReachesClientMenu(GameTestHelper helper) {
        placeBeacon(helper, BEACON);
        BeaconBlockEntity beacon = helper.getBlockEntity(BEACON, BeaconBlockEntity.class);
        helper.assertTrue(beacon instanceof ExtendedMenuProvider<?>, "beacon should provide menu-open data on Fabric");
        helper.assertTrue(MenuTypes.BBB_BEACON_MENU instanceof ExtendedMenuType<?, ?>, "beacon menu type should carry extra data");

        withPlayerAt(helper, Vec3.atCenterOf(BEACON).add(1, 0, 0), player -> {
            BeaconMenuData sent = ((ExtendedMenuProvider<BeaconMenuData>) (Object) beacon).getScreenOpeningData(player);

            // Encode/decode with the menu type's own codec, exactly as the open-menu packet does.
            ExtendedMenuType<BeaconMenu, BeaconMenuData> type = (ExtendedMenuType<BeaconMenu, BeaconMenuData>) MenuTypes.BBB_BEACON_MENU;
            StreamCodec<? super RegistryFriendlyByteBuf, BeaconMenuData> codec = type.getStreamCodec();
            RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
            codec.encode(buf, sent);
            BeaconMenu clientMenu = type.create(1, player.getInventory(), codec.decode(buf));

            assertClientMenuMatchesServer(helper, clientMenu, BEACON);
        });
        helper.succeed();
    }
}
