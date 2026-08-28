package dev.nova.hudfabric.gui;

import dev.nova.hudfabric.HudForgeClient;
import dev.nova.hudfabric.config.HudForgeConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
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

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

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
            addDrawableChild(new HudButton(width - 132, height - 28, 120, 20, tr("button.settings"), button -> {
                settingsOpen = true;
                clearAndInit();
            }));
            addDrawableChild(new HudButton(width - 258, height - 28, 120, 20, tr("button.done"), button -> close()));
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
                int controlX = contentLeft + 16;
                int controlW = Math.max(150, contentW / 2 - 30);
                addDrawableChild(new HudButton(controlX, y, controlW, 20, crosshairButtonText(config.crosshairMode), button -> {
                    config.crosshairMode = (config.crosshairMode + 1) % 4;
                    config.customCrosshair = config.crosshairMode != HudForgeConfig.CROSSHAIR_VANILLA;
                    button.setMessage(crosshairButtonText(config.crosshairMode));
                }));
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
                addDrawableChild(new HudButton(colorX, top + 78, 210, 20, tr("color.crosshair"), b -> { colorTarget = ColorTarget.CROSSHAIR; clearAndInit(); }));
                addDrawableChild(new HudButton(colorX, top + 102, 210, 20, tr("color.crosshair_outline"), b -> { colorTarget = ColorTarget.CROSSHAIR_OUTLINE; clearAndInit(); }));
            }
            case COLORS -> {
                addDrawableChild(new HudButton(contentLeft + 10, backY, 120, 20, tr("button.clean_preset"), button -> {
                    applyCleanPreset(config);
                    clearAndInit();
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

        addDrawableChild(new HudButton(left + 10, backY, 108, 20, tr("button.back_editor"), button -> {
            settingsOpen = false;
            clearAndInit();
        }));
        updateScrollableWidgetVisibility(top + 54, backY - 6, left + 122, backY);
    }

    private void updateScrollableWidgetVisibility(int contentTop, int contentBottom, int sidebarRight, int backY) {
        for (var element : children()) {
            if (element instanceof net.minecraft.client.gui.widget.ClickableWidget widget) {
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
                clearAndInit();
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void close() {
        HudForgeClient.saveActiveServerProfile();
        HudForgeClient.config.save();
        MinecraftClient.getInstance().setScreen(parent);
    }

    @Override
    public void removed() {
        HudForgeClient.saveActiveServerProfile();
        HudForgeClient.config.save();
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubleClick) {
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
            if (config.equipmentEnabled && inside(click.x(), click.y(), config.equipmentX, config.equipmentY, equipmentWidth(config), equipmentHeight(config))) {
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
        HudForgeClient.saveActiveServerProfile();
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
            renderSimplePreview(context, config.equipmentX, config.equipmentY, config.equipmentScale, config.equipmentWidth, 96, config.equipmentRadius, config.equipmentBackground, config.equipmentText, List.of("Tool 1532/1561", "Helmet 363/363", "Chest 528/528", "Legs 495/495"));
            renderHandle(context, config.equipmentX + equipmentWidth(config), config.equipmentY + equipmentHeight(config));
        }
        if (config.cpsEnabled) {
            renderCpsPreview(context, config);
            renderHandle(context, config.cpsX + Math.round(config.cpsWidth * config.cpsScale), config.cpsY + Math.round(22 * config.cpsScale));
        }
    }

    private void renderScoreboardPreview(DrawContext context, HudForgeConfig config) {
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

    private void renderStatsPreview(DrawContext context, HudForgeConfig config) {
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(config.statsX, config.statsY);
        context.getMatrices().scale(config.statsScale, config.statsScale);
        fillSoftRect(context, 0, 0, 82, 32, config.statsRadius, config.statsBackground);
        context.drawTextWithShadow(textRenderer, MinecraftClient.getInstance().getCurrentFps() + " FPS", 8, 5, config.statsText);
        context.drawTextWithShadow(textRenderer, currentPing() + " ms", 8, 18, config.statsText);
        context.getMatrices().popMatrix();
        renderHandle(context, config.statsX + statsWidth(config), config.statsY + statsHeight(config));
    }

    private void renderCpsPreview(DrawContext context, HudForgeConfig config) {
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(config.cpsX, config.cpsY);
        context.getMatrices().scale(config.cpsScale, config.cpsScale);
        fillSoftRect(context, 0, 0, config.cpsWidth, 22, config.cpsRadius, config.cpsBackground);
        String preview = "L 8   •   R 4";
        context.drawTextWithShadow(textRenderer, preview, (config.cpsWidth - textRenderer.getWidth(preview)) / 2, 7, config.cpsText);
        context.getMatrices().popMatrix();
    }

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
        fillSoftRect(context, left + 14, top + 11, 26, 26, 7, 0xFF6878ED);
        context.drawTextWithShadow(textRenderer, Text.literal("HF"), left + 21, top + 20, 0xFFFFFFFF);
        context.drawTextWithShadow(textRenderer, Text.literal("HudFabric"), left + 48, top + 20, WHITE);
        context.drawTextWithShadow(textRenderer, tr(page.titleKey), contentLeft + 10, top + 18, WHITE);
        context.drawTextWithShadow(textRenderer, Text.literal("Modules & HUD Studio  •  1.0.4"), contentLeft + 10, top + 31, 0xFF8E9AB6);
        if (page == SettingsPage.SCOREBOARD) {
            if (activeModule == null) {
                context.drawTextWithShadow(textRenderer, tr("modules.hint"), contentLeft + 12, top + 57, 0xFF9DA8C3);
            } else {
                int half = contentW / 2;
                fillSoftRect(context, contentLeft + 6, top + 52, half - 12, panelH - 96, 7, 0x801A2438);
                fillSoftRect(context, contentLeft + half, top + 52, half - 6, panelH - 96, 7, 0x801A2438);
                context.drawTextWithShadow(textRenderer, tr(activeModule.labelKey), contentLeft + 16, top + 57, 0xFF8A9BFF);
                context.drawTextWithShadow(textRenderer, tr("modules.appearance"), contentLeft + half + 10, top + 57, 0xFF8A9BFF);
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
            context.drawTextWithShadow(textRenderer, tr("title.crosshair"), contentLeft + 16, top + 57, 0xFF8A9BFF);
            context.drawTextWithShadow(textRenderer, tr("modules.appearance"), contentLeft + half + 10, top + 57, 0xFF8A9BFF);
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
        context.drawTextWithShadow(textRenderer, tr(colorTarget.labelKey), previewX, y + 34, WHITE);
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
            int color = target.get(HudForgeClient.config);
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

    private void drawNumberBox(DrawContext context, int x, int y, String label, int value) {
        context.fill(x, y, x + 48, y + 18, 0xFFE9EDF5);
        context.fill(x, y, x + 48, y + 1, 0xFFB9C0CE);
        context.fill(x, y + 17, x + 48, y + 18, 0xFFB9C0CE);
        String text = String.valueOf(value);
        context.drawText(textRenderer, text, x + 24 - textRenderer.getWidth(text) / 2, y + 5, 0xFF111318, false);
        context.drawTextWithShadow(textRenderer, label, x + 24 - textRenderer.getWidth(label) / 2, y + 22, WHITE);
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
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && client.getNetworkHandler() != null && client.getNetworkHandler().getPlayerListEntry(client.player.getUuid()) != null) {
            return client.getNetworkHandler().getPlayerListEntry(client.player.getUuid()).getLatency();
        }
        return 0;
    }

    private void initModulePage(HudForgeConfig config, int contentLeft, int contentW, int y, int backY) {
        if (activeModule == null) {
            int gap = 10;
            int cardW = (contentW - 40) / 3;
            int i = 0;
            for (HudModule module : HudModule.values()) {
                int x = contentLeft + 10 + (i % 3) * (cardW + gap);
                int cardY = y + 8 + (i / 3) * 48;
                addDrawableChild(new HudButton(x, cardY, cardW, 38, tr(module.labelKey), button -> {
                    activeModule = module;
                    colorTarget = module.colors[0];
                    settingsScroll = 0;
                    clearAndInit();
                }));
                i++;
            }
            return;
        }

        int controlsX = contentLeft + 16;
        int controlsW = Math.max(150, contentW / 2 - 30);
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
                addDrawableChild(new HudButton(controlsX, cy += 24, controlsW, 20, equipmentModeText(config.equipmentMode), b -> { config.equipmentMode = (config.equipmentMode + 1) % 2; b.setMessage(equipmentModeText(config.equipmentMode)); }));
                addDrawableChild(new HudButton(controlsX, cy += 24, controlsW, 20, equipmentSideText(config.equipmentHotbarSide), b -> { config.equipmentHotbarSide = (config.equipmentHotbarSide + 1) % 2; b.setMessage(equipmentSideText(config.equipmentHotbarSide)); }));
                addDrawableChild(new HudButton(controlsX, cy += 24, controlsW, 20, equipmentDisplayText(config.equipmentDurabilityDisplay), b -> { config.equipmentDurabilityDisplay = (config.equipmentDurabilityDisplay + 1) % 3; b.setMessage(equipmentDisplayText(config.equipmentDurabilityDisplay)); }));
                addSlider(controlsX, cy += 24, controlsW, "slider.equipment_scale", config.equipmentScale, 0.5f, 2.5f, value -> config.equipmentScale = value);
                addSlider(controlsX, cy += 24, controlsW, "slider.equipment_width", config.equipmentWidth, 120, 320, value -> config.equipmentWidth = Math.round(value));
                addSlider(controlsX, cy += 24, controlsW, "slider.equipment_radius", config.equipmentRadius, 0, 12, value -> config.equipmentRadius = Math.round(value));
                addToggle(controlsX, cy += 24, controlsW, "toggle.equipment_warning", config.equipmentWarning, value -> config.equipmentWarning = value);
                addSlider(controlsX, cy += 24, controlsW, "slider.equipment_warning", config.equipmentWarningPercent, 1, 50, value -> config.equipmentWarningPercent = Math.round(value));
            }
            case CPS -> {
                addToggle(controlsX, cy, controlsW, "toggle.cps", config.cpsEnabled, value -> config.cpsEnabled = value);
                addSlider(controlsX, cy += 24, controlsW, "slider.cps_scale", config.cpsScale, 0.5f, 2.5f, value -> config.cpsScale = value);
                addSlider(controlsX, cy += 24, controlsW, "slider.cps_width", config.cpsWidth, 72, 240, value -> config.cpsWidth = Math.round(value));
                addSlider(controlsX, cy += 24, controlsW, "slider.cps_radius", config.cpsRadius, 0, 12, value -> config.cpsRadius = Math.round(value));
            }
            case BLOCK_OUTLINE -> addToggle(controlsX, cy, controlsW, "toggle.block_outline", config.customBlockOutline, value -> config.customBlockOutline = value);
        }

        int colorX = colorPickerX((width - settingsPanelWidth()) / 2, settingsPanelWidth());
        int colorY = y + 12;
        for (ColorTarget target : activeModule.colors) {
            addDrawableChild(new HudButton(colorX, colorY, 210, 20, tr(target.labelKey), button -> {
                colorTarget = target;
                clearAndInit();
            }));
            colorY += 24;
        }
        addDrawableChild(new HudButton(contentLeft + 10, backY, 120, 20, tr("button.modules_back"), button -> {
            activeModule = null;
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
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.ui(SoundEvents.UI_BUTTON_CLICK, 1.0f));
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
        if (textRenderer.getWidth(value) <= maxWidth) {
            return value;
        }
        return textRenderer.trimToWidth(value, Math.max(8, maxWidth - textRenderer.getWidth("..."))) + "...";
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
        BLOCK_OUTLINE("module.block_outline", ColorTarget.BLOCK_OUTLINE);

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
        CPS_MOVE,
        CPS_RESIZE
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
        CPS_BG("color.cps_background", "CPB") {
            int get(HudForgeConfig c) { return c.cpsBackground; }
            void set(HudForgeConfig c, int v) { c.cpsBackground = v; }
        },
        CPS_TEXT("color.cps_text", "CPT") {
            int get(HudForgeConfig c) { return c.cpsText; }
            void set(HudForgeConfig c, int v) { c.cpsText = v; }
        },
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

        private HudButton(int x, int y, int width, int height, Text message, Consumer<HudButton> onPress) {
            super(x, y, width, height, message);
            this.onPress = onPress;
        }

        private void setSelectedStyle(boolean selectedStyle) {
            this.selectedStyle = selectedStyle;
        }

        @Override
        public void onPress(AbstractInput input) {
            onPress.accept(this);
        }

        @Override
        protected void drawIcon(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
            int bg = selectedStyle || isHovered() || isSelected() ? CONTROL_HOVER : CONTROL;
            fillSoftRect(context, getX(), getY(), getWidth(), getHeight(), 3, bg);
            drawBorder(context, getX(), getY(), getWidth(), getHeight(), selectedStyle || isHovered() || isSelected() ? BLUE : BORDER);
            TextRenderer tr = MinecraftClient.getInstance().textRenderer;
            Text message = getMessage();
            context.drawTextWithShadow(tr, message, getX() + getWidth() / 2 - tr.getWidth(message) / 2, getY() + (getHeight() - 8) / 2, WHITE);
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
