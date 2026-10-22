package dev.nova.hudfabric.gui;

import dev.nova.hudfabric.HudFabricClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Copies a privacy-safe profile code for Discord or imports one from the clipboard. */
public final class HudFabricShareScreen extends Screen {
    private final Screen parent;
    private String status = "";

    public HudFabricShareScreen(Screen parent) {
        super(Text.translatable("hudfabric.share.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int buttonWidth = Math.min(220, width - 24);
        int left = (width - buttonWidth) / 2;
        int top = height / 2 - 8;
        addDrawableChild(ButtonWidget.builder(Text.translatable("hudfabric.share.copy"), button -> {
            MinecraftClient.getInstance().keyboard.setClipboard(HudFabricClient.exportSettingsShareCode());
            status = "hudfabric.share.copied";
        }).dimensions(left, top, buttonWidth, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("hudfabric.share.import"), button -> {
            try {
                HudFabricClient.importSettingsShareCode(MinecraftClient.getInstance().keyboard.getClipboard());
                status = "hudfabric.share.imported";
            } catch (IllegalArgumentException exception) {
                status = "hudfabric.share.invalid";
            }
        }).dimensions(left, top + 26, buttonWidth, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("button.back"), button -> close())
                .dimensions(left, top + 58, buttonWidth, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, Text.translatable("hudfabric.share.instructions"), width / 2, height / 2 - 54, 0xFFEAF0FF);
        if (!status.isEmpty()) context.drawCenteredTextWithShadow(textRenderer, Text.translatable(status), width / 2, height / 2 + 88, 0xFF8CE6C0);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }
}
