/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1109
 *  net.minecraft.class_1113
 *  net.minecraft.class_11907
 *  net.minecraft.class_11909
 *  net.minecraft.class_2561
 *  net.minecraft.class_266
 *  net.minecraft.class_268
 *  net.minecraft.class_269
 *  net.minecraft.class_270
 *  net.minecraft.class_310
 *  net.minecraft.class_327
 *  net.minecraft.class_332
 *  net.minecraft.class_3417
 *  net.minecraft.class_3532
 *  net.minecraft.class_357
 *  net.minecraft.class_364
 *  net.minecraft.class_4264
 *  net.minecraft.class_437
 *  net.minecraft.class_5348
 *  net.minecraft.class_6382
 *  net.minecraft.class_6880
 *  net.minecraft.class_8646
 *  net.minecraft.class_9011
 */
package dev.abc_nova.hudfab.gui;

import dev.abc_nova.hudfab.HudForgeClient;
import dev.abc_nova.hudfab.config.HudForgeConfig;
import java.awt.Color;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.class_1109;
import net.minecraft.class_1113;
import net.minecraft.class_11907;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_266;
import net.minecraft.class_268;
import net.minecraft.class_269;
import net.minecraft.class_270;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3417;
import net.minecraft.class_3532;
import net.minecraft.class_357;
import net.minecraft.class_364;
import net.minecraft.class_4264;
import net.minecraft.class_437;
import net.minecraft.class_5348;
import net.minecraft.class_6382;
import net.minecraft.class_6880;
import net.minecraft.class_8646;
import net.minecraft.class_9011;

public final class HudForgeConfigScreen
extends class_437 {
    private static final int PANEL = -401730268;
    private static final int CONTROL = -971299528;
    private static final int CONTROL_HOVER = -584829357;
    private static final int BLUE = -10258433;
    private static final int BORDER = -2007208294;
    private static final int WHITE = -1380097;
    private final class_437 parent;
    private boolean settingsOpen;
    private SettingsPage page = SettingsPage.SCOREBOARD;
    private ColorTarget colorTarget = ColorTarget.BOARD_BG;
    private DragTarget dragTarget = DragTarget.NONE;
    private int dragOffsetX;
    private int dragOffsetY;

    public HudForgeConfigScreen(class_437 parent) {
        this(parent, false);
    }

    public HudForgeConfigScreen(class_437 parent, boolean openSettings) {
        super(HudForgeConfigScreen.tr("title", new Object[0]));
        this.parent = parent;
        this.settingsOpen = openSettings;
    }

    protected void method_25426() {
        HudForgeConfig config = HudForgeClient.config.clamped();
        if (this.settingsOpen) {
            this.initSettings(config);
        } else {
            this.method_37063((class_364)new HudButton(this.field_22789 - 132, this.field_22790 - 28, 120, 20, HudForgeConfigScreen.tr("button.settings", new Object[0]), button -> {
                this.settingsOpen = true;
                this.method_41843();
            }));
            this.method_37063((class_364)new HudButton(this.field_22789 - 258, this.field_22790 - 28, 120, 20, HudForgeConfigScreen.tr("button.done", new Object[0]), button -> this.method_25419()));
        }
    }

    private void initSettings(HudForgeConfig config) {
        int panelW = this.settingsPanelWidth();
        int panelH = this.settingsPanelHeight();
        int left = this.field_22789 - panelW - 12;
        int top = this.field_22790 - panelH - 34;
        int y = top + 52;
        int tabW = (panelW - 24) / SettingsPage.values().length;
        for (SettingsPage value2 : SettingsPage.values()) {
            int x = left + 8 + value2.ordinal() * tabW;
            HudButton tab = new HudButton(x, top + 6, tabW - 4, 18, this.tabText(value2), button -> {
                this.page = value2;
                this.method_41843();
            });
            tab.setSelectedStyle(this.page == value2);
            this.method_37063((class_364)tab);
        }
        switch (this.page.ordinal()) {
            case 0: {
                this.method_37063((class_364)new HudButton(left + 10, y, panelW - 20, 20, HudForgeConfigScreen.scoreboardButtonText(config.scoreboardMode), button -> {
                    config.scoreboardMode = (config.scoreboardMode + 1) % 3;
                    button.method_25355(HudForgeConfigScreen.scoreboardButtonText(config.scoreboardMode));
                }));
                this.addSlider(left + 10, y += 24, panelW - 20, "slider.width", config.scoreboardWidth, 90.0f, 320.0f, value -> {
                    config.scoreboardWidth = Math.round(value.floatValue());
                });
                this.addSlider(left + 10, y += 24, panelW - 20, "slider.scale", config.scoreboardScale, 0.5f, 2.5f, value -> {
                    config.scoreboardScale = value.floatValue();
                });
                this.addSlider(left + 10, y += 24, panelW - 20, "slider.board_radius", config.scoreboardRadius, 0.0f, 12.0f, value -> {
                    config.scoreboardRadius = Math.round(value.floatValue());
                });
                this.addToggle(left + 10, y += 24, panelW - 20, "toggle.server_colors", config.scoreboardServerColors, value -> {
                    config.scoreboardServerColors = value;
                });
                this.addToggle(left + 10, y += 24, panelW - 20, "toggle.stats", config.statsEnabled, value -> {
                    config.statsEnabled = value;
                });
                this.addToggle(left + 10, y += 24, panelW - 20, "toggle.block_outline", config.customBlockOutline, value -> {
                    config.customBlockOutline = value;
                });
                this.addSlider(left + 10, y += 24, panelW - 20, "slider.stats_scale", config.statsScale, 0.5f, 2.5f, value -> {
                    config.statsScale = value.floatValue();
                });
                this.addSlider(left + 10, y += 24, panelW - 20, "slider.stats_radius", config.statsRadius, 0.0f, 12.0f, value -> {
                    config.statsRadius = Math.round(value.floatValue());
                });
                break;
            }
            case 2: {
                this.method_37063((class_364)new HudButton(left + 10, y, panelW - 20, 20, HudForgeConfigScreen.crosshairButtonText(config.crosshairMode), button -> {
                    config.crosshairMode = (config.crosshairMode + 1) % 4;
                    config.customCrosshair = config.crosshairMode != 0;
                    button.method_25355(HudForgeConfigScreen.crosshairButtonText(config.crosshairMode));
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
                break;
            }
            case 1: {
                this.method_37063((class_364)new HudButton(left + panelW - 124, top + panelH - 48, 112, 18, HudForgeConfigScreen.tr("button.clean_preset", new Object[0]), button -> {
                    HudForgeConfigScreen.applyCleanPreset(config);
                    this.method_41843();
                }));
                break;
            }
            case 3: {
                this.addToggle(left + 10, y, panelW - 20, "toggle.auto_respawn", config.autoRespawn, value -> {
                    config.autoRespawn = value;
                });
            }
        }
        this.method_37063((class_364)new HudButton(left + panelW - 88, top + panelH - 24, 78, 18, HudForgeConfigScreen.tr("button.back", new Object[0]), button -> {
            this.settingsOpen = false;
            this.method_41843();
        }));
    }

    public void method_25419() {
        HudForgeClient.saveActiveServerProfile();
        HudForgeClient.config.save();
        class_310.method_1551().method_1507(this.parent);
    }

    public void method_25432() {
        HudForgeClient.saveActiveServerProfile();
        HudForgeClient.config.save();
    }

    public boolean method_25402(class_11909 click, boolean doubleClick) {
        if (this.settingsOpen) {
            if (this.page == SettingsPage.COLORS && click.method_74245() == 0 && this.handleColorTargetClick(click.comp_4798(), click.comp_4799())) {
                HudForgeConfigScreen.playButtonSound();
                return true;
            }
            if (this.page == SettingsPage.COLORS && click.method_74245() == 0 && this.handleColorPick(click.comp_4798(), click.comp_4799())) {
                return true;
            }
            return super.method_25402(click, doubleClick);
        }
        HudForgeConfig config = HudForgeClient.config;
        if (click.method_74245() == 0) {
            if (config.scoreboardMode == 0 && this.hasServerScoreboard() && HudForgeConfigScreen.inside(click.comp_4798(), click.comp_4799(), config.scoreboardX, config.scoreboardY, this.scoreboardWidth(config), this.scoreboardHeight(config))) {
                if (HudForgeConfigScreen.inside(click.comp_4798(), click.comp_4799(), config.scoreboardX + this.scoreboardWidth(config) - 10, config.scoreboardY + this.scoreboardHeight(config) - 10, 10, 10)) {
                    this.dragTarget = DragTarget.SCOREBOARD_RESIZE;
                } else {
                    this.dragTarget = DragTarget.SCOREBOARD_MOVE;
                    this.dragOffsetX = (int)click.comp_4798() - config.scoreboardX;
                    this.dragOffsetY = (int)click.comp_4799() - config.scoreboardY;
                }
                return true;
            }
            if (config.statsEnabled && HudForgeConfigScreen.inside(click.comp_4798(), click.comp_4799(), config.statsX, config.statsY, this.statsWidth(config), this.statsHeight(config))) {
                if (HudForgeConfigScreen.inside(click.comp_4798(), click.comp_4799(), config.statsX + this.statsWidth(config) - 10, config.statsY + this.statsHeight(config) - 10, 10, 10)) {
                    this.dragTarget = DragTarget.STATS_RESIZE;
                } else {
                    this.dragTarget = DragTarget.STATS_MOVE;
                    this.dragOffsetX = (int)click.comp_4798() - config.statsX;
                    this.dragOffsetY = (int)click.comp_4799() - config.statsY;
                }
                return true;
            }
        }
        return super.method_25402(click, doubleClick);
    }

    public boolean method_25403(class_11909 click, double deltaX, double deltaY) {
        if (this.settingsOpen) {
            if (this.page == SettingsPage.COLORS && this.handleColorPick(click.comp_4798(), click.comp_4799())) {
                return true;
            }
            return super.method_25403(click, deltaX, deltaY);
        }
        HudForgeConfig config = HudForgeClient.config;
        switch (this.dragTarget.ordinal()) {
            case 1: {
                config.scoreboardX = HudForgeConfigScreen.clamp((int)click.comp_4798() - this.dragOffsetX, 0, Math.max(0, this.field_22789 - this.scoreboardWidth(config)));
                config.scoreboardY = HudForgeConfigScreen.clamp((int)click.comp_4799() - this.dragOffsetY, 0, Math.max(0, this.field_22790 - this.scoreboardHeight(config)));
                return true;
            }
            case 3: {
                config.statsX = HudForgeConfigScreen.clamp((int)click.comp_4798() - this.dragOffsetX, 0, Math.max(0, this.field_22789 - this.statsWidth(config)));
                config.statsY = HudForgeConfigScreen.clamp((int)click.comp_4799() - this.dragOffsetY, 0, Math.max(0, this.field_22790 - this.statsHeight(config)));
                return true;
            }
            case 2: {
                int rawWidth = Math.round(((float)click.comp_4798() - (float)config.scoreboardX) / Math.max(0.5f, config.scoreboardScale));
                config.scoreboardWidth = HudForgeConfigScreen.clamp(rawWidth, 90, 320);
                float newScale = ((float)click.comp_4799() - (float)config.scoreboardY) / (float)Math.max(42, this.scoreboardBaseHeight());
                config.scoreboardScale = HudForgeConfigScreen.clamp(newScale, 0.5f, 2.5f);
                return true;
            }
            case 4: {
                float newScale = Math.max(((float)click.comp_4798() - (float)config.statsX) / 82.0f, ((float)click.comp_4799() - (float)config.statsY) / 32.0f);
                config.statsScale = HudForgeConfigScreen.clamp(newScale, 0.5f, 2.5f);
                return true;
            }
        }
        return super.method_25403(click, deltaX, deltaY);
    }

    public boolean method_25406(class_11909 click) {
        this.dragTarget = DragTarget.NONE;
        HudForgeClient.saveActiveServerProfile();
        return super.method_25406(click);
    }

    public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
        context.method_25294(0, 0, this.field_22789, this.field_22790, this.settingsOpen ? -1610612736 : 0x70000000);
        if (!this.settingsOpen) {
            this.renderHudCanvas(context);
        }
        if (this.settingsOpen) {
            this.renderSettingsPanel(context, mouseX, mouseY);
        } else {
            context.method_27535(this.field_22793, HudForgeConfigScreen.tr("editor.help", new Object[0]), 10, 10, -4669228);
        }
        super.method_25394(context, mouseX, mouseY, delta);
    }

    private void renderHudCanvas(class_332 context) {
        HudForgeConfig config = HudForgeClient.config;
        if (config.scoreboardMode == 0 && this.hasServerScoreboard()) {
            this.renderScoreboardPreview(context, config);
        }
        if (config.statsEnabled) {
            this.renderStatsPreview(context, config);
        }
    }

    private void renderScoreboardPreview(class_332 context, HudForgeConfig config) {
        class_310 client = class_310.method_1551();
        class_266 objective = client.field_1687.method_8428().method_1189(class_8646.field_45157);
        List<class_9011> entries = this.scoreboardEntries(objective);
        if (objective == null || entries.isEmpty()) {
            return;
        }
        class_327 tr = this.field_22793;
        int width = config.scoreboardWidth;
        int titleHeight = 18;
        int rowHeight = 11;
        int height = titleHeight + entries.size() * rowHeight + 6;
        context.method_51448().pushMatrix();
        context.method_51448().translate((float)config.scoreboardX, (float)config.scoreboardY);
        context.method_51448().scale(config.scoreboardScale, config.scoreboardScale);
        HudForgeConfigScreen.fillSoftRect(context, 0, 0, width, height, config.scoreboardRadius, config.scoreboardBackground);
        context.method_27535(tr, objective.method_1114(), class_3532.method_15340((int)((width - tr.method_27525((class_5348)objective.method_1114())) / 2), (int)8, (int)(width - 8)), 5, config.scoreboardTitle);
        int y = titleHeight;
        for (class_9011 entry : entries) {
            class_2561 line;
            Object object = line = entry.comp_2129() == null ? class_268.method_1142((class_270)client.field_1687.method_8428().method_1164(entry.comp_2127()), (class_2561)class_2561.method_43470((String)entry.comp_2127())) : entry.comp_2129();
            if (config.scoreboardServerColors) {
                context.method_27535(tr, line, 9, y, config.scoreboardText);
            } else {
                context.method_25303(tr, this.trimToWidth(line.getString(), width - 18), 9, y, config.scoreboardText);
            }
            y += rowHeight;
        }
        context.method_51448().popMatrix();
        HudForgeConfigScreen.renderHandle(context, config.scoreboardX + this.scoreboardWidth(config), config.scoreboardY + this.scoreboardHeight(config));
    }

    private void renderStatsPreview(class_332 context, HudForgeConfig config) {
        context.method_51448().pushMatrix();
        context.method_51448().translate((float)config.statsX, (float)config.statsY);
        context.method_51448().scale(config.statsScale, config.statsScale);
        HudForgeConfigScreen.fillSoftRect(context, 0, 0, 82, 32, config.statsRadius, config.statsBackground);
        context.method_25303(this.field_22793, class_310.method_1551().method_47599() + " FPS", 8, 5, config.statsText);
        context.method_25303(this.field_22793, this.currentPing() + " ms", 8, 18, config.statsText);
        context.method_51448().popMatrix();
        HudForgeConfigScreen.renderHandle(context, config.statsX + this.statsWidth(config), config.statsY + this.statsHeight(config));
    }

    private void renderSettingsPanel(class_332 context, int mouseX, int mouseY) {
        int panelW = this.settingsPanelWidth();
        int panelH = this.settingsPanelHeight();
        int left = this.field_22789 - panelW - 12;
        int top = this.field_22790 - panelH - 34;
        context.method_25294(left, top, left + panelW, top + panelH, -401730268);
        context.method_25294(left, top, left + panelW, top + 1, -2007208294);
        context.method_25294(left, top + panelH - 1, left + panelW, top + panelH, -2007208294);
        context.method_25294(left, top, left + 1, top + panelH, -2007208294);
        context.method_25294(left + panelW - 1, top, left + panelW, top + panelH, -2007208294);
        context.method_27535(this.field_22793, HudForgeConfigScreen.tr(this.page.titleKey, new Object[0]), left + 10, top + 30, -1380097);
        if (this.page == SettingsPage.COLORS) {
            this.renderColorTargets(context, left + 10, top + 52);
            this.renderColorPicker(context, left + 10, top + 140, panelW - 20, this.colorTarget.get(HudForgeClient.config));
        }
    }

    private void renderColorPicker(class_332 context, int x, int y, int width, int color) {
        int yy;
        int square = Math.min(78, Math.max(52, width - 210));
        float hue = HudForgeConfigScreen.rgbToHue(color);
        for (int xx = 0; xx < square; xx += 2) {
            for (yy = 0; yy < square; yy += 2) {
                float sat = (float)xx / (float)(square - 1);
                float val = 1.0f - (float)yy / (float)(square - 1);
                context.method_25294(x + xx, y + yy, x + xx + 2, y + yy + 2, HudForgeConfigScreen.hsvToRgb(hue, sat, val, HudForgeConfigScreen.alpha(color)));
            }
        }
        int hueX = x + square + 10;
        for (yy = 0; yy < square; yy += 2) {
            context.method_25294(hueX, y + yy, hueX + 12, y + yy + 2, HudForgeConfigScreen.hsvToRgb((float)yy / (float)square, 1.0f, 1.0f, 255));
        }
        int alphaY = y + square + 8;
        for (int xx = 0; xx < square; xx += 2) {
            context.method_25294(x + xx, alphaY, x + xx + 2, alphaY + 10, HudForgeConfigScreen.withAlpha(color, Math.round((float)xx / (float)square * 255.0f)));
        }
        int previewX = x + square + 28;
        HudForgeConfigScreen.fillSoftRect(context, previewX, y, 26, 26, 4, color);
        context.method_27535(this.field_22793, HudForgeConfigScreen.tr(this.colorTarget.labelKey, new Object[0]), previewX, y + 34, -1380097);
        int inputY = alphaY + 18;
        this.drawNumberBox(context, x, inputY, "R", color >> 16 & 0xFF);
        this.drawNumberBox(context, x + 54, inputY, "G", color >> 8 & 0xFF);
        this.drawNumberBox(context, x + 108, inputY, "B", color & 0xFF);
        this.drawNumberBox(context, x + 162, inputY, "A", HudForgeConfigScreen.alpha(color));
    }

    private boolean handleColorPick(double mouseX, double mouseY) {
        int panelW = this.settingsPanelWidth();
        int left = this.field_22789 - panelW - 12;
        int top = this.field_22790 - this.settingsPanelHeight() - 34;
        int x = left + 10;
        int y = top + 140;
        int square = Math.min(78, Math.max(52, panelW - 230));
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
        int left = this.field_22789 - panelW - 12;
        int top = this.field_22790 - this.settingsPanelHeight() - 34;
        int x = left + 10;
        int y = top + 52;
        int i = 0;
        for (ColorTarget target : ColorTarget.values()) {
            int tx = x + i % 3 * 102;
            int ty = y + i / 3 * 27;
            if (HudForgeConfigScreen.inside(mouseX, mouseY, tx, ty, 96, 22)) {
                this.colorTarget = target;
                return true;
            }
            ++i;
        }
        return false;
    }

    private int settingsPanelWidth() {
        return Math.min(this.page == SettingsPage.COLORS ? 326 : 336, this.field_22789 - 24);
    }

    private int settingsPanelHeight() {
        return Math.min(this.page == SettingsPage.COLORS ? 318 : 292, this.field_22790 - 34);
    }

    private boolean hasServerScoreboard() {
        class_310 client = class_310.method_1551();
        return client.field_1687 != null && client.field_1687.method_8428().method_1189(class_8646.field_45157) != null;
    }

    private void renderColorTargets(class_332 context, int x, int y) {
        int i = 0;
        for (ColorTarget target : ColorTarget.values()) {
            int tx = x + i % 3 * 102;
            int ty = y + i / 3 * 27;
            int color = target.get(HudForgeClient.config);
            if (target == this.colorTarget) {
                HudForgeConfigScreen.fillSoftRect(context, tx - 2, ty - 2, 100, 26, 4, -9601793);
            }
            HudForgeConfigScreen.fillSoftRect(context, tx, ty, 96, 22, 3, target == this.colorTarget ? -584829357 : -971299528);
            HudForgeConfigScreen.fillSoftRect(context, tx + 4, ty + 4, 14, 14, 2, color);
            context.method_25303(this.field_22793, this.fitLabel(HudForgeConfigScreen.tr(target.labelKey, new Object[0]).getString(), 66), tx + 24, ty + 7, -3288094);
            ++i;
        }
    }

    private String fitLabel(String label, int maxWidth) {
        if (this.field_22793.method_1727(label) <= maxWidth) {
            return label;
        }
        return this.field_22793.method_27523(label, Math.max(8, maxWidth - this.field_22793.method_1727("..."))) + "...";
    }

    private void drawNumberBox(class_332 context, int x, int y, String label, int value) {
        context.method_25294(x, y, x + 48, y + 18, -1446411);
        context.method_25294(x, y, x + 48, y + 1, -4603698);
        context.method_25294(x, y + 17, x + 48, y + 18, -4603698);
        String text = String.valueOf(value);
        context.method_51433(this.field_22793, text, x + 24 - this.field_22793.method_1727(text) / 2, y + 5, -15658216, false);
        context.method_25303(this.field_22793, label, x + 24 - this.field_22793.method_1727(label) / 2, y + 22, -1380097);
    }

    private List<class_9011> scoreboardEntries(class_266 objective) {
        if (objective == null || class_310.method_1551().field_1687 == null) {
            return List.of();
        }
        class_269 scoreboard = class_310.method_1551().field_1687.method_8428();
        return scoreboard.method_1184(objective).stream().filter(entry -> entry.comp_2128() != 0 || entry.comp_2129() != null).sorted(Comparator.comparingInt(class_9011::comp_2128).reversed()).limit(15L).toList();
    }

    private int scoreboardBaseHeight() {
        class_310 client = class_310.method_1551();
        if (client.field_1687 == null) {
            return 0;
        }
        class_266 objective = client.field_1687.method_8428().method_1189(class_8646.field_45157);
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

    private int currentPing() {
        class_310 client = class_310.method_1551();
        if (client.field_1724 != null && client.method_1562() != null && client.method_1562().method_2871(client.field_1724.method_5667()) != null) {
            return client.method_1562().method_2871(client.field_1724.method_5667()).method_2959();
        }
        return 0;
    }

    private void addToggle(int x, int y, int width, String key, boolean value, Consumer<Boolean> setter) {
        this.method_37063((class_364)new HudButton(x, y, width, 20, HudForgeConfigScreen.toggleText(key, value), button -> {
            boolean next = !button.method_25369().getString().contains(HudForgeConfigScreen.tr("value.on", new Object[0]).getString());
            setter.accept(next);
            button.method_25355(HudForgeConfigScreen.toggleText(key, next));
        }));
    }

    private void addSlider(int x, int y, int width, String key, float value, float min, float max, Consumer<Float> setter) {
        this.method_37063((class_364)new HudSlider(x, y, width, 20, key, value, min, max, setter));
    }

    private static void playButtonSound() {
        class_310.method_1551().method_1483().method_4873((class_1113)class_1109.method_47978((class_6880)class_3417.field_15015, (float)1.0f));
    }

    private static void renderHandle(class_332 context, int x, int y) {
        context.method_25294(x - 8, y - 8, x, y, -855638017);
        context.method_25294(x - 6, y - 2, x - 2, y, -15658216);
        context.method_25294(x - 2, y - 6, x, y - 2, -15658216);
    }

    private static void fillSoftRect(class_332 context, int x, int y, int width, int height, int radius, int color) {
        int r = Math.max(0, Math.min(radius, Math.min(width, height) / 2));
        if (r <= 1) {
            context.method_25294(x, y, x + width, y + height, color);
            return;
        }
        context.method_25294(x + r, y, x + width - r, y + height, color);
        context.method_25294(x, y + r, x + width, y + height - r, color);
        for (int i = 0; i < r; ++i) {
            int inset = Math.max(0, r - i - 1);
            context.method_25294(x + inset, y + i, x + width - inset, y + i + 1, color);
            context.method_25294(x + inset, y + height - i - 1, x + width - inset, y + height - i, color);
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
    }

    private String trimToWidth(String value, int maxWidth) {
        if (this.field_22793.method_1727(value) <= maxWidth) {
            return value;
        }
        return this.field_22793.method_27523(value, Math.max(8, maxWidth - this.field_22793.method_1727("..."))) + "...";
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

    private static class_2561 tr(String key, Object ... args) {
        return class_2561.method_43469((String)("hudfabric." + key), (Object[])args);
    }

    private static class_2561 scoreboardButtonText(int mode) {
        return HudForgeConfigScreen.tr("button.scoreboard", HudForgeConfigScreen.tr(HudForgeConfigScreen.scoreboardModeLabel(mode), new Object[0]));
    }

    private static class_2561 crosshairButtonText(int mode) {
        return HudForgeConfigScreen.tr("button.mode", HudForgeConfigScreen.tr(HudForgeConfigScreen.crosshairModeLabel(mode), new Object[0]));
    }

    private static class_2561 toggleText(String key, boolean value) {
        return HudForgeConfigScreen.tr("button.toggle", HudForgeConfigScreen.tr(key, new Object[0]), HudForgeConfigScreen.tr(value ? "value.on" : "value.off", new Object[0]));
    }

    private class_2561 tabText(SettingsPage value) {
        return value == this.page ? class_2561.method_43470((String)"* ").method_10852(HudForgeConfigScreen.tr(value.labelKey, new Object[0])) : HudForgeConfigScreen.tr(value.labelKey, new Object[0]);
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

    private static void drawBorder(class_332 context, int x, int y, int width, int height, int color) {
        context.method_25294(x, y, x + width, y + 1, color);
        context.method_25294(x, y + height - 1, x + width, y + height, color);
        context.method_25294(x, y, x + 1, y + height, color);
        context.method_25294(x + width - 1, y, x + width, y + height, color);
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
        STATS_RESIZE;

    }

    private static final class HudButton
    extends class_4264 {
        private final Consumer<HudButton> onPress;
        private boolean selectedStyle;

        private HudButton(int x, int y, int width, int height, class_2561 message, Consumer<HudButton> onPress) {
            super(x, y, width, height, message);
            this.onPress = onPress;
        }

        private void setSelectedStyle(boolean selectedStyle) {
            this.selectedStyle = selectedStyle;
        }

        public void method_25306(class_11907 input) {
            this.onPress.accept(this);
        }

        protected void method_75752(class_332 context, int mouseX, int mouseY, float deltaTicks) {
            int bg = this.selectedStyle || this.method_49606() || this.method_25367() ? -584829357 : -971299528;
            HudForgeConfigScreen.fillSoftRect(context, this.method_46426(), this.method_46427(), this.method_25368(), this.method_25364(), 3, bg);
            HudForgeConfigScreen.drawBorder(context, this.method_46426(), this.method_46427(), this.method_25368(), this.method_25364(), this.selectedStyle || this.method_49606() || this.method_25367() ? -10258433 : -2007208294);
            if (this.selectedStyle) {
                context.method_25294(this.method_46426() + 2, this.method_46427() + this.method_25364() - 3, this.method_46426() + this.method_25368() - 2, this.method_46427() + this.method_25364() - 2, -10258433);
            }
            class_327 tr = class_310.method_1551().field_1772;
            class_2561 message = this.method_25369();
            context.method_27535(tr, message, this.method_46426() + this.method_25368() / 2 - tr.method_27525((class_5348)message) / 2, this.method_46427() + (this.method_25364() - 8) / 2, -1380097);
        }

        protected void method_47399(class_6382 builder) {
            this.method_37021(builder);
        }
    }

    private static final class HudSlider
    extends class_357 {
        private final String key;
        private final float min;
        private final float max;
        private final Consumer<Float> setter;

        private HudSlider(int x, int y, int width, int height, String key, float value, float min, float max, Consumer<Float> setter) {
            super(x, y, width, height, (class_2561)class_2561.method_43473(), Math.max(0.0, Math.min(1.0, (double)((value - min) / (max - min)))));
            this.key = key;
            this.min = min;
            this.max = max;
            this.setter = setter;
            this.method_25346();
        }

        protected void method_25346() {
            float current = this.min + (float)this.field_22753 * (this.max - this.min);
            String rendered = Math.abs(current - (float)Math.round(current)) < 0.01f ? String.valueOf(Math.round(current)) : String.format(Locale.ROOT, "%.2f", Float.valueOf(current));
            this.method_25355(HudForgeConfigScreen.tr("button.slider", HudForgeConfigScreen.tr(this.key, new Object[0]), rendered));
        }

        protected void method_25344() {
            this.setter.accept(Float.valueOf(this.min + (float)this.field_22753 * (this.max - this.min)));
        }

        public void method_48579(class_332 context, int mouseX, int mouseY, float deltaTicks) {
            int bg = this.method_49606() || this.method_25367() ? -584829357 : -971299528;
            HudForgeConfigScreen.fillSoftRect(context, this.method_46426(), this.method_46427(), this.method_25368(), this.method_25364(), 3, bg);
            HudForgeConfigScreen.drawBorder(context, this.method_46426(), this.method_46427(), this.method_25368(), this.method_25364(), this.method_49606() || this.method_25367() ? -10258433 : -2007208294);
            int knobX = this.method_46426() + 2 + Math.round((float)(this.method_25368() - 8) * (float)this.field_22753);
            HudForgeConfigScreen.fillSoftRect(context, knobX, this.method_46427() + 2, 5, this.method_25364() - 4, 1, -1774593);
            class_327 tr = class_310.method_1551().field_1772;
            class_2561 message = this.method_25369();
            context.method_27535(tr, message, this.method_46426() + this.method_25368() / 2 - tr.method_27525((class_5348)message) / 2, this.method_46427() + 6, -1380097);
        }
    }
}

