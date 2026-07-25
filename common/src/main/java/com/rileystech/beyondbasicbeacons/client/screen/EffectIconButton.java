package com.rileystech.beyondbasicbeacons.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;

// A single clickable icon cell in the effect picker grid.
public class EffectIconButton extends Button {
    private final Holder.Reference<MobEffect> effect;

    public EffectIconButton(Holder.Reference<MobEffect> effect, Button.OnPress onPress) {
        super(0, 0, 20, 20, effect.value().getDisplayName(), onPress, supplier -> supplier.get());
        this.effect = effect;
        this.setTooltip(Tooltip.create(effect.value().getDisplayName()));
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        this.extractDefaultSprite(graphics);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Hud.getMobEffectSprite(this.effect), this.getX() + 1, this.getY() + 1, 18, 18);
    }
}
