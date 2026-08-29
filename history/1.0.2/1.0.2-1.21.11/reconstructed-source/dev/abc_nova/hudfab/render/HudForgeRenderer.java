/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2561
 *  net.minecraft.class_266
 *  net.minecraft.class_268
 *  net.minecraft.class_269
 *  net.minecraft.class_270
 *  net.minecraft.class_310
 *  net.minecraft.class_327
 *  net.minecraft.class_332
 *  net.minecraft.class_3532
 *  net.minecraft.class_5348
 *  net.minecraft.class_640
 *  net.minecraft.class_8646
 *  net.minecraft.class_9011
 *  net.minecraft.class_9779
 */
package dev.abc_nova.hudfab.render;

import dev.abc_nova.hudfab.HudForgeClient;
import dev.abc_nova.hudfab.config.HudForgeConfig;
import java.util.ArrayList;
import java.util.Comparator;
import net.minecraft.class_2561;
import net.minecraft.class_266;
import net.minecraft.class_268;
import net.minecraft.class_269;
import net.minecraft.class_270;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_5348;
import net.minecraft.class_640;
import net.minecraft.class_8646;
import net.minecraft.class_9011;
import net.minecraft.class_9779;

public final class HudForgeRenderer {
    private HudForgeRenderer() {
    }

    public static void render(class_332 context, class_9779 tickCounter) {
        class_310 client = class_310.method_1551();
        HudForgeConfig config = HudForgeClient.config;
        if (client.field_1690.field_1842 || config == null) {
            return;
        }
        if (config.statsEnabled) {
            HudForgeRenderer.renderStats(context, client, config);
        }
        if (config.scoreboardMode == 0) {
            HudForgeRenderer.renderScoreboard(context, client, config);
        }
        if (config.crosshairMode != 0 && client.field_1755 == null && client.field_1690.method_31044().method_31034()) {
            HudForgeRenderer.renderCrosshair(context, config, context.method_51421(), context.method_51443());
        }
    }

    private static void renderStats(class_332 context, class_310 client, HudForgeConfig config) {
        class_640 entry;
        class_327 textRenderer = client.field_1772;
        int ping = 0;
        if (client.field_1724 != null && client.method_1562() != null && (entry = client.method_1562().method_2871(client.field_1724.method_5667())) != null) {
            ping = entry.method_2959();
        }
        String fps = client.method_47599() + " FPS";
        String latency = ping + " ms";
        int width = 82;
        int height = 32;
        context.method_51448().pushMatrix();
        context.method_51448().translate((float)config.statsX, (float)config.statsY);
        context.method_51448().scale(config.statsScale, config.statsScale);
        HudForgeRenderer.fillSoftRect(context, 0, 0, width, height, config.statsRadius, config.statsBackground);
        context.method_25303(textRenderer, fps, 8, 5, config.statsText);
        context.method_25303(textRenderer, latency, 8, 18, config.statsText);
        context.method_51448().popMatrix();
    }

    private static void renderScoreboard(class_332 context, class_310 client, HudForgeConfig config) {
        if (client.field_1687 == null) {
            return;
        }
        class_269 scoreboard = client.field_1687.method_8428();
        class_266 objective = scoreboard.method_1189(class_8646.field_45157);
        if (objective == null) {
            return;
        }
        class_327 textRenderer = client.field_1772;
        ArrayList<class_9011> entries = new ArrayList<class_9011>(16);
        for (class_9011 entry : scoreboard.method_1184(objective)) {
            if (entry.comp_2128() == 0 && entry.comp_2129() == null) continue;
            entries.add(entry);
        }
        entries.sort(Comparator.comparingInt(class_9011::comp_2128).reversed());
        if (entries.isEmpty()) {
            return;
        }
        int width = config.scoreboardWidth;
        int titleHeight = 18;
        int rowHeight = 11;
        int rows = Math.min(15, entries.size());
        int height = titleHeight + rows * rowHeight + 6;
        context.method_51448().pushMatrix();
        context.method_51448().translate((float)config.scoreboardX, (float)config.scoreboardY);
        context.method_51448().scale(config.scoreboardScale, config.scoreboardScale);
        HudForgeRenderer.fillSoftRect(context, 0, 0, width, height, config.scoreboardRadius, config.scoreboardBackground);
        class_2561 title = objective.method_1114();
        int titleX = class_3532.method_15340((int)((width - textRenderer.method_27525((class_5348)title)) / 2), (int)8, (int)(width - 8));
        context.method_27535(textRenderer, title, titleX, 5, config.scoreboardTitle);
        int y = titleHeight;
        for (int i = 0; i < rows; ++i) {
            class_2561 line;
            class_9011 entry = (class_9011)entries.get(i);
            Object object = line = entry.comp_2129() == null ? class_268.method_1142((class_270)scoreboard.method_1164(entry.comp_2127()), (class_2561)class_2561.method_43470((String)entry.comp_2127())) : entry.comp_2129();
            if (config.scoreboardServerColors) {
                context.method_27535(textRenderer, line, 9, y, config.scoreboardText);
            } else {
                context.method_25303(textRenderer, HudForgeRenderer.trimToWidth(textRenderer, line.getString(), width - 18), 9, y, config.scoreboardText);
            }
            y += rowHeight;
        }
        context.method_51448().popMatrix();
    }

    private static void renderCrosshair(class_332 context, HudForgeConfig config, int width, int height) {
        int centerX = width / 2;
        int centerY = height / 2;
        int size = config.crosshairSize;
        int gap = config.crosshairGap;
        int thickness = config.crosshairThickness;
        int color = config.crosshairColor;
        if (config.crosshairMode == 3) {
            HudForgeRenderer.drawCenteredRect(context, centerX, centerY, Math.max(thickness, 2), Math.max(thickness, 2), color);
            return;
        }
        if (config.crosshairMode == 2) {
            int r = Math.max(3, size + gap);
            for (int i = -r; i <= r; ++i) {
                int span = Math.round((float)Math.sqrt(Math.max(0, r * r - i * i)));
                if (Math.abs(i) < r - thickness && span < r - thickness) continue;
                context.method_25294(centerX + i, centerY - span, centerX + i + 1, centerY - span + thickness, color);
                context.method_25294(centerX + i, centerY + span - thickness + 1, centerX + i + 1, centerY + span + 1, color);
            }
            return;
        }
        HudForgeRenderer.drawCenteredRect(context, centerX - gap - size / 2, centerY, size, thickness, color);
        HudForgeRenderer.drawCenteredRect(context, centerX + gap + size / 2, centerY, size, thickness, color);
        HudForgeRenderer.drawCenteredRect(context, centerX, centerY - gap - size / 2, thickness, size, color);
        HudForgeRenderer.drawCenteredRect(context, centerX, centerY + gap + size / 2, thickness, size, color);
        if (config.crosshairDot) {
            HudForgeRenderer.drawCenteredRect(context, centerX, centerY, thickness, thickness, color);
        }
    }

    private static void drawCenteredRect(class_332 context, int centerX, int centerY, int width, int height, int color) {
        int left = centerX - width / 2;
        int top = centerY - height / 2;
        context.method_25294(left, top, left + width, top + height, color);
    }

    private static String trimToWidth(class_327 textRenderer, String value, int maxWidth) {
        if (textRenderer.method_1727(value) <= maxWidth) {
            return value;
        }
        return textRenderer.method_27523(value, Math.max(8, maxWidth - textRenderer.method_1727("..."))) + "...";
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
}

