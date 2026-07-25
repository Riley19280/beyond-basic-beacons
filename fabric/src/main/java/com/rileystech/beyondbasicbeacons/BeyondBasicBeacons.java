package com.rileystech.beyondbasicbeacons;

import com.rileystech.beyondbasicbeacons.client.screen.BeaconScreen;
import com.rileystech.beyondbasicbeacons.menu.MenuTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

public class BeyondBasicBeacons implements ModInitializer, ClientModInitializer {

    @Override
    public void onInitialize() {

        // This method is invoked by the Fabric mod loader when it is ready
        // to load your mod. You can access Fabric and Common code in this
        // project.

        // Use Fabric to bootstrap the Common mod.
        Constants.LOG.info("Hello Fabric world!");
        CommonClass.init();
    }


    @Override
    public void onInitializeClient() {
        MenuScreens.register(MenuTypes.BBB_BEACON_MENU, BeaconScreen::new);
    }
}
