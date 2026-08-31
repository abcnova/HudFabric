package dev.nova.hudfabric.gui;

import dev.nova.hudfabric.HudForgeClient;
import dev.nova.hudfabric.config.HudForgeConfig;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

public final class HudForgeConfigScreen extends Screen {
    private static final int PANEL = 0xF20A1020;
    private static final int CONTROL = 0xE0182035;
    private static final int CONTROL_HOVER = 0xF02A3657;
    private static final int BLUE = 0xFF7286FF;
    private static final int BORDER = 0x996A7DB2;
    private static final int WHITE = 0xFFEAF0FF;

    private final Screen parent;
    private boolean settingsOpen;
    private SettingsPage page = SettingsPage.SCOREBOARD;
    private HudModule activeModule;
    private ColorTarget colorTarget = ColorTarget.BOARD_BG;
    private DragTarget dragTarget = DragTarget.NONE;
    private int dragOffsetX;
    private int dragOffsetY;
    private int draggedEquipmentSlot = -1;
    private int settingsScroll;
    private int settingsMaxScroll;
    private boolean scrollbarDragging;
    private int scrollbarDragOffset;

    public HudForgeConfigScreen(Screen parent) {
        this(parent, false);
    }

    public HudForgeConfigScreen(Screen parent, boolean openSettings) {
        super(tr("title"));
        this.parent = parent;
        this.settingsOpen = openSettings;
    }

    @Override
    protected void init() {
        HudForgeConfig config = HudForgeClient.config.clamped();
        if (settingsOpen) {
            initSettings(config);
        } else {
            addRenderableWidget(new HudButton(width - 132, height - 28, 120, 20, tr("button.settings"), button -> {
                settingsOpen = true;
                rebuildWidgets();
            }));
            addRenderableWidget(new HudButton(width - 258, height - 28, 120, 20, tr("button.done"), button -> onClose()));
        }
    }

    private void initSettings(HudForgeConfig config) {
        int panelW = settingsPanelWidth();
        int panelH = settingsPanelHeight();
        int left = (width - panelW) / 2;
        int top = (height - panelH) / 2;
        int contentLeft = left + 132;
        int contentW = panelW - 142;
        int viewportHeight = Math.max(20, panelH - 94);
        settingsMaxScroll = switch (page) {
            case SCOREBOARD -> 0;
            case CROSSHAIR -> Math.max(0, 164 - viewportHeight);
            default -> 0;
        };
        settingsScroll = clamp(settingsScroll, 0, settingsMaxScroll);
        int y = top + 78 - settingsScroll;
        int backY = top + panelH - 30;

        int navIndex = 0;
        for (SettingsPage value : SettingsPage.values()) {
            if (value == SettingsPage.COLORS) continue;
            int x = left + 10;
            int tabY = top + 58 + navIndex++ * 38;
            HudButton tab = new HudButton(x, tabY, 108, 30, tabText(value), button -> {
                page = value;
                if (value == SettingsPage.SCOREBOARD) activeModule = null;
                if (value == SettingsPage.CROSSHAIR) colorTarget = ColorTarget.CROSSHAIR;
                settingsScroll = 0;
                rebuildWidgets();
            });
            tab.setSelectedStyle(page == value);
            addRenderableWidget(tab);
        }

        switch (page) {
            case SCOREBOARD -> {
                if (contentW > 0) {
                    initModulePage(config, contentLeft, contentW, y, backY);
                } else {
                int gap = 12;
                int columnW = (contentW - 22 - gap) / 2;
                int leftCol = contentLeft + 10;
                int rightCol = leftCol + columnW + gap;
                int leftY = y;
                int rightY = y;
                addRenderableWidget(new HudButton(leftCol, leftY, columnW, 20, scoreboardButtonText(config.scoreboardMode), button -> {
                    config.scoreboardMode = (config.scoreboardMode + 1) % 3;
                    button.setMessage(scoreboardButtonText(config.scoreboardMode));
                }));
                leftY += 24;
                addSlider(leftCol, leftY, columnW, "slider.width", config.scoreboardWidth, 90, 320, value -> config.scoreboardWidth = Math.round(value));
                leftY += 24;
                addSlider(leftCol, leftY, columnW, "slider.scale", config.scoreboardScale, 0.5f, 2.5f, value -> config.scoreboardScale = value);
                leftY += 24;
                addSlider(leftCol, leftY, columnW, "slider.board_radius", config.scoreboardRadius, 0, 12, value -> config.scoreboardRadius = Math.round(value));
                leftY += 24;
                addToggle(leftCol, leftY, columnW, "toggle.server_colors", config.scoreboardServerColors, value -> config.scoreboardServerColors = value);
                leftY += 24;
                addToggle(leftCol, leftY, columnW, "toggle.block_outline", config.customBlockOutline, value -> config.customBlockOutline = value);
                leftY += 24;
                addToggle(leftCol, leftY, columnW, "toggle.stats", config.statsEnabled, value -> config.statsEnabled = value);
                leftY += 24;
                addSlider(leftCol, leftY, columnW, "slider.stats_scale", config.statsScale, 0.5f, 2.5f, value -> config.statsScale = value);
                leftY += 24;
                addSlider(leftCol, leftY, columnW, "slider.stats_radius", config.statsRadius, 0, 12, value -> config.statsRadius = Math.round(value));
                leftY += 24;
                addToggle(leftCol, leftY, columnW, "toggle.coords", config.coordsEnabled, value -> config.coordsEnabled = value);
                leftY += 24;
                addSlider(leftCol, leftY, columnW, "slider.coords_scale", config.coordsScale, 0.5f, 2.5f, value -> config.coordsScale = value);
                leftY += 24;
                addSlider(leftCol, leftY, columnW, "slider.coords_width", config.coordsWidth, 92, 280, value -> config.coordsWidth = Math.round(value));
                leftY += 24;
                addSlider(leftCol, leftY, columnW, "slider.coords_radius", config.coordsRadius, 0, 12, value -> config.coordsRadius = Math.round(value));

                addToggle(rightCol, rightY, columnW, "toggle.effects", config.effectsEnabled, value -> config.effectsEnabled = value);
                rightY += 24;
                addSlider(rightCol, rightY, columnW, "slider.effects_scale", config.effectsScale, 0.5f, 2.5f, value -> config.effectsScale = value);
                rightY += 24;
                addSlider(rightCol, rightY, columnW, "slider.effects_width", config.effectsWidth, 110, 320, value -> config.effectsWidth = Math.round(value));
                rightY += 24;
                addToggle(rightCol, rightY, columnW, "toggle.hide_vanilla_effects", config.hideVanillaEffects, value -> config.hideVanillaEffects = value);
                rightY += 24;
                addToggle(rightCol, rightY, columnW, "toggle.equipment", config.equipmentEnabled, value -> config.equipmentEnabled = value);
                rightY += 24;
                addSlider(rightCol, rightY, columnW, "slider.equipment_scale", config.equipmentScale, 0.5f, 2.5f, value -> config.equipmentScale = value);
                rightY += 24;
                addSlider(rightCol, rightY, columnW, "slider.equipment_width", config.equipmentWidth, 120, 320, value -> config.equipmentWidth = Math.round(value));
                rightY += 24;
                addSlider(rightCol, rightY, columnW, "slider.effects_radius", config.effectsRadius, 0, 12, value -> config.effectsRadius = Math.round(value));
                rightY += 24;
                addSlider(rightCol, rightY, columnW, "slider.equipment_radius", config.equipmentRadius, 0, 12, value -> config.equipmentRadius = Math.round(value));
                rightY += 24;
                addToggle(rightCol, rightY, columnW, "toggle.cps", config.cpsEnabled, value -> config.cpsEnabled = value);
                rightY += 24;
                addSlider(rightCol, rightY, columnW, "slider.cps_scale", config.cpsScale, 0.5f, 2.5f, value -> config.cpsScale = value);
                rightY += 24;
                addSlider(rightCol, rightY, columnW, "slider.cps_width", config.cpsWidth, 72, 240, value -> config.cpsWidth = Math.round(value));
                rightY += 24;
                addSlider(rightCol, rightY, columnW, "slider.cps_radius", config.cpsRadius, 0, 12, value -> config.cpsRadius = Math.round(value));
                }
            }
            case CROSSHAIR -> {
                int controlX = contentLeft + 16, controlW = Math.max(150, contentW / 2 - 30);
                addRenderableWidget(new HudButton(controlX, y, controlW, 20, crosshairButtonText(config.crosshairMode), button -> {
                    config.crosshairMode = (config.crosshairMode + 1) % 4;
                    config.customCrosshair = config.crosshairMode != HudForgeConfig.CROSSHAIR_VANILLA;
                    button.setMessage(crosshairButtonText(config.crosshairMode));
                }));
                y += 24;
                addToggle(controlX, y, controlW, "toggle.crosshair_indicator", config.crosshairIndicator, value -> config.crosshairIndicator = value);
                y += 24;
                addToggle(controlX, y, controlW, "toggle.center_dot", config.crosshairDot, value -> config.crosshairDot = value);
                y += 24;
                addSlider(controlX, y, controlW, "slider.size", config.crosshairSize, 1, 24, value -> config.crosshairSize = Math.round(value));
                y += 24;
                addSlider(controlX, y, controlW, "slider.gap", config.crosshairGap, 0, 12, value -> config.crosshairGap = Math.round(value));
                y += 24;
                addSlider(controlX, y, controlW, "slider.thickness", config.crosshairThickness, 1, 6, value -> config.crosshairThickness = Math.round(value));
                y += 24;
                addToggle(controlX, y, controlW, "toggle.crosshair_outline", config.crosshairOutline, value -> config.crosshairOutline = value);
                y += 24;
                addSlider(controlX, y, controlW, "slider.outline_thickness", config.crosshairOutlineThickness, 1, 4, value -> config.crosshairOutlineThickness = Math.round(value));
                int colorX = colorPickerX(left, panelW);
                addRenderableWidget(new HudButton(colorX, top + 78, 210, 20, tr("color.crosshair"), b -> { colorTarget = ColorTarget.CROSSHAIR; rebuildWidgets(); }));
                addRenderableWidget(new HudButton(colorX, top + 102, 210, 20, tr("color.crosshair_outline"), b -> { colorTarget = ColorTarget.CROSSHAIR_OUTLINE; rebuildWidgets(); }));
            }
            case COLORS -> {
                addRenderableWidget(new HudButton(contentLeft + 10, backY, 120, 20, tr("button.clean_preset"), button -> {
                    applyCleanPreset(config);
                    rebuildWidgets();
                }));
            }
            case EXTRAS -> {
                addToggle(contentLeft + 10, y, contentW - 20, "toggle.auto_respawn", config.autoRespawn, value -> config.autoRespawn = value);
                y += 24;
                addToggle(contentLeft + 10, y, contentW - 20, "toggle.auto_reconnect", config.autoReconnect, value -> config.autoReconnect = value);
                y += 24;
                addSlider(contentLeft + 10, y, contentW - 20, "slider.respawn_delay", config.autoRespawnDelayTicks / 20.0f, 1, 5, value -> config.autoRespawnDelayTicks = Math.round(value * 20));
                y += 24;
                addSlider(contentLeft + 10, y, contentW - 20, "slider.reconnect_delay", config.autoReconnectDelaySeconds, 1, 60, value -> config.autoReconnectDelaySeconds = Math.round(value));
            }
        }

        addRenderableWidget(new HudButton(left + 10, backY, 108, 20, tr("button.back_editor"), button -> {
            settingsOpen = false;
            rebuildWidgets();
        }));
        updateScrollableWidgetVisibility(top + 54, backY - 6, left + 122, backY);
    }

    private void updateScrollableWidgetVisibility(int contentTop, int contentBottom, int sidebarRight, int backY) {
        for (var element : children()) {
            if (element instanceof net.minecraft.client.gui.components.AbstractWidget widget) {
                boolean fixed = widget.getX() < sidebarRight || widget.getY() == backY;
                widget.visible = fixed || widget.getY() >= contentTop && widget.getY() + widget.getHeight() <= contentBottom;
                widget.active = widget.visible;
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (settingsOpen && settingsMaxScroll > 0) {
            int next = clamp(settingsScroll - (int) Math.round(verticalAmount * 24.0), 0, settingsMaxScroll);
            if (next != settingsScroll) {
                settingsScroll = next;
                rebuildWidgets();
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void onClose() {
        HudForgeClient.saveActiveServerProfile();
        HudForgeClient.config.save();
        Minecraft.getInstance().gui.setScreen(parent);
    }

    @Override
    public void removed() {
        HudForgeClient.saveActiveServerProfile();
        HudForgeClient.config.save();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubleClick) {
        if (settingsOpen) {
            if (click.button() == 0 && handleScrollbarClick(click.x(), click.y())) {
                return true;
            }
            if (((page == SettingsPage.SCOREBOARD && activeModule != null) || page == SettingsPage.CROSSHAIR) && click.button() == 0 && handleColorPick(click.x(), click.y())) {
                return true;
            }
            return super.mouseClicked(click, doubleClick);
        }

        HudForgeConfig config = HudForgeClient.config;
        if (click.button() == 0) {
            if (config.scoreboardMode == HudForgeConfig.SCOREBOARD_CUSTOM && hasServerScoreboard() && inside(click.x(), click.y(), config.scoreboardX, config.scoreboardY, scoreboardWidth(config), scoreboardHeight(config))) {
                if (inside(click.x(), click.y(), config.scoreboardX + scoreboardWidth(config) - 10, config.scoreboardY + scoreboardHeight(config) - 10, 10, 10)) {
                    dragTarget = DragTarget.SCOREBOARD_RESIZE;
                } else {
                    dragTarget = DragTarget.SCOREBOARD_MOVE;
                    dragOffsetX = (int) click.x() - config.scoreboardX;
                    dragOffsetY = (int) click.y() - config.scoreboardY;
                }
                return true;
            }
            if (config.statsEnabled && inside(click.x(), click.y(), config.statsX, config.statsY, statsWidth(config), statsHeight(config))) {
                if (inside(click.x(), click.y(), config.statsX + statsWidth(config) - 10, config.statsY + statsHeight(config) - 10, 10, 10)) {
                    dragTarget = DragTarget.STATS_RESIZE;
                } else {
                    dragTarget = DragTarget.STATS_MOVE;
                    dragOffsetX = (int) click.x() - config.statsX;
                    dragOffsetY = (int) click.y() - config.statsY;
                }
                return true;
            }
            if (config.coordsEnabled && inside(click.x(), click.y(), config.coordsX, config.coordsY, coordsWidth(config), coordsHeight(config))) {
                dragTarget = inside(click.x(), click.y(), config.coordsX + coordsWidth(config) - 10, config.coordsY + coordsHeight(config) - 10, 10, 10) ? DragTarget.COORDS_RESIZE : DragTarget.COORDS_MOVE;
                dragOffsetX = (int) click.x() - config.coordsX;
                dragOffsetY = (int) click.y() - config.coordsY;
                return true;
            }
            if (config.effectsEnabled && inside(click.x(), click.y(), config.effectsX, config.effectsY, effectsWidth(config), effectsHeight(config))) {
                dragTarget = inside(click.x(), click.y(), config.effectsX + effectsWidth(config) - 10, config.effectsY + effectsHeight(config) - 10, 10, 10) ? DragTarget.EFFECTS_RESIZE : DragTarget.EFFECTS_MOVE;
                dragOffsetX = (int) click.x() - config.effectsX;
                dragOffsetY = (int) click.y() - config.effectsY;
                return true;
            }
            if (config.equipmentEnabled && config.equipmentMode == 1 && config.equipmentHotbarSeparate) {
                for (int i = 0; i < 4; i++) {
                    int sx = equipmentSlotX(config, i), sy = equipmentSlotY(config, i);
                    if (inside(click.x(), click.y(), sx, sy, 22, 22)) {
                        config.equipmentSlotX[i] = sx; config.equipmentSlotY[i] = sy; draggedEquipmentSlot = i;
                        dragTarget = DragTarget.EQUIPMENT_HOTBAR_SLOT_MOVE; dragOffsetX = (int) click.x() - sx; dragOffsetY = (int) click.y() - sy; return true;
                    }
                }
            }
            if (config.equipmentEnabled && config.equipmentMode == 1 && !config.equipmentHotbarSeparate && inside(click.x(), click.y(), hotbarX(config), hotbarY(config), 82, 22)) {
                if (config.equipmentHotbarX < 0) config.equipmentHotbarX = hotbarX(config);
                if (config.equipmentHotbarY < 0) config.equipmentHotbarY = hotbarY(config);
                dragTarget = DragTarget.EQUIPMENT_HOTBAR_MOVE;
                dragOffsetX = (int) click.x() - config.equipmentHotbarX;
                dragOffsetY = (int) click.y() - config.equipmentHotbarY;
                return true;
            }
            if (config.equipmentEnabled && config.equipmentMode == 0 && inside(click.x(), click.y(), config.equipmentX, config.equipmentY, equipmentWidth(config), equipmentHeight(config))) {
                dragTarget = inside(click.x(), click.y(), config.equipmentX + equipmentWidth(config) - 10, config.equipmentY + equipmentHeight(config) - 10, 10, 10) ? DragTarget.EQUIPMENT_RESIZE : DragTarget.EQUIPMENT_MOVE;
                dragOffsetX = (int) click.x() - config.equipmentX;
                dragOffsetY = (int) click.y() - config.equipmentY;
                return true;
            }
            if (config.cpsEnabled && inside(click.x(), click.y(), config.cpsX, config.cpsY, Math.round(config.cpsWidth * config.cpsScale), Math.round(22 * config.cpsScale))) {
                dragTarget = inside(click.x(), click.y(), config.cpsX + Math.round(config.cpsWidth * config.cpsScale) - 10, config.cpsY + Math.round(22 * config.cpsScale) - 10, 10, 10) ? DragTarget.CPS_RESIZE : DragTarget.CPS_MOVE; dragOffsetX = (int) click.x() - config.cpsX; dragOffsetY = (int) click.y() - config.cpsY; return true;
            }
            if (config.jumpResetEnabled && config.jumpResetMode == 1 && inside(click.x(), click.y(), config.jumpResetX, config.jumpResetY, Math.round(config.jumpResetWidth * config.jumpResetScale), Math.round(24 * config.jumpResetScale))) { dragTarget = inside(click.x(), click.y(), config.jumpResetX + Math.round(config.jumpResetWidth * config.jumpResetScale) - 10, config.jumpResetY + Math.round(24 * config.jumpResetScale) - 10, 10, 10) ? DragTarget.JUMP_RESET_RESIZE : DragTarget.JUMP_RESET_MOVE; dragOffsetX = (int) click.x() - config.jumpResetX; dragOffsetY = (int) click.y() - config.jumpResetY; return true; }
        }
        return super.mouseClicked(click, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double deltaX, double deltaY) {
        if (settingsOpen) {
            if (scrollbarDragging) {
                updateScrollFromMouse(click.y());
                return true;
            }
            if (((page == SettingsPage.SCOREBOARD && activeModule != null) || page == SettingsPage.CROSSHAIR) && handleColorPick(click.x(), click.y())) {
                return true;
            }
            return super.mouseDragged(click, deltaX, deltaY);
        }

        HudForgeConfig config = HudForgeClient.config;
        switch (dragTarget) {
            case SCOREBOARD_MOVE -> {
                config.scoreboardX = clamp((int) click.x() - dragOffsetX, 0, Math.max(0, width - scoreboardWidth(config)));
                config.scoreboardY = clamp((int) click.y() - dragOffsetY, 0, Math.max(0, height - scoreboardHeight(config)));
                return true;
            }
            case STATS_MOVE -> {
                config.statsX = clamp((int) click.x() - dragOffsetX, 0, Math.max(0, width - statsWidth(config)));
                config.statsY = clamp((int) click.y() - dragOffsetY, 0, Math.max(0, height - statsHeight(config)));
                return true;
            }
            case COORDS_MOVE -> {
                config.coordsX = clamp((int) click.x() - dragOffsetX, 0, Math.max(0, width - coordsWidth(config)));
                config.coordsY = clamp((int) click.y() - dragOffsetY, 0, Math.max(0, height - coordsHeight(config)));
                return true;
            }
            case EFFECTS_MOVE -> {
                config.effectsX = clamp((int) click.x() - dragOffsetX, 0, Math.max(0, width - effectsWidth(config)));
                config.effectsY = clamp((int) click.y() - dragOffsetY, 0, Math.max(0, height - effectsHeight(config)));
                return true;
            }
            case EQUIPMENT_MOVE -> {
                config.equipmentX = clamp((int) click.x() - dragOffsetX, 0, Math.max(0, width - equipmentWidth(config)));
                config.equipmentY = clamp((int) click.y() - dragOffsetY, 0, Math.max(0, height - equipmentHeight(config)));
                return true;
            }
            case EQUIPMENT_HOTBAR_MOVE -> {
                config.equipmentHotbarX = clamp((int) click.x() - dragOffsetX, 0, Math.max(0, width - 82));
                config.equipmentHotbarY = clamp((int) click.y() - dragOffsetY, 0, Math.max(0, height - 22));
                return true;
            }
            case EQUIPMENT_HOTBAR_SLOT_MOVE -> {
                if (draggedEquipmentSlot >= 0 && draggedEquipmentSlot < 4) {
                    config.equipmentSlotX[draggedEquipmentSlot] = clamp((int) click.x() - dragOffsetX, 0, Math.max(0, width - 22));
                    config.equipmentSlotY[draggedEquipmentSlot] = clamp((int) click.y() - dragOffsetY, 0, Math.max(0, height - 22));
                }
                return true;
            }
            case CPS_MOVE -> {
                config.cpsX = clamp((int) click.x() - dragOffsetX, 0, Math.max(0, width - Math.round(config.cpsWidth * config.cpsScale)));
                config.cpsY = clamp((int) click.y() - dragOffsetY, 0, Math.max(0, height - Math.round(22 * config.cpsScale))); return true;
            }
            case CPS_RESIZE -> {
                config.cpsWidth = clamp(Math.round(((float) click.x() - config.cpsX) / Math.max(0.5f, config.cpsScale)), 72, 240);
                config.cpsScale = clamp(((float) click.y() - config.cpsY) / 22.0f, 0.5f, 2.5f); return true;
            }
            case JUMP_RESET_MOVE -> { config.jumpResetX = clamp((int) click.x() - dragOffsetX, 0, Math.max(0, width - Math.round(config.jumpResetWidth * config.jumpResetScale))); config.jumpResetY = clamp((int) click.y() - dragOffsetY, 0, Math.max(0, height - Math.round(24 * config.jumpResetScale))); return true; }
            case JUMP_RESET_RESIZE -> { config.jumpResetWidth = clamp(Math.round(((float) click.x() - config.jumpResetX) / Math.max(0.5f, config.jumpResetScale)), 92, 260); config.jumpResetScale = clamp(((float) click.y() - config.jumpResetY) / 24.0f, 0.5f, 2.5f); return true; }
            case SCOREBOARD_RESIZE -> {
                int rawWidth = Math.round(((float) click.x() - config.scoreboardX) / Math.max(0.5f, config.scoreboardScale));
                config.scoreboardWidth = clamp(rawWidth, 90, 320);
                float newScale = ((float) click.y() - config.scoreboardY) / Math.max(42, scoreboardBaseHeight());
                config.scoreboardScale = clamp(newScale, 0.5f, 2.5f);
                return true;
            }
            case STATS_RESIZE -> {
                float newScale = Math.max(((float) click.x() - config.statsX) / 82.0f, ((float) click.y() - config.statsY) / 32.0f);
                config.statsScale = clamp(newScale, 0.5f, 2.5f);
                return true;
            }
            case COORDS_RESIZE -> {
                config.coordsWidth = clamp(Math.round(((float) click.x() - config.coordsX) / Math.max(0.5f, config.coordsScale)), 92, 280);
                config.coordsScale = clamp(((float) click.y() - config.coordsY) / 52.0f, 0.5f, 2.5f);
                return true;
            }
            case EFFECTS_RESIZE -> {
                config.effectsWidth = clamp(Math.round(((float) click.x() - config.effectsX) / Math.max(0.5f, config.effectsScale)), 110, 320);
                config.effectsScale = clamp(((float) click.y() - config.effectsY) / 74.0f, 0.5f, 2.5f);
                return true;
            }
            case EQUIPMENT_RESIZE -> {
                config.equipmentWidth = clamp(Math.round(((float) click.x() - config.equipmentX) / Math.max(0.5f, config.equipmentScale)), 120, 320);
                config.equipmentScale = clamp(((float) click.y() - config.equipmentY) / 96.0f, 0.5f, 2.5f);
                return true;
            }
            default -> {
            }
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        scrollbarDragging = false;
        dragTarget = DragTarget.NONE;
        draggedEquipmentSlot = -1;
        HudForgeClient.saveActiveServerProfile();
        return super.mouseReleased(click);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, settingsOpen ? 0xA0000000 : 0x70000000);
        if (!settingsOpen) {
            renderHudCanvas(context);
        }
        if (settingsOpen) {
            renderSettingsPanel(context, mouseX, mouseY);
        } else {
            context.text(font, tr("editor.help"), 10, 10, 0xFFB8C0D4);
        }
        super.extractRenderState(context, mouseX, mouseY, delta);
    }

    private void renderHudCanvas(GuiGraphicsExtractor context) {
        HudForgeConfig config = HudForgeClient.config;
        if (config.scoreboardMode == HudForgeConfig.SCOREBOARD_CUSTOM && hasServerScoreboard()) {
            renderScoreboardPreview(context, config);
        }
        if (config.statsEnabled) {
            renderStatsPreview(context, config);
        }
        if (config.coordsEnabled) {
            renderSimplePreview(context, config.coordsX, config.coordsY, config.coordsScale, config.coordsWidth, 52, config.coordsRadius, config.coordsBackground, config.coordsText, List.of("X: -129", "Y: 68", "Z: -76", "Biome: Plains"));
            renderHandle(context, config.coordsX + coordsWidth(config), config.coordsY + coordsHeight(config));
        }
        if (config.effectsEnabled) {
            renderSimplePreview(context, config.effectsX, config.effectsY, config.effectsScale, config.effectsWidth, 74, config.effectsRadius, config.effectsBackground, config.effectsText, List.of("Haste 1:24", "Speed 0:42", "Strength 2:11"));
            renderHandle(context, config.effectsX + effectsWidth(config), config.effectsY + effectsHeight(config));
        }
        if (config.equipmentEnabled) {
            if (config.equipmentMode == 1) renderEquipmentHotbarPreview(context, config);
            else {
                renderSimplePreview(context, config.equipmentX, config.equipmentY, config.equipmentScale, config.equipmentWidth, 96, config.equipmentRadius, config.equipmentBackground, config.equipmentText, List.of("Tool 1532/1561", "Helmet 363/363", "Chest 528/528", "Legs 495/495"));
                renderHandle(context, config.equipmentX + equipmentWidth(config), config.equipmentY + equipmentHeight(config));
            }
        }
        if (config.cpsEnabled) { renderCpsPreview(context, config); renderHandle(context, config.cpsX + Math.round(config.cpsWidth * config.cpsScale), config.cpsY + Math.round(22 * config.cpsScale)); }
        if (config.jumpResetEnabled && config.jumpResetMode == 1) { renderSimplePreview(context, config.jumpResetX, config.jumpResetY, config.jumpResetScale, config.jumpResetWidth, 24, config.jumpResetRadius, config.jumpResetBackground, config.jumpResetText, List.of(tr("jump_reset.perfect").getString())); renderHandle(context, config.jumpResetX + Math.round(config.jumpResetWidth * config.jumpResetScale), config.jumpResetY + Math.round(24 * config.jumpResetScale)); }
    }

    private void renderScoreboardPreview(GuiGraphicsExtractor context, HudForgeConfig config) {
        Minecraft client = Minecraft.getInstance();
        Objective objective = client.level.getScoreboard().getDisplayObjective(DisplaySlot.SIDEBAR);
        List<PlayerScoreEntry> entries = scoreboardEntries(objective);
        if (objective == null || entries.isEmpty()) {
            return;
        }

        Font tr = font;
        int width = config.scoreboardWidth;
        int titleHeight = 18;
        int rowHeight = 11;
        int height = titleHeight + entries.size() * rowHeight + 6;
        context.pose().pushMatrix();
        context.pose().translate(config.scoreboardX, config.scoreboardY);
        context.pose().scale(config.scoreboardScale, config.scoreboardScale);
        fillSoftRect(context, 0, 0, width, height, config.scoreboardRadius, config.scoreboardBackground);
        context.text(tr, objective.getDisplayName(), Mth.clamp((width - tr.width(objective.getDisplayName())) / 2, 8, width - 8), 5, config.scoreboardTitle);
        int y = titleHeight;
        for (PlayerScoreEntry entry : entries) {
            Component line = entry.display() == null ? PlayerTeam.formatNameForTeam(client.level.getScoreboard().getPlayersTeam(entry.owner()), Component.literal(entry.owner())) : entry.display();
            if (config.scoreboardServerColors) {
                context.text(tr, line, 9, y, config.scoreboardText);
            } else {
                context.text(tr, trimToWidth(line.getString(), width - 18), 9, y, config.scoreboardText);
            }
            y += rowHeight;
        }
        context.pose().popMatrix();
        renderHandle(context, config.scoreboardX + scoreboardWidth(config), config.scoreboardY + scoreboardHeight(config));
    }

    private void renderStatsPreview(GuiGraphicsExtractor context, HudForgeConfig config) {
        context.pose().pushMatrix();
        context.pose().translate(config.statsX, config.statsY);
        context.pose().scale(config.statsScale, config.statsScale);
        fillSoftRect(context, 0, 0, 82, 32, config.statsRadius, config.statsBackground);
        context.text(font, Minecraft.getInstance().getFps() + " FPS", 8, 5, config.statsText);
        context.text(font, currentPing() + " ms", 8, 18, config.statsText);
        context.pose().popMatrix();
        renderHandle(context, config.statsX + statsWidth(config), config.statsY + statsHeight(config));
    }

    private void renderCpsPreview(GuiGraphicsExtractor context, HudForgeConfig config) {
        context.pose().pushMatrix(); context.pose().translate(config.cpsX, config.cpsY); context.pose().scale(config.cpsScale, config.cpsScale);
        fillSoftRect(context, 0, 0, config.cpsWidth, 22, config.cpsRadius, config.cpsBackground);
        String preview = "L 8   •   R 4";
        context.text(font, preview, (config.cpsWidth - font.width(preview)) / 2, 7, config.cpsText); context.pose().popMatrix();
    }

    private void renderEquipmentHotbarPreview(GuiGraphicsExtractor context, HudForgeConfig config) {
        int x = hotbarX(config), y = hotbarY(config);
        if (config.equipmentHotbarBackground && !config.equipmentHotbarSeparate) fillSoftRect(context, x, y, 82, 22, 4, 0xB0101218);
        for (int i = 0; i < 4; i++) {
            int baseX = config.equipmentHotbarSeparate ? equipmentSlotX(config, i) : x + i * 20;
            int baseY = config.equipmentHotbarSeparate ? equipmentSlotY(config, i) : y;
            if (config.equipmentHotbarBackground && config.equipmentHotbarSeparate) drawIndependentSlot(context, baseX, baseY);
            int slotX = baseX + 4;
            context.fill(slotX, baseY + 3, slotX + 16, baseY + 17, 0x332A3142);
            int bar = 13 - i * 2;
            if (i > 0) { context.fill(slotX + 1, baseY + 18, slotX + 14, baseY + 20, 0xFF000000); context.fill(slotX + 1, baseY + 18, slotX + 1 + bar, baseY + 19, i < 3 ? 0xFF65D94F : 0xFFFFAA33); }
        }
    }

    private static void drawIndependentSlot(GuiGraphicsExtractor context, int x, int y) { context.fill(x, y, x + 22, y + 22, 0xFF080808); context.fill(x + 1, y + 1, x + 21, y + 21, 0xFFB0B0B0); context.fill(x + 2, y + 2, x + 20, y + 20, 0xFF5A5A5A); context.fill(x + 3, y + 3, x + 19, y + 19, 0xCC151515); }

    private int hotbarX(HudForgeConfig config) {
        if (config.equipmentHotbarX >= 0) return clamp(config.equipmentHotbarX, 0, Math.max(0, width - 82));
        boolean offhandOnLeft = Minecraft.getInstance().player == null || Minecraft.getInstance().player.getMainArm() == net.minecraft.world.entity.HumanoidArm.RIGHT;
        return Math.max(0, width / 2 - 91 - 88 - (offhandOnLeft ? 29 : 0));
    }

    private int hotbarY(HudForgeConfig config) {
        return config.equipmentHotbarY < 0 ? Math.max(0, height - 22) : clamp(config.equipmentHotbarY, 0, Math.max(0, height - 22));
    }
    private int equipmentSlotX(HudForgeConfig c, int i) { return c.equipmentSlotX[i] < 0 ? hotbarX(c) + i * 20 : clamp(c.equipmentSlotX[i], 0, Math.max(0, width - 22)); }
    private int equipmentSlotY(HudForgeConfig c, int i) { return c.equipmentSlotY[i] < 0 ? hotbarY(c) : clamp(c.equipmentSlotY[i], 0, Math.max(0, height - 22)); }

    private void renderSimplePreview(GuiGraphicsExtractor context, int x, int y, float scale, int panelW, int panelH, int radius, int background, int textColor, List<String> lines) {
        context.pose().pushMatrix();
        context.pose().translate(x, y);
        context.pose().scale(scale, scale);
        fillSoftRect(context, 0, 0, panelW, panelH, radius, background);
        int lineY = 6;
        for (String line : lines) {
            context.text(font, line, 8, lineY, textColor);
            lineY += 11;
        }
        context.pose().popMatrix();
    }

    private void renderSettingsPanel(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        int panelW = settingsPanelWidth();
        int panelH = settingsPanelHeight();
        int left = (width - panelW) / 2;
        int top = (height - panelH) / 2;
        int contentLeft = left + 132;
        int contentW = panelW - 142;
        context.fill(left, top, left + panelW, top + panelH, PANEL);
        context.fill(left + 124, top + 1, left + 125, top + panelH - 1, 0x55384964);
        fillSoftRect(context, contentLeft, top + 48, contentW, panelH - 86, 8, 0xB8121A2B);
        context.fill(left, top, left + panelW, top + 1, BORDER);
        context.fill(left, top + panelH - 1, left + panelW, top + panelH, BORDER);
        context.fill(left, top, left + 1, top + panelH, BORDER);
        context.fill(left + panelW - 1, top, left + panelW, top + panelH, BORDER);
        fillSoftRect(context, left + 14, top + 11, 26, 26, 7, 0xFF6878ED);
        context.text(font, Component.literal("HF"), left + 21, top + 20, 0xFFFFFFFF);
        context.text(font, Component.literal("HudFabric"), left + 48, top + 20, WHITE);
        context.text(font, tr(page.titleKey), contentLeft + 10, top + 18, WHITE);
        context.text(font, Component.literal("Modules & HUD Studio  •  1.0.5"), contentLeft + 10, top + 31, 0xFF8E9AB6);
        if (page == SettingsPage.SCOREBOARD) {
            if (activeModule == null) {
                context.text(font, tr("modules.hint"), contentLeft + 12, top + 57, 0xFF9DA8C3);
            } else {
                int half = contentW / 2;
                fillSoftRect(context, contentLeft + 6, top + 52, half - 12, panelH - 96, 7, 0x801A2438);
                fillSoftRect(context, contentLeft + half, top + 52, half - 6, panelH - 96, 7, 0x801A2438);
                context.text(font, tr(activeModule.labelKey), contentLeft + 16, top + 57, 0xFF8A9BFF);
                context.text(font, tr("modules.appearance"), contentLeft + half + 10, top + 57, 0xFF8A9BFF);
                int pickerX = colorPickerX(left, panelW);
                renderColorPicker(context, pickerX, top + 174, contentW / 2 - 34, colorTarget.get(HudForgeClient.config));
                int swatchY = top + 90;
                for (ColorTarget target : activeModule.colors) {
                    fillSoftRect(context, pickerX + 188, swatchY, 16, 12, 3, target.get(HudForgeClient.config));
                    swatchY += 24;
                }
            }
        }
        if (page == SettingsPage.COLORS) {
            renderColorTargets(context, contentLeft + 10, top + 58);
            int pickerX = contentLeft + 222;
            fillSoftRect(context, pickerX - 8, top + 54, contentW - 226, 224, 8, 0xA01A2438);
            renderColorPicker(context, pickerX, top + 66, contentW - 238, colorTarget.get(HudForgeClient.config));
        }
        if (page == SettingsPage.CROSSHAIR) {
            int half = contentW / 2;
            fillSoftRect(context, contentLeft + 6, top + 52, half - 12, panelH - 96, 7, 0x801A2438);
            fillSoftRect(context, contentLeft + half, top + 52, half - 6, panelH - 96, 7, 0x801A2438);
            context.text(font, tr("title.crosshair"), contentLeft + 16, top + 57, 0xFF8A9BFF);
            context.text(font, tr("modules.appearance"), contentLeft + half + 10, top + 57, 0xFF8A9BFF);
            int pickerX = colorPickerX(left, panelW);
            renderColorPicker(context, pickerX, top + 150, contentW / 2 - 34, colorTarget.get(HudForgeClient.config));
            fillSoftRect(context, pickerX + 188, top + 82, 16, 12, 3, HudForgeClient.config.crosshairColor);
            fillSoftRect(context, pickerX + 188, top + 106, 16, 12, 3, HudForgeClient.config.crosshairOutlineColor);
        }
        if (settingsMaxScroll > 0) {
            int trackTop = scrollbarTrackTop();
            int trackBottom = scrollbarTrackBottom();
            int thumbH = scrollbarThumbHeight();
            int thumbY = scrollbarThumbY();
            fillSoftRect(context, left + panelW - 6, trackTop, 3, trackBottom - trackTop, 1, 0x55384964);
            fillSoftRect(context, left + panelW - 7, thumbY, 5, thumbH, 2, BLUE);
        }
    }

    private boolean handleScrollbarClick(double mouseX, double mouseY) {
        if (settingsMaxScroll <= 0) return false;
        int x = (width + settingsPanelWidth()) / 2 - 12;
        int trackTop = scrollbarTrackTop();
        int trackBottom = scrollbarTrackBottom();
        if (!inside(mouseX, mouseY, x, trackTop, 12, trackBottom - trackTop)) return false;
        int thumbY = scrollbarThumbY();
        int thumbH = scrollbarThumbHeight();
        scrollbarDragOffset = mouseY >= thumbY && mouseY < thumbY + thumbH ? (int) mouseY - thumbY : thumbH / 2;
        scrollbarDragging = true;
        updateScrollFromMouse(mouseY);
        return true;
    }

    private void updateScrollFromMouse(double mouseY) {
        int travel = scrollbarTrackBottom() - scrollbarTrackTop() - scrollbarThumbHeight();
        int next = travel <= 0 ? 0 : Math.round(((float) mouseY - scrollbarDragOffset - scrollbarTrackTop()) * settingsMaxScroll / travel);
        next = clamp(next, 0, settingsMaxScroll);
        if (next != settingsScroll) {
            settingsScroll = next;
            rebuildWidgets();
        }
    }

    private int scrollbarTrackTop() { return (height - settingsPanelHeight()) / 2 + 56; }
    private int scrollbarTrackBottom() { return (height + settingsPanelHeight()) / 2 - 38; }
    private int scrollbarThumbHeight() {
        int track = scrollbarTrackBottom() - scrollbarTrackTop();
        return Math.max(24, Math.round(track * track / (float) (track + settingsMaxScroll)));
    }
    private int scrollbarThumbY() {
        int travel = scrollbarTrackBottom() - scrollbarTrackTop() - scrollbarThumbHeight();
        return scrollbarTrackTop() + Math.round(travel * (settingsScroll / (float) settingsMaxScroll));
    }

    private void renderColorPicker(GuiGraphicsExtractor context, int x, int y, int width, int color) {
        int square = colorPickerSquare(settingsPanelWidth());
        float hue = rgbToHue(color);
        for (int xx = 0; xx < square; xx += 2) {
            for (int yy = 0; yy < square; yy += 2) {
                float sat = xx / (float) (square - 1);
                float val = 1.0f - yy / (float) (square - 1);
                context.fill(x + xx, y + yy, x + xx + 2, y + yy + 2, hsvToRgb(hue, sat, val, alpha(color)));
            }
        }
        int hueX = x + square + 10;
        for (int yy = 0; yy < square; yy += 2) {
            context.fill(hueX, y + yy, hueX + 12, y + yy + 2, hsvToRgb(yy / (float) square, 1.0f, 1.0f, 255));
        }
        int alphaY = y + square + 8;
        for (int xx = 0; xx < square; xx += 2) {
            context.fill(x + xx, alphaY, x + xx + 2, alphaY + 10, withAlpha(color, Math.round(xx / (float) square * 255)));
        }
        int previewX = x + square + 28;
        fillSoftRect(context, previewX, y, 26, 26, 4, color);
        context.text(font, tr(colorTarget.labelKey), previewX, y + 34, WHITE);
        int inputY = alphaY + 18;
        drawNumberBox(context, x, inputY, "R", (color >> 16) & 255);
        drawNumberBox(context, x + 54, inputY, "G", (color >> 8) & 255);
        drawNumberBox(context, x + 108, inputY, "B", color & 255);
        drawNumberBox(context, x + 162, inputY, "A", alpha(color));
    }

    private int colorTargetColumns(int panelW) {
        return 1;
    }

    private int colorTargetWidth(int panelW) {
        return 194;
    }

    private int colorPickerX(int left, int panelW) {
        return left + 132 + (panelW - 142) / 2 + 10;
    }

    private int colorPickerSquare(int panelW) {
        int contentW = panelW - 142;
        return Math.min(132, Math.max(72, Math.min(contentW - 422, settingsPanelHeight() - 250)));
    }

    private boolean handleColorPick(double mouseX, double mouseY) {
        int panelW = settingsPanelWidth();
        int left = (width - panelW) / 2;
        int top = (height - settingsPanelHeight()) / 2;
        int x = colorPickerX(left, panelW);
        int y = top + (page == SettingsPage.CROSSHAIR ? 150 : 174);
        int square = colorPickerSquare(panelW);
        HudForgeConfig config = HudForgeClient.config;
        int current = colorTarget.get(config);
        float hue = rgbToHue(current);

        if (inside(mouseX, mouseY, x, y, square, square)) {
            float sat = clamp((float) ((mouseX - x) / square), 0.0f, 1.0f);
            float val = 1.0f - clamp((float) ((mouseY - y) / square), 0.0f, 1.0f);
            colorTarget.set(config, hsvToRgb(hue, sat, val, alpha(current)));
            return true;
        }
        if (inside(mouseX, mouseY, x + square + 10, y, 12, square)) {
            hue = clamp((float) ((mouseY - y) / square), 0.0f, 1.0f);
            float[] sv = rgbToSatVal(current);
            colorTarget.set(config, hsvToRgb(hue, sv[0], sv[1], alpha(current)));
            return true;
        }
        if (inside(mouseX, mouseY, x, y + square + 8, square, 10)) {
            int alpha = Math.round(clamp((float) ((mouseX - x) / square), 0.0f, 1.0f) * 255);
            colorTarget.set(config, withAlpha(current, alpha));
            return true;
        }
        return false;
    }

    private boolean handleColorTargetClick(double mouseX, double mouseY) {
        int panelW = settingsPanelWidth();
        int left = (width - panelW) / 2;
        int top = (height - settingsPanelHeight()) / 2;
        int x = left + 142;
        int y = top + 58;
        int columns = colorTargetColumns(panelW);
        int targetW = colorTargetWidth(panelW);
        int i = 0;
        for (ColorTarget target : ColorTarget.values()) {
            int tx = x + (i % columns) * (targetW + 8);
            int ty = y + (i / columns) * 25;
            if (inside(mouseX, mouseY, tx, ty, targetW, 21)) {
                colorTarget = target;
                return true;
            }
            i++;
        }
        return false;
    }

    private int settingsPanelWidth() {
        return Math.max(1, Math.min(900, width - 24));
    }

    private int settingsPanelHeight() {
        return Math.max(1, Math.min(520, height - 24));
    }

    private boolean hasServerScoreboard() {
        Minecraft client = Minecraft.getInstance();
        return client.level != null && client.level.getScoreboard().getDisplayObjective(DisplaySlot.SIDEBAR) != null;
    }

    private void renderColorTargets(GuiGraphicsExtractor context, int x, int y) {
        int panelW = settingsPanelWidth();
        int columns = colorTargetColumns(panelW);
        int targetW = colorTargetWidth(panelW);
        int i = 0;
        for (ColorTarget target : ColorTarget.values()) {
            int tx = x + (i % columns) * (targetW + 8);
            int ty = y + (i / columns) * 25;
            int color = target.get(HudForgeClient.config);
            if (target == colorTarget) {
                fillSoftRect(context, tx - 2, ty - 2, targetW + 4, 25, 4, 0xFF6D7CFF);
            }
            fillSoftRect(context, tx, ty, targetW, 21, 3, target == colorTarget ? CONTROL_HOVER : CONTROL);
            fillSoftRect(context, tx + 4, ty + 4, 14, 14, 2, color);
            context.text(font, fitLabel(tr(target.labelKey).getString(), targetW - 30), tx + 24, ty + 7, 0xFFCDD3E2);
            i++;
        }
    }

    private String fitLabel(String label, int maxWidth) {
        if (font.width(label) <= maxWidth) {
            return label;
        }
        return font.plainSubstrByWidth(label, Math.max(8, maxWidth - font.width("..."))) + "...";
    }

    private void drawNumberBox(GuiGraphicsExtractor context, int x, int y, String label, int value) {
        context.fill(x, y, x + 48, y + 18, 0xFFE9EDF5);
        context.fill(x, y, x + 48, y + 1, 0xFFB9C0CE);
        context.fill(x, y + 17, x + 48, y + 18, 0xFFB9C0CE);
        String text = String.valueOf(value);
        context.text(font, text, x + 24 - font.width(text) / 2, y + 5, 0xFF111318, false);
        context.text(font, label, x + 24 - font.width(label) / 2, y + 22, WHITE);
    }

    private List<PlayerScoreEntry> scoreboardEntries(Objective objective) {
        if (objective == null || Minecraft.getInstance().level == null) {
            return List.of();
        }
        Scoreboard scoreboard = Minecraft.getInstance().level.getScoreboard();
        return scoreboard.listPlayerScores(objective).stream()
                .filter(entry -> entry.value() != 0 || entry.display() != null)
                .sorted(Comparator.comparingInt(PlayerScoreEntry::value).reversed())
                .limit(15)
                .toList();
    }

    private int scoreboardBaseHeight() {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            return 0;
        }
        Objective objective = client.level.getScoreboard().getDisplayObjective(DisplaySlot.SIDEBAR);
        int rows = scoreboardEntries(objective).size();
        return rows == 0 ? 0 : 18 + rows * 11 + 6;
    }

    private int scoreboardWidth(HudForgeConfig config) {
        return Math.round(config.scoreboardWidth * config.scoreboardScale);
    }

    private int scoreboardHeight(HudForgeConfig config) {
        return Math.round(scoreboardBaseHeight() * config.scoreboardScale);
    }

    private int statsWidth(HudForgeConfig config) {
        return Math.round(82 * config.statsScale);
    }

    private int statsHeight(HudForgeConfig config) {
        return Math.round(32 * config.statsScale);
    }

    private int coordsWidth(HudForgeConfig config) {
        return Math.round(config.coordsWidth * config.coordsScale);
    }

    private int coordsHeight(HudForgeConfig config) {
        return Math.round(52 * config.coordsScale);
    }

    private int effectsWidth(HudForgeConfig config) {
        return Math.round(config.effectsWidth * config.effectsScale);
    }

    private int effectsHeight(HudForgeConfig config) {
        return Math.round(74 * config.effectsScale);
    }

    private int equipmentWidth(HudForgeConfig config) {
        return Math.round(config.equipmentWidth * config.equipmentScale);
    }

    private int equipmentHeight(HudForgeConfig config) {
        return Math.round(96 * config.equipmentScale);
    }

    private int currentPing() {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && client.getConnection() != null && client.getConnection().getPlayerInfo(client.player.getUUID()) != null) {
            return client.getConnection().getPlayerInfo(client.player.getUUID()).getLatency();
        }
        return 0;
    }

    private void initModulePage(HudForgeConfig c, int left, int width, int y, int backY) {
        if (activeModule == null) {
            int gap = 10, cardW = (width - 40) / 3, i = 0;
            for (HudModule module : HudModule.values()) {
                int x = left + 10 + (i % 3) * (cardW + gap), cardY = y + 8 + (i / 3) * 48;
                addRenderableWidget(new HudButton(x, cardY, cardW, 38, tr(module.labelKey), b -> { activeModule = module; if (module.colors.length > 0) colorTarget = module.colors[0]; settingsScroll = 0; rebuildWidgets(); }));
                i++;
            }
            return;
        }
        int x = left + 16, w = Math.max(150, width / 2 - 30), cy = y + 12;
        switch (activeModule) {
            case SCOREBOARD -> {
                addRenderableWidget(new HudButton(x, cy, w, 20, scoreboardButtonText(c.scoreboardMode), b -> { c.scoreboardMode = (c.scoreboardMode + 1) % 3; b.setMessage(scoreboardButtonText(c.scoreboardMode)); }));
                addSlider(x, cy += 24, w, "slider.width", c.scoreboardWidth, 90, 320, v -> c.scoreboardWidth = Math.round(v));
                addSlider(x, cy += 24, w, "slider.scale", c.scoreboardScale, .5f, 2.5f, v -> c.scoreboardScale = v);
                addSlider(x, cy += 24, w, "slider.board_radius", c.scoreboardRadius, 0, 12, v -> c.scoreboardRadius = Math.round(v));
                addToggle(x, cy += 24, w, "toggle.server_colors", c.scoreboardServerColors, v -> c.scoreboardServerColors = v);
            }
            case STATS -> {
                addToggle(x, cy, w, "toggle.stats", c.statsEnabled, v -> c.statsEnabled = v);
                addSlider(x, cy += 24, w, "slider.stats_scale", c.statsScale, .5f, 2.5f, v -> c.statsScale = v);
                addSlider(x, cy += 24, w, "slider.stats_radius", c.statsRadius, 0, 12, v -> c.statsRadius = Math.round(v));
            }
            case COORDS -> {
                addToggle(x, cy, w, "toggle.coords", c.coordsEnabled, v -> c.coordsEnabled = v);
                addSlider(x, cy += 24, w, "slider.coords_scale", c.coordsScale, .5f, 2.5f, v -> c.coordsScale = v);
                addSlider(x, cy += 24, w, "slider.coords_width", c.coordsWidth, 92, 280, v -> c.coordsWidth = Math.round(v));
                addSlider(x, cy += 24, w, "slider.coords_radius", c.coordsRadius, 0, 12, v -> c.coordsRadius = Math.round(v));
            }
            case EFFECTS -> {
                addToggle(x, cy, w, "toggle.effects", c.effectsEnabled, v -> c.effectsEnabled = v);
                addSlider(x, cy += 24, w, "slider.effects_scale", c.effectsScale, .5f, 2.5f, v -> c.effectsScale = v);
                addSlider(x, cy += 24, w, "slider.effects_width", c.effectsWidth, 110, 320, v -> c.effectsWidth = Math.round(v));
                addSlider(x, cy += 24, w, "slider.effects_radius", c.effectsRadius, 0, 12, v -> c.effectsRadius = Math.round(v));
                addToggle(x, cy += 24, w, "toggle.hide_vanilla_effects", c.hideVanillaEffects, v -> c.hideVanillaEffects = v);
            }
            case EQUIPMENT -> {
                addToggle(x, cy, w, "toggle.equipment", c.equipmentEnabled, v -> c.equipmentEnabled = v);
                addRenderableWidget(new HudButton(x, cy += 24, w, 20, equipmentModeText(c.equipmentMode), b -> { c.equipmentMode = (c.equipmentMode + 1) % 2; rebuildWidgets(); }));
                if (c.equipmentMode == 1) {
                    addToggle(x, cy += 24, w, "toggle.equipment_hotbar_background", c.equipmentHotbarBackground, v -> c.equipmentHotbarBackground = v);
                    addToggle(x, cy += 24, w, "toggle.equipment_hotbar_separate", c.equipmentHotbarSeparate, v -> { c.equipmentHotbarSeparate = v; if (!v) { c.equipmentSlotX = new int[]{-1,-1,-1,-1}; c.equipmentSlotY = new int[]{-1,-1,-1,-1}; } });
                    addRenderableWidget(new HudButton(x, cy += 24, w, 20, equipmentDisplayText(c.equipmentDurabilityDisplay), b -> { c.equipmentDurabilityDisplay = (c.equipmentDurabilityDisplay + 1) % 3; b.setMessage(equipmentDisplayText(c.equipmentDurabilityDisplay)); }));
                } else {
                    addSlider(x, cy += 24, w, "slider.equipment_scale", c.equipmentScale, .5f, 2.5f, v -> c.equipmentScale = v);
                    addSlider(x, cy += 24, w, "slider.equipment_width", c.equipmentWidth, 120, 320, v -> c.equipmentWidth = Math.round(v));
                    addSlider(x, cy += 24, w, "slider.equipment_radius", c.equipmentRadius, 0, 12, v -> c.equipmentRadius = Math.round(v));
                }
                addToggle(x, cy += 24, w, "toggle.equipment_warning", c.equipmentWarning, v -> c.equipmentWarning = v);
                addSlider(x, cy += 24, w, "slider.equipment_warning", c.equipmentWarningPercent, 1, 50, v -> c.equipmentWarningPercent = Math.round(v));
            }
            case CPS -> {
                addToggle(x, cy, w, "toggle.cps", c.cpsEnabled, v -> c.cpsEnabled = v);
                addSlider(x, cy += 24, w, "slider.cps_scale", c.cpsScale, .5f, 2.5f, v -> c.cpsScale = v);
                addSlider(x, cy += 24, w, "slider.cps_width", c.cpsWidth, 72, 240, v -> c.cpsWidth = Math.round(v));
                addSlider(x, cy += 24, w, "slider.cps_radius", c.cpsRadius, 0, 12, v -> c.cpsRadius = Math.round(v));
            }
            case JUMP_RESET -> {
                addToggle(x, cy, w, "toggle.jump_reset", c.jumpResetEnabled, v -> c.jumpResetEnabled = v);
                addRenderableWidget(new HudButton(x, cy += 24, w, 20, tr(c.jumpResetMode == 0 ? "jump_reset.mode.target" : "jump_reset.mode.panel"), b -> { c.jumpResetMode = (c.jumpResetMode + 1) % 2; rebuildWidgets(); }));
                addSlider(x, cy += 24, w, "slider.scale", c.jumpResetScale, 0.5f, 2.5f, v -> c.jumpResetScale = v);
                if (c.jumpResetMode == 1) { addSlider(x, cy += 24, w, "slider.width", c.jumpResetWidth, 92, 260, v -> c.jumpResetWidth = Math.round(v)); addSlider(x, cy += 24, w, "slider.board_radius", c.jumpResetRadius, 0, 12, v -> c.jumpResetRadius = Math.round(v)); }
            }
            case BLOCK_OUTLINE -> addToggle(x, cy, w, "toggle.block_outline", c.customBlockOutline, v -> c.customBlockOutline = v);
        }
        int colorX = colorPickerX((this.width - settingsPanelWidth()) / 2, settingsPanelWidth()), colorY = y + 12;
        for (ColorTarget target : activeModule.colors) {
            addRenderableWidget(new HudButton(colorX, colorY, 210, 20, tr(target.labelKey), b -> { colorTarget = target; rebuildWidgets(); }));
            colorY += 24;
        }
        addRenderableWidget(new HudButton(left + 10, backY, 120, 20, tr("button.modules_back"), b -> { activeModule = null; rebuildWidgets(); }));
    }

    private void addToggle(int x, int y, int width, String key, boolean value, Consumer<Boolean> setter) {
        addRenderableWidget(new HudButton(x, y, width, 20, toggleText(key, value), button -> {
            boolean next = !button.getMessage().getString().contains(tr("value.on").getString());
            setter.accept(next);
            button.setMessage(toggleText(key, next));
        }));
    }

    private void addSlider(int x, int y, int width, String key, float value, float min, float max, Consumer<Float> setter) {
        addRenderableWidget(new HudSlider(x, y, width, 20, key, value, min, max, setter));
    }

    private static void playButtonSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
    }

    private static void renderHandle(GuiGraphicsExtractor context, int x, int y) {
        context.fill(x - 8, y - 8, x, y, 0xCCFFFFFF);
        context.fill(x - 6, y - 2, x - 2, y, 0xFF111318);
        context.fill(x - 2, y - 6, x, y - 2, 0xFF111318);
    }

    private static void fillSoftRect(GuiGraphicsExtractor context, int x, int y, int width, int height, int radius, int color) {
        int r = Math.max(0, Math.min(radius, Math.min(width, height) / 2));
        if (r <= 1) {
            context.fill(x, y, x + width, y + height, color);
            return;
        }
        context.fill(x + r, y, x + width - r, y + height, color);
        context.fill(x, y + r, x + width, y + height - r, color);
        for (int i = 0; i < r; i++) {
            int inset = Math.max(0, r - i - 1);
            context.fill(x + inset, y + i, x + width - inset, y + i + 1, color);
            context.fill(x + inset, y + height - i - 1, x + width - inset, y + height - i, color);
        }
    }

    private static void applyCleanPreset(HudForgeConfig config) {
        config.scoreboardBackground = 0xB0101218;
        config.scoreboardAccent = 0xFF66E3FF;
        config.scoreboardText = 0xFFEAF0FF;
        config.scoreboardTitle = 0xFFFFFFFF;
        config.statsBackground = 0x98101218;
        config.statsAccent = 0xFF7C5CFF;
        config.statsText = 0xFFEAF0FF;
        config.cpsBackground = 0x98101218;
        config.cpsText = 0xFFEAF0FF;
        config.crosshairColor = 0xFFEAF0FF;
        config.crosshairOutlineColor = 0xD0000000;
    }

    private String trimToWidth(String value, int maxWidth) {
        if (font.width(value) <= maxWidth) {
            return value;
        }
        return font.plainSubstrByWidth(value, Math.max(8, maxWidth - font.width("..."))) + "...";
    }

    private static String scoreboardModeLabel(int mode) {
        return switch (mode) {
            case HudForgeConfig.SCOREBOARD_VANILLA -> "mode.vanilla";
            case HudForgeConfig.SCOREBOARD_HIDDEN -> "mode.off";
            default -> "mode.custom";
        };
    }

    private static String crosshairModeLabel(int mode) {
        return switch (mode) {
            case HudForgeConfig.CROSSHAIR_VANILLA -> "mode.vanilla";
            case HudForgeConfig.CROSSHAIR_CIRCLE -> "mode.circle";
            case HudForgeConfig.CROSSHAIR_DOT -> "mode.dot";
            default -> "mode.clean_cross";
        };
    }

    private static Component tr(String key, Object... args) {
        return Component.translatable("hudfabric." + key, args);
    }

    private static Component scoreboardButtonText(int mode) {
        return tr("button.scoreboard", tr(scoreboardModeLabel(mode)));
    }

    private static Component crosshairButtonText(int mode) {
        return tr("button.mode", tr(crosshairModeLabel(mode)));
    }

    private static Component equipmentModeText(int mode) { return tr("button.mode", tr(mode == 1 ? "equipment.mode_hotbar" : "equipment.mode_panel")); }
    private static Component equipmentSideText(int side) { return tr("equipment.side", tr(side == 1 ? "value.right" : "value.left")); }
    private static Component equipmentDisplayText(int display) {
        String key = display == 1 ? "equipment.numeric" : display == 2 ? "equipment.percent" : "equipment.bar";
        return tr("equipment.display", tr(key));
    }

    private static Component toggleText(String key, boolean value) {
        return tr("button.toggle", tr(key), tr(value ? "value.on" : "value.off"));
    }

    private Component tabText(SettingsPage value) {
        return value == page ? Component.literal("* ").append(tr(value.labelKey)) : tr(value.labelKey);
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private static int alpha(int color) {
        return color >>> 24;
    }

    private static int withAlpha(int color, int alpha) {
        return (clamp(alpha, 0, 255) << 24) | (color & 0x00FFFFFF);
    }

    private static int hsvToRgb(float hue, float sat, float val, int alpha) {
        int rgb = java.awt.Color.HSBtoRGB(hue, sat, val) & 0x00FFFFFF;
        return (clamp(alpha, 0, 255) << 24) | rgb;
    }

    private static float rgbToHue(int color) {
        float[] hsb = java.awt.Color.RGBtoHSB((color >> 16) & 255, (color >> 8) & 255, color & 255, null);
        return hsb[0];
    }

    private static float[] rgbToSatVal(int color) {
        float[] hsb = java.awt.Color.RGBtoHSB((color >> 16) & 255, (color >> 8) & 255, color & 255, null);
        return new float[]{hsb[1], hsb[2]};
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private enum SettingsPage {
        SCOREBOARD("tab.hud", "title.hud"),
        COLORS("tab.colors", "title.colors"),
        CROSSHAIR("tab.crosshair", "title.crosshair"),
        EXTRAS("tab.extras", "title.extras");

        private final String labelKey;
        private final String titleKey;

        SettingsPage(String labelKey, String titleKey) {
            this.labelKey = labelKey;
            this.titleKey = titleKey;
        }
    }

    private enum HudModule {
        SCOREBOARD("module.scoreboard", ColorTarget.BOARD_BG, ColorTarget.BOARD_TITLE, ColorTarget.BOARD_TEXT),
        STATS("module.stats", ColorTarget.STATS_BG, ColorTarget.STATS_TEXT),
        COORDS("module.coords", ColorTarget.COORDS_BG, ColorTarget.COORDS_TEXT),
        EFFECTS("module.effects", ColorTarget.EFFECTS_BG, ColorTarget.EFFECTS_TEXT),
        EQUIPMENT("module.equipment", ColorTarget.EQUIPMENT_BG, ColorTarget.EQUIPMENT_TEXT),
        CPS("module.cps", ColorTarget.CPS_BG, ColorTarget.CPS_TEXT),
        JUMP_RESET("module.jump_reset", ColorTarget.JUMP_RESET_BG, ColorTarget.JUMP_RESET_TEXT),
        BLOCK_OUTLINE("module.block_outline", ColorTarget.BLOCK_OUTLINE);
        private final String labelKey;
        private final ColorTarget[] colors;
        HudModule(String labelKey, ColorTarget... colors) { this.labelKey = labelKey; this.colors = colors; }
    }

    private enum DragTarget {
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
        EQUIPMENT_RESIZE,
        EQUIPMENT_HOTBAR_MOVE,
        EQUIPMENT_HOTBAR_SLOT_MOVE,
        CPS_MOVE,
        CPS_RESIZE,
        JUMP_RESET_MOVE,
        JUMP_RESET_RESIZE
    }

    private enum ColorTarget {
        BOARD_BG("color.scoreboard_background", "BB") {
            int get(HudForgeConfig c) { return c.scoreboardBackground; }
            void set(HudForgeConfig c, int v) { c.scoreboardBackground = v; }
        },
        BOARD_TITLE("color.scoreboard_title", "TT") {
            int get(HudForgeConfig c) { return c.scoreboardTitle; }
            void set(HudForgeConfig c, int v) { c.scoreboardTitle = v; }
        },
        BOARD_TEXT("color.scoreboard_text", "BT") {
            int get(HudForgeConfig c) { return c.scoreboardText; }
            void set(HudForgeConfig c, int v) { c.scoreboardText = v; }
        },
        STATS_BG("color.stats_background", "SB") {
            int get(HudForgeConfig c) { return c.statsBackground; }
            void set(HudForgeConfig c, int v) { c.statsBackground = v; }
        },
        STATS_TEXT("color.stats_text", "ST") {
            int get(HudForgeConfig c) { return c.statsText; }
            void set(HudForgeConfig c, int v) { c.statsText = v; }
        },
        COORDS_BG("color.coords_background", "CB") {
            int get(HudForgeConfig c) { return c.coordsBackground; }
            void set(HudForgeConfig c, int v) { c.coordsBackground = v; }
        },
        COORDS_TEXT("color.coords_text", "CT") {
            int get(HudForgeConfig c) { return c.coordsText; }
            void set(HudForgeConfig c, int v) { c.coordsText = v; }
        },
        EFFECTS_BG("color.effects_background", "EB") {
            int get(HudForgeConfig c) { return c.effectsBackground; }
            void set(HudForgeConfig c, int v) { c.effectsBackground = v; }
        },
        EFFECTS_TEXT("color.effects_text", "ET") {
            int get(HudForgeConfig c) { return c.effectsText; }
            void set(HudForgeConfig c, int v) { c.effectsText = v; }
        },
        EQUIPMENT_BG("color.equipment_background", "QB") {
            int get(HudForgeConfig c) { return c.equipmentBackground; }
            void set(HudForgeConfig c, int v) { c.equipmentBackground = v; }
        },
        EQUIPMENT_TEXT("color.equipment_text", "QT") {
            int get(HudForgeConfig c) { return c.equipmentText; }
            void set(HudForgeConfig c, int v) { c.equipmentText = v; }
        },
        CPS_BG("color.cps_background", "CPB") { int get(HudForgeConfig c) { return c.cpsBackground; } void set(HudForgeConfig c, int v) { c.cpsBackground = v; } },
        CPS_TEXT("color.cps_text", "CPT") { int get(HudForgeConfig c) { return c.cpsText; } void set(HudForgeConfig c, int v) { c.cpsText = v; } },
        JUMP_RESET_BG("color.jump_reset_background", "JRB") { int get(HudForgeConfig c) { return c.jumpResetBackground; } void set(HudForgeConfig c, int v) { c.jumpResetBackground = v; } },
        JUMP_RESET_TEXT("color.jump_reset_text", "JRT") { int get(HudForgeConfig c) { return c.jumpResetText; } void set(HudForgeConfig c, int v) { c.jumpResetText = v; } },
        CROSSHAIR("color.crosshair", "CH") {
            int get(HudForgeConfig c) { return c.crosshairColor; }
            void set(HudForgeConfig c, int v) { c.crosshairColor = v; }
        },
        CROSSHAIR_OUTLINE("color.crosshair_outline", "CO") {
            int get(HudForgeConfig c) { return c.crosshairOutlineColor; }
            void set(HudForgeConfig c, int v) { c.crosshairOutlineColor = v; }
        },
        BLOCK_OUTLINE("color.block_outline", "BO") {
            int get(HudForgeConfig c) { return c.blockOutlineColor; }
            void set(HudForgeConfig c, int v) { c.blockOutlineColor = v; }
        };

        private final String labelKey;
        private final String shortLabel;

        ColorTarget(String labelKey, String shortLabel) {
            this.labelKey = labelKey;
            this.shortLabel = shortLabel;
        }

        abstract int get(HudForgeConfig config);

        abstract void set(HudForgeConfig config, int value);
    }

    private static final class HudSlider extends AbstractSliderButton {
        private final String key;
        private final float min;
        private final float max;
        private final Consumer<Float> setter;

        private HudSlider(int x, int y, int width, int height, String key, float value, float min, float max, Consumer<Float> setter) {
            super(x, y, width, height, Component.empty(), Math.max(0.0, Math.min(1.0, (value - min) / (max - min))));
            this.key = key;
            this.min = min;
            this.max = max;
            this.setter = setter;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            float current = min + (float) value * (max - min);
            String rendered = Math.abs(current - Math.round(current)) < 0.01f ? String.valueOf(Math.round(current)) : String.format(java.util.Locale.ROOT, "%.2f", current);
            setMessage(tr("button.slider", tr(key), rendered));
        }

        @Override
        protected void applyValue() {
            setter.accept(min + (float) value * (max - min));
        }

    }

    private static final class HudButton extends AbstractButton {
        private final Consumer<HudButton> onPress;
        private boolean selectedStyle;

        private HudButton(int x, int y, int width, int height, Component message, Consumer<HudButton> onPress) {
            super(x, y, width, height, message);
            this.onPress = onPress;
        }

        private void setSelectedStyle(boolean selectedStyle) {
            this.selectedStyle = selectedStyle;
        }

        @Override
        public void onPress(InputWithModifiers input) {
            onPress.accept(this);
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
            int bg = selectedStyle || isHovered() || isHoveredOrFocused() ? CONTROL_HOVER : CONTROL;
            fillSoftRect(context, getX(), getY(), getWidth(), getHeight(), 3, bg);
            drawBorder(context, getX(), getY(), getWidth(), getHeight(), selectedStyle || isHovered() || isHoveredOrFocused() ? BLUE : BORDER);
            Font tr = Minecraft.getInstance().font;
            Component message = getMessage();
            context.text(tr, message, getX() + getWidth() / 2 - tr.width(message) / 2, getY() + (getHeight() - 8) / 2, WHITE);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput builder) {
            defaultButtonNarrationText(builder);
        }
    }

    private static void drawBorder(GuiGraphicsExtractor context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x, y, x + 1, y + height, color);
        context.fill(x + width - 1, y, x + width, y + height, color);
    }
}
