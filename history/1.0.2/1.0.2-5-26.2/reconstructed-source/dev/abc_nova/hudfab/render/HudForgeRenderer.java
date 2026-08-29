/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.DeltaTracker
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.multiplayer.PlayerInfo
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.util.Mth
 *  net.minecraft.world.scores.DisplaySlot
 *  net.minecraft.world.scores.Objective
 *  net.minecraft.world.scores.PlayerScoreEntry
 *  net.minecraft.world.scores.PlayerTeam
 *  net.minecraft.world.scores.Scoreboard
 *  net.minecraft.world.scores.Team
 */
package dev.abc_nova.hudfab.render;

import dev.abc_nova.hudfab.HudForgeClient;
import dev.abc_nova.hudfab.config.HudForgeConfig;
import java.util.ArrayList;
import java.util.Comparator;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.Mth;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;

public final class HudForgeRenderer {
    private HudForgeRenderer() {
    }

    public static void render(GuiGraphicsExtractor context, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        HudForgeConfig config = HudForgeClient.config;
        if (client.gui.hud.isHidden() || config == null) {
            return;
        }
        if (config.statsEnabled) {
            HudForgeRenderer.renderStats(context, client, config);
        }
        if (config.scoreboardMode == 0) {
            HudForgeRenderer.renderScoreboard(context, client, config);
        }
        if (config.crosshairMode != 0 && client.gui.screen() == null && client.options.getCameraType().isFirstPerson()) {
            HudForgeRenderer.renderCrosshair(context, config, context.guiWidth(), context.guiHeight());
        }
    }

    private static void renderStats(GuiGraphicsExtractor context, Minecraft client, HudForgeConfig config) {
        PlayerInfo entry;
        Font textRenderer = client.font;
        int ping = 0;
        if (client.player != null && client.getConnection() != null && (entry = client.getConnection().getPlayerInfo(client.player.getUUID())) != null) {
            ping = entry.getLatency();
        }
        String fps = client.getFps() + " FPS";
        String latency = ping + " ms";
        int width = 82;
        int height = 32;
        context.pose().pushMatrix();
        context.pose().translate((float)config.statsX, (float)config.statsY);
        context.pose().scale(config.statsScale, config.statsScale);
        HudForgeRenderer.fillSoftRect(context, 0, 0, width, height, config.statsRadius, config.statsBackground);
        context.text(textRenderer, fps, 8, 5, config.statsText);
        context.text(textRenderer, latency, 8, 18, config.statsText);
        context.pose().popMatrix();
    }

    private static void renderScoreboard(GuiGraphicsExtractor context, Minecraft client, HudForgeConfig config) {
        if (client.level == null) {
            return;
        }
        Scoreboard scoreboard = client.level.getScoreboard();
        Objective objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
        if (objective == null) {
            return;
        }
        Font textRenderer = client.font;
        ArrayList<PlayerScoreEntry> entries = new ArrayList<PlayerScoreEntry>(16);
        for (PlayerScoreEntry entry : scoreboard.listPlayerScores(objective)) {
            if (entry.value() == 0 && entry.display() == null) continue;
            entries.add(entry);
        }
        entries.sort(Comparator.comparingInt(PlayerScoreEntry::value).reversed());
        if (entries.isEmpty()) {
            return;
        }
        int width = config.scoreboardWidth;
        int titleHeight = 18;
        int rowHeight = 11;
        int rows = Math.min(15, entries.size());
        int height = titleHeight + rows * rowHeight + 6;
        context.pose().pushMatrix();
        context.pose().translate((float)config.scoreboardX, (float)config.scoreboardY);
        context.pose().scale(config.scoreboardScale, config.scoreboardScale);
        HudForgeRenderer.fillSoftRect(context, 0, 0, width, height, config.scoreboardRadius, config.scoreboardBackground);
        Component title = objective.getDisplayName();
        int titleX = Mth.clamp((int)((width - textRenderer.width((FormattedText)title)) / 2), (int)8, (int)(width - 8));
        context.text(textRenderer, title, titleX, 5, config.scoreboardTitle);
        int y = titleHeight;
        for (int i = 0; i < rows; ++i) {
            Component line;
            PlayerScoreEntry entry = (PlayerScoreEntry)entries.get(i);
            Object object = line = entry.display() == null ? PlayerTeam.formatNameForTeam((Team)scoreboard.getPlayersTeam(entry.owner()), (Component)Component.literal((String)entry.owner())) : entry.display();
            if (config.scoreboardServerColors) {
                context.text(textRenderer, line, 9, y, config.scoreboardText);
            } else {
                context.text(textRenderer, HudForgeRenderer.trimToWidth(textRenderer, line.getString(), width - 18), 9, y, config.scoreboardText);
            }
            y += rowHeight;
        }
        context.pose().popMatrix();
    }

    private static void renderCrosshair(GuiGraphicsExtractor context, HudForgeConfig config, int width, int height) {
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
                context.fill(centerX + i, centerY - span, centerX + i + 1, centerY - span + thickness, color);
                context.fill(centerX + i, centerY + span - thickness + 1, centerX + i + 1, centerY + span + 1, color);
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

    private static void drawCenteredRect(GuiGraphicsExtractor context, int centerX, int centerY, int width, int height, int color) {
        int left = centerX - width / 2;
        int top = centerY - height / 2;
        context.fill(left, top, left + width, top + height, color);
    }

    private static String trimToWidth(Font textRenderer, String value, int maxWidth) {
        if (textRenderer.width(value) <= maxWidth) {
            return value;
        }
        return textRenderer.plainSubstrByWidth(value, Math.max(8, maxWidth - textRenderer.width("..."))) + "...";
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
}

