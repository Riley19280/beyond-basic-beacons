package com.rileystech.beyondbasicbeacons;

import com.rileystech.beyondbasicbeacons.data.BeaconBaseBlockPointManager;
import com.rileystech.beyondbasicbeacons.data.BeaconEffectCostManager;
import com.rileystech.beyondbasicbeacons.data.BeaconEffectFilterManager;
import com.rileystech.beyondbasicbeacons.data.BeaconSettingsManager;
import com.rileystech.beyondbasicbeacons.menu.MenuTypes;
import com.rileystech.beyondbasicbeacons.packet.PacketTypes;
import com.rileystech.beyondbasicbeacons.platform.NeoForgeNetworkHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(Constants.MOD_ID)
public class BeyondBasicBeaconsNeoForge {

    public BeyondBasicBeaconsNeoForge(IEventBus modBus) {
        // Unlike Fabric, NeoForge has already frozen the vanilla registries by the time mods are
        // constructed, so CommonClass.init() can't run here as-is: MenuTypes registers straight into
        // BuiltInRegistries.MENU, which is only writable during that registry's RegisterEvent.
        // Packets only need to be queued now - NeoForgeNetworkHelper hands them to NeoForge once
        // RegisterPayloadHandlersEvent fires.
        PacketTypes.register();
        modBus.addListener(NeoForgeNetworkHelper::onRegisterPayloadHandlers);
        modBus.addListener(RegisterEvent.class, event -> {
            if (event.getRegistryKey().equals(Registries.MENU)) {
                MenuTypes.register();
            }
        });

        // Server data, so these load on a dedicated server too (same as the Fabric entrypoint).
        NeoForge.EVENT_BUS.addListener(AddServerReloadListenersEvent.class, event -> {
            event.addListener(id("beacon_base_block_points"), BeaconBaseBlockPointManager.getInstance());
            event.addListener(id("beacon_settings"), BeaconSettingsManager.getInstance());
            event.addListener(id("beacon_effect_filters"), BeaconEffectFilterManager.getInstance());
            event.addListener(id("beacon_effect_costs"), BeaconEffectCostManager.getInstance());
        });
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(Constants.MOD_ID, path);
    }
}
