package dev.nova.hudfabric.gui;

import dev.nova.hudfabric.HudFabricClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Lets players copy a privacy-safe settings code into Discord or import one from the clipboard. */
public final class HudFabricShareScreen extends Screen {
    private final Screen parent;
    private String status = "";

    public HudFabricShareScreen(Screen parent) {
        super(Component.translatable("hudfabric.share.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int buttonWidth = Math.min(220, width - 24);
        int left = (width - buttonWidth) / 2;
        int top = height / 2 - 8;
        addRenderableWidget(Button.builder(Component.translatable("hudfabric.share.copy"), button -> {
            Minecraft.getInstance().keyboardHandler.setClipboard(HudFabricClient.exportSettingsShareCode());
            status = "hudfabric.share.copied";
        }).bounds(left, top, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("hudfabric.share.import"), button -> {
            try {
                HudFabricClient.importSettingsShareCode(Minecraft.getInstance().keyboardHandler.getClipboard());
                status = "hudfabric.share.imported";
            } catch (IllegalArgumentException exception) {
                status = "hudfabric.share.invalid";
            }
        }).bounds(left, top + 26, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("button.back"), button -> onClose())
                .bounds(left, top + 58, buttonWidth, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xC0080D18);
        context.text(font, Component.translatable("hudfabric.share.instructions"), Math.max(12, width / 2 - 150), height / 2 - 54, 0xFFEAF0FF);
        if (!status.isEmpty()) context.text(font, Component.translatable(status), width / 2 - 80, height / 2 + 86, 0xFF8CE6C0);
        super.extractRenderState(context, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().gui.setScreen(parent);
    }
}
