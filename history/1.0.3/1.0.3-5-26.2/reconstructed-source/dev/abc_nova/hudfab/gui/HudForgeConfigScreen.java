/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.AbstractButton
 *  net.minecraft.client.gui.components.AbstractSliderButton
 *  net.minecraft.client.gui.components.AbstractWidget
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.narration.NarrationElementOutput
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.InputWithModifiers
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.client.resources.sounds.SimpleSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.core.Holder
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.util.Mth
 *  net.minecraft.world.scores.DisplaySlot
 *  net.minecraft.world.scores.Objective
 *  net.minecraft.world.scores.PlayerScoreEntry
 *  net.minecraft.world.scores.PlayerTeam
 *  net.minecraft.world.scores.Scoreboard
 *  net.minecraft.world.scores.Team
 */
package dev.abc_nova.hudfab.gui;

import dev.abc_nova.hudfab.HudForgeClient;
import dev.abc_nova.hudfab.config.HudForgeConfig;
import java.awt.Color;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;

public final class HudForgeConfigScreen
extends Screen {
    private static final int PANEL = -401730268;
    private static final int CONTROL = -971299528;
    private static final int CONTROL_HOVER = -584829357;
    private static final int BLUE = -10258433;
    private static final int BORDER = -2007208294;
    private static final int WHITE = -1380097;
    private final Screen parent;
    private boolean settingsOpen;
    private SettingsPage page = SettingsPage.SCOREBOARD;
    private ColorTarget colorTarget = ColorTarget.BOARD_BG;
    private DragTarget dragTarget = DragTarget.NONE;
    private int dragOffsetX;
    private int dragOffsetY;
    private int settingsScroll;
    private int settingsMaxScroll;
    private boolean scrollbarDragging;
    private int scrollbarDragOffset;

    public HudForgeConfigScreen(Screen parent) {
        this(parent, false);
    }

    public HudForgeConfigScreen(Screen parent, boolean openSettings) {
        super(HudForgeConfigScreen.tr("title", new Object[0]));
        this.parent = parent;
        this.settingsOpen = openSettings;
    }

    protected void init() {
        HudForgeConfig config = HudForgeClient.config.clamped();
        if (this.settingsOpen) {
            this.initSettings(config);
        } else {
            this.addRenderableWidget((GuiEventListener)new HudButton(this.width - 132, this.height - 28, 120, 20, HudForgeConfigScreen.tr("button.settings", new Object[0]), button -> {
                this.settingsOpen = true;
                this.rebuildWidgets();
            }));
            this.addRenderableWidget((GuiEventListener)new HudButton(this.width - 258, this.height - 28, 120, 20, HudForgeConfigScreen.tr("button.done", new Object[0]), button -> this.onClose()));
        }
    }

    private void initSettings(HudForgeConfig config) {
        int panelW = this.settingsPanelWidth();
        int panelH = this.settingsPanelHeight();
        int left = (this.width - panelW) / 2;
        int top = (this.height - panelH) / 2;
        int viewportHeight = Math.max(20, panelH - 94);
        this.settingsMaxScroll = switch (this.page.ordinal()) {
            case 0 -> Math.max(0, 308 - viewportHeight);
            case 2 -> Math.max(0, 164 - viewportHeight);
            default -> 0;
        };
        this.settingsScroll = HudForgeConfigScreen.clamp(this.settingsScroll, 0, this.settingsMaxScroll);
        int y = top + 58 - this.settingsScroll;
        int tabW = (panelW - 24) / SettingsPage.values().length;
        int backY = top + panelH - 30;
        for (SettingsPage value2 : SettingsPage.values()) {
            int x = left + 8 + value2.ordinal() * tabW;
            HudButton tab = new HudButton(x, top + 6, tabW - 4, 18, this.tabText(value2), button -> {
                this.page = value2;
                this.settingsScroll = 0;
                this.rebuildWidgets();
            });
            tab.setSelectedStyle(this.page == value2);
            this.addRenderableWidget((GuiEventListener)tab);
        }
        switch (this.page.ordinal()) {
            case 0: {
                int gap = 12;
                int columnW = (panelW - 32 - gap) / 2;
                int leftCol = left + 10;
                int rightCol = leftCol + columnW + gap;
                int leftY = y;
                int rightY = y;
                this.addRenderableWidget((GuiEventListener)new HudButton(leftCol, leftY, columnW, 20, HudForgeConfigScreen.scoreboardButtonText(config.scoreboardMode), button -> {
                    config.scoreboardMode = (config.scoreboardMode + 1) % 3;
                    button.setMessage(HudForgeConfigScreen.scoreboardButtonText(config.scoreboardMode));
                }));
                this.addSlider(leftCol, leftY += 24, columnW, "slider.width", config.scoreboardWidth, 90.0f, 320.0f, value -> {
                    config.scoreboardWidth = Math.round(value.floatValue());
                });
                this.addSlider(leftCol, leftY += 24, columnW, "slider.scale", config.scoreboardScale, 0.5f, 2.5f, value -> {
                    config.scoreboardScale = value.floatValue();
                });
                this.addSlider(leftCol, leftY += 24, columnW, "slider.board_radius", config.scoreboardRadius, 0.0f, 12.0f, value -> {
                    config.scoreboardRadius = Math.round(value.floatValue());
                });
                this.addToggle(leftCol, leftY += 24, columnW, "toggle.server_colors", config.scoreboardServerColors, value -> {
                    config.scoreboardServerColors = value;
                });
                this.addToggle(leftCol, leftY += 24, columnW, "toggle.block_outline", config.customBlockOutline, value -> {
                    config.customBlockOutline = value;
                });
                this.addToggle(leftCol, leftY += 24, columnW, "toggle.stats", config.statsEnabled, value -> {
                    config.statsEnabled = value;
                });
                this.addSlider(leftCol, leftY += 24, columnW, "slider.stats_scale", config.statsScale, 0.5f, 2.5f, value -> {
                    config.statsScale = value.floatValue();
                });
                this.addSlider(leftCol, leftY += 24, columnW, "slider.stats_radius", config.statsRadius, 0.0f, 12.0f, value -> {
                    config.statsRadius = Math.round(value.floatValue());
                });
                this.addToggle(leftCol, leftY += 24, columnW, "toggle.coords", config.coordsEnabled, value -> {
                    config.coordsEnabled = value;
                });
                this.addSlider(leftCol, leftY += 24, columnW, "slider.coords_scale", config.coordsScale, 0.5f, 2.5f, value -> {
                    config.coordsScale = value.floatValue();
                });
                this.addSlider(leftCol, leftY += 24, columnW, "slider.coords_width", config.coordsWidth, 92.0f, 280.0f, value -> {
                    config.coordsWidth = Math.round(value.floatValue());
                });
                this.addSlider(leftCol, leftY += 24, columnW, "slider.coords_radius", config.coordsRadius, 0.0f, 12.0f, value -> {
                    config.coordsRadius = Math.round(value.floatValue());
                });
                this.addToggle(rightCol, rightY, columnW, "toggle.effects", config.effectsEnabled, value -> {
                    config.effectsEnabled = value;
                });
                this.addSlider(rightCol, rightY += 24, columnW, "slider.effects_scale", config.effectsScale, 0.5f, 2.5f, value -> {
                    config.effectsScale = value.floatValue();
                });
                this.addSlider(rightCol, rightY += 24, columnW, "slider.effects_width", config.effectsWidth, 110.0f, 320.0f, value -> {
                    config.effectsWidth = Math.round(value.floatValue());
                });
                this.addToggle(rightCol, rightY += 24, columnW, "toggle.hide_vanilla_effects", config.hideVanillaEffects, value -> {
                    config.hideVanillaEffects = value;
                });
                this.addToggle(rightCol, rightY += 24, columnW, "toggle.equipment", config.equipmentEnabled, value -> {
                    config.equipmentEnabled = value;
                });
                this.addSlider(rightCol, rightY += 24, columnW, "slider.equipment_scale", config.equipmentScale, 0.5f, 2.5f, value -> {
                    config.equipmentScale = value.floatValue();
                });
                this.addSlider(rightCol, rightY += 24, columnW, "slider.equipment_width", config.equipmentWidth, 120.0f, 320.0f, value -> {
                    config.equipmentWidth = Math.round(value.floatValue());
                });
                this.addSlider(rightCol, rightY += 24, columnW, "slider.effects_radius", config.effectsRadius, 0.0f, 12.0f, value -> {
                    config.effectsRadius = Math.round(value.floatValue());
                });
                this.addSlider(rightCol, rightY += 24, columnW, "slider.equipment_radius", config.equipmentRadius, 0.0f, 12.0f, value -> {
                    config.equipmentRadius = Math.round(value.floatValue());
                });
                break;
            }
            case 2: {
                this.addRenderableWidget((GuiEventListener)new HudButton(left + 10, y, panelW - 20, 20, HudForgeConfigScreen.crosshairButtonText(config.crosshairMode), button -> {
                    config.crosshairMode = (config.crosshairMode + 1) % 4;
                    config.customCrosshair = config.crosshairMode != 0;
                    button.setMessage(HudForgeConfigScreen.crosshairButtonText(config.crosshairMode));
                }));
                this.addToggle(left + 10, y += 24, panelW - 20, "toggle.center_dot", config.crosshairDot, value -> {
                    config.crosshairDot = value;
                });
                this.addSlider(left + 10, y += 24, panelW - 20, "slider.size", config.crosshairSize, 1.0f, 24.0f, value -> {
                    config.crosshairSize = Math.round(value.floatValue());
                });
                this.addSlider(left + 10, y += 24, panelW - 20, "slider.gap", config.crosshairGap, 0.0f, 12.0f, value -> {
                    config.crosshairGap = Math.round(value.floatValue());
                });
                this.addSlider(left + 10, y += 24, panelW - 20, "slider.thickness", config.crosshairThickness, 1.0f, 6.0f, value -> {
                    config.crosshairThickness = Math.round(value.floatValue());
                });
                this.addToggle(left + 10, y += 24, panelW - 20, "toggle.crosshair_outline", config.crosshairOutline, value -> {
                    config.crosshairOutline = value;
                });
                this.addSlider(left + 10, y += 24, panelW - 20, "slider.outline_thickness", config.crosshairOutlineThickness, 1.0f, 4.0f, value -> {
                    config.crosshairOutlineThickness = Math.round(value.floatValue());
                });
                break;
            }
            case 1: {
                this.addRenderableWidget((GuiEventListener)new HudButton(left + 10, backY, 120, 20, HudForgeConfigScreen.tr("button.clean_preset", new Object[0]), button -> {
                    HudForgeConfigScreen.applyCleanPreset(config);
                    this.rebuildWidgets();
                }));
                break;
            }
            case 3: {
                this.addToggle(left + 10, y, panelW - 20, "toggle.auto_respawn", config.autoRespawn, value -> {
                    config.autoRespawn = value;
                });
                this.addToggle(left + 10, y += 24, panelW - 20, "toggle.auto_reconnect", config.autoReconnect, value -> {
                    config.autoReconnect = value;
                });
            }
        }
        this.addRenderableWidget((GuiEventListener)new HudButton(left + panelW - 94, backY, 84, 20, HudForgeConfigScreen.tr("button.back", new Object[0]), button -> {
            this.settingsOpen = false;
            this.rebuildWidgets();
        }));
        this.updateScrollableWidgetVisibility(top + 54, backY - 6, top + 6, backY);
    }

    private void updateScrollableWidgetVisibility(int contentTop, int contentBottom, int tabY, int backY) {
        for (GuiEventListener element : this.children()) {
            if (!(element instanceof AbstractWidget)) continue;
            AbstractWidget widget = (AbstractWidget)element;
            boolean fixed = widget.getY() == tabY || widget.getY() == backY;
            widget.active = widget.visible = fixed || widget.getY() >= contentTop && widget.getY() + widget.getHeight() <= contentBottom;
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int next;
        if (this.settingsOpen && this.settingsMaxScroll > 0 && (next = HudForgeConfigScreen.clamp(this.settingsScroll - (int)Math.round(verticalAmount * 24.0), 0, this.settingsMaxScroll)) != this.settingsScroll) {
            this.settingsScroll = next;
            this.rebuildWidgets();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public void onClose() {
        HudForgeClient.saveActiveServerProfile();
        HudForgeClient.config.save();
        Minecraft.getInstance().gui.setScreen(this.parent);
    }

    public void removed() {
        HudForgeClient.saveActiveServerProfile();
        HudForgeClient.config.save();
    }

    public boolean mouseClicked(MouseButtonEvent click, boolean doubleClick) {
        if (this.settingsOpen) {
            if (click.button() == 0 && this.handleScrollbarClick(click.x(), click.y())) {
                return true;
            }
            if (this.page == SettingsPage.COLORS && click.button() == 0 && this.handleColorTargetClick(click.x(), click.y())) {
                HudForgeConfigScreen.playButtonSound();
                return true;
            }
            if (this.page == SettingsPage.COLORS && click.button() == 0 && this.handleColorPick(click.x(), click.y())) {
                return true;
            }
            return super.mouseClicked(click, doubleClick);
        }
        HudForgeConfig config = HudForgeClient.config;
        if (click.button() == 0) {
            if (config.scoreboardMode == 0 && this.hasServerScoreboard() && HudForgeConfigScreen.inside(click.x(), click.y(), config.scoreboardX, config.scoreboardY, this.scoreboardWidth(config), this.scoreboardHeight(config))) {
                if (HudForgeConfigScreen.inside(click.x(), click.y(), config.scoreboardX + this.scoreboardWidth(config) - 10, config.scoreboardY + this.scoreboardHeight(config) - 10, 10, 10)) {
                    this.dragTarget = DragTarget.SCOREBOARD_RESIZE;
                } else {
                    this.dragTarget = DragTarget.SCOREBOARD_MOVE;
                    this.dragOffsetX = (int)click.x() - config.scoreboardX;
                    this.dragOffsetY = (int)click.y() - config.scoreboardY;
                }
                return true;
            }
            if (config.statsEnabled && HudForgeConfigScreen.inside(click.x(), click.y(), config.statsX, config.statsY, this.statsWidth(config), this.statsHeight(config))) {
                if (HudForgeConfigScreen.inside(click.x(), click.y(), config.statsX + this.statsWidth(config) - 10, config.statsY + this.statsHeight(config) - 10, 10, 10)) {
                    this.dragTarget = DragTarget.STATS_RESIZE;
                } else {
                    this.dragTarget = DragTarget.STATS_MOVE;
                    this.dragOffsetX = (int)click.x() - config.statsX;
                    this.dragOffsetY = (int)click.y() - config.statsY;
                }
                return true;
            }
            if (config.coordsEnabled && HudForgeConfigScreen.inside(click.x(), click.y(), config.coordsX, config.coordsY, this.coordsWidth(config), this.coordsHeight(config))) {
                this.dragTarget = HudForgeConfigScreen.inside(click.x(), click.y(), config.coordsX + this.coordsWidth(config) - 10, config.coordsY + this.coordsHeight(config) - 10, 10, 10) ? DragTarget.COORDS_RESIZE : DragTarget.COORDS_MOVE;
                this.dragOffsetX = (int)click.x() - config.coordsX;
                this.dragOffsetY = (int)click.y() - config.coordsY;
                return true;
            }
            if (config.effectsEnabled && HudForgeConfigScreen.inside(click.x(), click.y(), config.effectsX, config.effectsY, this.effectsWidth(config), this.effectsHeight(config))) {
                this.dragTarget = HudForgeConfigScreen.inside(click.x(), click.y(), config.effectsX + this.effectsWidth(config) - 10, config.effectsY + this.effectsHeight(config) - 10, 10, 10) ? DragTarget.EFFECTS_RESIZE : DragTarget.EFFECTS_MOVE;
                this.dragOffsetX = (int)click.x() - config.effectsX;
                this.dragOffsetY = (int)click.y() - config.effectsY;
                return true;
            }
            if (config.equipmentEnabled && HudForgeConfigScreen.inside(click.x(), click.y(), config.equipmentX, config.equipmentY, this.equipmentWidth(config), this.equipmentHeight(config))) {
                this.dragTarget = HudForgeConfigScreen.inside(click.x(), click.y(), config.equipmentX + this.equipmentWidth(config) - 10, config.equipmentY + this.equipmentHeight(config) - 10, 10, 10) ? DragTarget.EQUIPMENT_RESIZE : DragTarget.EQUIPMENT_MOVE;
                this.dragOffsetX = (int)click.x() - config.equipmentX;
                this.dragOffsetY = (int)click.y() - config.equipmentY;
                return true;
            }
        }
        return super.mouseClicked(click, doubleClick);
    }

    public boolean mouseDragged(MouseButtonEvent click, double deltaX, double deltaY) {
        if (this.settingsOpen) {
            if (this.scrollbarDragging) {
                this.updateScrollFromMouse(click.y());
                return true;
            }
            if (this.page == SettingsPage.COLORS && this.handleColorPick(click.x(), click.y())) {
                return true;
            }
            return super.mouseDragged(click, deltaX, deltaY);
        }
        HudForgeConfig config = HudForgeClient.config;
        switch (this.dragTarget.ordinal()) {
            case 1: {
                config.scoreboardX = HudForgeConfigScreen.clamp((int)click.x() - this.dragOffsetX, 0, Math.max(0, this.width - this.scoreboardWidth(config)));
                config.scoreboardY = HudForgeConfigScreen.clamp((int)click.y() - this.dragOffsetY, 0, Math.max(0, this.height - this.scoreboardHeight(config)));
                return true;
            }
            case 3: {
                config.statsX = HudForgeConfigScreen.clamp((int)click.x() - this.dragOffsetX, 0, Math.max(0, this.width - this.statsWidth(config)));
                config.statsY = HudForgeConfigScreen.clamp((int)click.y() - this.dragOffsetY, 0, Math.max(0, this.height - this.statsHeight(config)));
                return true;
            }
            case 5: {
                config.coordsX = HudForgeConfigScreen.clamp((int)click.x() - this.dragOffsetX, 0, Math.max(0, this.width - this.coordsWidth(config)));
                config.coordsY = HudForgeConfigScreen.clamp((int)click.y() - this.dragOffsetY, 0, Math.max(0, this.height - this.coordsHeight(config)));
                return true;
            }
            case 7: {
                config.effectsX = HudForgeConfigScreen.clamp((int)click.x() - this.dragOffsetX, 0, Math.max(0, this.width - this.effectsWidth(config)));
                config.effectsY = HudForgeConfigScreen.clamp((int)click.y() - this.dragOffsetY, 0, Math.max(0, this.height - this.effectsHeight(config)));
                return true;
            }
            case 9: {
                config.equipmentX = HudForgeConfigScreen.clamp((int)click.x() - this.dragOffsetX, 0, Math.max(0, this.width - this.equipmentWidth(config)));
                config.equipmentY = HudForgeConfigScreen.clamp((int)click.y() - this.dragOffsetY, 0, Math.max(0, this.height - this.equipmentHeight(config)));
                return true;
            }
            case 2: {
                int rawWidth = Math.round(((float)click.x() - (float)config.scoreboardX) / Math.max(0.5f, config.scoreboardScale));
                config.scoreboardWidth = HudForgeConfigScreen.clamp(rawWidth, 90, 320);
                float newScale = ((float)click.y() - (float)config.scoreboardY) / (float)Math.max(42, this.scoreboardBaseHeight());
                config.scoreboardScale = HudForgeConfigScreen.clamp(newScale, 0.5f, 2.5f);
                return true;
            }
            case 4: {
                float newScale = Math.max(((float)click.x() - (float)config.statsX) / 82.0f, ((float)click.y() - (float)config.statsY) / 32.0f);
                config.statsScale = HudForgeConfigScreen.clamp(newScale, 0.5f, 2.5f);
                return true;
            }
            case 6: {
                config.coordsWidth = HudForgeConfigScreen.clamp(Math.round(((float)click.x() - (float)config.coordsX) / Math.max(0.5f, config.coordsScale)), 92, 280);
                config.coordsScale = HudForgeConfigScreen.clamp(((float)click.y() - (float)config.coordsY) / 52.0f, 0.5f, 2.5f);
                return true;
            }
            case 8: {
                config.effectsWidth = HudForgeConfigScreen.clamp(Math.round(((float)click.x() - (float)config.effectsX) / Math.max(0.5f, config.effectsScale)), 110, 320);
                config.effectsScale = HudForgeConfigScreen.clamp(((float)click.y() - (float)config.effectsY) / 74.0f, 0.5f, 2.5f);
                return true;
            }
            case 10: {
                config.equipmentWidth = HudForgeConfigScreen.clamp(Math.round(((float)click.x() - (float)config.equipmentX) / Math.max(0.5f, config.equipmentScale)), 120, 320);
                config.equipmentScale = HudForgeConfigScreen.clamp(((float)click.y() - (float)config.equipmentY) / 96.0f, 0.5f, 2.5f);
                return true;
            }
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    public boolean mouseReleased(MouseButtonEvent click) {
        this.scrollbarDragging = false;
        this.dragTarget = DragTarget.NONE;
        HudForgeClient.saveActiveServerProfile();
        return super.mouseReleased(click);
    }

    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, this.settingsOpen ? -1610612736 : 0x70000000);
        if (!this.settingsOpen) {
            this.renderHudCanvas(context);
        }
        if (this.settingsOpen) {
            this.renderSettingsPanel(context, mouseX, mouseY);
        } else {
            context.text(this.font, HudForgeConfigScreen.tr("editor.help", new Object[0]), 10, 10, -4669228);
        }
        super.extractRenderState(context, mouseX, mouseY, delta);
    }

    private void renderHudCanvas(GuiGraphicsExtractor context) {
        HudForgeConfig config = HudForgeClient.config;
        if (config.scoreboardMode == 0 && this.hasServerScoreboard()) {
            this.renderScoreboardPreview(context, config);
        }
        if (config.statsEnabled) {
            this.renderStatsPreview(context, config);
        }
        if (config.coordsEnabled) {
            this.renderSimplePreview(context, config.coordsX, config.coordsY, config.coordsScale, config.coordsWidth, 52, config.coordsRadius, config.coordsBackground, config.coordsText, List.of("X: -129", "Y: 68", "Z: -76", "Biome: Plains"));
            HudForgeConfigScreen.renderHandle(context, config.coordsX + this.coordsWidth(config), config.coordsY + this.coordsHeight(config));
        }
        if (config.effectsEnabled) {
            this.renderSimplePreview(context, config.effectsX, config.effectsY, config.effectsScale, config.effectsWidth, 74, config.effectsRadius, config.effectsBackground, config.effectsText, List.of("Haste 1:24", "Speed 0:42", "Strength 2:11"));
            HudForgeConfigScreen.renderHandle(context, config.effectsX + this.effectsWidth(config), config.effectsY + this.effectsHeight(config));
        }
        if (config.equipmentEnabled) {
            this.renderSimplePreview(context, config.equipmentX, config.equipmentY, config.equipmentScale, config.equipmentWidth, 96, config.equipmentRadius, config.equipmentBackground, config.equipmentText, List.of("Tool 1532/1561", "Helmet 363/363", "Chest 528/528", "Legs 495/495"));
            HudForgeConfigScreen.renderHandle(context, config.equipmentX + this.equipmentWidth(config), config.equipmentY + this.equipmentHeight(config));
        }
    }

    private void renderScoreboardPreview(GuiGraphicsExtractor context, HudForgeConfig config) {
        Minecraft client = Minecraft.getInstance();
        Objective objective = client.level.getScoreboard().getDisplayObjective(DisplaySlot.SIDEBAR);
        List<PlayerScoreEntry> entries = this.scoreboardEntries(objective);
        if (objective == null || entries.isEmpty()) {
            return;
        }
        Font tr = this.font;
        int width = config.scoreboardWidth;
        int titleHeight = 18;
        int rowHeight = 11;
        int height = titleHeight + entries.size() * rowHeight + 6;
        context.pose().pushMatrix();
        context.pose().translate((float)config.scoreboardX, (float)config.scoreboardY);
        context.pose().scale(config.scoreboardScale, config.scoreboardScale);
        HudForgeConfigScreen.fillSoftRect(context, 0, 0, width, height, config.scoreboardRadius, config.scoreboardBackground);
        context.text(tr, objective.getDisplayName(), Mth.clamp((int)((width - tr.width((FormattedText)objective.getDisplayName())) / 2), (int)8, (int)(width - 8)), 5, config.scoreboardTitle);
        int y = titleHeight;
        for (PlayerScoreEntry entry : entries) {
            Component line;
            Object object = line = entry.display() == null ? PlayerTeam.formatNameForTeam((Team)client.level.getScoreboard().getPlayersTeam(entry.owner()), (Component)Component.literal((String)entry.owner())) : entry.display();
            if (config.scoreboardServerColors) {
                context.text(tr, line, 9, y, config.scoreboardText);
            } else {
                context.text(tr, this.trimToWidth(line.getString(), width - 18), 9, y, config.scoreboardText);
            }
            y += rowHeight;
        }
        context.pose().popMatrix();
        HudForgeConfigScreen.renderHandle(context, config.scoreboardX + this.scoreboardWidth(config), config.scoreboardY + this.scoreboardHeight(config));
    }

    private void renderStatsPreview(GuiGraphicsExtractor context, HudForgeConfig config) {
        context.pose().pushMatrix();
        context.pose().translate((float)config.statsX, (float)config.statsY);
        context.pose().scale(config.statsScale, config.statsScale);
        HudForgeConfigScreen.fillSoftRect(context, 0, 0, 82, 32, config.statsRadius, config.statsBackground);
        context.text(this.font, Minecraft.getInstance().getFps() + " FPS", 8, 5, config.statsText);
        context.text(this.font, this.currentPing() + " ms", 8, 18, config.statsText);
        context.pose().popMatrix();
        HudForgeConfigScreen.renderHandle(context, config.statsX + this.statsWidth(config), config.statsY + this.statsHeight(config));
    }

    private void renderSimplePreview(GuiGraphicsExtractor context, int x, int y, float scale, int panelW, int panelH, int radius, int background, int textColor, List<String> lines) {
        context.pose().pushMatrix();
        context.pose().translate((float)x, (float)y);
        context.pose().scale(scale, scale);
        HudForgeConfigScreen.fillSoftRect(context, 0, 0, panelW, panelH, radius, background);
        int lineY = 6;
        for (String line : lines) {
            context.text(this.font, line, 8, lineY, textColor);
            lineY += 11;
        }
        context.pose().popMatrix();
    }

    private void renderSettingsPanel(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        int panelW = this.settingsPanelWidth();
        int panelH = this.settingsPanelHeight();
        int left = (this.width - panelW) / 2;
        int top = (this.height - panelH) / 2;
        context.fill(left, top, left + panelW, top + panelH, -401730268);
        context.fill(left, top, left + panelW, top + 1, -2007208294);
        context.fill(left, top + panelH - 1, left + panelW, top + panelH, -2007208294);
        context.fill(left, top, left + 1, top + panelH, -2007208294);
        context.fill(left + panelW - 1, top, left + panelW, top + panelH, -2007208294);
        context.text(this.font, HudForgeConfigScreen.tr(this.page.titleKey, new Object[0]), left + 10, top + 30, -1380097);
        if (this.page == SettingsPage.COLORS) {
            this.renderColorTargets(context, left + 10, top + 58);
            this.renderColorPicker(context, this.colorPickerX(left, panelW), top + 58, panelW - (this.colorPickerX(left, panelW) - left) - 14, this.colorTarget.get(HudForgeClient.config));
        }
        if (this.settingsMaxScroll > 0) {
            int trackTop = this.scrollbarTrackTop();
            int trackBottom = this.scrollbarTrackBottom();
            int thumbH = this.scrollbarThumbHeight();
            int thumbY = this.scrollbarThumbY();
            HudForgeConfigScreen.fillSoftRect(context, left + panelW - 6, trackTop, 3, trackBottom - trackTop, 1, 1429752164);
            HudForgeConfigScreen.fillSoftRect(context, left + panelW - 7, thumbY, 5, thumbH, 2, -10258433);
        }
    }

    private boolean handleScrollbarClick(double mouseX, double mouseY) {
        int trackBottom;
        int trackTop;
        if (this.settingsMaxScroll <= 0) {
            return false;
        }
        int x = (this.width + this.settingsPanelWidth()) / 2 - 12;
        if (!HudForgeConfigScreen.inside(mouseX, mouseY, x, trackTop = this.scrollbarTrackTop(), 12, (trackBottom = this.scrollbarTrackBottom()) - trackTop)) {
            return false;
        }
        int thumbY = this.scrollbarThumbY();
        int thumbH = this.scrollbarThumbHeight();
        this.scrollbarDragOffset = mouseY >= (double)thumbY && mouseY < (double)(thumbY + thumbH) ? (int)mouseY - thumbY : thumbH / 2;
        this.scrollbarDragging = true;
        this.updateScrollFromMouse(mouseY);
        return true;
    }

    private void updateScrollFromMouse(double mouseY) {
        int travel = this.scrollbarTrackBottom() - this.scrollbarTrackTop() - this.scrollbarThumbHeight();
        int next = travel <= 0 ? 0 : Math.round(((float)mouseY - (float)this.scrollbarDragOffset - (float)this.scrollbarTrackTop()) * (float)this.settingsMaxScroll / (float)travel);
        if ((next = HudForgeConfigScreen.clamp(next, 0, this.settingsMaxScroll)) != this.settingsScroll) {
            this.settingsScroll = next;
            this.rebuildWidgets();
        }
    }

    private int scrollbarTrackTop() {
        return (this.height - this.settingsPanelHeight()) / 2 + 56;
    }

    private int scrollbarTrackBottom() {
        return (this.height + this.settingsPanelHeight()) / 2 - 38;
    }

    private int scrollbarThumbHeight() {
        int track = this.scrollbarTrackBottom() - this.scrollbarTrackTop();
        return Math.max(24, Math.round((float)(track * track) / (float)(track + this.settingsMaxScroll)));
    }

    private int scrollbarThumbY() {
        int travel = this.scrollbarTrackBottom() - this.scrollbarTrackTop() - this.scrollbarThumbHeight();
        return this.scrollbarTrackTop() + Math.round((float)travel * ((float)this.settingsScroll / (float)this.settingsMaxScroll));
    }

    private void renderColorPicker(GuiGraphicsExtractor context, int x, int y, int width, int color) {
        int yy;
        int square = this.colorPickerSquare(this.settingsPanelWidth());
        float hue = HudForgeConfigScreen.rgbToHue(color);
        for (int xx = 0; xx < square; xx += 2) {
            for (yy = 0; yy < square; yy += 2) {
                float sat = (float)xx / (float)(square - 1);
                float val = 1.0f - (float)yy / (float)(square - 1);
                context.fill(x + xx, y + yy, x + xx + 2, y + yy + 2, HudForgeConfigScreen.hsvToRgb(hue, sat, val, HudForgeConfigScreen.alpha(color)));
            }
        }
        int hueX = x + square + 10;
        for (yy = 0; yy < square; yy += 2) {
            context.fill(hueX, y + yy, hueX + 12, y + yy + 2, HudForgeConfigScreen.hsvToRgb((float)yy / (float)square, 1.0f, 1.0f, 255));
        }
        int alphaY = y + square + 8;
        for (int xx = 0; xx < square; xx += 2) {
            context.fill(x + xx, alphaY, x + xx + 2, alphaY + 10, HudForgeConfigScreen.withAlpha(color, Math.round((float)xx / (float)square * 255.0f)));
        }
        int previewX = x + square + 28;
        HudForgeConfigScreen.fillSoftRect(context, previewX, y, 26, 26, 4, color);
        context.text(this.font, HudForgeConfigScreen.tr(this.colorTarget.labelKey, new Object[0]), previewX, y + 34, -1380097);
        int inputY = alphaY + 18;
        this.drawNumberBox(context, x, inputY, "R", color >> 16 & 0xFF);
        this.drawNumberBox(context, x + 54, inputY, "G", color >> 8 & 0xFF);
        this.drawNumberBox(context, x + 108, inputY, "B", color & 0xFF);
        this.drawNumberBox(context, x + 162, inputY, "A", HudForgeConfigScreen.alpha(color));
    }

    private int colorTargetColumns(int panelW) {
        return panelW >= 520 ? 2 : 1;
    }

    private int colorTargetWidth(int panelW) {
        return this.colorTargetColumns(panelW) == 2 ? 154 : 174;
    }

    private int colorPickerX(int left, int panelW) {
        int columns = this.colorTargetColumns(panelW);
        int targetW = this.colorTargetWidth(panelW);
        return left + 10 + columns * targetW + (columns - 1) * 8 + 20;
    }

    private int colorPickerSquare(int panelW) {
        int pickerWidth = Math.max(220, panelW - this.colorPickerX(0, panelW) - 14);
        return Math.min(112, Math.max(76, pickerWidth - 178));
    }

    private boolean handleColorPick(double mouseX, double mouseY) {
        int panelW = this.settingsPanelWidth();
        int left = (this.width - panelW) / 2;
        int top = (this.height - this.settingsPanelHeight()) / 2;
        int x = this.colorPickerX(left, panelW);
        int y = top + 58;
        int square = this.colorPickerSquare(panelW);
        HudForgeConfig config = HudForgeClient.config;
        int current = this.colorTarget.get(config);
        float hue = HudForgeConfigScreen.rgbToHue(current);
        if (HudForgeConfigScreen.inside(mouseX, mouseY, x, y, square, square)) {
            float sat = HudForgeConfigScreen.clamp((float)((mouseX - (double)x) / (double)square), 0.0f, 1.0f);
            float val = 1.0f - HudForgeConfigScreen.clamp((float)((mouseY - (double)y) / (double)square), 0.0f, 1.0f);
            this.colorTarget.set(config, HudForgeConfigScreen.hsvToRgb(hue, sat, val, HudForgeConfigScreen.alpha(current)));
            return true;
        }
        if (HudForgeConfigScreen.inside(mouseX, mouseY, x + square + 10, y, 12, square)) {
            hue = HudForgeConfigScreen.clamp((float)((mouseY - (double)y) / (double)square), 0.0f, 1.0f);
            float[] sv = HudForgeConfigScreen.rgbToSatVal(current);
            this.colorTarget.set(config, HudForgeConfigScreen.hsvToRgb(hue, sv[0], sv[1], HudForgeConfigScreen.alpha(current)));
            return true;
        }
        if (HudForgeConfigScreen.inside(mouseX, mouseY, x, y + square + 8, square, 10)) {
            int alpha = Math.round(HudForgeConfigScreen.clamp((float)((mouseX - (double)x) / (double)square), 0.0f, 1.0f) * 255.0f);
            this.colorTarget.set(config, HudForgeConfigScreen.withAlpha(current, alpha));
            return true;
        }
        return false;
    }

    private boolean handleColorTargetClick(double mouseX, double mouseY) {
        int panelW = this.settingsPanelWidth();
        int left = (this.width - panelW) / 2;
        int top = (this.height - this.settingsPanelHeight()) / 2;
        int x = left + 10;
        int y = top + 58;
        int columns = this.colorTargetColumns(panelW);
        int targetW = this.colorTargetWidth(panelW);
        int i = 0;
        for (ColorTarget target : ColorTarget.values()) {
            int tx = x + i % columns * (targetW + 8);
            int ty = y + i / columns * 25;
            if (HudForgeConfigScreen.inside(mouseX, mouseY, tx, ty, targetW, 21)) {
                this.colorTarget = target;
                return true;
            }
            ++i;
        }
        return false;
    }

    private int settingsPanelWidth() {
        return Math.max(1, Math.min(900, this.width - 24));
    }

    private int settingsPanelHeight() {
        return Math.max(1, Math.min(520, this.height - 24));
    }

    private boolean hasServerScoreboard() {
        Minecraft client = Minecraft.getInstance();
        return client.level != null && client.level.getScoreboard().getDisplayObjective(DisplaySlot.SIDEBAR) != null;
    }

    private void renderColorTargets(GuiGraphicsExtractor context, int x, int y) {
        int panelW = this.settingsPanelWidth();
        int columns = this.colorTargetColumns(panelW);
        int targetW = this.colorTargetWidth(panelW);
        int i = 0;
        for (ColorTarget target : ColorTarget.values()) {
            int tx = x + i % columns * (targetW + 8);
            int ty = y + i / columns * 25;
            int color = target.get(HudForgeClient.config);
            if (target == this.colorTarget) {
                HudForgeConfigScreen.fillSoftRect(context, tx - 2, ty - 2, targetW + 4, 25, 4, -9601793);
            }
            HudForgeConfigScreen.fillSoftRect(context, tx, ty, targetW, 21, 3, target == this.colorTarget ? -584829357 : -971299528);
            HudForgeConfigScreen.fillSoftRect(context, tx + 4, ty + 4, 14, 14, 2, color);
            context.text(this.font, this.fitLabel(HudForgeConfigScreen.tr(target.labelKey, new Object[0]).getString(), targetW - 30), tx + 24, ty + 7, -3288094);
            ++i;
        }
    }

    private String fitLabel(String label, int maxWidth) {
        if (this.font.width(label) <= maxWidth) {
            return label;
        }
        return this.font.plainSubstrByWidth(label, Math.max(8, maxWidth - this.font.width("..."))) + "...";
    }

    private void drawNumberBox(GuiGraphicsExtractor context, int x, int y, String label, int value) {
        context.fill(x, y, x + 48, y + 18, -1446411);
        context.fill(x, y, x + 48, y + 1, -4603698);
        context.fill(x, y + 17, x + 48, y + 18, -4603698);
        String text = String.valueOf(value);
        context.text(this.font, text, x + 24 - this.font.width(text) / 2, y + 5, -15658216, false);
        context.text(this.font, label, x + 24 - this.font.width(label) / 2, y + 22, -1380097);
    }

    private List<PlayerScoreEntry> scoreboardEntries(Objective objective) {
        if (objective == null || Minecraft.getInstance().level == null) {
            return List.of();
        }
        Scoreboard scoreboard = Minecraft.getInstance().level.getScoreboard();
        return scoreboard.listPlayerScores(objective).stream().filter(entry -> entry.value() != 0 || entry.display() != null).sorted(Comparator.comparingInt(PlayerScoreEntry::value).reversed()).limit(15L).toList();
    }

    private int scoreboardBaseHeight() {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            return 0;
        }
        Objective objective = client.level.getScoreboard().getDisplayObjective(DisplaySlot.SIDEBAR);
        int rows = this.scoreboardEntries(objective).size();
        return rows == 0 ? 0 : 18 + rows * 11 + 6;
    }

    private int scoreboardWidth(HudForgeConfig config) {
        return Math.round((float)config.scoreboardWidth * config.scoreboardScale);
    }

    private int scoreboardHeight(HudForgeConfig config) {
        return Math.round((float)this.scoreboardBaseHeight() * config.scoreboardScale);
    }

    private int statsWidth(HudForgeConfig config) {
        return Math.round(82.0f * config.statsScale);
    }

    private int statsHeight(HudForgeConfig config) {
        return Math.round(32.0f * config.statsScale);
    }

    private int coordsWidth(HudForgeConfig config) {
        return Math.round((float)config.coordsWidth * config.coordsScale);
    }

    private int coordsHeight(HudForgeConfig config) {
        return Math.round(52.0f * config.coordsScale);
    }

    private int effectsWidth(HudForgeConfig config) {
        return Math.round((float)config.effectsWidth * config.effectsScale);
    }

    private int effectsHeight(HudForgeConfig config) {
        return Math.round(74.0f * config.effectsScale);
    }

    private int equipmentWidth(HudForgeConfig config) {
        return Math.round((float)config.equipmentWidth * config.equipmentScale);
    }

    private int equipmentHeight(HudForgeConfig config) {
        return Math.round(96.0f * config.equipmentScale);
    }

    private int currentPing() {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && client.getConnection() != null && client.getConnection().getPlayerInfo(client.player.getUUID()) != null) {
            return client.getConnection().getPlayerInfo(client.player.getUUID()).getLatency();
        }
        return 0;
    }

    private void addToggle(int x, int y, int width, String key, boolean value, Consumer<Boolean> setter) {
        this.addRenderableWidget((GuiEventListener)new HudButton(x, y, width, 20, HudForgeConfigScreen.toggleText(key, value), button -> {
            boolean next = !button.getMessage().getString().contains(HudForgeConfigScreen.tr("value.on", new Object[0]).getString());
            setter.accept(next);
            button.setMessage(HudForgeConfigScreen.toggleText(key, next));
        }));
    }

    private void addSlider(int x, int y, int width, String key, float value, float min, float max, Consumer<Float> setter) {
        this.addRenderableWidget((GuiEventListener)new HudSlider(x, y, width, 20, key, value, min, max, setter));
    }

    private static void playButtonSound() {
        Minecraft.getInstance().getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((Holder)SoundEvents.UI_BUTTON_CLICK, (float)1.0f));
    }

    private static void renderHandle(GuiGraphicsExtractor context, int x, int y) {
        context.fill(x - 8, y - 8, x, y, -855638017);
        context.fill(x - 6, y - 2, x - 2, y, -15658216);
        context.fill(x - 2, y - 6, x, y - 2, -15658216);
    }

    private static void fillSoftRect(GuiGraphicsExtractor context, int x, int y, int width, int height, int radius, int color) {
        int r = Math.max(0, Math.min(radius, Math.min(width, height) / 2));
        if (r <= 1) {
            context.fill(x, y, x + width, y + height, color);
            return;
        }
        context.fill(x + r, y, x + width - r, y + height, color);
        context.fill(x, y + r, x + width, y + height - r, color);
        for (int i = 0; i < r; ++i) {
            int inset = Math.max(0, r - i - 1);
            context.fill(x + inset, y + i, x + width - inset, y + i + 1, color);
            context.fill(x + inset, y + height - i - 1, x + width - inset, y + height - i, color);
        }
    }

    private static void applyCleanPreset(HudForgeConfig config) {
        config.scoreboardBackground = -1341124072;
        config.scoreboardAccent = -10034177;
        config.scoreboardText = -1380097;
        config.scoreboardTitle = -1;
        config.statsBackground = -1743777256;
        config.statsAccent = -8626945;
        config.statsText = -1380097;
        config.crosshairColor = -1380097;
        config.crosshairOutlineColor = -805306368;
    }

    private String trimToWidth(String value, int maxWidth) {
        if (this.font.width(value) <= maxWidth) {
            return value;
        }
        return this.font.plainSubstrByWidth(value, Math.max(8, maxWidth - this.font.width("..."))) + "...";
    }

    private static String scoreboardModeLabel(int mode) {
        return switch (mode) {
            case 1 -> "mode.vanilla";
            case 2 -> "mode.off";
            default -> "mode.custom";
        };
    }

    private static String crosshairModeLabel(int mode) {
        return switch (mode) {
            case 0 -> "mode.vanilla";
            case 2 -> "mode.circle";
            case 3 -> "mode.dot";
            default -> "mode.clean_cross";
        };
    }

    private static Component tr(String key, Object ... args) {
        return Component.translatable((String)("hudfabric." + key), (Object[])args);
    }

    private static Component scoreboardButtonText(int mode) {
        return HudForgeConfigScreen.tr("button.scoreboard", HudForgeConfigScreen.tr(HudForgeConfigScreen.scoreboardModeLabel(mode), new Object[0]));
    }

    private static Component crosshairButtonText(int mode) {
        return HudForgeConfigScreen.tr("button.mode", HudForgeConfigScreen.tr(HudForgeConfigScreen.crosshairModeLabel(mode), new Object[0]));
    }

    private static Component toggleText(String key, boolean value) {
        return HudForgeConfigScreen.tr("button.toggle", HudForgeConfigScreen.tr(key, new Object[0]), HudForgeConfigScreen.tr(value ? "value.on" : "value.off", new Object[0]));
    }

    private Component tabText(SettingsPage value) {
        return value == this.page ? Component.literal((String)"* ").append(HudForgeConfigScreen.tr(value.labelKey, new Object[0])) : HudForgeConfigScreen.tr(value.labelKey, new Object[0]);
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= (double)x && mouseX <= (double)(x + width) && mouseY >= (double)y && mouseY <= (double)(y + height);
    }

    private static int alpha(int color) {
        return color >>> 24;
    }

    private static int withAlpha(int color, int alpha) {
        return HudForgeConfigScreen.clamp(alpha, 0, 255) << 24 | color & 0xFFFFFF;
    }

    private static int hsvToRgb(float hue, float sat, float val, int alpha) {
        int rgb = Color.HSBtoRGB(hue, sat, val) & 0xFFFFFF;
        return HudForgeConfigScreen.clamp(alpha, 0, 255) << 24 | rgb;
    }

    private static float rgbToHue(int color) {
        float[] hsb = Color.RGBtoHSB(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, null);
        return hsb[0];
    }

    private static float[] rgbToSatVal(int color) {
        float[] hsb = Color.RGBtoHSB(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, null);
        return new float[]{hsb[1], hsb[2]};
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static void drawBorder(GuiGraphicsExtractor context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x, y, x + 1, y + height, color);
        context.fill(x + width - 1, y, x + width, y + height, color);
    }

    private static enum SettingsPage {
        SCOREBOARD("tab.hud", "title.hud"),
        COLORS("tab.colors", "title.colors"),
        CROSSHAIR("tab.crosshair", "title.crosshair"),
        EXTRAS("tab.extras", "title.extras");

        private final String labelKey;
        private final String titleKey;

        private SettingsPage(String labelKey, String titleKey) {
            this.labelKey = labelKey;
            this.titleKey = titleKey;
        }
    }

    private static enum ColorTarget {
        BOARD_BG("color.scoreboard_background", "BB"){

            @Override
            int get(HudForgeConfig c) {
                return c.scoreboardBackground;
            }

            @Override
            void set(HudForgeConfig c, int v) {
                c.scoreboardBackground = v;
            }
        }
        ,
        BOARD_TITLE("color.scoreboard_title", "TT"){

            @Override
            int get(HudForgeConfig c) {
                return c.scoreboardTitle;
            }

            @Override
            void set(HudForgeConfig c, int v) {
                c.scoreboardTitle = v;
            }
        }
        ,
        BOARD_TEXT("color.scoreboard_text", "BT"){

            @Override
            int get(HudForgeConfig c) {
                return c.scoreboardText;
            }

            @Override
            void set(HudForgeConfig c, int v) {
                c.scoreboardText = v;
            }
        }
        ,
        STATS_BG("color.stats_background", "SB"){

            @Override
            int get(HudForgeConfig c) {
                return c.statsBackground;
            }

            @Override
            void set(HudForgeConfig c, int v) {
                c.statsBackground = v;
            }
        }
        ,
        STATS_TEXT("color.stats_text", "ST"){

            @Override
            int get(HudForgeConfig c) {
                return c.statsText;
            }

            @Override
            void set(HudForgeConfig c, int v) {
                c.statsText = v;
            }
        }
        ,
        COORDS_BG("color.coords_background", "CB"){

            @Override
            int get(HudForgeConfig c) {
                return c.coordsBackground;
            }

            @Override
            void set(HudForgeConfig c, int v) {
                c.coordsBackground = v;
            }
        }
        ,
        COORDS_TEXT("color.coords_text", "CT"){

            @Override
            int get(HudForgeConfig c) {
                return c.coordsText;
            }

            @Override
            void set(HudForgeConfig c, int v) {
                c.coordsText = v;
            }
        }
        ,
        EFFECTS_BG("color.effects_background", "EB"){

            @Override
            int get(HudForgeConfig c) {
                return c.effectsBackground;
            }

            @Override
            void set(HudForgeConfig c, int v) {
                c.effectsBackground = v;
            }
        }
        ,
        EFFECTS_TEXT("color.effects_text", "ET"){

            @Override
            int get(HudForgeConfig c) {
                return c.effectsText;
            }

            @Override
            void set(HudForgeConfig c, int v) {
                c.effectsText = v;
            }
        }
        ,
        EQUIPMENT_BG("color.equipment_background", "QB"){

            @Override
            int get(HudForgeConfig c) {
                return c.equipmentBackground;
            }

            @Override
            void set(HudForgeConfig c, int v) {
                c.equipmentBackground = v;
            }
        }
        ,
        EQUIPMENT_TEXT("color.equipment_text", "QT"){

            @Override
            int get(HudForgeConfig c) {
                return c.equipmentText;
            }

            @Override
            void set(HudForgeConfig c, int v) {
                c.equipmentText = v;
            }
        }
        ,
        CROSSHAIR("color.crosshair", "CH"){

            @Override
            int get(HudForgeConfig c) {
                return c.crosshairColor;
            }

            @Override
            void set(HudForgeConfig c, int v) {
                c.crosshairColor = v;
            }
        }
        ,
        CROSSHAIR_OUTLINE("color.crosshair_outline", "CO"){

            @Override
            int get(HudForgeConfig c) {
                return c.crosshairOutlineColor;
            }

            @Override
            void set(HudForgeConfig c, int v) {
                c.crosshairOutlineColor = v;
            }
        }
        ,
        BLOCK_OUTLINE("color.block_outline", "BO"){

            @Override
            int get(HudForgeConfig c) {
                return c.blockOutlineColor;
            }

            @Override
            void set(HudForgeConfig c, int v) {
                c.blockOutlineColor = v;
            }
        };

        private final String labelKey;
        private final String shortLabel;

        private ColorTarget(String labelKey, String shortLabel) {
            this.labelKey = labelKey;
            this.shortLabel = shortLabel;
        }

        abstract int get(HudForgeConfig var1);

        abstract void set(HudForgeConfig var1, int var2);
    }

    private static enum DragTarget {
        NONE,
        SCOREBOARD_MOVE,
        SCOREBOARD_RESIZE,
        STATS_MOVE,
        STATS_RESIZE,
        COORDS_MOVE,
        COORDS_RESIZE,
        EFFECTS_MOVE,
        EFFECTS_RESIZE,
        EQUIPMENT_MOVE,
        EQUIPMENT_RESIZE;

    }

    private static final class HudButton
    extends AbstractButton {
        private final Consumer<HudButton> onPress;
        private boolean selectedStyle;

        private HudButton(int x, int y, int width, int height, Component message, Consumer<HudButton> onPress) {
            super(x, y, width, height, message);
            this.onPress = onPress;
        }

        private void setSelectedStyle(boolean selectedStyle) {
            this.selectedStyle = selectedStyle;
        }

        public void onPress(InputWithModifiers input) {
            this.onPress.accept(this);
        }

        protected void extractContents(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
            int bg = this.selectedStyle || this.isHovered() || this.isHoveredOrFocused() ? -584829357 : -971299528;
            HudForgeConfigScreen.fillSoftRect(context, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 3, bg);
            HudForgeConfigScreen.drawBorder(context, this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.selectedStyle || this.isHovered() || this.isHoveredOrFocused() ? -10258433 : -2007208294);
            if (this.selectedStyle) {
                context.fill(this.getX() + 2, this.getY() + this.getHeight() - 3, this.getX() + this.getWidth() - 2, this.getY() + this.getHeight() - 2, -10258433);
            }
            Font tr = Minecraft.getInstance().font;
            Component message = this.getMessage();
            context.text(tr, message, this.getX() + this.getWidth() / 2 - tr.width((FormattedText)message) / 2, this.getY() + (this.getHeight() - 8) / 2, -1380097);
        }

        protected void updateWidgetNarration(NarrationElementOutput builder) {
            this.defaultButtonNarrationText(builder);
        }
    }

    private static final class HudSlider
    extends AbstractSliderButton {
        private final String key;
        private final float min;
        private final float max;
        private final Consumer<Float> setter;

        private HudSlider(int x, int y, int width, int height, String key, float value, float min, float max, Consumer<Float> setter) {
            super(x, y, width, height, (Component)Component.empty(), Math.max(0.0, Math.min(1.0, (double)((value - min) / (max - min)))));
            this.key = key;
            this.min = min;
            this.max = max;
            this.setter = setter;
            this.updateMessage();
        }

        protected void updateMessage() {
            float current = this.min + (float)this.value * (this.max - this.min);
            String rendered = Math.abs(current - (float)Math.round(current)) < 0.01f ? String.valueOf(Math.round(current)) : String.format(Locale.ROOT, "%.2f", Float.valueOf(current));
            this.setMessage(HudForgeConfigScreen.tr("button.slider", HudForgeConfigScreen.tr(this.key, new Object[0]), rendered));
        }

        protected void applyValue() {
            this.setter.accept(Float.valueOf(this.min + (float)this.value * (this.max - this.min)));
        }
    }
}

