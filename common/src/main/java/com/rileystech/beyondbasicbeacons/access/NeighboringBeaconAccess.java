package com.rileystech.beyondbasicbeacons.access;

import com.rileystech.beyondbasicbeacons.beacon.BeaconEffectEntry;

import java.util.List;

// Lets code outside the mixin package read/write the beacon's @Unique fields on a specific
// BeaconBlockEntity instance - needed both by the static updateBase injection (which has no
// `this`) and by BeaconMenu.getBeaconAccess() (which only has a BlockEntity looked up via
// ContainerLevelAccess, not the mixin instance itself). Deliberately kept out of the mixin
// package - Mixin forbids referencing classes that live inside the package declared in the
// mixin config from anywhere outside the transformer itself.
public interface NeighboringBeaconAccess {
    int bbb_getBaseBlockPoints();

    void bbb_setBaseBlockPoints(int points);

    int bbb_getNeighboringBeacons();

    void bbb_setNeighboringBeacons(int count);

    int bbb_getLevels();

    List<BeaconEffectEntry> bbb_getEffects();

    int bbb_getRange();

    void bbb_setRange(int range);

    boolean bbb_isHideParticles();

    void bbb_setHideParticles(boolean hideParticles);
}
