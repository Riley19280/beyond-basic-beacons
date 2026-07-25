package com.rileystech.beyondbasicbeacons.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import org.jspecify.annotations.Nullable;

// The per-row "choose effect" button - shows the chosen effect's icon next to its name, or a
// placeholder label when nothing has been chosen yet.
public class EffectChooserButton extends Button {
    private final Holder.@Nullable Reference<MobEffect> effect;

    public EffectChooserButton(int width, int height, Holder.@Nullable Reference<MobEffect> effect, Button.OnPress onPress) {
        super(0, 0, width, height,
                effect != null ? effect.value().getDisplayName() : Component.literal("Choose Effect"),
                onPress, supplier -> supplier.get());
        this.effect = effect;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        this.extractDefaultSprite(graphics);
        if (this.effect != null) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Hud.getMobEffectSprite(this.effect), this.getX() + 2, this.getY() + 1, 18, 18);
        }
        this.extractDefaultLabel(graphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE));
    }
}
