package dev.nova.hudfabric.render;

import dev.nova.hudfabric.HudForgeClient;
import dev.nova.hudfabric.config.HudForgeConfig;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class HudForgeRenderer {
    private HudForgeRenderer() {
    }

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        HudForgeConfig config = HudForgeClient.config;
        if (client.options.hudHidden || config == null) {
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
        if (config.cpsEnabled) {
            renderCps(context, client.textRenderer, config);
        }

        if (config.scoreboardMode == HudForgeConfig.SCOREBOARD_CUSTOM) {
            renderScoreboard(context, client, config);
        }

        if (config.crosshairMode != HudForgeConfig.CROSSHAIR_VANILLA && client.currentScreen == null && client.options.getPerspective().isFirstPerson()) {
            renderCrosshair(context, config, context.getScaledWindowWidth(), context.getScaledWindowHeight());
        }
    }

    private static void renderStats(DrawContext context, MinecraftClient client, HudForgeConfig config) {
        TextRenderer textRenderer = client.textRenderer;
        int ping = 0;
        if (client.player != null && client.getNetworkHandler() != null) {
            PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
            if (entry != null) {
                ping = entry.getLatency();
            }
        }

        String fps = client.getCurrentFps() + " FPS";
        String latency = ping + " ms";
        int width = 82;
        int height = 32;

        context.getMatrices().pushMatrix();
        context.getMatrices().translate(config.statsX, config.statsY);
        context.getMatrices().scale(config.statsScale, config.statsScale);
        fillSoftRect(context, 0, 0, width, height, config.statsRadius, config.statsBackground);
        context.drawTextWithShadow(textRenderer, fps, 8, 5, config.statsText);
        context.drawTextWithShadow(textRenderer, latency, 8, 18, config.statsText);
        context.getMatrices().popMatrix();
    }

    private static void renderCps(DrawContext context, TextRenderer textRenderer, HudForgeConfig config) {
        String value = "L " + HudForgeClient.leftCps() + "   •   R " + HudForgeClient.rightCps();
        int width = Math.max(config.cpsWidth, textRenderer.getWidth(value) + 20);
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(config.cpsX, config.cpsY);
        context.getMatrices().scale(config.cpsScale, config.cpsScale);
        fillSoftRect(context, 0, 0, width, 22, config.cpsRadius, config.cpsBackground);
        context.drawTextWithShadow(textRenderer, value, (width - textRenderer.getWidth(value)) / 2, 7, config.cpsText);
        context.getMatrices().popMatrix();
    }

    private static void renderCoords(DrawContext context, MinecraftClient client, HudForgeConfig config) {
        if (client.player == null || client.world == null) {
            return;
        }
        BlockPos pos = client.player.getBlockPos();
        String biome = "unknown";
        try {
            Optional<Identifier> key = client.world.getRegistryManager()
                    .getOptionalEntry(client.world.getBiome(pos).getKey().orElseThrow())
                    .flatMap(entry -> entry.getKey().map(registryKey -> registryKey.getValue()));
            if (key.isPresent()) {
                biome = cleanName(key.get().getPath());
            }
        } catch (RuntimeException ignored) {
        }

        List<String> lines = List.of("X: " + pos.getX(), "Y: " + pos.getY(), "Z: " + pos.getZ(), "Biome: " + biome);
        renderTextPanel(context, client.textRenderer, config.coordsX, config.coordsY, config.coordsScale, config.coordsWidth, config.coordsRadius, config.coordsBackground, config.coordsText, lines);
    }

    private static void renderEffects(DrawContext context, MinecraftClient client, HudForgeConfig config) {
        if (client.player == null || client.player.getStatusEffects().isEmpty()) {
            return;
        }
        List<EffectLine> lines = new ArrayList<>();
        for (StatusEffectInstance effect : client.player.getStatusEffects()) {
            if (lines.size() >= 6) {
                break;
            }
            String name = effect.getEffectType().value().getName().getString();
            if (effect.getAmplifier() > 0) {
                name += " " + (effect.getAmplifier() + 1);
            }
            int color = 0xFF000000 | (effect.getEffectType().value().getColor() & 0x00FFFFFF);
            String texturePath = effect.getEffectType().getKey()
                    .map(key -> "textures/mob_effect/" + key.getValue().getPath() + ".png")
                    .orElse("");
            lines.add(new EffectLine(name, formatDuration(effect.getDuration()), color, texturePath));
        }
        renderEffectsPanel(context, client.textRenderer, config, lines);
    }

    private static void renderEquipment(DrawContext context, MinecraftClient client, HudForgeConfig config) {
        if (client.player == null) {
            return;
        }
        if (config.equipmentMode == 1) {
            List<ItemStack> armor = new ArrayList<>();
            addDurability(armor, client.player.getEquippedStack(EquipmentSlot.HEAD));
            addDurability(armor, client.player.getEquippedStack(EquipmentSlot.CHEST));
            addDurability(armor, client.player.getEquippedStack(EquipmentSlot.LEGS));
            addDurability(armor, client.player.getEquippedStack(EquipmentSlot.FEET));
            if (!armor.isEmpty()) renderEquipmentHotbar(context, client.textRenderer, config, armor);
            return;
        }
        List<ItemStack> lines = new ArrayList<>();
        addDurability(lines, client.player.getMainHandStack());
        addDurability(lines, client.player.getEquippedStack(EquipmentSlot.HEAD));
        addDurability(lines, client.player.getEquippedStack(EquipmentSlot.CHEST));
        addDurability(lines, client.player.getEquippedStack(EquipmentSlot.LEGS));
        addDurability(lines, client.player.getEquippedStack(EquipmentSlot.FEET));
        if (lines.isEmpty()) {
            return;
        }
        renderEquipmentPanel(context, client.textRenderer, config, lines);
    }

    private static void addDurability(List<ItemStack> lines, ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.isDamageable()) {
            return;
        }
        lines.add(stack);
    }

    private static void renderEquipmentHotbar(DrawContext context, TextRenderer textRenderer, HudForgeConfig config, List<ItemStack> stacks) {
        int width = 2 + stacks.size() * 20;
        int screenW = context.getScaledWindowWidth();
        int screenH = context.getScaledWindowHeight();
        int x = config.equipmentHotbarSide == 0 ? screenW / 2 - 91 - width - 6 : screenW / 2 + 91 + 6;
        int y = screenH - 22;
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(x, y);
        context.getMatrices().scale(config.equipmentScale, config.equipmentScale);
        fillSoftRect(context, 0, 0, width, 22, Math.min(4, config.equipmentRadius), config.equipmentBackground);
        for (int i = 0; i < stacks.size(); i++) {
            ItemStack stack = stacks.get(i);
            int slotX = 2 + i * 20;
            float fraction = (stack.getMaxDamage() - stack.getDamage()) / (float) Math.max(1, stack.getMaxDamage());
            if (i > 0) context.fill(slotX - 1, 3, slotX, 19, 0x554E5870);
            if (config.equipmentWarning && fraction * 100.0f <= config.equipmentWarningPercent) {
                context.fill(slotX, 1, slotX + 18, 3, 0xFFFF5C5C);
            }
            context.drawItem(stack, slotX + 1, 3);
            if (config.equipmentDurabilityDisplay != 0) {
                int left = Math.max(0, stack.getMaxDamage() - stack.getDamage());
                String value = config.equipmentDurabilityDisplay == 1 ? String.valueOf(left) : Math.round(fraction * 100.0f) + "%";
                context.drawTextWithShadow(textRenderer, value, slotX + 9 - textRenderer.getWidth(value) / 2, -9, durabilityColor(fraction));
            }
        }
        context.getMatrices().popMatrix();
    }

    private static void renderEffectsPanel(DrawContext context, TextRenderer textRenderer, HudForgeConfig config, List<EffectLine> lines) {
        int width = config.effectsWidth;
        for (EffectLine line : lines) {
            width = Math.max(width, textRenderer.getWidth(line.name) + textRenderer.getWidth(line.duration) + 48);
        }
        width = Math.max(110, width);
        int rowHeight = 22;
        int height = 8 + lines.size() * rowHeight;
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(config.effectsX, config.effectsY);
        context.getMatrices().scale(config.effectsScale, config.effectsScale);
        fillSoftRect(context, 0, 0, width, height, config.effectsRadius, config.effectsBackground);
        int y = 6;
        for (EffectLine line : lines) {
            fillSoftRect(context, 7, y - 1, 18, 18, Math.min(5, config.effectsRadius), 0x66000000);
            if (!line.texturePath.isBlank()) {
                context.drawTexture(RenderPipelines.GUI_TEXTURED, Identifier.ofVanilla(line.texturePath), 8, y, 0.0f, 0.0f, 18, 18, 18, 18);
            }
            context.drawTextWithShadow(textRenderer, line.name, 31, y + 1, config.effectsText);
            int tx = width - textRenderer.getWidth(line.duration) - 8;
            context.drawTextWithShadow(textRenderer, line.duration, tx, y + 10, 0xFFB8C0D4);
            y += rowHeight;
        }
        context.getMatrices().popMatrix();
    }

    private static void renderEquipmentPanel(DrawContext context, TextRenderer textRenderer, HudForgeConfig config, List<ItemStack> stacks) {
        int width = config.equipmentWidth;
        int rowHeight = 22;
        int height = 8 + stacks.size() * rowHeight;
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(config.equipmentX, config.equipmentY);
        context.getMatrices().scale(config.equipmentScale, config.equipmentScale);
        fillSoftRect(context, 0, 0, width, height, config.equipmentRadius, config.equipmentBackground);
        int y = 6;
        for (ItemStack stack : stacks) {
            context.drawItem(stack, 7, y - 1);
            int max = stack.getMaxDamage();
            int left = Math.max(0, max - stack.getDamage());
            String label = left + "/" + max;
            context.drawTextWithShadow(textRenderer, label, 30, y + 1, config.equipmentText);
            int barW = Math.max(52, width - 42);
            int filled = Math.round(barW * (left / (float) Math.max(1, max)));
            int barColor = durabilityColor(left / (float) Math.max(1, max));
            context.fill(30, y + 13, 30 + barW, y + 16, 0x99000000);
            context.fill(30, y + 13, 30 + filled, y + 16, barColor);
            y += rowHeight;
        }
        context.getMatrices().popMatrix();
    }

    private static void renderTextPanel(DrawContext context, TextRenderer textRenderer, int x, int y, float scale, int preferredWidth, int radius, int background, int textColor, List<String> lines) {
        if (lines.isEmpty()) {
            return;
        }
        int width = 0;
        for (String line : lines) {
            width = Math.max(width, textRenderer.getWidth(line));
        }
        width = Math.max(preferredWidth, Math.max(76, width + 16));
        int height = 8 + lines.size() * 11;
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(x, y);
        context.getMatrices().scale(scale, scale);
        fillSoftRect(context, 0, 0, width, height, radius, background);
        int lineY = 5;
        for (String line : lines) {
            context.drawTextWithShadow(textRenderer, line, 8, lineY, textColor);
            lineY += 11;
        }
        context.getMatrices().popMatrix();
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

    private static void renderScoreboard(DrawContext context, MinecraftClient client, HudForgeConfig config) {
        if (client.world == null) {
            return;
        }

        Scoreboard scoreboard = client.world.getScoreboard();
        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (objective == null) {
            return;
        }

        TextRenderer textRenderer = client.textRenderer;
        List<ScoreboardEntry> entries = new ArrayList<>(16);
        entries.addAll(scoreboard.getScoreboardEntries(objective));
        entries.sort(Comparator.comparingInt(ScoreboardEntry::value).reversed());

        if (entries.isEmpty()) {
            return;
        }

        int width = Math.max(config.scoreboardWidth, textRenderer.getWidth(objective.getDisplayName()) + 20);
        int titleHeight = 18;
        int rowHeight = 11;
        int rows = Math.min(30, entries.size());
        for (int i = 0; i < rows; i++) {
            ScoreboardEntry entry = entries.get(i);
            Text line = entry.display() == null ? Team.decorateName(scoreboard.getScoreHolderTeam(entry.owner()), Text.literal(entry.owner())) : entry.display();
            width = Math.min(420, Math.max(width, textRenderer.getWidth(line) + 18));
        }
        int height = titleHeight + rows * rowHeight + 6;

        context.getMatrices().pushMatrix();
        context.getMatrices().translate(config.scoreboardX, config.scoreboardY);
        context.getMatrices().scale(config.scoreboardScale, config.scoreboardScale);
        fillSoftRect(context, 0, 0, width, height, config.scoreboardRadius, config.scoreboardBackground);

        Text title = objective.getDisplayName();
        int titleX = MathHelper.clamp((width - textRenderer.getWidth(title)) / 2, 8, width - 8);
        context.drawTextWithShadow(textRenderer, title, titleX, 5, config.scoreboardTitle);

        int y = titleHeight;
        for (int i = 0; i < rows; i++) {
            ScoreboardEntry entry = entries.get(i);
            Text line = entry.display() == null ? Team.decorateName(scoreboard.getScoreHolderTeam(entry.owner()), Text.literal(entry.owner())) : entry.display();
            if (config.scoreboardServerColors) {
                context.drawTextWithShadow(textRenderer, line, 9, y, config.scoreboardText);
            } else {
                context.drawTextWithShadow(textRenderer, trimToWidth(textRenderer, line.getString(), width - 18), 9, y, config.scoreboardText);
            }
            y += rowHeight;
        }

        context.getMatrices().popMatrix();
    }

    private static void renderCrosshair(DrawContext context, HudForgeConfig config, int width, int height) {
        if (config.crosshairOutline && config.crosshairMode != HudForgeConfig.CROSSHAIR_VANILLA) {
            drawCrosshair(context, config, width / 2, height / 2,
                    config.crosshairThickness + config.crosshairOutlineThickness * 2,
                    config.crosshairOutlineColor);
        }
        drawCrosshair(context, config, width / 2, height / 2, config.crosshairThickness, config.crosshairColor);
    }

    private static void drawCrosshair(DrawContext context, HudForgeConfig config, int centerX, int centerY, int thickness, int color) {
        int size = config.crosshairSize;
        int gap = config.crosshairGap;

        if (config.crosshairMode == HudForgeConfig.CROSSHAIR_DOT) {
            drawCenteredRect(context, centerX, centerY, Math.max(thickness, 2), Math.max(thickness, 2), color);
            return;
        }
        if (config.crosshairMode == HudForgeConfig.CROSSHAIR_CIRCLE) {
            int r = Math.max(3, size + gap);
            for (int i = -r; i <= r; i++) {
                int span = Math.round((float) Math.sqrt(Math.max(0, r * r - i * i)));
                if (Math.abs(i) >= r - thickness || span >= r - thickness) {
                    context.fill(centerX + i, centerY - span, centerX + i + 1, centerY - span + thickness, color);
                    context.fill(centerX + i, centerY + span - thickness + 1, centerX + i + 1, centerY + span + 1, color);
                }
            }
            return;
        }

        drawCenteredRect(context, centerX - gap - size / 2, centerY, size, thickness, color);
        drawCenteredRect(context, centerX + gap + size / 2, centerY, size, thickness, color);
        drawCenteredRect(context, centerX, centerY - gap - size / 2, thickness, size, color);
        drawCenteredRect(context, centerX, centerY + gap + size / 2, thickness, size, color);
        if (config.crosshairDot) {
            drawCenteredRect(context, centerX, centerY, thickness, thickness, color);
        }
    }

    private static void drawCenteredRect(DrawContext context, int centerX, int centerY, int width, int height, int color) {
        int left = centerX - width / 2;
        int top = centerY - height / 2;
        context.fill(left, top, left + width, top + height, color);
    }

    private static String trimToWidth(TextRenderer textRenderer, String value, int maxWidth) {
        if (textRenderer.getWidth(value) <= maxWidth) {
            return value;
        }
        return textRenderer.trimToWidth(value, Math.max(8, maxWidth - textRenderer.getWidth("..."))) + "...";
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

    private static String formatDuration(int ticks) {
        if (ticks < 0) return "∞";
        long seconds = ticks / 20L;
        long days = seconds / 86400L;
        long hours = (seconds % 86400L) / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        long secs = seconds % 60L;
        if (days > 0) return String.format(java.util.Locale.ROOT, "%dd %02d:%02d:%02d", days, hours, minutes, secs);
        if (hours > 0) return String.format(java.util.Locale.ROOT, "%d:%02d:%02d", hours, minutes, secs);
        return String.format(java.util.Locale.ROOT, "%d:%02d", minutes, secs);
    }

    private record EffectLine(String name, String duration, int color, String texturePath) {
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
}
