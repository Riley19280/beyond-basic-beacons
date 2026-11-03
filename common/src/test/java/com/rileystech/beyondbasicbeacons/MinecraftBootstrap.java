package com.rileystech.beyondbasicbeacons;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;

// Initializes vanilla's built-in registries (blocks, mob effects, ...) so tests can resolve ids
// like "minecraft:speed" the same way the datapack managers do in-game. Safe to call repeatedly.
public final class MinecraftBootstrap {
    private MinecraftBootstrap() {
    }

    public static synchronized void init() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }
}
