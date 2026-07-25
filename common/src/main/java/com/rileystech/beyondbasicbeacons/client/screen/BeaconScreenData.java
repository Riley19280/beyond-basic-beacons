package com.rileystech.beyondbasicbeacons.client.screen;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

// Holds the in-progress edits for a BeaconScreen. Purely local/client-side for now - nothing here is
// sent to or read from the server yet.
public class BeaconScreenData {

    public static class EffectRow {
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

    private final List<EffectRow> rows = new ArrayList<>();
    private int range = 50;
    private boolean hideParticles = false;
    private final int totalPoints = 10;

    public List<EffectRow> getRows() {
        return this.rows;
    }

    public void addRow() {
        this.rows.add(new EffectRow());
    }

    public void removeRow(int index) {
        this.rows.remove(index);
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

    public int getRequiredPoints() {
        int required = this.range * this.rows.size();
        for (EffectRow row : this.rows) {
            required += (int) Math.pow(4 + row.getAmplifier(), 3);
        }
        return required;
    }
}
