package com.rileystech.beyondbasicbeacons.platform;

import com.rileystech.beyondbasicbeacons.platform.services.IRegistryHelper;
import net.minecraft.core.Registry;

public class NeoForgeRegistryHelper implements IRegistryHelper {
    public <T> T register(final Registry<? super T> registry, final String id, final T value) {
        return Registry.register(registry, id, value);
    }
}