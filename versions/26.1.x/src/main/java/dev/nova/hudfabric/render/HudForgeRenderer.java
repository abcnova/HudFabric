package dev.nova.hudfabric.render;

import dev.nova.hudfabric.HudForgeClient;
import dev.nova.hudfabric.JumpResetTiming;
import dev.nova.hudfabric.config.HudForgeConfig;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

public final class HudForgeRenderer {
    private HudForgeRenderer() {
    }

    public static void render(GuiGraphicsExtractor context, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        HudForgeConfig config = HudForgeClient.config;
        if (client.options.hideGui || config == null) {
            return;
        }

        if (config.statsEnabled) {
            renderStats(context, client, config);
        }
        if (config.coordsEnabled) {
            renderCoords(context, client, config);
        }
        if (config.effectsEnabled) {
            renderEffects(context, client, config);
        }
        if (config.equipmentEnabled) {
            renderEquipment(context, client, config);
        }
        if (config.cpsEnabled) renderCps(context, client.font, config);
        if (HudForgeClient.jumpResetVisible()) renderJumpReset(context, client.font, config);

        if (config.scoreboardMode == HudForgeConfig.SCOREBOARD_CUSTOM) {
            renderScoreboard(context, client, config);
        }

        if (config.crosshairMode != HudForgeConfig.CROSSHAIR_VANILLA && client.screen == null && client.options.getCameraType().isFirstPerson()) {
            renderCrosshair(context, config, context.guiWidth(), context.guiHeight());
        }
    }

    private static void renderStats(GuiGraphicsExtractor context, Minecraft client, HudForgeConfig config) {
        Font textRenderer = client.font;
        int ping = 0;
        if (client.player != null && client.getConnection() != null) {
            PlayerInfo entry = client.getConnection().getPlayerInfo(client.player.getUUID());
            if (entry != null) {
                ping = entry.getLatency();
            }
        }

        String fps = client.getFps() + " FPS";
        String latency = ping + " ms";
        int width = 82;
        int height = 32;

        context.pose().pushMatrix();
        context.pose().translate(config.statsX, config.statsY);
        context.pose().scale(config.statsScale, config.statsScale);
        fillSoftRect(context, 0, 0, width, height, config.statsRadius, config.statsBackground);
        context.text(textRenderer, fps, 8, 5, config.statsText);
        context.text(textRenderer, latency, 8, 18, config.statsText);
        context.pose().popMatrix();
    }

    private static void renderCps(GuiGraphicsExtractor context, Font font, HudForgeConfig config) {
        String value = "L " + HudForgeClient.leftCps() + "   •   R " + HudForgeClient.rightCps();
        int width = Math.max(config.cpsWidth, font.width(value) + 20);
        context.pose().pushMatrix();
        context.pose().translate(config.cpsX, config.cpsY);
        context.pose().scale(config.cpsScale, config.cpsScale);
        fillSoftRect(context, 0, 0, width, 22, config.cpsRadius, config.cpsBackground);
        context.text(font, value, (width - font.width(value)) / 2, 7, config.cpsText);
        context.pose().popMatrix();
    }
    private static void renderJumpReset(GuiGraphicsExtractor context, Font font, HudForgeConfig config) {
        int d = HudForgeClient.jumpResetDelta();
        JumpResetTiming.Result result = JumpResetTiming.classify(d); String key = "hudfabric.jump_reset." + result.name().toLowerCase(java.util.Locale.ROOT);
        Component line = result == JumpResetTiming.Result.PERFECT || result == JumpResetTiming.Result.MISSED ? Component.translatable(key) : Component.translatable(key, Math.abs(d));
        int w = Math.max(config.jumpResetWidth, font.width(line) + 20); int x = config.jumpResetMode == 0 ? (context.guiWidth() - font.width(line)) / 2 : config.jumpResetX; int y = config.jumpResetMode == 0 ? context.guiHeight() / 2 - 38 : config.jumpResetY;
        context.pose().pushMatrix(); context.pose().translate(x, y); context.pose().scale(config.jumpResetScale, config.jumpResetScale);
        int color = result == JumpResetTiming.Result.PERFECT ? 0xFF65E58A : result == JumpResetTiming.Result.EARLY || result == JumpResetTiming.Result.LATE ? 0xFFFFC857 : 0xFFFF647C;
        if (config.jumpResetMode == 1) { fillSoftRect(context, 0, 0, w, 24, config.jumpResetRadius, config.jumpResetBackground); context.text(font, line, (w - font.width(line)) / 2, 8, config.jumpResetText); } else context.text(font, line, 0, 0, color); context.pose().popMatrix();
    }

    private static void renderCoords(GuiGraphicsExtractor context, Minecraft client, HudForgeConfig config) {
        if (client.player == null || client.level == null) {
            return;
        }
        BlockPos pos = client.player.blockPosition();
        String biome = "unknown";
        try {
            String key = client.level.getBiome(pos).unwrapKey().map(Object::toString).orElse("unknown");
            biome = cleanName(resourcePath(key));
        } catch (RuntimeException ignored) {
        }

        List<String> lines = List.of("X: " + pos.getX(), "Y: " + pos.getY(), "Z: " + pos.getZ(), "Biome: " + biome);
        renderTextPanel(context, client.font, config.coordsX, config.coordsY, config.coordsScale, config.coordsWidth, config.coordsRadius, config.coordsBackground, config.coordsText, lines);
    }

    private static void renderEffects(GuiGraphicsExtractor context, Minecraft client, HudForgeConfig config) {
        if (client.player == null || client.player.getActiveEffects().isEmpty()) {
            return;
        }
        List<EffectLine> lines = new ArrayList<>();
        for (MobEffectInstance effect : client.player.getActiveEffects()) {
            if (lines.size() >= 6) {
                break;
            }
            String name = Component.translatable(effect.getDescriptionId()).getString();
            if (effect.getAmplifier() > 0) {
                name += " " + (effect.getAmplifier() + 1);
            }
            int color = 0xFF000000 | (effect.getEffect().value().getColor() & 0x00FFFFFF);
            String texturePath = effect.getEffect().unwrapKey()
                    .map(key -> "textures/mob_effect/" + resourcePath(key.toString()) + ".png")
                    .orElse("");
            lines.add(new EffectLine(name, formatDuration(effect.getDuration()), color, texturePath));
        }
        renderEffectsPanel(context, client.font, config, lines);
    }

    private static void renderEquipment(GuiGraphicsExtractor context, Minecraft client, HudForgeConfig config) {
        if (client.player == null) {
            return;
        }
        if (config.equipmentMode == 1) {
            List<ItemStack> armor = new ArrayList<>();
            armor.add(client.player.getItemBySlot(EquipmentSlot.HEAD)); armor.add(client.player.getItemBySlot(EquipmentSlot.CHEST));
            armor.add(client.player.getItemBySlot(EquipmentSlot.LEGS)); armor.add(client.player.getItemBySlot(EquipmentSlot.FEET));
            if (armor.stream().anyMatch(s -> !s.isEmpty() && s.isDamageableItem())) renderEquipmentHotbar(context, client.font, config, armor);
            return;
        }
        List<ItemStack> lines = new ArrayList<>();
        addDurability(lines, client.player.getMainHandItem());
        addDurability(lines, client.player.getItemBySlot(EquipmentSlot.HEAD));
        addDurability(lines, client.player.getItemBySlot(EquipmentSlot.CHEST));
        addDurability(lines, client.player.getItemBySlot(EquipmentSlot.LEGS));
        addDurability(lines, client.player.getItemBySlot(EquipmentSlot.FEET));
        if (lines.isEmpty()) {
            return;
        }
        renderEquipmentPanel(context, client.font, config, lines);
    }

    private static void addDurability(List<ItemStack> lines, ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.isDamageableItem()) {
            return;
        }
        lines.add(stack);
    }

    private static void renderEquipmentHotbar(GuiGraphicsExtractor context, Font font, HudForgeConfig config, List<ItemStack> stacks) {
        int width = 22 + (stacks.size() - 1) * 20;
        boolean offhandOnLeft = Minecraft.getInstance().player == null || Minecraft.getInstance().player.getMainArm() == net.minecraft.world.entity.HumanoidArm.RIGHT;
        int defaultX = context.guiWidth() / 2 - 91 - width - 6 - (offhandOnLeft ? 29 : 0);
        int x = config.equipmentHotbarX < 0 ? defaultX : Math.min(config.equipmentHotbarX, Math.max(0, context.guiWidth() - width));
        int y = config.equipmentHotbarY < 0 ? context.guiHeight() - 22 : Math.min(config.equipmentHotbarY, Math.max(0, context.guiHeight() - 22));
        context.pose().pushMatrix(); if (!config.equipmentHotbarSeparate) context.pose().translate(x, y);
        if (config.equipmentHotbarBackground && !config.equipmentHotbarSeparate) {
            Identifier hotbar = Identifier.withDefaultNamespace("hud/hotbar");
            context.blitSprite(RenderPipelines.GUI_TEXTURED, hotbar, 182, 22, 0, 0, 0, 0, width - 3, 22, 0xFFFFFFFF);
            context.blitSprite(RenderPipelines.GUI_TEXTURED, hotbar, 182, 22, 179, 0, width - 3, 0, 3, 22, 0xFFFFFFFF);
        }
        for (int i = 0; i < stacks.size(); i++) {
            ItemStack stack = stacks.get(i);
            int slotX = config.equipmentHotbarSeparate ? (config.equipmentSlotX[i] < 0 ? x + i * 20 : config.equipmentSlotX[i]) : i * 20;
            int slotY = config.equipmentHotbarSeparate ? (config.equipmentSlotY[i] < 0 ? y : config.equipmentSlotY[i]) : 0;
            if (config.equipmentHotbarBackground && config.equipmentHotbarSeparate) drawIndependentVanillaSlot(context, slotX, slotY);
            if (stack.isEmpty() || !stack.isDamageableItem()) continue;
            float fraction = (stack.getMaxDamage() - stack.getDamageValue()) / (float) Math.max(1, stack.getMaxDamage());
            context.item(stack, slotX + 3, slotY + 3);
            int barWidth = Math.max(0, Math.min(13, Math.round(13.0f * fraction)));
            if (stack.getDamageValue() > 0) {
                context.fill(slotX + 5, slotY + 18, slotX + 18, slotY + 20, 0xFF000000);
                context.fill(slotX + 5, slotY + 18, slotX + 5 + barWidth, slotY + 19, durabilityColor(fraction));
            }
            if (config.equipmentDurabilityDisplay != 0) {
                int left = Math.max(0, stack.getMaxDamage() - stack.getDamageValue());
                String value = config.equipmentDurabilityDisplay == 1 ? String.valueOf(left) : Math.round(fraction * 100.0f) + "%";
                context.text(font, value, slotX + 11 - font.width(value) / 2, slotY - 9, durabilityColor(fraction));
            }
            if (config.equipmentWarning && fraction * 100.0f <= config.equipmentWarningPercent) context.text(font, "!", slotX + 16, slotY + 1, 0xFFFF5555);
        }
        context.pose().popMatrix();
    }

    private static void drawIndependentVanillaSlot(GuiGraphicsExtractor context, int x, int y) { context.fill(x, y, x + 22, y + 22, 0xFF080808); context.fill(x + 1, y + 1, x + 21, y + 21, 0xFFB0B0B0); context.fill(x + 2, y + 2, x + 20, y + 20, 0xFF5A5A5A); context.fill(x + 3, y + 3, x + 19, y + 19, 0xCC151515); }

    private static void renderEffectsPanel(GuiGraphicsExtractor context, Font textRenderer, HudForgeConfig config, List<EffectLine> lines) {
        int width = config.effectsWidth;
        for (EffectLine line : lines) {
            width = Math.max(width, textRenderer.width(line.name) + textRenderer.width(line.duration) + 48);
        }
        width = Math.max(110, width);
        int rowHeight = 22;
        int height = 8 + lines.size() * rowHeight;
        context.pose().pushMatrix();
        context.pose().translate(config.effectsX, config.effectsY);
        context.pose().scale(config.effectsScale, config.effectsScale);
        fillSoftRect(context, 0, 0, width, height, config.effectsRadius, config.effectsBackground);
        int y = 6;
        for (EffectLine line : lines) {
            fillSoftRect(context, 7, y - 1, 18, 18, Math.min(5, config.effectsRadius), 0x66000000);
            if (!line.texturePath.isBlank()) {
                context.blit(RenderPipelines.GUI_TEXTURED, Identifier.withDefaultNamespace(line.texturePath), 8, y, 0.0f, 0.0f, 18, 18, 18, 18);
            }
            context.text(textRenderer, line.name, 31, y + 1, config.effectsText);
            int tx = width - textRenderer.width(line.duration) - 8;
            context.text(textRenderer, line.duration, tx, y + 10, 0xFFB8C0D4);
            y += rowHeight;
        }
        context.pose().popMatrix();
    }

    private static void renderEquipmentPanel(GuiGraphicsExtractor context, Font textRenderer, HudForgeConfig config, List<ItemStack> stacks) {
        int width = config.equipmentWidth;
        int rowHeight = 22;
        int height = 8 + stacks.size() * rowHeight;
        context.pose().pushMatrix();
        context.pose().translate(config.equipmentX, config.equipmentY);
        context.pose().scale(config.equipmentScale, config.equipmentScale);
        fillSoftRect(context, 0, 0, width, height, config.equipmentRadius, config.equipmentBackground);
        int y = 6;
        for (ItemStack stack : stacks) {
            context.item(stack, 7, y - 1);
            int max = stack.getMaxDamage();
            int left = Math.max(0, max - stack.getDamageValue());
            String label = left + "/" + max;
            context.text(textRenderer, label, 30, y + 1, config.equipmentText);
            int barW = Math.max(52, width - 42);
            int filled = Math.round(barW * (left / (float) Math.max(1, max)));
            context.fill(30, y + 13, 30 + barW, y + 16, 0x99000000);
            context.fill(30, y + 13, 30 + filled, y + 16, durabilityColor(left / (float) Math.max(1, max)));
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
        context.pose().translate(x, y);
        context.pose().scale(scale, scale);
        fillSoftRect(context, 0, 0, width, height, radius, background);
        int lineY = 5;
        for (String line : lines) {
            context.text(textRenderer, line, 8, lineY, textColor);
            lineY += 11;
        }
        context.pose().popMatrix();
    }

    private static int durabilityColor(float fraction) {
        if (fraction > 0.65f) {
            return 0xFF55FF7A;
        }
        if (fraction > 0.3f) {
            return 0xFFFFD45A;
        }
        return 0xFFFF5C5C;
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
        List<PlayerScoreEntry> entries = new ArrayList<>(16);
        entries.addAll(scoreboard.listPlayerScores(objective));
        entries.sort(Comparator.comparingInt(PlayerScoreEntry::value).reversed());

        if (entries.isEmpty()) {
            return;
        }

        int width = Math.max(config.scoreboardWidth, textRenderer.width(objective.getDisplayName()) + 20);
        int titleHeight = 18;
        int rowHeight = 11;
        int rows = Math.min(30, entries.size());
        for (int i = 0; i < rows; i++) {
            PlayerScoreEntry entry = entries.get(i);
            Component line = entry.display() == null ? PlayerTeam.formatNameForTeam(scoreboard.getPlayersTeam(entry.owner()), Component.literal(entry.owner())) : entry.display();
            width = Math.min(420, Math.max(width, textRenderer.width(line) + 18));
        }
        int height = titleHeight + rows * rowHeight + 6;

        context.pose().pushMatrix();
        context.pose().translate(config.scoreboardX, config.scoreboardY);
        context.pose().scale(config.scoreboardScale, config.scoreboardScale);
        fillSoftRect(context, 0, 0, width, height, config.scoreboardRadius, config.scoreboardBackground);

        Component title = objective.getDisplayName();
        int titleX = Mth.clamp((width - textRenderer.width(title)) / 2, 8, width - 8);
        context.text(textRenderer, title, titleX, 5, config.scoreboardTitle);

        int y = titleHeight;
        for (int i = 0; i < rows; i++) {
            PlayerScoreEntry entry = entries.get(i);
            Component line = entry.display() == null ? PlayerTeam.formatNameForTeam(scoreboard.getPlayersTeam(entry.owner()), Component.literal(entry.owner())) : entry.display();
            if (config.scoreboardServerColors) {
                context.text(textRenderer, line, 9, y, config.scoreboardText);
            } else {
                context.text(textRenderer, trimToWidth(textRenderer, line.getString(), width - 18), 9, y, config.scoreboardText);
            }
            y += rowHeight;
        }

        context.pose().popMatrix();
    }

    private static void renderCrosshair(GuiGraphicsExtractor context, HudForgeConfig config, int width, int height) {
        if (config.crosshairOutline && config.crosshairMode != HudForgeConfig.CROSSHAIR_VANILLA) {
            drawCrosshair(context, config, width / 2, height / 2,
                    config.crosshairThickness + config.crosshairOutlineThickness * 2,
                    config.crosshairOutlineColor);
        }
        drawCrosshair(context, config, width / 2, height / 2, config.crosshairThickness, config.crosshairColor);
    }

    private static void drawCrosshair(GuiGraphicsExtractor context, HudForgeConfig config, int centerX, int centerY, int thickness, int color) {
        int size = config.crosshairSize;
        int gap = config.crosshairGap;

        if (config.crosshairMode == HudForgeConfig.CROSSHAIR_DOT) {
            drawCenteredRect(context, centerX, centerY, Math.max(thickness, 2), Math.max(thickness, 2), color);
            return;
        }
        if (config.crosshairMode == HudForgeConfig.CROSSHAIR_CIRCLE) {
            int r = Math.max(3, size + gap);
            int inner = Math.max(0, r - thickness);
            for (int x = -r; x <= r; x++) for (int y = -r; y <= r; y++) {
                int distance = x * x + y * y;
                if (distance <= r * r && distance >= inner * inner) context.fill(centerX + x, centerY + y, centerX + x + 1, centerY + y + 1, color);
            }
            return;
        }

        int half = thickness / 2;
        context.fill(centerX - gap - size, centerY - half, centerX - gap, centerY - half + thickness, color);
        context.fill(centerX + gap + 1, centerY - half, centerX + gap + size + 1, centerY - half + thickness, color);
        context.fill(centerX - half, centerY - gap - size, centerX - half + thickness, centerY - gap, color);
        context.fill(centerX - half, centerY + gap + 1, centerX - half + thickness, centerY + gap + size + 1, color);
        if (config.crosshairDot) {
            drawCenteredRect(context, centerX, centerY, thickness, thickness, color);
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
            if (word.isEmpty()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return builder.isEmpty() ? value : builder.toString();
    }

    private static String resourcePath(String key) {
        int slash = key.lastIndexOf('/');
        int colon = key.lastIndexOf(':');
        int start = Math.max(slash, colon) + 1;
        return start > 0 && start < key.length() ? key.substring(start) : key;
    }

    private static String formatDuration(int ticks) {
        if (ticks < 0) return "∞";
        long seconds = ticks / 20L;
        long days = seconds / 86400L, hours = seconds % 86400L / 3600L, minutes = seconds % 3600L / 60L, secs = seconds % 60L;
        if (days > 0) return String.format(java.util.Locale.ROOT, "%dd %02d:%02d:%02d", days, hours, minutes, secs);
        if (hours > 0) return String.format(java.util.Locale.ROOT, "%d:%02d:%02d", hours, minutes, secs);
        return String.format(java.util.Locale.ROOT, "%d:%02d", minutes, secs);
    }

    private record EffectLine(String name, String duration, int color, String texturePath) {
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
}
