package com.rileystech.beyondbasicbeacons;

import com.rileystech.beyondbasicbeacons.client.screen.BeaconScreen;
import com.rileystech.beyondbasicbeacons.menu.MenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

// Client-only entrypoint, kept in its own class so a dedicated server never loads client classes.
@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
public class BeyondBasicBeaconsNeoForgeClient {

    public BeyondBasicBeaconsNeoForgeClient(IEventBus modBus) {
        modBus.addListener(RegisterMenuScreensEvent.class, event ->
                event.register(MenuTypes.BBB_BEACON_MENU, BeaconScreen::new));
    }
}
