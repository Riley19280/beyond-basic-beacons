package com.rileystech.beyondbasicbeacons.gametest.fabric;

import com.rileystech.beyondbasicbeacons.gametest.BeaconGameTests;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.List;

// Registers the shared GameTests' functions. Their test instances (which live in a datapack-driven
// registry) are added by FabricGameTestModInitializerMixin.
public class FabricGameTestRegistration implements ModInitializer {
    // The shared tests plus the Fabric-only ones.
    public static final List<BeaconGameTests.Test> TESTS = BeaconGameTests.collect(FabricMenuOpenGameTests.class);

    @Override
    public void onInitialize() {
        for (BeaconGameTests.Test test : TESTS) {
            Registry.register(BuiltInRegistries.TEST_FUNCTION, test.id(), test.function());
        }
    }
}
