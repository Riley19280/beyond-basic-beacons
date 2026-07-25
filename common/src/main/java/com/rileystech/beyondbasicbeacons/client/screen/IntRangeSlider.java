package com.rileystech.beyondbasicbeacons.client.screen;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;
import java.util.function.IntFunction;

// A reusable integer-ranged slider (e.g. amplifier, range) built on top of AbstractSliderButton's
// 0.0-1.0 backing value.
public class IntRangeSlider extends AbstractSliderButton {
    private final int min;
    private final int max;
    private final IntFunction<Component> messageFactory;
    private final IntConsumer onChange;

    public IntRangeSlider(int x, int y, int width, int height, int min, int max, int initialValue,
                           IntFunction<Component> messageFactory, IntConsumer onChange) {
        super(x, y, width, height, messageFactory.apply(initialValue), (double) (initialValue - min) / (max - min));
        this.min = min;
        this.max = max;
        this.messageFactory = messageFactory;
        this.onChange = onChange;
    }

    private int currentValue() {
        return this.min + (int) Math.round(this.value * (this.max - this.min));
    }

    @Override
    protected void updateMessage() {
        this.setMessage(this.messageFactory.apply(this.currentValue()));
    }

    @Override
    protected void applyValue() {
        this.onChange.accept(this.currentValue());
    }
}
