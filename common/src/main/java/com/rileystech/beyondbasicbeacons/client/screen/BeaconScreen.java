package com.rileystech.beyondbasicbeacons.client.screen;

import com.rileystech.beyondbasicbeacons.Constants;
import com.rileystech.beyondbasicbeacons.access.NeighboringBeaconAccess;
import com.rileystech.beyondbasicbeacons.beacon.BeaconEffectEntry;
import com.rileystech.beyondbasicbeacons.menu.BeaconMenu;
import com.rileystech.beyondbasicbeacons.packet.BeaconPacket;
import com.rileystech.beyondbasicbeacons.platform.Services;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BeaconScreen extends Screen implements MenuAccess<BeaconMenu> {
    private static final int MARGIN = 8;
    private static final int SPACING = 4;
    private static final int TITLE_HEIGHT = 12;
    private static final int ROW_HEIGHT = 20;
    private static final int EFFECT_BUTTON_WIDTH = 110;
    private static final int SLIDER_WIDTH = 80;
    private static final int REMOVE_BUTTON_WIDTH = 20;
    private static final int ADD_BUTTON_HEIGHT = 20;
    private static final int SAVE_BUTTON_HEIGHT = 20;
    private static final int ROW_CONTENT_WIDTH = EFFECT_BUTTON_WIDTH + SPACING + SLIDER_WIDTH + SPACING + REMOVE_BUTTON_WIDTH;
    private static final int SCROLLBAR_SPACING = 4;
    private static final int SCROLLBAR_RESERVE = AbstractScrollArea.SCROLLBAR_WIDTH + SCROLLBAR_SPACING;
    private static final int PANEL_WIDTH = ROW_CONTENT_WIDTH + MARGIN * 2 + SCROLLBAR_RESERVE;
    private static final int PICKER_COLUMNS = 8;
    private static final int MAX_VISIBLE_ROWS = 5;
    private static final int ROWS_AREA_HEIGHT = MAX_VISIBLE_ROWS * ROW_HEIGHT + (MAX_VISIBLE_ROWS - 1) * SPACING;
    private static final int PICKER_MAX_HEIGHT = 160;

    private final BeaconMenu menu;
    private final BeaconScreenData data = new BeaconScreenData();
    private final List<Holder.Reference<MobEffect>> allEffects;

    private @Nullable Integer pickerRowIndex;
    private @Nullable ScreenRectangle pickerRectangle;
    private @Nullable StringWidget requiredPointsWidget;
    private int leftPos;
    private int topPos;
    private int panelHeight;

    public BeaconScreen(BeaconMenu menu, Inventory playerInventory, Component title) {
        super(title);
        this.menu = menu;
        this.allEffects = BuiltInRegistries.MOB_EFFECT.listElements().toList();

        // The beacon's data lives on the client's own local block entity, already kept in sync
        // by vanilla's normal block-entity update packet - no custom networking round trip
        // needed to read it, and no ordering to worry about since it's read synchronously here.
        this.syncFromMenu();
    }

    // Reads the beacon's data straight from the client's local block entity into this.data.
    private void syncFromMenu() {
        Optional<NeighboringBeaconAccess> beaconAccess = this.menu.getBeaconAccess();
        if (beaconAccess.isEmpty()) {
            Constants.LOG.info("BeaconScreen.syncFromMenu: no local beacon block entity found yet");
            return;
        }
        NeighboringBeaconAccess beacon = beaconAccess.get();

        Constants.LOG.info("BeaconScreen.syncFromMenu: {} effect(s), baseBlockPoints={}, neighboringBeacons={}, range={}, hideParticles={}",
                beacon.bbb_getEffects().size(), beacon.bbb_getBaseBlockPoints(), beacon.bbb_getNeighboringBeacons(),
                beacon.bbb_getRange(), beacon.bbb_isHideParticles());

        List<BeaconScreenData.EffectSelection> selections = new ArrayList<>();
        for (BeaconEffectEntry entry : beacon.bbb_getEffects()) {
            ResourceKey<MobEffect> key = ResourceKey.create(Registries.MOB_EFFECT, Identifier.parse(entry.key()));
            BuiltInRegistries.MOB_EFFECT.get(key).ifPresentOrElse(effect -> {
                BeaconScreenData.EffectSelection selection = new BeaconScreenData.EffectSelection();
                selection.setEffect(effect);
                selection.setAmplifier(entry.amplifier());
                selections.add(selection);
            }, () -> Constants.LOG.info("Could not resolve effect key '{}' to a registered MobEffect - skipping", entry.key()));
        }
        this.data.setEffects(selections);
        this.data.setRange(beacon.bbb_getRange());
        this.data.setHideParticles(beacon.bbb_isHideParticles());

        // Placeholder combination until the real "total points available" formula is decided.
        this.data.setTotalPoints(beacon.bbb_getBaseBlockPoints() + beacon.bbb_getNeighboringBeacons());

        Constants.LOG.info("BeaconScreenData synced: {} selection(s), totalPoints={}", selections.size(), this.data.getTotalPoints());
    }

    // Sends the player's current edits back to the server. levels/baseBlockPoints/neighboringBeacons
    // are server-computed and never sent from the client - the 0/0/0 here are unused placeholders,
    // since BeaconPacket.onServerReceived only applies effects/range/hideParticles.
    private void saveToServer() {
        List<BeaconEffectEntry> effects = new ArrayList<>();
        for (BeaconScreenData.EffectSelection selection : this.data.getEffects()) {
            Holder.Reference<MobEffect> effect = selection.getEffect();
            if (effect != null) {
                effects.add(new BeaconEffectEntry(effect.key().identifier().toString(), selection.getAmplifier()));
            }
        }

        Services.NETWORKING.sendToServer(new BeaconPacket(0, 0, 0, effects, this.data.getRange(), this.data.isHideParticles()));
    }

    @Override
    public BeaconMenu getMenu() {
        return this.menu;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        if (this.minecraft.player != null) {
            this.minecraft.player.closeContainer();
        }
        super.onClose();
    }

    @Override
    protected void init() {
        List<BeaconScreenData.EffectSelection> rows = this.data.getEffects();
        boolean pickerOpen = this.pickerRowIndex != null;

        // Built early (at a placeholder position) purely so its real height can be measured -
        // Checkbox's builder has no way to force a specific height, so the bottom margin is
        // computed from whatever height it actually reports rather than an assumed constant.
        Checkbox hideParticlesCheckbox = Checkbox.builder(Component.literal("Hide Particles"), this.font)
                .selected(this.data.isHideParticles())
                .onValueChange((checkbox, value) -> this.data.setHideParticles(value))
                .build();
        int miscRowHeight = Math.max(ROW_HEIGHT, hideParticlesCheckbox.getHeight());

        int contentHeight = TITLE_HEIGHT + SPACING
                + ROWS_AREA_HEIGHT + SPACING
                + ADD_BUTTON_HEIGHT + SPACING
                + ROW_HEIGHT + SPACING
                + miscRowHeight + SPACING
                + SAVE_BUTTON_HEIGHT;
        this.panelHeight = contentHeight + MARGIN * 2;

        this.leftPos = (this.width - PANEL_WIDTH) / 2;
        this.topPos = (this.height - this.panelHeight) / 2;

        int y = this.topPos + MARGIN;

        this.addRenderableWidget(new StringWidget(this.leftPos + MARGIN, y, PANEL_WIDTH / 2, TITLE_HEIGHT,
                Component.literal("Beyond Basic Beacons"), this.font));
        Component pointsText = Component.literal("Points: " + this.data.getTotalPoints());
        int pointsWidth = this.font.width(pointsText);
        this.addRenderableWidget(new StringWidget(this.leftPos + PANEL_WIDTH - pointsWidth - MARGIN, y, pointsWidth,
                TITLE_HEIGHT, pointsText, this.font));
        y += TITLE_HEIGHT + SPACING;

        // While the picker popup is open, the rest of the panel's widgets are left out entirely
        // instead of just drawn-under, so they can't steal clicks meant for the popup's icons.
        this.requiredPointsWidget = null;
        if (!pickerOpen) {
            LinearLayout rowsColumn = LinearLayout.vertical().spacing(SPACING);
            for (int i = 0; i < rows.size(); i++) {
                int rowIndex = i;
                BeaconScreenData.EffectSelection row = rows.get(i);

                LinearLayout rowLayout = LinearLayout.horizontal().spacing(SPACING);
                rowLayout.addChild(new EffectChooserButton(EFFECT_BUTTON_WIDTH, ROW_HEIGHT, row.getEffect(),
                        button -> {
                            this.pickerRowIndex = rowIndex;
                            this.rebuildWidgets();
                        }));
                rowLayout.addChild(new IntRangeSlider(0, 0, SLIDER_WIDTH, ROW_HEIGHT,
                        0, 4, row.getAmplifier(),
                        value -> Component.literal("Amplifier " + (value + 1)),
                        value -> {
                            row.setAmplifier(value);
                            this.refreshRequiredPoints();
                        }));
                rowLayout.addChild(Button.builder(Component.literal("X"), button -> {
                            this.data.removeEffect(rowIndex);
                            this.rebuildWidgets();
                        })
                        .size(REMOVE_BUTTON_WIDTH, ROW_HEIGHT)
                        .build());

                rowsColumn.addChild(rowLayout);
            }

            ScrollableLayout rowsScroll = new ScrollableLayout(this.minecraft, rowsColumn, ROWS_AREA_HEIGHT,
                    ScrollableLayout.ReserveStrategy.RIGHT);
            rowsScroll.setScrollbarSpacing(SCROLLBAR_SPACING);
            rowsScroll.setMinWidth(ROW_CONTENT_WIDTH);
            rowsScroll.arrangeElements();
            rowsScroll.setPosition(this.leftPos + MARGIN, y);
            rowsScroll.visitWidgets(this::addRenderableWidget);
            y += ROWS_AREA_HEIGHT + SPACING;

            this.addRenderableWidget(Button.builder(Component.literal("+"), button -> {
                        this.data.addEffect();
                        this.rebuildWidgets();
                    })
                    .bounds(this.leftPos + MARGIN, y, PANEL_WIDTH - MARGIN * 2, ADD_BUTTON_HEIGHT)
                    .build());
            y += ADD_BUTTON_HEIGHT + SPACING;

            this.addRenderableWidget(new IntRangeSlider(this.leftPos + MARGIN, y, PANEL_WIDTH - MARGIN * 2, ROW_HEIGHT,
                    1, 100, this.data.getRange(),
                    value -> Component.literal("Range: " + value),
                    value -> {
                        this.data.setRange(value);
                        this.refreshRequiredPoints();
                    }));
            y += ROW_HEIGHT + SPACING;

            hideParticlesCheckbox.setPosition(this.leftPos + MARGIN, y);
            this.addRenderableWidget(hideParticlesCheckbox);

            Component requiredText = Component.literal("Required: " + this.data.getRequiredPoints());
            int requiredWidth = this.font.width(requiredText);
            this.requiredPointsWidget = this.addRenderableWidget(new StringWidget(
                    this.leftPos + PANEL_WIDTH - requiredWidth - MARGIN, y + (miscRowHeight - TITLE_HEIGHT) / 2, requiredWidth, TITLE_HEIGHT, requiredText, this.font));
            y += miscRowHeight + SPACING;

            this.addRenderableWidget(Button.builder(Component.literal("Save"), button -> {
                        this.saveToServer();
                        this.onClose();
                    })
                    .bounds(this.leftPos + MARGIN, y, PANEL_WIDTH - MARGIN * 2, SAVE_BUTTON_HEIGHT)
                    .build());
        }

        this.pickerRectangle = null;
        if (pickerOpen) {
            GridLayout grid = new GridLayout(0, 0);
            grid.columnSpacing(2);
            grid.rowSpacing(2);
            GridLayout.RowHelper rowHelper = grid.createRowHelper(PICKER_COLUMNS);
            for (Holder.Reference<MobEffect> effect : this.allEffects) {
                rowHelper.addChild(new EffectIconButton(effect, button -> this.selectEffect(effect)));
            }

            ScrollableLayout pickerScroll = new ScrollableLayout(this.minecraft, grid, PICKER_MAX_HEIGHT,
                    ScrollableLayout.ReserveStrategy.RIGHT);
            pickerScroll.setScrollbarSpacing(SCROLLBAR_SPACING);
            pickerScroll.arrangeElements();

            // ScrollableLayout always reserves scrollbar space even when nothing needs to scroll,
            // which would otherwise leave unwanted blank padding on the right/bottom of the popup.
            boolean needsScrollbar = grid.getHeight() > PICKER_MAX_HEIGHT;
            int pickerWidth = needsScrollbar ? pickerScroll.getWidth() : grid.getWidth();
            int pickerHeight = needsScrollbar ? PICKER_MAX_HEIGHT : grid.getHeight();

            int pickerX = this.leftPos + (PANEL_WIDTH - pickerWidth) / 2;
            pickerScroll.setPosition(pickerX, y);
            pickerScroll.visitWidgets(this::addRenderableWidget);
            this.pickerRectangle = new ScreenRectangle(pickerX, y, pickerWidth, pickerHeight);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        this.extractMenuBackground(graphics, this.leftPos, this.topPos, PANEL_WIDTH, this.panelHeight);
        if (this.pickerRectangle != null) {
            this.extractMenuBackground(graphics, this.pickerRectangle.left() - 4, this.pickerRectangle.top() - 4,
                    this.pickerRectangle.width() + 8, this.pickerRectangle.height() + 8);
        }
        super.extractRenderState(graphics, mouseX, mouseY, a);
    }

    private void selectEffect(Holder.Reference<MobEffect> effect) {
        if (this.pickerRowIndex != null) {
            this.data.getEffects().get(this.pickerRowIndex).setEffect(effect);
        }
        this.pickerRowIndex = null;
        this.rebuildWidgets();
    }

    private void refreshRequiredPoints() {
        if (this.requiredPointsWidget != null) {
            Component requiredText = Component.literal("Required: " + this.data.getRequiredPoints());
            this.requiredPointsWidget.setX(this.leftPos + PANEL_WIDTH - this.font.width(requiredText) - MARGIN);
            this.requiredPointsWidget.setMessage(requiredText);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (this.pickerRowIndex != null && this.pickerRectangle != null
                && !this.pickerRectangle.containsPoint((int) event.x(), (int) event.y())) {
            this.pickerRowIndex = null;
            this.rebuildWidgets();
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }
}
