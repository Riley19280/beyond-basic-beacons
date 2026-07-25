package com.rileystech.beyondbasicbeacons.platform.services;

import net.minecraft.core.Registry;

public interface IRegistryHelper {
    <T> T register(final Registry<? super T> registry, final String id, final T value);
}
