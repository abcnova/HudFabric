package dev.nova.hudfabric.render;

import dev.nova.hudfabric.HudForgeClient;
import dev.nova.hudfabric.JumpResetTiming;
import dev.nova.hudfabric.config.HudForgeConfig;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
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
        if (HudForgeClient.jumpResetVisible()) renderJumpReset(context, client.textRenderer, config);

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

    private static void renderJumpReset(DrawContext context, TextRenderer font, HudForgeConfig config) {
        int d = HudForgeClient.jumpResetDelta();
        JumpResetTiming.Result result = JumpResetTiming.classify(d);
        String key = "hudfabric.jump_reset." + result.name().toLowerCase(java.util.Locale.ROOT);
        Text line = result == JumpResetTiming.Result.PERFECT || result == JumpResetTiming.Result.MISSED ? Text.translatable(key) : Text.translatable(key, Math.abs(d));
        int w = Math.max(config.jumpResetWidth, font.getWidth(line) + 20);
        int x = config.jumpResetMode == 0 ? (context.getScaledWindowWidth() - font.getWidth(line)) / 2 : config.jumpResetX;
        int y = config.jumpResetMode == 0 ? context.getScaledWindowHeight() / 2 - 38 : config.jumpResetY;
        context.getMatrices().pushMatrix(); context.getMatrices().translate(x, y); context.getMatrices().scale(config.jumpResetScale, config.jumpResetScale);
        int color = result == JumpResetTiming.Result.PERFECT ? 0xFF65E58A : result == JumpResetTiming.Result.EARLY || result == JumpResetTiming.Result.LATE ? 0xFFFFC857 : 0xFFFF647C;
        if (config.jumpResetMode == 1) { fillSoftRect(context, 0, 0, w, 24, config.jumpResetRadius, config.jumpResetBackground); context.drawTextWithShadow(font, line, (w - font.getWidth(line)) / 2, 8, config.jumpResetText); }
        else context.drawTextWithShadow(font, line, 0, 0, color);
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
            armor.add(client.player.getEquippedStack(EquipmentSlot.HEAD)); armor.add(client.player.getEquippedStack(EquipmentSlot.CHEST));
            armor.add(client.player.getEquippedStack(EquipmentSlot.LEGS)); armor.add(client.player.getEquippedStack(EquipmentSlot.FEET));
            if (armor.stream().anyMatch(s -> !s.isEmpty() && s.isDamageable())) renderEquipmentHotbar(context, client.textRenderer, config, armor);
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
        int width = 22 + (stacks.size() - 1) * 20;
        int screenW = context.getScaledWindowWidth();
        int screenH = context.getScaledWindowHeight();
        boolean offhandOnLeft = MinecraftClient.getInstance().player == null || MinecraftClient.getInstance().player.getMainArm() == net.minecraft.util.Arm.RIGHT;
        int defaultX = screenW / 2 - 91 - width - 6 - (offhandOnLeft ? 29 : 0);
        int x = config.equipmentHotbarX < 0 ? defaultX : Math.min(config.equipmentHotbarX, Math.max(0, screenW - width));
        int y = config.equipmentHotbarY < 0 ? screenH - 22 : Math.min(config.equipmentHotbarY, Math.max(0, screenH - 22));
        context.getMatrices().pushMatrix();
        if (!config.equipmentHotbarSeparate) context.getMatrices().translate(x, y);
        if (config.equipmentHotbarBackground && !config.equipmentHotbarSeparate) {
            Identifier hotbar = Identifier.ofVanilla("hud/hotbar");
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, hotbar, 182, 22, 0, 0, 0, 0, width - 3, 22);
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, hotbar, 182, 22, 179, 0, width - 3, 0, 3, 22);
        }
        for (int i = 0; i < stacks.size(); i++) {
            ItemStack stack = stacks.get(i);
            int slotX = config.equipmentHotbarSeparate ? (config.equipmentSlotX[i] < 0 ? x + i * 20 : config.equipmentSlotX[i]) : i * 20;
            int slotY = config.equipmentHotbarSeparate ? (config.equipmentSlotY[i] < 0 ? y : config.equipmentSlotY[i]) : 0;
            if (config.equipmentHotbarBackground && config.equipmentHotbarSeparate) {
                drawIndependentVanillaSlot(context, slotX, slotY);
            }
            if (stack.isEmpty() || !stack.isDamageable()) continue;
            float fraction = (stack.getMaxDamage() - stack.getDamage()) / (float) Math.max(1, stack.getMaxDamage());
            context.drawItem(stack, slotX + 3, slotY + 3);
            int barWidth = Math.max(0, Math.min(13, Math.round(13.0f * fraction)));
            if (stack.getDamage() > 0) {
                context.fill(slotX + 5, slotY + 18, slotX + 18, slotY + 20, 0xFF000000);
                context.fill(slotX + 5, slotY + 18, slotX + 5 + barWidth, slotY + 19, durabilityColor(fraction));
            }
            if (config.equipmentDurabilityDisplay != 0) {
                int left = Math.max(0, stack.getMaxDamage() - stack.getDamage());
                String value = config.equipmentDurabilityDisplay == 1 ? String.valueOf(left) : Math.round(fraction * 100.0f) + "%";
                context.drawTextWithShadow(textRenderer, value, slotX + 11 - textRenderer.getWidth(value) / 2, slotY - 9, durabilityColor(fraction));
            }
            if (config.equipmentWarning && fraction * 100.0f <= config.equipmentWarningPercent) {
                context.drawTextWithShadow(textRenderer, "!", slotX + 16, slotY + 1, 0xFFFF5555);
            }
        }
        context.getMatrices().popMatrix();
    }

    private static void drawIndependentVanillaSlot(DrawContext context, int x, int y) {
        context.fill(x, y, x + 22, y + 22, 0xFF080808); context.fill(x + 1, y + 1, x + 21, y + 21, 0xFFB0B0B0);
        context.fill(x + 2, y + 2, x + 20, y + 20, 0xFF5A5A5A); context.fill(x + 3, y + 3, x + 19, y + 19, 0xCC151515);
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
