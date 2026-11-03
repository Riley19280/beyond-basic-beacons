package com.rileystech.beyondbasicbeacons.gametest.neoforge;

import com.rileystech.beyondbasicbeacons.gametest.BeaconGameTests;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.List;

// Test-only mod: registers the shared GameTests (common/src/gametest) on NeoForge. Never shipped.
@Mod(BeaconGameTests.NAMESPACE)
public class NeoForgeGameTestRegistration {
    // The shared tests plus the NeoForge-only ones.
    private static final List<BeaconGameTests.Test> TESTS = BeaconGameTests.collect(NeoForgeMenuOpenGameTests.class);

    public NeoForgeGameTestRegistration(IEventBus modBus) {
        modBus.addListener(RegisterEvent.class, event -> {
            if (event.getRegistryKey().equals(Registries.TEST_FUNCTION)) {
                for (BeaconGameTests.Test test : TESTS) {
                    Registry.register(BuiltInRegistries.TEST_FUNCTION, test.id(), test.function());
                }
            }
        });

        modBus.addListener(RegisterGameTestsEvent.class, event -> {
            // An environment with no extra setup - the equivalent of vanilla's minecraft:default.
            Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
                    Identifier.fromNamespaceAndPath(BeaconGameTests.NAMESPACE, "default"));
            for (BeaconGameTests.Test test : TESTS) {
                event.registerTest(test.id(), test.instance(environment));
            }
        });
    }
}
