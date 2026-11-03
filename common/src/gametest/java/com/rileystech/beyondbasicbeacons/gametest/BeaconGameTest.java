package com.rileystech.beyondbasicbeacons.gametest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Marks a GameTest method. Loader-neutral on purpose: the same test classes run on Fabric and
// NeoForge, each of which registers everything BeaconGameTests finds (see the loaders' gametest
// source sets). Methods must be public, non-static, and take a single GameTestHelper.
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BeaconGameTest {
    int maxTicks() default 20;
}
