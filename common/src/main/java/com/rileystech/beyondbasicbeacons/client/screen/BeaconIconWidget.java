package com.rileystech.beyondbasicbeacons.client.screen;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

// A small non-interactive beacon-item icon shown next to the power total, darkened with a warning
// badge to flag that the power pool is being shared with other neighboring beacons.
public class BeaconIconWidget extends AbstractWidget {
    private static final ItemStack ICON = new ItemStack(Items.BEACON);
    private static final int DARKEN_COLOR = 0x80000000;
    private static final int WARNING_COLOR = 0xFFFFFF55;

    private final Font font;

    public BeaconIconWidget(int x, int y, int size, Font font, Component tooltip) {
        super(x, y, size, size, Component.empty());
        this.font = font;
        this.setTooltip(Tooltip.create(tooltip));
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        graphics.item(ICON, this.getX(), this.getY());
        graphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), DARKEN_COLOR);
        graphics.text(this.font, "!", this.getX() + this.getWidth() - 5, this.getY() + this.getHeight() - 9, WARNING_COLOR);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }
}
