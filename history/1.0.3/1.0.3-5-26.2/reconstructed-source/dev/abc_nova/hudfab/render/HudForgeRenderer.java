/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.DeltaTracker
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.multiplayer.PlayerInfo
 *  net.minecraft.client.renderer.RenderPipelines
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.resources.Identifier
 *  net.minecraft.util.Mth
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.item.ItemStack
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
import java.lang.invoke.CallSite;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
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

    private static void renderCoords(GuiGraphicsExtractor context, Minecraft client, HudForgeConfig config) {
        if (client.player == null || client.level == null) {
            return;
        }
        BlockPos pos = client.player.blockPosition();
        String biome = "unknown";
        try {
            String key = client.level.getBiome(pos).unwrapKey().map(Object::toString).orElse("unknown");
            biome = HudForgeRenderer.cleanName(HudForgeRenderer.resourcePath(key));
        }
        catch (RuntimeException key) {
            // empty catch block
        }
        List<CallSite> lines = List.of("X: " + pos.getX(), "Y: " + pos.getY(), "Z: " + pos.getZ(), "Biome: " + biome);
        HudForgeRenderer.renderTextPanel(context, client.font, config.coordsX, config.coordsY, config.coordsScale, config.coordsWidth, config.coordsRadius, config.coordsBackground, config.coordsText, lines);
    }

    private static void renderEffects(GuiGraphicsExtractor context, Minecraft client, HudForgeConfig config) {
        if (client.player == null || client.player.getActiveEffects().isEmpty()) {
            return;
        }
        ArrayList<EffectLine> lines = new ArrayList<EffectLine>();
        for (MobEffectInstance effect : client.player.getActiveEffects()) {
            if (lines.size() >= 6) break;
            Object name = Component.translatable((String)effect.getDescriptionId()).getString();
            if (effect.getAmplifier() > 0) {
                name = (String)name + " " + (effect.getAmplifier() + 1);
            }
            int color = 0xFF000000 | ((MobEffect)effect.getEffect().value()).getColor() & 0xFFFFFF;
            String texturePath = effect.getEffect().unwrapKey().map(key -> "textures/mob_effect/" + HudForgeRenderer.resourcePath(key.toString()) + ".png").orElse("");
            lines.add(new EffectLine((String)name, HudForgeRenderer.formatDuration(effect.getDuration()), color, texturePath));
        }
        HudForgeRenderer.renderEffectsPanel(context, client.font, config, lines);
    }

    private static void renderEquipment(GuiGraphicsExtractor context, Minecraft client, HudForgeConfig config) {
        if (client.player == null) {
            return;
        }
        ArrayList<ItemStack> lines = new ArrayList<ItemStack>();
        HudForgeRenderer.addDurability(lines, client.player.getMainHandItem());
        HudForgeRenderer.addDurability(lines, client.player.getItemBySlot(EquipmentSlot.HEAD));
        HudForgeRenderer.addDurability(lines, client.player.getItemBySlot(EquipmentSlot.CHEST));
        HudForgeRenderer.addDurability(lines, client.player.getItemBySlot(EquipmentSlot.LEGS));
        HudForgeRenderer.addDurability(lines, client.player.getItemBySlot(EquipmentSlot.FEET));
        if (lines.isEmpty()) {
            return;
        }
        HudForgeRenderer.renderEquipmentPanel(context, client.font, config, lines);
    }

    private static void addDurability(List<ItemStack> lines, ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.isDamageableItem()) {
            return;
        }
        lines.add(stack);
    }

    private static void renderEffectsPanel(GuiGraphicsExtractor context, Font textRenderer, HudForgeConfig config, List<EffectLine> lines) {
        int width = config.effectsWidth;
        for (EffectLine line : lines) {
            width = Math.max(width, textRenderer.width(line.name) + textRenderer.width(line.duration) + 48);
        }
        width = Math.max(110, width);
        int rowHeight = 22;
        int height = 8 + lines.size() * rowHeight;
        context.pose().pushMatrix();
        context.pose().translate((float)config.effectsX, (float)config.effectsY);
        context.pose().scale(config.effectsScale, config.effectsScale);
        HudForgeRenderer.fillSoftRect(context, 0, 0, width, height, config.effectsRadius, config.effectsBackground);
        int y = 6;
        for (EffectLine line : lines) {
            HudForgeRenderer.fillSoftRect(context, 7, y - 1, 18, 18, Math.min(5, config.effectsRadius), 0x66000000);
            if (!line.texturePath.isBlank()) {
                context.blit(RenderPipelines.GUI_TEXTURED, Identifier.withDefaultNamespace((String)line.texturePath), 8, y, 0.0f, 0.0f, 18, 18, 18, 18);
            }
            context.text(textRenderer, line.name, 31, y + 1, config.effectsText);
            int tx = width - textRenderer.width(line.duration) - 8;
            context.text(textRenderer, line.duration, tx, y + 10, -4669228);
            y += rowHeight;
        }
        context.pose().popMatrix();
    }

    private static void renderEquipmentPanel(GuiGraphicsExtractor context, Font textRenderer, HudForgeConfig config, List<ItemStack> stacks) {
        int width = config.equipmentWidth;
        int rowHeight = 22;
        int height = 8 + stacks.size() * rowHeight;
        context.pose().pushMatrix();
        context.pose().translate((float)config.equipmentX, (float)config.equipmentY);
        context.pose().scale(config.equipmentScale, config.equipmentScale);
        HudForgeRenderer.fillSoftRect(context, 0, 0, width, height, config.equipmentRadius, config.equipmentBackground);
        int y = 6;
        for (ItemStack stack : stacks) {
            context.item(stack, 7, y - 1);
            int max = stack.getMaxDamage();
            int left = Math.max(0, max - stack.getDamageValue());
            String label = left + "/" + max;
            context.text(textRenderer, label, 30, y + 1, config.equipmentText);
            int barW = Math.max(52, width - 42);
            int filled = Math.round((float)barW * ((float)left / (float)Math.max(1, max)));
            context.fill(30, y + 13, 30 + barW, y + 16, -1728053248);
            context.fill(30, y + 13, 30 + filled, y + 16, HudForgeRenderer.durabilityColor((float)left / (float)Math.max(1, max)));
            y += rowHeight;
        }
        context.pose().popMatrix();
    }

    private static void renderTextPanel(GuiGraphicsExtractor context, Font textRenderer, int x, int y, float scale, int preferredWidth, int radius, int background, int textColor, List<String> lines) {
        if (lines.isEmpty()) {
            return;
        }
        int width = 0;
        for (String line : lines) {
            width = Math.max(width, textRenderer.width(line));
        }
        width = Math.max(preferredWidth, Math.max(76, width + 16));
        int height = 8 + lines.size() * 11;
        context.pose().pushMatrix();
        context.pose().translate((float)x, (float)y);
        context.pose().scale(scale, scale);
        HudForgeRenderer.fillSoftRect(context, 0, 0, width, height, radius, background);
        int lineY = 5;
        for (String line : lines) {
            context.text(textRenderer, line, 8, lineY, textColor);
            lineY += 11;
        }
        context.pose().popMatrix();
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
        if (config.crosshairOutline && config.crosshairMode != 0) {
            HudForgeRenderer.drawCrosshair(context, config, width / 2, height / 2, config.crosshairThickness + config.crosshairOutlineThickness * 2, config.crosshairOutlineColor);
        }
        HudForgeRenderer.drawCrosshair(context, config, width / 2, height / 2, config.crosshairThickness, config.crosshairColor);
    }

    private static void drawCrosshair(GuiGraphicsExtractor context, HudForgeConfig config, int centerX, int centerY, int thickness, int color) {
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

    private static String resourcePath(String key) {
        int colon;
        int slash = key.lastIndexOf(47);
        int start = Math.max(slash, colon = key.lastIndexOf(58)) + 1;
        return start > 0 && start < key.length() ? key.substring(start) : key;
    }

    private static String formatDuration(int ticks) {
        if (ticks < 0 || ticks > 72000) {
            return "--:--";
        }
        int seconds = ticks / 20;
        return seconds / 60 + ":" + String.format(Locale.ROOT, "%02d", seconds % 60);
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

    private record EffectLine(String name, String duration, int color, String texturePath) {
    }
}

