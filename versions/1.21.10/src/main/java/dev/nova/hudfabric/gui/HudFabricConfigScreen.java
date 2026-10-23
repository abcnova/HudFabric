package dev.nova.hudfabric.gui;

import dev.nova.hudfabric.HudFabricClient;
import dev.nova.hudfabric.gui.HudFabricShareScreen;
import dev.nova.hudfabric.config.HudFabricConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.AbstractInput;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.Identifier;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public final class HudFabricConfigScreen extends Screen {
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

    public HudFabricConfigScreen(Screen parent) {
        this(parent, false);
    }

    public HudFabricConfigScreen(Screen parent, boolean openSettings) {
        super(tr("title"));
        this.parent = parent;
        this.settingsOpen = openSettings;
    }

    @Override
    protected void init() {
        HudFabricConfig config = HudFabricClient.config.clamped();
        if (settingsOpen) {
            initSettings(config);
        } else {
            keepModulesOnCanvas(config);
            addDrawableChild(new HudButton(width - 132, height - 28, 120, 20, tr("button.settings"), button -> {
                settingsOpen = true;
                clearAndInit();
            }));
            addDrawableChild(new HudButton(width - 258, height - 28, 120, 20, tr("button.done"), button -> close()));
        }
    }

    private void keepModulesOnCanvas(HudFabricConfig config) {
        config.statsX = clamp(config.statsX, 0, Math.max(0, width - statsWidth(config)));
        config.statsY = clamp(config.statsY, 0, Math.max(0, height - statsHeight(config)));
        config.coordsX = clamp(config.coordsX, 0, Math.max(0, width - coordsWidth(config)));
        config.coordsY = clamp(config.coordsY, 0, Math.max(0, height - coordsHeight(config)));
        config.effectsX = clamp(config.effectsX, 0, Math.max(0, width - effectsWidth(config)));
        config.effectsY = clamp(config.effectsY, 0, Math.max(0, height - effectsHeight(config)));
        config.equipmentX = clamp(config.equipmentX, 0, Math.max(0, width - equipmentWidth(config)));
        config.equipmentY = clamp(config.equipmentY, 0, Math.max(0, height - equipmentHeight(config)));
        config.cpsX = clamp(config.cpsX, 0, Math.max(0, width - Math.round(config.cpsWidth * config.cpsScale)));
        config.cpsY = clamp(config.cpsY, 0, Math.max(0, height - Math.round(22 * config.cpsScale)));
        config.jumpResetX = clamp(config.jumpResetX, 0, Math.max(0, width - Math.round(config.jumpResetWidth * config.jumpResetScale)));
        config.jumpResetY = clamp(config.jumpResetY, 0, Math.max(0, height - Math.round(24 * config.jumpResetScale)));
        config.inventoryCountersX = clamp(config.inventoryCountersX, 0, Math.max(0, width - Math.round(118 * config.inventoryCountersScale)));
        config.inventoryCountersY = clamp(config.inventoryCountersY, 0, Math.max(0, height - Math.round(24 * config.inventoryCountersScale)));
        config.noteBoardX = clamp(config.noteBoardX, 0, Math.max(0, width - Math.round(210 * config.noteBoardScale)));
        config.noteBoardY = clamp(config.noteBoardY, 0, Math.max(0, height - Math.round(84 * config.noteBoardScale)));
        if (config.equipmentHotbarX >= 0) config.equipmentHotbarX = clamp(config.equipmentHotbarX, 0, Math.max(0, width - 82));
        if (config.equipmentHotbarY >= 0) config.equipmentHotbarY = clamp(config.equipmentHotbarY, 0, Math.max(0, height - 22));
        for (int i = 0; i < 4; i++) {
            if (config.equipmentSlotX[i] >= 0) config.equipmentSlotX[i] = clamp(config.equipmentSlotX[i], 0, Math.max(0, width - 22));
            if (config.equipmentSlotY[i] >= 0) config.equipmentSlotY[i] = clamp(config.equipmentSlotY[i], 0, Math.max(0, height - 22));
        }
        config.scoreboardX = clamp(config.scoreboardX, 0, Math.max(0, width - scoreboardWidth(config)));
        config.scoreboardY = clamp(config.scoreboardY, 0, Math.max(0, height - scoreboardHeight(config)));
    }

    private void initSettings(HudFabricConfig config) {
        int panelW = settingsPanelWidth();
        int panelH = settingsPanelHeight();
        int left = (width - panelW) / 2;
        int top = (height - panelH) / 2;
        int contentLeft = left + 132;
        int contentW = panelW - 142;
        int contentEnd = switch (page) {
            case SCOREBOARD -> activeModule == HudModule.EQUIPMENT ? 294 : activeModule == null ? compactLayout() ? 178 : 220 : 230;
            case CROSSHAIR -> 300;
            case EXTRAS -> 170;
            case COLORS -> 206;
        };
        int contentBottom = panelH - (compactLayout() && (page == SettingsPage.CROSSHAIR
                || page == SettingsPage.SCOREBOARD && activeModule != null) ? 64 : 36);
        settingsMaxScroll = Math.max(0, contentEnd - contentBottom);
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
                clearAndInit();
            });
            tab.setSelectedStyle(page == value);
            addDrawableChild(tab);
        }

        switch (page) {
            case SCOREBOARD -> {
                initModulePage(config, contentLeft, contentW, y, backY);
            }
            case CROSSHAIR -> {
                addResetButton(contentLeft, contentW, backY, "CROSSHAIR");
                int controlX = contentLeft + 16;
                int controlW = compactLayout() ? contentW - 32 : Math.max(150, contentW / 2 - 30);
                addDrawableChild(new HudButton(controlX, y, controlW, 20, crosshairButtonText(config.crosshairMode), button -> {
                    config.crosshairMode = (config.crosshairMode + 1) % 4;
                    config.customCrosshair = config.crosshairMode != HudFabricConfig.CROSSHAIR_VANILLA;
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
                if (compactLayout()) {
                    addCompactColorButton(contentLeft, backY, ColorTarget.CROSSHAIR);
                } else {
                    int colorX = colorPickerX(left, panelW);
                    addDrawableChild(new HudButton(colorX, top + 78, 210, 20, tr("color.crosshair"), b -> { colorTarget = ColorTarget.CROSSHAIR; clearAndInit(); }));
                    addDrawableChild(new HudButton(colorX, top + 102, 210, 20, tr("color.crosshair_outline"), b -> { colorTarget = ColorTarget.CROSSHAIR_OUTLINE; clearAndInit(); }));
                }
            }
            case COLORS -> {
                if (compactLayout()) {
                    addDrawableChild(new HudButton(contentLeft + 10, top + 56, contentW - 20, 20, tr(colorTarget.labelKey), button -> {
                        ColorTarget[] targets = colorTarget == ColorTarget.CROSSHAIR || colorTarget == ColorTarget.CROSSHAIR_OUTLINE
                                ? new ColorTarget[]{ColorTarget.CROSSHAIR, ColorTarget.CROSSHAIR_OUTLINE}
                                : activeModule == null ? ColorTarget.values() : activeModule.colors;
                        int index = java.util.Arrays.asList(targets).indexOf(colorTarget);
                        colorTarget = targets[(index + 1) % targets.length];
                        button.setMessage(tr(colorTarget.labelKey));
                    }));
                    addDrawableChild(new HudButton(contentLeft + 10, backY, 120, 20, tr("button.back"), button -> {
                        page = colorTarget == ColorTarget.CROSSHAIR || colorTarget == ColorTarget.CROSSHAIR_OUTLINE
                                ? SettingsPage.CROSSHAIR : SettingsPage.SCOREBOARD;
                        clearAndInit();
                    }));
                } else {
                    addDrawableChild(new HudButton(contentLeft + 10, backY, 120, 20, tr("button.clean_preset"), button -> {
                        applyCleanPreset(config);
                        clearAndInit();
                    }));
                         addDrawableChild(new HudButton(contentLeft + 10, y, contentW - 20, 20, tr("hudfabric.share.open"), button -> {
                    HudFabricClient.config.save();
                    MinecraftClient.getInstance().setScreen(new HudFabricShareScreen(this));
                }));
                y += 24;
       }
            }
            case EXTRAS -> {
                addResetButton(contentLeft, contentW, backY, "EXTRAS");
                addToggle(contentLeft + 10, y, contentW - 20, "toggle.auto_respawn", config.autoRespawn, value -> config.autoRespawn = value);
                y += 24;
                addToggle(contentLeft + 10, y, contentW - 20, "toggle.auto_reconnect", config.autoReconnect, value -> config.autoReconnect = value);
                y += 24;
                addSlider(contentLeft + 10, y, contentW - 20, "slider.respawn_delay", config.autoRespawnDelayTicks / 20.0f, 1, 5, value -> config.autoRespawnDelayTicks = Math.round(value * 20));
                y += 24;
                addSlider(contentLeft + 10, y, contentW - 20, "slider.reconnect_delay", config.autoReconnectDelaySeconds, 1, 60, value -> config.autoReconnectDelaySeconds = Math.round(value));
            }
        }

        addDrawableChild(new HudButton(left + 10, backY, 108, 20, tr("button.back_editor"), button -> {
            settingsOpen = false;
            clearAndInit();
        }));
        updateScrollableWidgetVisibility(top + 54, top + contentBottom, left + 122, backY);
    }

    private void updateScrollableWidgetVisibility(int contentTop, int contentBottom, int sidebarRight, int backY) {
        for (var element : children()) {
            if (element instanceof net.minecraft.client.gui.widget.ClickableWidget widget) {
                boolean fixed = widget.getX() < sidebarRight || widget.getY() == backY || widget.getY() == backY - 28
                        || widget instanceof HudButton button && button.resetStyle;
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
                clearAndInit();
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void close() {
        HudFabricClient.saveActiveServerProfile();
        HudFabricClient.config.save();
        MinecraftClient.getInstance().setScreen(parent);
    }

    @Override
    public void removed() {
        HudFabricClient.saveActiveServerProfile();
        HudFabricClient.config.save();
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubleClick) {
        if (settingsOpen) {
            if (click.button() == 0 && handleScrollbarClick(click.x(), click.y())) {
                return true;
            }
            if (((page == SettingsPage.SCOREBOARD && activeModule != null) || page == SettingsPage.CROSSHAIR || page == SettingsPage.COLORS) && click.button() == 0 && handleColorPick(click.x(), click.y())) {
                return true;
            }
            return super.mouseClicked(click, doubleClick);
        }

        HudFabricConfig config = HudFabricClient.config;
        if (click.button() == 0) {
            if (config.scoreboardMode == HudFabricConfig.SCOREBOARD_CUSTOM && hasServerScoreboard() && inside(click.x(), click.y(), config.scoreboardX, config.scoreboardY, scoreboardWidth(config), scoreboardHeight(config))) {
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
                        config.equipmentSlotX[i] = sx; config.equipmentSlotY[i] = sy;
                        draggedEquipmentSlot = i;
                        dragTarget = DragTarget.EQUIPMENT_HOTBAR_SLOT_MOVE;
                        dragOffsetX = (int) click.x() - sx; dragOffsetY = (int) click.y() - sy;
                        return true;
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
                dragTarget = inside(click.x(), click.y(), config.cpsX + Math.round(config.cpsWidth * config.cpsScale) - 10, config.cpsY + Math.round(22 * config.cpsScale) - 10, 10, 10) ? DragTarget.CPS_RESIZE : DragTarget.CPS_MOVE;
                dragOffsetX = (int) click.x() - config.cpsX;
                dragOffsetY = (int) click.y() - config.cpsY;
                return true;
            }
            if (config.jumpResetEnabled && config.jumpResetMode == 1 && inside(click.x(), click.y(), config.jumpResetX, config.jumpResetY, Math.round(config.jumpResetWidth * config.jumpResetScale), Math.round(24 * config.jumpResetScale))) {
                dragTarget = inside(click.x(), click.y(), config.jumpResetX + Math.round(config.jumpResetWidth * config.jumpResetScale) - 10, config.jumpResetY + Math.round(24 * config.jumpResetScale) - 10, 10, 10) ? DragTarget.JUMP_RESET_RESIZE : DragTarget.JUMP_RESET_MOVE;
                dragOffsetX = (int) click.x() - config.jumpResetX; dragOffsetY = (int) click.y() - config.jumpResetY; return true;
            }
            if (config.inventoryCountersEnabled && inside(click.x(), click.y(), config.inventoryCountersX, config.inventoryCountersY, Math.round(118 * config.inventoryCountersScale), Math.round(24 * config.inventoryCountersScale))) {
                dragTarget = DragTarget.INVENTORY_COUNTERS_MOVE; dragOffsetX = (int) click.x() - config.inventoryCountersX; dragOffsetY = (int) click.y() - config.inventoryCountersY; return true;
            }
            if (config.noteBoardEnabled && inside(click.x(), click.y(), config.noteBoardX, config.noteBoardY, Math.round(210 * config.noteBoardScale), Math.round(84 * config.noteBoardScale))) {
                dragTarget = DragTarget.NOTE_BOARD_MOVE; dragOffsetX = (int) click.x() - config.noteBoardX; dragOffsetY = (int) click.y() - config.noteBoardY; return true;
            }
        }
        return super.mouseClicked(click, doubleClick);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        if (settingsOpen) {
            if (scrollbarDragging) {
                updateScrollFromMouse(click.y());
                return true;
            }
            if (((page == SettingsPage.SCOREBOARD && activeModule != null) || page == SettingsPage.CROSSHAIR || page == SettingsPage.COLORS) && handleColorPick(click.x(), click.y())) {
                return true;
            }
            return super.mouseDragged(click, deltaX, deltaY);
        }

        HudFabricConfig config = HudFabricClient.config;
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
            case INVENTORY_COUNTERS_MOVE -> {
                config.inventoryCountersX = clamp((int) click.x() - dragOffsetX, 0, Math.max(0, width - Math.round(118 * config.inventoryCountersScale)));
                config.inventoryCountersY = clamp((int) click.y() - dragOffsetY, 0, Math.max(0, height - Math.round(24 * config.inventoryCountersScale)));
                return true;
            }
            case NOTE_BOARD_MOVE -> {
                config.noteBoardX = clamp((int) click.x() - dragOffsetX, 0, Math.max(0, width - Math.round(210 * config.noteBoardScale)));
                config.noteBoardY = clamp((int) click.y() - dragOffsetY, 0, Math.max(0, height - Math.round(84 * config.noteBoardScale)));
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
                config.cpsY = clamp((int) click.y() - dragOffsetY, 0, Math.max(0, height - Math.round(22 * config.cpsScale)));
                return true;
            }
            case CPS_RESIZE -> {
                config.cpsWidth = clamp(Math.round(((float) click.x() - config.cpsX) / Math.max(0.5f, config.cpsScale)), 72, 240);
                config.cpsScale = clamp(((float) click.y() - config.cpsY) / 22.0f, 0.5f, 2.5f);
                return true;
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
    public boolean mouseReleased(Click click) {
        scrollbarDragging = false;
        dragTarget = DragTarget.NONE;
        draggedEquipmentSlot = -1;
        HudFabricClient.saveActiveServerProfile();
        return super.mouseReleased(click);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, settingsOpen ? 0xA0000000 : 0x70000000);
        if (!settingsOpen) {
            renderHudCanvas(context);
        }
        if (settingsOpen) {
            renderSettingsPanel(context, mouseX, mouseY);
        } else {
            context.drawTextWithShadow(textRenderer, tr("editor.help"), 10, 10, 0xFFB8C0D4);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private void renderHudCanvas(DrawContext context) {
        HudFabricConfig config = HudFabricClient.config;
        if (config.scoreboardMode == HudFabricConfig.SCOREBOARD_CUSTOM && hasServerScoreboard()) {
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
        if (config.cpsEnabled) {
            renderCpsPreview(context, config);
            renderHandle(context, config.cpsX + Math.round(config.cpsWidth * config.cpsScale), config.cpsY + Math.round(22 * config.cpsScale));
        }
        if (config.jumpResetEnabled && config.jumpResetMode == 1) { renderSimplePreview(context, config.jumpResetX, config.jumpResetY, config.jumpResetScale, config.jumpResetWidth, 24, config.jumpResetRadius, config.jumpResetBackground, config.jumpResetText, List.of(tr("jump_reset.perfect").getString())); renderHandle(context, config.jumpResetX + Math.round(config.jumpResetWidth * config.jumpResetScale), config.jumpResetY + Math.round(24 * config.jumpResetScale)); }
        if (config.inventoryCountersEnabled) {
            context.getMatrices().pushMatrix(); context.getMatrices().translate(config.inventoryCountersX, config.inventoryCountersY); context.getMatrices().scale(config.inventoryCountersScale, config.inventoryCountersScale);
            context.fill(0, 0, 118, 24, 0xC0101726); context.drawItem(new net.minecraft.item.ItemStack(net.minecraft.item.Items.TOTEM_OF_UNDYING), 4, 4);
            context.drawTextWithShadow(textRenderer, "12", 24, 8, WHITE); context.drawItem(new net.minecraft.item.ItemStack(net.minecraft.item.Items.ARROW), 53, 4); context.drawTextWithShadow(textRenderer, "32", 73, 8, WHITE); context.getMatrices().popMatrix();
            renderHandle(context, config.inventoryCountersX + Math.round(118 * config.inventoryCountersScale), config.inventoryCountersY + Math.round(24 * config.inventoryCountersScale));
        }
        if (config.noteBoardEnabled) {
            context.getMatrices().pushMatrix(); context.getMatrices().translate(config.noteBoardX, config.noteBoardY); context.getMatrices().scale(config.noteBoardScale, config.noteBoardScale);
            context.fill(2, 3, 210, 84, 0x66000000); context.fill(0, 0, 208, 81, 0xFF50321E); context.fill(2, 2, 206, 79, 0xFF9A6336);
            context.fill(4, 4, 204, 77, 0xFF684326); context.fill(7, 7, 201, 74, 0xFF98744A); context.fill(10, 10, 198, 71, 0xFFE8D8B5);
            context.fill(12, 12, 196, 21, 0xFFF4E9CF); context.fill(103, 8, 108, 13, 0xFF8B2520); context.fill(104, 8, 107, 11, 0xFFE35B43);
            context.drawTextWithShadow(textRenderer, tr("note.title"), 15, 14, 0xFF49351F);
            List<String> previewLines = config.noteBoardText.isBlank() ? List.of(tr("note.empty").getString()) : config.noteBoardText.lines().limit(3).toList();
            int noteY = 31; for (String line : previewLines) { context.drawTextWithShadow(textRenderer, trimToWidth(line, 180), 15, noteY, 0xFF49351F); context.fill(14, noteY + 10, 196, noteY + 11, 0x667D6546); noteY += 12; }
            context.getMatrices().popMatrix(); renderHandle(context, config.noteBoardX + Math.round(210 * config.noteBoardScale), config.noteBoardY + Math.round(84 * config.noteBoardScale));
        }
    }

    private void renderScoreboardPreview(DrawContext context, HudFabricConfig config) {
        MinecraftClient client = MinecraftClient.getInstance();
        ScoreboardObjective objective = client.world.getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        List<ScoreboardEntry> entries = scoreboardEntries(objective);
        if (objective == null || entries.isEmpty()) {
            return;
        }

        TextRenderer tr = textRenderer;
        int width = config.scoreboardWidth;
        int titleHeight = 18;
        int rowHeight = 11;
        int height = titleHeight + entries.size() * rowHeight + 6;
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(config.scoreboardX, config.scoreboardY);
        context.getMatrices().scale(config.scoreboardScale, config.scoreboardScale);
        fillSoftRect(context, 0, 0, width, height, config.scoreboardRadius, config.scoreboardBackground);
        context.drawTextWithShadow(tr, objective.getDisplayName(), MathHelper.clamp((width - tr.getWidth(objective.getDisplayName())) / 2, 8, width - 8), 5, config.scoreboardTitle);
        int y = titleHeight;
        for (ScoreboardEntry entry : entries) {
            Text line = entry.display() == null ? Team.decorateName(client.world.getScoreboard().getScoreHolderTeam(entry.owner()), Text.literal(entry.owner())) : entry.display();
            if (config.scoreboardServerColors) {
                context.drawTextWithShadow(tr, line, 9, y, config.scoreboardText);
            } else {
                context.drawTextWithShadow(tr, trimToWidth(line.getString(), width - 18), 9, y, config.scoreboardText);
            }
            y += rowHeight;
        }
        context.getMatrices().popMatrix();
        renderHandle(context, config.scoreboardX + scoreboardWidth(config), config.scoreboardY + scoreboardHeight(config));
    }

    private void renderStatsPreview(DrawContext context, HudFabricConfig config) {
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(config.statsX, config.statsY);
        context.getMatrices().scale(config.statsScale, config.statsScale);
        fillSoftRect(context, 0, 0, 82, 32, config.statsRadius, config.statsBackground);
        context.drawTextWithShadow(textRenderer, MinecraftClient.getInstance().getCurrentFps() + " FPS", 8, 5, config.statsText);
        context.drawTextWithShadow(textRenderer, currentPing() + " ms", 8, 18, config.statsText);
        context.getMatrices().popMatrix();
        renderHandle(context, config.statsX + statsWidth(config), config.statsY + statsHeight(config));
    }

    private void renderCpsPreview(DrawContext context, HudFabricConfig config) {
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(config.cpsX, config.cpsY);
        context.getMatrices().scale(config.cpsScale, config.cpsScale);
        fillSoftRect(context, 0, 0, config.cpsWidth, 22, config.cpsRadius, config.cpsBackground);
        String preview = "L 8   •   R 4";
        context.drawTextWithShadow(textRenderer, preview, (config.cpsWidth - textRenderer.getWidth(preview)) / 2, 7, config.cpsText);
        context.getMatrices().popMatrix();
    }

    private void renderEquipmentHotbarPreview(DrawContext context, HudFabricConfig config) {
        int x = hotbarX(config), y = hotbarY(config);
        if (config.equipmentHotbarBackground && !config.equipmentHotbarSeparate) fillSoftRect(context, x, y, 82, 22, 4, 0xB0101218);
        for (int i = 0; i < 4; i++) {
            int baseX = config.equipmentHotbarSeparate ? equipmentSlotX(config, i) : x + i * 20;
            int baseY = config.equipmentHotbarSeparate ? equipmentSlotY(config, i) : y;
            if (config.equipmentHotbarBackground && config.equipmentHotbarSeparate) drawIndependentSlot(context, baseX, baseY);
            int slotX = baseX + 4;
            context.fill(slotX, baseY + 3, slotX + 16, baseY + 17, 0x332A3142);
            int bar = 13 - i * 2;
            if (i > 0) {
                context.fill(slotX + 1, baseY + 18, slotX + 14, baseY + 20, 0xFF000000);
                context.fill(slotX + 1, baseY + 18, slotX + 1 + bar, baseY + 19, i < 3 ? 0xFF65D94F : 0xFFFFAA33);
            }
        }
    }

    private static void drawIndependentSlot(DrawContext context, int x, int y) {
        Identifier hotbar = Identifier.ofVanilla("hud/hotbar");
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, hotbar, 182, 22, 20, 0, x, y, 20, 22);
    }

    private int hotbarX(HudFabricConfig config) {
        if (config.equipmentHotbarX >= 0) return clamp(config.equipmentHotbarX, 0, Math.max(0, width - 82));
        boolean offhandOnLeft = MinecraftClient.getInstance().player == null || MinecraftClient.getInstance().player.getMainArm() == net.minecraft.util.Arm.RIGHT;
        return Math.max(0, width / 2 - 91 - 88 - (offhandOnLeft ? 29 : 0));
    }

    private int hotbarY(HudFabricConfig config) {
        return config.equipmentHotbarY < 0 ? Math.max(0, height - 22) : clamp(config.equipmentHotbarY, 0, Math.max(0, height - 22));
    }

    private int equipmentSlotX(HudFabricConfig config, int slot) { return config.equipmentSlotX[slot] < 0 ? hotbarX(config) + slot * 20 : clamp(config.equipmentSlotX[slot], 0, Math.max(0, width - 22)); }
    private int equipmentSlotY(HudFabricConfig config, int slot) { return config.equipmentSlotY[slot] < 0 ? hotbarY(config) : clamp(config.equipmentSlotY[slot], 0, Math.max(0, height - 22)); }

    private void renderSimplePreview(DrawContext context, int x, int y, float scale, int panelW, int panelH, int radius, int background, int textColor, List<String> lines) {
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(x, y);
        context.getMatrices().scale(scale, scale);
        fillSoftRect(context, 0, 0, panelW, panelH, radius, background);
        int lineY = 6;
        for (String line : lines) {
            context.drawTextWithShadow(textRenderer, line, 8, lineY, textColor);
            lineY += 11;
        }
        context.getMatrices().popMatrix();
    }

    private void renderSettingsPanel(DrawContext context, int mouseX, int mouseY) {
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
        context.drawTexturedQuad(Identifier.of("hudfabric", "icon/icon.png"), left + 14, top + 11, left + 40, top + 37, 0.0f, 1.0f, 0.0f, 1.0f);
        context.drawTextWithShadow(textRenderer, Text.literal("HudFabric"), left + 48, top + 20, WHITE);
        context.drawTextWithShadow(textRenderer, tr(page.titleKey), contentLeft + 10, top + 18, WHITE);
        context.drawTextWithShadow(textRenderer, Text.literal("HUD Studio  •  " + displayVersion()), contentLeft + 10, top + 31, 0xFF8E9AB6);
        if (page == SettingsPage.SCOREBOARD) {
            if (activeModule == null) {
                context.drawTextWithShadow(textRenderer, tr("modules.hint"), contentLeft + 12, top + 57, 0xFF9DA8C3);
            } else {
                int half = contentW / 2;
                fillSoftRect(context, contentLeft + 6, top + 52, compactLayout() ? contentW - 12 : half - 12, panelH - 96, 7, 0x801A2438);
                if (!compactLayout()) fillSoftRect(context, contentLeft + half, top + 52, half - 6, panelH - 96, 7, 0x801A2438);
                context.drawTextWithShadow(textRenderer, tr(activeModule.labelKey), contentLeft + 16, top + 57, 0xFF8A9BFF);
                if (!compactLayout()) {
                    context.drawTextWithShadow(textRenderer, activeModule == HudModule.JUMP_RESET ? tr(activeModule.labelKey) : tr("modules.appearance"), contentLeft + half + 10, top + 57, 0xFF8A9BFF);
                    int pickerX = colorPickerX(left, panelW);
                    renderColorPicker(context, pickerX, top + 174, contentW / 2 - 34, colorTarget.get(HudFabricClient.config));
                    int swatchY = top + 90;
                    for (ColorTarget target : activeModule.colors) {
                        fillSoftRect(context, pickerX + 188, swatchY, 16, 12, 3, target.get(HudFabricClient.config));
                        swatchY += 24;
                    }
                }
            }
        }
        if (page == SettingsPage.COLORS) {
            if (compactLayout()) {
                renderColorPicker(context, colorPickerX(left, panelW), top + 85, contentW - 24, colorTarget.get(HudFabricClient.config));
            } else {
                renderColorTargets(context, contentLeft + 10, top + 58);
                int pickerX = contentLeft + 222;
                fillSoftRect(context, pickerX - 8, top + 54, contentW - 226, 224, 8, 0xA01A2438);
                renderColorPicker(context, pickerX, top + 66, contentW - 238, colorTarget.get(HudFabricClient.config));
            }
        }
        if (page == SettingsPage.CROSSHAIR) {
            int half = contentW / 2;
            fillSoftRect(context, contentLeft + 6, top + 52, compactLayout() ? contentW - 12 : half - 12, panelH - 96, 7, 0x801A2438);
            if (!compactLayout()) fillSoftRect(context, contentLeft + half, top + 52, half - 6, panelH - 96, 7, 0x801A2438);
            context.drawTextWithShadow(textRenderer, tr("title.crosshair"), contentLeft + 16, top + 57, 0xFF8A9BFF);
            if (!compactLayout()) {
                context.drawTextWithShadow(textRenderer, tr("modules.appearance"), contentLeft + half + 10, top + 57, 0xFF8A9BFF);
                int pickerX = colorPickerX(left, panelW);
                renderColorPicker(context, pickerX, top + 150, contentW / 2 - 34, colorTarget.get(HudFabricClient.config));
                fillSoftRect(context, pickerX + 188, top + 82, 16, 12, 3, HudFabricClient.config.crosshairColor);
                fillSoftRect(context, pickerX + 188, top + 106, 16, 12, 3, HudFabricClient.config.crosshairOutlineColor);
            }
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
        scrollbarDragOffset = mouseY >= thumbY && mouseY < thumbY + thumbH
                ? (int) mouseY - thumbY
                : thumbH / 2;
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
            clearAndInit();
        }
    }

    private int scrollbarTrackTop() {
        return (height - settingsPanelHeight()) / 2 + 56;
    }

    private int scrollbarTrackBottom() {
        return (height + settingsPanelHeight()) / 2 - 38;
    }

    private int scrollbarThumbHeight() {
        int track = scrollbarTrackBottom() - scrollbarTrackTop();
        return Math.max(24, Math.round(track * track / (float) (track + settingsMaxScroll)));
    }

    private int scrollbarThumbY() {
        int travel = scrollbarTrackBottom() - scrollbarTrackTop() - scrollbarThumbHeight();
        return scrollbarTrackTop() + Math.round(travel * (settingsScroll / (float) settingsMaxScroll));
    }

    private void renderColorPicker(DrawContext context, int x, int y, int width, int color) {
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
        int labelWidth = Math.max(16, (this.width + settingsPanelWidth()) / 2 - previewX - 16);
        context.drawTextWithShadow(textRenderer, Text.literal(fitLabel(tr(colorTarget.labelKey).getString(), labelWidth)), previewX, y + 34, WHITE);
        int inputY = alphaY + 18;
        int boxW = compactLayout() ? 32 : 48;
        int step = boxW + 4;
        drawNumberBox(context, x, inputY, "R", (color >> 16) & 255, boxW);
        drawNumberBox(context, x + step, inputY, "G", (color >> 8) & 255, boxW);
        drawNumberBox(context, x + step * 2, inputY, "B", color & 255, boxW);
        drawNumberBox(context, x + step * 3, inputY, "A", alpha(color), boxW);
    }

    private int colorTargetColumns(int panelW) {
        return 1;
    }

    private int colorTargetWidth(int panelW) {
        return 194;
    }

    private int colorPickerX(int left, int panelW) {
        if (compactLayout()) return left + 142;
        return left + 132 + (panelW - 142) / 2 + 10;
    }

    private boolean compactLayout() { return settingsPanelWidth() < 600; }

    private int colorPickerSquare(int panelW) {
        int contentW = panelW - 142;
        return Math.min(132, Math.max(72, Math.min(contentW - 422, settingsPanelHeight() - 250)));
    }

    private boolean handleColorPick(double mouseX, double mouseY) {
        int panelW = settingsPanelWidth();
        int left = (width - panelW) / 2;
        int top = (height - settingsPanelHeight()) / 2;
        int x = colorPickerX(left, panelW);
        int y = top + (page == SettingsPage.COLORS ? 85 : page == SettingsPage.CROSSHAIR ? 150 : 174);
        int square = colorPickerSquare(panelW);
        HudFabricConfig config = HudFabricClient.config;
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

    private static String displayVersion() {
        return FabricLoader.getInstance().getModContainer("hudfabric")
                .map(mod -> mod.getMetadata().getVersion().getFriendlyString().replaceFirst("-\\d+$", ""))
                .orElse("1.0.7");
    }

    private int settingsPanelHeight() {
        return Math.max(1, Math.min(520, height - 24));
    }

    private boolean hasServerScoreboard() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.world != null && client.world.getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR) != null;
    }

    private void renderColorTargets(DrawContext context, int x, int y) {
        int panelW = settingsPanelWidth();
        int columns = colorTargetColumns(panelW);
        int targetW = colorTargetWidth(panelW);
        int i = 0;
        for (ColorTarget target : ColorTarget.values()) {
            int tx = x + (i % columns) * (targetW + 8);
            int ty = y + (i / columns) * 25;
            int color = target.get(HudFabricClient.config);
            if (target == colorTarget) {
                fillSoftRect(context, tx - 2, ty - 2, targetW + 4, 25, 4, 0xFF6D7CFF);
            }
            fillSoftRect(context, tx, ty, targetW, 21, 3, target == colorTarget ? CONTROL_HOVER : CONTROL);
            fillSoftRect(context, tx + 4, ty + 4, 14, 14, 2, color);
            context.drawTextWithShadow(textRenderer, fitLabel(tr(target.labelKey).getString(), targetW - 30), tx + 24, ty + 7, 0xFFCDD3E2);
            i++;
        }
    }

    private String fitLabel(String label, int maxWidth) {
        if (textRenderer.getWidth(label) <= maxWidth) {
            return label;
        }
        return textRenderer.trimToWidth(label, Math.max(8, maxWidth - textRenderer.getWidth("..."))) + "...";
    }

    private void drawNumberBox(DrawContext context, int x, int y, String label, int value, int boxW) {
        context.fill(x, y, x + boxW, y + 18, 0xFFE9EDF5);
        context.fill(x, y, x + boxW, y + 1, 0xFFB9C0CE);
        context.fill(x, y + 17, x + boxW, y + 18, 0xFFB9C0CE);
        String text = String.valueOf(value);
        context.drawText(textRenderer, text, x + boxW / 2 - textRenderer.getWidth(text) / 2, y + 5, 0xFF111318, false);
        context.drawTextWithShadow(textRenderer, label, x + boxW / 2 - textRenderer.getWidth(label) / 2, y + 22, WHITE);
    }

    private List<ScoreboardEntry> scoreboardEntries(ScoreboardObjective objective) {
        if (objective == null || MinecraftClient.getInstance().world == null) {
            return List.of();
        }
        Scoreboard scoreboard = MinecraftClient.getInstance().world.getScoreboard();
        return scoreboard.getScoreboardEntries(objective).stream()
                .filter(entry -> entry.value() != 0 || entry.display() != null)
                .sorted(Comparator.comparingInt(ScoreboardEntry::value).reversed())
                .limit(15)
                .toList();
    }

    private int scoreboardBaseHeight() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            return 0;
        }
        ScoreboardObjective objective = client.world.getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        int rows = scoreboardEntries(objective).size();
        return rows == 0 ? 0 : 18 + rows * 11 + 6;
    }

    private int scoreboardWidth(HudFabricConfig config) {
        return Math.round(config.scoreboardWidth * config.scoreboardScale);
    }

    private int scoreboardHeight(HudFabricConfig config) {
        return Math.round(scoreboardBaseHeight() * config.scoreboardScale);
    }

    private int statsWidth(HudFabricConfig config) {
        return Math.round(82 * config.statsScale);
    }

    private int statsHeight(HudFabricConfig config) {
        return Math.round(32 * config.statsScale);
    }

    private int coordsWidth(HudFabricConfig config) {
        return Math.round(config.coordsWidth * config.coordsScale);
    }

    private int coordsHeight(HudFabricConfig config) {
        return Math.round(52 * config.coordsScale);
    }

    private int effectsWidth(HudFabricConfig config) {
        return Math.round(config.effectsWidth * config.effectsScale);
    }

    private int effectsHeight(HudFabricConfig config) {
        return Math.round(74 * config.effectsScale);
    }

    private int equipmentWidth(HudFabricConfig config) {
        return Math.round(config.equipmentWidth * config.equipmentScale);
    }

    private int equipmentHeight(HudFabricConfig config) {
        return Math.round(96 * config.equipmentScale);
    }

    private int currentPing() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && client.getNetworkHandler() != null && client.getNetworkHandler().getPlayerListEntry(client.player.getUuid()) != null) {
            return client.getNetworkHandler().getPlayerListEntry(client.player.getUuid()).getLatency();
        }
        return 0;
    }

    private void initModulePage(HudFabricConfig config, int contentLeft, int contentW, int y, int backY) {
        if (activeModule == null) {
            int gap = 10;
            int columns = compactLayout() ? 2 : 3;
            int cardHeight = compactLayout() ? 24 : 38;
            int rowStep = compactLayout() ? 28 : 48;
            int cardW = (contentW - 20 - (columns - 1) * gap) / columns;
            int i = 0;
            for (HudModule module : HudModule.values()) {
                int x = contentLeft + 10 + (i % columns) * (cardW + gap);
                int cardY = y + (compactLayout() ? -8 : 8) + (i / columns) * rowStep;
                addDrawableChild(new HudButton(x, cardY, cardW, cardHeight, tr(module.labelKey), button -> {
                    activeModule = module;
                    if (module.colors.length > 0) colorTarget = module.colors[0];
                    settingsScroll = 0;
                    clearAndInit();
                }));
                i++;
            }
            addResetButton(contentLeft, contentW, backY, "ALL");
            return;
        }

        int controlsX = contentLeft + 16;
        int controlsW = compactLayout() ? contentW - 32 : Math.max(150, contentW / 2 - 30);
        int cy = y + 12;
        switch (activeModule) {
            case SCOREBOARD -> {
                addDrawableChild(new HudButton(controlsX, cy, controlsW, 20, scoreboardButtonText(config.scoreboardMode), button -> { config.scoreboardMode = (config.scoreboardMode + 1) % 3; button.setMessage(scoreboardButtonText(config.scoreboardMode)); }));
                addSlider(controlsX, cy += 24, controlsW, "slider.width", config.scoreboardWidth, 90, 320, value -> config.scoreboardWidth = Math.round(value));
                addSlider(controlsX, cy += 24, controlsW, "slider.scale", config.scoreboardScale, 0.5f, 2.5f, value -> config.scoreboardScale = value);
                addSlider(controlsX, cy += 24, controlsW, "slider.board_radius", config.scoreboardRadius, 0, 12, value -> config.scoreboardRadius = Math.round(value));
                addToggle(controlsX, cy += 24, controlsW, "toggle.server_colors", config.scoreboardServerColors, value -> config.scoreboardServerColors = value);
            }
            case STATS -> {
                addToggle(controlsX, cy, controlsW, "toggle.stats", config.statsEnabled, value -> config.statsEnabled = value);
                addSlider(controlsX, cy += 24, controlsW, "slider.stats_scale", config.statsScale, 0.5f, 2.5f, value -> config.statsScale = value);
                addSlider(controlsX, cy += 24, controlsW, "slider.stats_radius", config.statsRadius, 0, 12, value -> config.statsRadius = Math.round(value));
            }
            case COORDS -> {
                addToggle(controlsX, cy, controlsW, "toggle.coords", config.coordsEnabled, value -> config.coordsEnabled = value);
                addSlider(controlsX, cy += 24, controlsW, "slider.coords_scale", config.coordsScale, 0.5f, 2.5f, value -> config.coordsScale = value);
                addSlider(controlsX, cy += 24, controlsW, "slider.coords_width", config.coordsWidth, 92, 280, value -> config.coordsWidth = Math.round(value));
                addSlider(controlsX, cy += 24, controlsW, "slider.coords_radius", config.coordsRadius, 0, 12, value -> config.coordsRadius = Math.round(value));
            }
            case EFFECTS -> {
                addToggle(controlsX, cy, controlsW, "toggle.effects", config.effectsEnabled, value -> config.effectsEnabled = value);
                addSlider(controlsX, cy += 24, controlsW, "slider.effects_scale", config.effectsScale, 0.5f, 2.5f, value -> config.effectsScale = value);
                addSlider(controlsX, cy += 24, controlsW, "slider.effects_width", config.effectsWidth, 110, 320, value -> config.effectsWidth = Math.round(value));
                addSlider(controlsX, cy += 24, controlsW, "slider.effects_radius", config.effectsRadius, 0, 12, value -> config.effectsRadius = Math.round(value));
                addToggle(controlsX, cy += 24, controlsW, "toggle.hide_vanilla_effects", config.hideVanillaEffects, value -> config.hideVanillaEffects = value);
            }
            case EQUIPMENT -> {
                addToggle(controlsX, cy, controlsW, "toggle.equipment", config.equipmentEnabled, value -> config.equipmentEnabled = value);
                addDrawableChild(new HudButton(controlsX, cy += 24, controlsW, 20, equipmentModeText(config.equipmentMode), b -> { config.equipmentMode = (config.equipmentMode + 1) % 2; clearAndInit(); }));
                if (config.equipmentMode == 1) {
                    addToggle(controlsX, cy += 24, controlsW, "toggle.equipment_hotbar_background", config.equipmentHotbarBackground, value -> config.equipmentHotbarBackground = value);
                    addToggle(controlsX, cy += 24, controlsW, "toggle.equipment_hotbar_separate", config.equipmentHotbarSeparate, value -> { config.equipmentHotbarSeparate = value; if (!value) { config.equipmentSlotX = new int[]{-1,-1,-1,-1}; config.equipmentSlotY = new int[]{-1,-1,-1,-1}; } });
                    addDrawableChild(new HudButton(controlsX, cy += 24, controlsW, 20, equipmentDisplayText(config.equipmentDurabilityDisplay), b -> { config.equipmentDurabilityDisplay = (config.equipmentDurabilityDisplay + 1) % 3; b.setMessage(equipmentDisplayText(config.equipmentDurabilityDisplay)); }));
                } else {
                    addSlider(controlsX, cy += 24, controlsW, "slider.equipment_scale", config.equipmentScale, 0.5f, 2.5f, value -> config.equipmentScale = value);
                    addSlider(controlsX, cy += 24, controlsW, "slider.equipment_width", config.equipmentWidth, 120, 320, value -> config.equipmentWidth = Math.round(value));
                    addSlider(controlsX, cy += 24, controlsW, "slider.equipment_radius", config.equipmentRadius, 0, 12, value -> config.equipmentRadius = Math.round(value));
                }
                addToggle(controlsX, cy += 24, controlsW, "toggle.equipment_warning", config.equipmentWarning, value -> config.equipmentWarning = value);
                addSlider(controlsX, cy += 24, controlsW, "slider.equipment_warning", config.equipmentWarningPercent, 1, 50, value -> config.equipmentWarningPercent = Math.round(value));
                addDrawableChild(new HudButton(controlsX, cy += 24, controlsW, 20, tr(config.equipmentBreakWarningEnabled ? (config.equipmentBreakWarningSound ? "equipment.break_sound" : "equipment.break_visual") : "equipment.break_off"), b -> {
                    if (!config.equipmentBreakWarningEnabled) { config.equipmentBreakWarningEnabled = true; config.equipmentBreakWarningSound = false; }
                    else if (!config.equipmentBreakWarningSound) config.equipmentBreakWarningSound = true;
                    else { config.equipmentBreakWarningEnabled = false; config.equipmentBreakWarningSound = false; }
                    b.setMessage(tr(config.equipmentBreakWarningEnabled ? (config.equipmentBreakWarningSound ? "equipment.break_sound" : "equipment.break_visual") : "equipment.break_off"));
                }));
            }
            case CPS -> {
                addToggle(controlsX, cy, controlsW, "toggle.cps", config.cpsEnabled, value -> config.cpsEnabled = value);
                addSlider(controlsX, cy += 24, controlsW, "slider.cps_scale", config.cpsScale, 0.5f, 2.5f, value -> config.cpsScale = value);
                addSlider(controlsX, cy += 24, controlsW, "slider.cps_width", config.cpsWidth, 72, 240, value -> config.cpsWidth = Math.round(value));
                addSlider(controlsX, cy += 24, controlsW, "slider.cps_radius", config.cpsRadius, 0, 12, value -> config.cpsRadius = Math.round(value));
            }
            case INVENTORY_COUNTERS -> {
                addToggle(controlsX, cy, controlsW, "toggle.inventory_counters", config.inventoryCountersEnabled, value -> config.inventoryCountersEnabled = value);
                addSlider(controlsX, cy += 24, controlsW, "slider.scale", config.inventoryCountersScale, 0.5f, 2.5f, value -> config.inventoryCountersScale = value);
            }
            case NOTE_BOARD -> {
                addToggle(controlsX, cy, controlsW, "toggle.note_board", config.noteBoardEnabled, value -> config.noteBoardEnabled = value);
                addDrawableChild(new HudButton(controlsX, cy += 24, controlsW, 20, tr("note.edit"), b -> { HudFabricClient.config.save(); MinecraftClient.getInstance().setScreen(new NoteBoardScreen(this)); }));
                addSlider(controlsX, cy += 24, controlsW, "slider.scale", config.noteBoardScale, 0.5f, 2.5f, value -> config.noteBoardScale = value);
            }
            case JUMP_RESET -> {
                addToggle(controlsX, cy, controlsW, "toggle.jump_reset", config.jumpResetEnabled, value -> config.jumpResetEnabled = value);
                addDrawableChild(new HudButton(controlsX, cy += 24, controlsW, 20, tr(config.jumpResetMode == 0 ? "jump_reset.mode.target" : "jump_reset.mode.panel"), b -> { config.jumpResetMode = (config.jumpResetMode + 1) % 2; clearAndInit(); }));
                addSlider(controlsX, cy += 24, controlsW, "slider.scale", config.jumpResetScale, 0.5f, 2.5f, value -> config.jumpResetScale = value);
                if (config.jumpResetMode == 1) { addSlider(controlsX, cy += 24, controlsW, "slider.width", config.jumpResetWidth, 92, 260, value -> config.jumpResetWidth = Math.round(value)); addSlider(controlsX, cy += 24, controlsW, "slider.board_radius", config.jumpResetRadius, 0, 12, value -> config.jumpResetRadius = Math.round(value)); }
            }
            case BLOCK_OUTLINE -> addToggle(controlsX, cy, controlsW, "toggle.block_outline", config.customBlockOutline, value -> config.customBlockOutline = value);
        }

        if (compactLayout()) {
            if (activeModule.colors.length > 0) addCompactColorButton(contentLeft, backY, activeModule.colors[0]);
        } else {
            int colorX = colorPickerX((width - settingsPanelWidth()) / 2, settingsPanelWidth());
            int colorY = y + 12;
            for (ColorTarget target : activeModule.colors) {
                addDrawableChild(new HudButton(colorX, colorY, 210, 20, tr(target.labelKey), button -> {
                    colorTarget = target;
                    clearAndInit();
                }));
                colorY += 24;
            }
        }
        addDrawableChild(new HudButton(contentLeft + 10, backY, 120, 20, tr("button.modules_back"), button -> {
            activeModule = null;
            clearAndInit();
        }));
        addResetButton(contentLeft, contentW, backY, activeModule.name());
    }

    private void addResetButton(int left, int contentW, int backY, String scope) {
        int buttonW = Math.min(148, Math.max(90, (contentW - 30) / 2));
        int buttonY = backY;
        HudButton button = new HudButton(left + contentW - buttonW - 10, buttonY, buttonW, 20,
                tr(scope.equals("ALL") ? "button.reset_all" : "button.reset_module"), b -> {
                    HudFabricClient.config.reset(scope);
                    HudFabricClient.saveActiveServerProfile();
                    HudFabricClient.config.save();
                    settingsScroll = 0;
                    clearAndInit();
                });
        button.setResetStyle(true);
        addDrawableChild(button);
    }

    private void addCompactColorButton(int left, int backY, ColorTarget target) {
        addDrawableChild(new HudButton(left + 10, backY - 28, 120, 20, tr("tab.colors"), button -> {
            colorTarget = target;
            page = SettingsPage.COLORS;
            settingsScroll = 0;
            clearAndInit();
        }));
    }

    private void addToggle(int x, int y, int width, String key, boolean value, Consumer<Boolean> setter) {
        addDrawableChild(new HudButton(x, y, width, 20, toggleText(key, value), button -> {
            boolean next = !button.getMessage().getString().contains(tr("value.on").getString());
            setter.accept(next);
            button.setMessage(toggleText(key, next));
        }));
    }

    private void addSlider(int x, int y, int width, String key, float value, float min, float max, Consumer<Float> setter) {
        addDrawableChild(new HudSlider(x, y, width, 20, key, value, min, max, setter));
    }

    private static void playButtonSound() {
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
    }

    private static void renderHandle(DrawContext context, int x, int y) {
        context.fill(x - 8, y - 8, x, y, 0xCCFFFFFF);
        context.fill(x - 6, y - 2, x - 2, y, 0xFF111318);
        context.fill(x - 2, y - 6, x, y - 2, 0xFF111318);
    }

    private static void fillSoftRect(DrawContext context, int x, int y, int width, int height, int radius, int color) {
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

    private static void applyCleanPreset(HudFabricConfig config) {
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
        if (textRenderer.getWidth(value) <= maxWidth) {
            return value;
        }
        return textRenderer.trimToWidth(value, Math.max(8, maxWidth - textRenderer.getWidth("..."))) + "...";
    }

    private static String scoreboardModeLabel(int mode) {
        return switch (mode) {
            case HudFabricConfig.SCOREBOARD_VANILLA -> "mode.vanilla";
            case HudFabricConfig.SCOREBOARD_HIDDEN -> "mode.off";
            default -> "mode.custom";
        };
    }

    private static String crosshairModeLabel(int mode) {
        return switch (mode) {
            case HudFabricConfig.CROSSHAIR_VANILLA -> "mode.vanilla";
            case HudFabricConfig.CROSSHAIR_CIRCLE -> "mode.circle";
            case HudFabricConfig.CROSSHAIR_DOT -> "mode.dot";
            default -> "mode.clean_cross";
        };
    }

    private static Text tr(String key, Object... args) {
        return Text.translatable("hudfabric." + key, args);
    }

    private static Text scoreboardButtonText(int mode) {
        return tr("button.scoreboard", tr(scoreboardModeLabel(mode)));
    }

    private static Text crosshairButtonText(int mode) {
        return tr("button.mode", tr(crosshairModeLabel(mode)));
    }

    private static Text equipmentModeText(int mode) {
        return tr("button.mode", tr(mode == 1 ? "equipment.mode_hotbar" : "equipment.mode_panel"));
    }

    private static Text equipmentSideText(int side) {
        return tr("equipment.side", tr(side == 1 ? "value.right" : "value.left"));
    }

    private static Text equipmentDisplayText(int display) {
        String key = display == 1 ? "equipment.numeric" : display == 2 ? "equipment.percent" : "equipment.bar";
        return tr("equipment.display", tr(key));
    }

    private static Text toggleText(String key, boolean value) {
        return tr("button.toggle", tr(key), tr(value ? "value.on" : "value.off"));
    }

    private Text tabText(SettingsPage value) {
        return value == page ? Text.literal("* ").append(tr(value.labelKey)) : tr(value.labelKey);
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
        BLOCK_OUTLINE("module.block_outline", ColorTarget.BLOCK_OUTLINE),
        INVENTORY_COUNTERS("module.inventory_counters"),
        NOTE_BOARD("module.note_board");

        private final String labelKey;
        private final ColorTarget[] colors;

        HudModule(String labelKey, ColorTarget... colors) {
            this.labelKey = labelKey;
            this.colors = colors;
        }
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
        INVENTORY_COUNTERS_MOVE,
        NOTE_BOARD_MOVE,
        JUMP_RESET_MOVE,
        JUMP_RESET_RESIZE
    }

    private enum ColorTarget {
        BOARD_BG("color.scoreboard_background", "BB") {
            int get(HudFabricConfig c) { return c.scoreboardBackground; }
            void set(HudFabricConfig c, int v) { c.scoreboardBackground = v; }
        },
        BOARD_TITLE("color.scoreboard_title", "TT") {
            int get(HudFabricConfig c) { return c.scoreboardTitle; }
            void set(HudFabricConfig c, int v) { c.scoreboardTitle = v; }
        },
        BOARD_TEXT("color.scoreboard_text", "BT") {
            int get(HudFabricConfig c) { return c.scoreboardText; }
            void set(HudFabricConfig c, int v) { c.scoreboardText = v; }
        },
        STATS_BG("color.stats_background", "SB") {
            int get(HudFabricConfig c) { return c.statsBackground; }
            void set(HudFabricConfig c, int v) { c.statsBackground = v; }
        },
        STATS_TEXT("color.stats_text", "ST") {
            int get(HudFabricConfig c) { return c.statsText; }
            void set(HudFabricConfig c, int v) { c.statsText = v; }
        },
        COORDS_BG("color.coords_background", "CB") {
            int get(HudFabricConfig c) { return c.coordsBackground; }
            void set(HudFabricConfig c, int v) { c.coordsBackground = v; }
        },
        COORDS_TEXT("color.coords_text", "CT") {
            int get(HudFabricConfig c) { return c.coordsText; }
            void set(HudFabricConfig c, int v) { c.coordsText = v; }
        },
        EFFECTS_BG("color.effects_background", "EB") {
            int get(HudFabricConfig c) { return c.effectsBackground; }
            void set(HudFabricConfig c, int v) { c.effectsBackground = v; }
        },
        EFFECTS_TEXT("color.effects_text", "ET") {
            int get(HudFabricConfig c) { return c.effectsText; }
            void set(HudFabricConfig c, int v) { c.effectsText = v; }
        },
        EQUIPMENT_BG("color.equipment_background", "QB") {
            int get(HudFabricConfig c) { return c.equipmentBackground; }
            void set(HudFabricConfig c, int v) { c.equipmentBackground = v; }
        },
        EQUIPMENT_TEXT("color.equipment_text", "QT") {
            int get(HudFabricConfig c) { return c.equipmentText; }
            void set(HudFabricConfig c, int v) { c.equipmentText = v; }
        },
        CPS_BG("color.cps_background", "CPB") {
            int get(HudFabricConfig c) { return c.cpsBackground; }
            void set(HudFabricConfig c, int v) { c.cpsBackground = v; }
        },
        CPS_TEXT("color.cps_text", "CPT") {
            int get(HudFabricConfig c) { return c.cpsText; }
            void set(HudFabricConfig c, int v) { c.cpsText = v; }
        },
        JUMP_RESET_BG("color.jump_reset_background", "JRB") {
            int get(HudFabricConfig c) { return c.jumpResetBackground; }
            void set(HudFabricConfig c, int v) { c.jumpResetBackground = v; }
        },
        JUMP_RESET_TEXT("color.jump_reset_text", "JRT") {
            int get(HudFabricConfig c) { return c.jumpResetText; }
            void set(HudFabricConfig c, int v) { c.jumpResetText = v; }
        },
        CROSSHAIR("color.crosshair", "CH") {
            int get(HudFabricConfig c) { return c.crosshairColor; }
            void set(HudFabricConfig c, int v) { c.crosshairColor = v; }
        },
        CROSSHAIR_OUTLINE("color.crosshair_outline", "CO") {
            int get(HudFabricConfig c) { return c.crosshairOutlineColor; }
            void set(HudFabricConfig c, int v) { c.crosshairOutlineColor = v; }
        },
        BLOCK_OUTLINE("color.block_outline", "BO") {
            int get(HudFabricConfig c) { return c.blockOutlineColor; }
            void set(HudFabricConfig c, int v) { c.blockOutlineColor = v; }
        };

        private final String labelKey;
        private final String shortLabel;

        ColorTarget(String labelKey, String shortLabel) {
            this.labelKey = labelKey;
            this.shortLabel = shortLabel;
        }

        abstract int get(HudFabricConfig config);

        abstract void set(HudFabricConfig config, int value);
    }

    private static final class HudSlider extends SliderWidget {
        private final String key;
        private final float min;
        private final float max;
        private final Consumer<Float> setter;

        private HudSlider(int x, int y, int width, int height, String key, float value, float min, float max, Consumer<Float> setter) {
            super(x, y, width, height, Text.empty(), Math.max(0.0, Math.min(1.0, (value - min) / (max - min))));
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

        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
            int bg = isHovered() || isSelected() ? CONTROL_HOVER : CONTROL;
            fillSoftRect(context, getX(), getY(), getWidth(), getHeight(), 3, bg);
            drawBorder(context, getX(), getY(), getWidth(), getHeight(), isHovered() || isSelected() ? BLUE : BORDER);
            int knobX = getX() + 2 + Math.round((getWidth() - 8) * (float) value);
            fillSoftRect(context, knobX, getY() + 2, 5, getHeight() - 4, 1, 0xFFE4EBFF);
            TextRenderer tr = MinecraftClient.getInstance().textRenderer;
            Text message = getMessage();
            int messageCenter = getX() + getWidth() / 2;
            if (key.contains("radius")) {
                float current = min + (float) value * (max - min);
                int previewW = 28;
                int previewX = getX() + getWidth() - previewW - 7;
                fillSoftRect(context, previewX, getY() + 4, previewW, getHeight() - 8, Math.round(current), 0xFF6577A8);
                messageCenter -= 18;
            }
            context.drawTextWithShadow(tr, message, messageCenter - tr.getWidth(message) / 2, getY() + 6, WHITE);
        }
    }

    private static final class HudButton extends PressableWidget {
        private final Consumer<HudButton> onPress;
        private boolean selectedStyle;
        private boolean resetStyle;

        private HudButton(int x, int y, int width, int height, Text message, Consumer<HudButton> onPress) {
            super(x, y, width, height, message);
            this.onPress = onPress;
        }

        private void setSelectedStyle(boolean selectedStyle) {
            this.selectedStyle = selectedStyle;
        }

        private void setResetStyle(boolean resetStyle) {
            this.resetStyle = resetStyle;
        }

        @Override
        public void onPress(AbstractInput input) {
            onPress.accept(this);
        }

        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
            int bg = resetStyle ? (isHovered() ? 0xD05C3941 : 0xC0392733)
                    : selectedStyle || isHovered() || isSelected() ? CONTROL_HOVER : CONTROL;
            fillSoftRect(context, getX(), getY(), getWidth(), getHeight(), 3, bg);
            drawBorder(context, getX(), getY(), getWidth(), getHeight(), resetStyle ? 0xFFB77888
                    : selectedStyle || isHovered() || isSelected() ? BLUE : BORDER);
            TextRenderer tr = MinecraftClient.getInstance().textRenderer;
            String label = getMessage().getString();
            int available = Math.max(8, getWidth() - 12);
            if (tr.getWidth(label) > available) {
                while (label.length() > 1 && tr.getWidth(label + "…") > available) label = label.substring(0, label.length() - 1);
                label += "…";
            }
            context.drawTextWithShadow(tr, Text.literal(label), getX() + getWidth() / 2 - tr.getWidth(label) / 2, getY() + (getHeight() - 8) / 2, WHITE);
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {
            appendDefaultNarrations(builder);
        }
    }

    private static void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x, y, x + 1, y + height, color);
        context.fill(x + width - 1, y, x + width, y + height, color);
    }
}

