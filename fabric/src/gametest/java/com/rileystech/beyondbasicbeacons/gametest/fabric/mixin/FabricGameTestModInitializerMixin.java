package com.rileystech.beyondbasicbeacons.gametest.fabric.mixin;

import com.rileystech.beyondbasicbeacons.gametest.BeaconGameTests;
import com.rileystech.beyondbasicbeacons.gametest.fabric.FabricGameTestRegistration;
import net.fabricmc.fabric.impl.gametest.FabricGameTestModInitializer;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryLoadTask;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

// Fabric API only registers test instances for its own @GameTest annotation. This hooks the same
// point (just before the test-instance registry freezes) to add the shared, loader-neutral tests.
// Test-only code - it relies on Fabric API internals, so it never ships.
@Mixin(FabricGameTestModInitializer.class)
public class FabricGameTestModInitializerMixin {

    @SuppressWarnings("unchecked")
    @Inject(method = "registerDynamicEntries", at = @At("TAIL"))
    private static void bbbtest_registerSharedTests(List<RegistryLoadTask<?>> loadTasks, CallbackInfo ci) {
        WritableRegistry<GameTestInstance> instances = null;
        Registry<TestEnvironmentDefinition<?>> environments = null;
        for (RegistryLoadTask<?> task : loadTasks) {
            WritableRegistry<?> registry = ((RegistryLoadTaskAccessor) task).bbbtest_registry();
            if (registry.key() == Registries.TEST_INSTANCE) {
                instances = (WritableRegistry<GameTestInstance>) registry;
            } else if (registry.key() == Registries.TEST_ENVIRONMENT) {
                environments = (Registry<TestEnvironmentDefinition<?>>) registry;
            }
        }
        if (instances == null || environments == null) {
            throw new IllegalStateException("GameTest registries not found while registering shared tests");
        }

        Holder<TestEnvironmentDefinition<?>> defaultEnvironment = environments.getOrThrow(
                ResourceKey.create(Registries.TEST_ENVIRONMENT, Identifier.withDefaultNamespace("default")));
        for (BeaconGameTests.Test test : FabricGameTestRegistration.TESTS) {
            Registry.register(instances, test.id(), test.instance(defaultEnvironment));
        }
    }
}
