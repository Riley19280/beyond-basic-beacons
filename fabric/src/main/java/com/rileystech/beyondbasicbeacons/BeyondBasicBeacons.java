package com.rileystech.beyondbasicbeacons;

import com.rileystech.beyondbasicbeacons.client.screen.BeaconScreen;
import com.rileystech.beyondbasicbeacons.data.BeaconBaseBlockPointManager;
import com.rileystech.beyondbasicbeacons.data.BeaconEffectCostManager;
import com.rileystech.beyondbasicbeacons.data.BeaconEffectFilterManager;
import com.rileystech.beyondbasicbeacons.data.BeaconSettingsManager;
import com.rileystech.beyondbasicbeacons.menu.MenuTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import org.jspecify.annotations.NonNull;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class BeyondBasicBeacons implements ModInitializer, ClientModInitializer {

    @Override
    public void onInitialize() {

        // This method is invoked by the Fabric mod loader when it is ready
        // to load your mod. You can access Fabric and Common code in this
        // project.

        // Use Fabric to bootstrap the Common mod.
        Constants.LOG.info("Hello Fabric world!");
        CommonClass.init();

        // Registered here (not onInitializeClient) so these actually load on a dedicated server too -
        // some of this data (max beacon size, neighboring-beacon check ranges) gates server-authoritative
        // beacon-pyramid detection, not just client-side UI.
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new IdentifiableResourceReloadListener() {
            @Override
            public @NonNull Identifier getFabricId() {
                return Identifier.fromNamespaceAndPath(Constants.MOD_ID, "beacon_base_block_points");
            }

            @Override
            public @NonNull CompletableFuture<Void> reload(SharedState currentReload, Executor taskExecutor, PreparationBarrier preparationBarrier, Executor reloadExecutor) {
                return BeaconBaseBlockPointManager.getInstance().reload(currentReload, taskExecutor, preparationBarrier, reloadExecutor);
            }
        });

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new IdentifiableResourceReloadListener() {
            @Override
            public @NonNull Identifier getFabricId() {
                return Identifier.fromNamespaceAndPath(Constants.MOD_ID, "beacon_settings");
            }

            @Override
            public @NonNull CompletableFuture<Void> reload(SharedState currentReload, Executor taskExecutor, PreparationBarrier preparationBarrier, Executor reloadExecutor) {
                return BeaconSettingsManager.getInstance().reload(currentReload, taskExecutor, preparationBarrier, reloadExecutor);
            }
        });

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new IdentifiableResourceReloadListener() {
            @Override
            public @NonNull Identifier getFabricId() {
                return Identifier.fromNamespaceAndPath(Constants.MOD_ID, "beacon_effect_filters");
            }

            @Override
            public @NonNull CompletableFuture<Void> reload(SharedState currentReload, Executor taskExecutor, PreparationBarrier preparationBarrier, Executor reloadExecutor) {
                return BeaconEffectFilterManager.getInstance().reload(currentReload, taskExecutor, preparationBarrier, reloadExecutor);
            }
        });

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new IdentifiableResourceReloadListener() {
            @Override
            public @NonNull Identifier getFabricId() {
                return Identifier.fromNamespaceAndPath(Constants.MOD_ID, "beacon_effect_costs");
            }

            @Override
            public @NonNull CompletableFuture<Void> reload(SharedState currentReload, Executor taskExecutor, PreparationBarrier preparationBarrier, Executor reloadExecutor) {
                return BeaconEffectCostManager.getInstance().reload(currentReload, taskExecutor, preparationBarrier, reloadExecutor);
            }
        });
    }


    @Override
    public void onInitializeClient() {
        MenuScreens.register(MenuTypes.BBB_BEACON_MENU, BeaconScreen::new);
    }
}
