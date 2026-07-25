package com.rileystech.beyondbasicbeacons.platform;

import com.rileystech.beyondbasicbeacons.Constants;
import com.rileystech.beyondbasicbeacons.menu.BeaconMenu;
import com.rileystech.beyondbasicbeacons.platform.services.IRegistryHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public class FabricRegistryHelper implements IRegistryHelper {
    public <T> T register(final Registry<? super T> registry, final String id, final T value) {
        return Registry.register(registry, id, value);
    }
}