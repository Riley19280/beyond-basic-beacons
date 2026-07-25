package com.rileystech.beyondbasicbeacons.access;

// Lets the static updateBase injection write the per-beacon @Unique fields back onto the
// specific BeaconBlockEntity it just scanned, since a static target method has no `this`.
// Deliberately kept out of the mixin package - Mixin forbids referencing classes that live
// inside the package declared in the mixin config from anywhere outside the transformer itself.
public interface NeighboringBeaconAccess {
    int bbb_getBaseBlockPointTotal();

    void bbb_setBaseBlockPointTotal(int total);

    int bbb_getNeighboringBeaconCount();

    void bbb_setNeighboringBeaconCount(int count);
}
