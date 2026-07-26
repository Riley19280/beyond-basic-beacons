package com.rileystech.beyondbasicbeacons.client.screen;

import com.rileystech.beyondbasicbeacons.beacon.BeaconPoints;
import com.rileystech.beyondbasicbeacons.data.BeaconEffectCostManager;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

// Holds the in-progress edits for a BeaconScreen. Purely local/client-side for now - nothing here is
// sent to or read from the server yet.
public class BeaconScreenData {

    public static class EffectSelection {
        private Holder.@Nullable Reference<MobEffect> effect;
        private int amplifier;

        public Holder.@Nullable Reference<MobEffect> getEffect() {
            return this.effect;
        }

        public void setEffect(Holder.@Nullable Reference<MobEffect> effect) {
            this.effect = effect;
        }

        public int getAmplifier() {
            return this.amplifier;
        }

        public void setAmplifier(int amplifier) {
            this.amplifier = amplifier;
        }
    }

    private final List<EffectSelection> effects = new ArrayList<>();
    private int range = 50;
    private boolean hideParticles = false;
    private int totalPoints = 10;
    private int neighboringBeacons = 0;

    public List<EffectSelection> getEffects() {
        return this.effects;
    }

    public void setEffects(List<EffectSelection> effects) {
        this.effects.clear();
        this.effects.addAll(effects);
    }

    public void addEffect() {
        this.effects.add(new EffectSelection());
    }

    public void removeEffect(int index) {
        this.effects.remove(index);
    }

    public int getRange() {
        return this.range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public boolean isHideParticles() {
        return this.hideParticles;
    }

    public void setHideParticles(boolean hideParticles) {
        this.hideParticles = hideParticles;
    }

    public int getTotalPoints() {
        return this.totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public int getNeighboringBeacons() {
        return this.neighboringBeacons;
    }

    public void setNeighboringBeacons(int neighboringBeacons) {
        this.neighboringBeacons = neighboringBeacons;
    }

    public int getRequiredPoints() {
        return BeaconPoints.requiredPoints(this.range, this.effects, EffectSelection::getAmplifier, BeaconScreenData::multiplierOf, this.hideParticles);
    }

    private static double multiplierOf(EffectSelection selection) {
        Holder.Reference<MobEffect> effect = selection.getEffect();
        return effect != null ? BeaconEffectCostManager.getMultiplier(effect.value()) : 1.0;
    }
}
