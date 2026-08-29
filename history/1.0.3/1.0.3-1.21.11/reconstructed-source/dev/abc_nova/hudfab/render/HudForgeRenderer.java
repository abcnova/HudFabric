/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10799
 *  net.minecraft.class_1291
 *  net.minecraft.class_1293
 *  net.minecraft.class_1304
 *  net.minecraft.class_1799
 *  net.minecraft.class_2338
 *  net.minecraft.class_2561
 *  net.minecraft.class_266
 *  net.minecraft.class_268
 *  net.minecraft.class_269
 *  net.minecraft.class_270
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_327
 *  net.minecraft.class_332
 *  net.minecraft.class_3532
 *  net.minecraft.class_5321
 *  net.minecraft.class_5348
 *  net.minecraft.class_640
 *  net.minecraft.class_8646
 *  net.minecraft.class_9011
 *  net.minecraft.class_9779
 */
package dev.abc_nova.hudfab.render;

import dev.abc_nova.hudfab.HudForgeClient;
import dev.abc_nova.hudfab.config.HudForgeConfig;
import java.lang.invoke.CallSite;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.class_10799;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1304;
import net.minecraft.class_1799;
import net.minecraft.class_2338;
import net.minecraft.class_2561;
import net.minecraft.class_266;
import net.minecraft.class_268;
import net.minecraft.class_269;
import net.minecraft.class_270;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_5321;
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
        if (config.coordsEnabled) {
            HudForgeRenderer.renderCoords(context, client, config);
        }
        if (config.effectsEnabled) {
            HudForgeRenderer.renderEffects(context, client, config);
        }
        if (config.equipmentEnabled) {
            HudForgeRenderer.renderEquipment(context, client, config);
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

    private static void renderCoords(class_332 context, class_310 client, HudForgeConfig config) {
        if (client.field_1724 == null || client.field_1687 == null) {
            return;
        }
        class_2338 pos = client.field_1724.method_24515();
        String biome = "unknown";
        try {
            Optional key = client.field_1687.method_30349().method_58561((class_5321)client.field_1687.method_23753(pos).method_40230().orElseThrow()).flatMap(entry -> entry.method_40230().map(registryKey -> registryKey.method_29177()));
            if (key.isPresent()) {
                biome = HudForgeRenderer.cleanName(((class_2960)key.get()).method_12832());
            }
        }
        catch (RuntimeException key) {
            // empty catch block
        }
        List<CallSite> lines = List.of("X: " + pos.method_10263(), "Y: " + pos.method_10264(), "Z: " + pos.method_10260(), "Biome: " + biome);
        HudForgeRenderer.renderTextPanel(context, client.field_1772, config.coordsX, config.coordsY, config.coordsScale, config.coordsWidth, config.coordsRadius, config.coordsBackground, config.coordsText, lines);
    }

    private static void renderEffects(class_332 context, class_310 client, HudForgeConfig config) {
        if (client.field_1724 == null || client.field_1724.method_6026().isEmpty()) {
            return;
        }
        ArrayList<EffectLine> lines = new ArrayList<EffectLine>();
        for (class_1293 effect : client.field_1724.method_6026()) {
            if (lines.size() >= 6) break;
            Object name = ((class_1291)effect.method_5579().comp_349()).method_5560().getString();
            if (effect.method_5578() > 0) {
                name = (String)name + " " + (effect.method_5578() + 1);
            }
            int color = 0xFF000000 | ((class_1291)effect.method_5579().comp_349()).method_5556() & 0xFFFFFF;
            String texturePath = effect.method_5579().method_40230().map(key -> "textures/mob_effect/" + key.method_29177().method_12832() + ".png").orElse("");
            lines.add(new EffectLine((String)name, HudForgeRenderer.formatDuration(effect.method_5584()), color, texturePath));
        }
        HudForgeRenderer.renderEffectsPanel(context, client.field_1772, config, lines);
    }

    private static void renderEquipment(class_332 context, class_310 client, HudForgeConfig config) {
        if (client.field_1724 == null) {
            return;
        }
        ArrayList<class_1799> lines = new ArrayList<class_1799>();
        HudForgeRenderer.addDurability(lines, client.field_1724.method_6047());
        HudForgeRenderer.addDurability(lines, client.field_1724.method_6118(class_1304.field_6169));
        HudForgeRenderer.addDurability(lines, client.field_1724.method_6118(class_1304.field_6174));
        HudForgeRenderer.addDurability(lines, client.field_1724.method_6118(class_1304.field_6172));
        HudForgeRenderer.addDurability(lines, client.field_1724.method_6118(class_1304.field_6166));
        if (lines.isEmpty()) {
            return;
        }
        HudForgeRenderer.renderEquipmentPanel(context, client.field_1772, config, lines);
    }

    private static void addDurability(List<class_1799> lines, class_1799 stack) {
        if (stack == null || stack.method_7960() || !stack.method_7963()) {
            return;
        }
        lines.add(stack);
    }

    private static void renderEffectsPanel(class_332 context, class_327 textRenderer, HudForgeConfig config, List<EffectLine> lines) {
        int width = config.effectsWidth;
        for (EffectLine line : lines) {
            width = Math.max(width, textRenderer.method_1727(line.name) + textRenderer.method_1727(line.duration) + 48);
        }
        width = Math.max(110, width);
        int rowHeight = 22;
        int height = 8 + lines.size() * rowHeight;
        context.method_51448().pushMatrix();
        context.method_51448().translate((float)config.effectsX, (float)config.effectsY);
        context.method_51448().scale(config.effectsScale, config.effectsScale);
        HudForgeRenderer.fillSoftRect(context, 0, 0, width, height, config.effectsRadius, config.effectsBackground);
        int y = 6;
        for (EffectLine line : lines) {
            HudForgeRenderer.fillSoftRect(context, 7, y - 1, 18, 18, Math.min(5, config.effectsRadius), 0x66000000);
            if (!line.texturePath.isBlank()) {
                context.method_25290(class_10799.field_56883, class_2960.method_60656((String)line.texturePath), 8, y, 0.0f, 0.0f, 18, 18, 18, 18);
            }
            context.method_25303(textRenderer, line.name, 31, y + 1, config.effectsText);
            int tx = width - textRenderer.method_1727(line.duration) - 8;
            context.method_25303(textRenderer, line.duration, tx, y + 10, -4669228);
            y += rowHeight;
        }
        context.method_51448().popMatrix();
    }

    private static void renderEquipmentPanel(class_332 context, class_327 textRenderer, HudForgeConfig config, List<class_1799> stacks) {
        int width = config.equipmentWidth;
        int rowHeight = 22;
        int height = 8 + stacks.size() * rowHeight;
        context.method_51448().pushMatrix();
        context.method_51448().translate((float)config.equipmentX, (float)config.equipmentY);
        context.method_51448().scale(config.equipmentScale, config.equipmentScale);
        HudForgeRenderer.fillSoftRect(context, 0, 0, width, height, config.equipmentRadius, config.equipmentBackground);
        int y = 6;
        for (class_1799 stack : stacks) {
            context.method_51427(stack, 7, y - 1);
            int max = stack.method_7936();
            int left = Math.max(0, max - stack.method_7919());
            String label = left + "/" + max;
            context.method_25303(textRenderer, label, 30, y + 1, config.equipmentText);
            int barW = Math.max(52, width - 42);
            int filled = Math.round((float)barW * ((float)left / (float)Math.max(1, max)));
            int barColor = HudForgeRenderer.durabilityColor((float)left / (float)Math.max(1, max));
            context.method_25294(30, y + 13, 30 + barW, y + 16, -1728053248);
            context.method_25294(30, y + 13, 30 + filled, y + 16, barColor);
            y += rowHeight;
        }
        context.method_51448().popMatrix();
    }

    private static void renderTextPanel(class_332 context, class_327 textRenderer, int x, int y, float scale, int preferredWidth, int radius, int background, int textColor, List<String> lines) {
        if (lines.isEmpty()) {
            return;
        }
        int width = 0;
        for (String line : lines) {
            width = Math.max(width, textRenderer.method_1727(line));
        }
        width = Math.max(preferredWidth, Math.max(76, width + 16));
        int height = 8 + lines.size() * 11;
        context.method_51448().pushMatrix();
        context.method_51448().translate((float)x, (float)y);
        context.method_51448().scale(scale, scale);
        HudForgeRenderer.fillSoftRect(context, 0, 0, width, height, radius, background);
        int lineY = 5;
        for (String line : lines) {
            context.method_25303(textRenderer, line, 8, lineY, textColor);
            lineY += 11;
        }
        context.method_51448().popMatrix();
    }

    private static int durabilityColor(float fraction) {
        if (fraction > 0.65f) {
            return -11141254;
        }
        if (fraction > 0.3f) {
            return -11174;
        }
        return -41892;
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
        if (config.crosshairOutline && config.crosshairMode != 0) {
            HudForgeRenderer.drawCrosshair(context, config, width / 2, height / 2, config.crosshairThickness + config.crosshairOutlineThickness * 2, config.crosshairOutlineColor);
        }
        HudForgeRenderer.drawCrosshair(context, config, width / 2, height / 2, config.crosshairThickness, config.crosshairColor);
    }

    private static void drawCrosshair(class_332 context, HudForgeConfig config, int centerX, int centerY, int thickness, int color) {
        int size = config.crosshairSize;
        int gap = config.crosshairGap;
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

    private static String cleanName(String value) {
        String[] words = value.replace('_', ' ').split(" ");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) continue;
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return builder.isEmpty() ? value : builder.toString();
    }

    private static String formatDuration(int ticks) {
        if (ticks < 0 || ticks > 72000) {
            return "--:--";
        }
        int seconds = ticks / 20;
        return seconds / 60 + ":" + String.format(Locale.ROOT, "%02d", seconds % 60);
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

    private record EffectLine(String name, String duration, int color, String texturePath) {
    }
}

