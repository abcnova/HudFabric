package dev.nova.hudfabric.gui;

import dev.nova.hudfabric.HudFabricClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/** Small local-only editor for the movable in-game note board. */
public final class NoteBoardScreen extends Screen {
    private final Screen parent;
    private final List<TextFieldWidget> lines = new ArrayList<>();

    public NoteBoardScreen(Screen parent) {
        super(Text.translatable("hudfabric.note.editor_title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        lines.clear();
        int panelX = (width - 300) / 2;
        int panelY = (height - 174) / 2;
        String[] saved = HudFabricClient.config.noteBoardText.split("\\n", -1);
        for (int i = 0; i < 5; i++) {
            TextFieldWidget field = new TextFieldWidget(textRenderer, panelX + 16, panelY + 42 + i * 23, 268, 20, Text.empty());
            field.setMaxLength(64);
            if (i < saved.length) field.setText(saved[i]);
            lines.add(addDrawableChild(field));
        }
        setInitialFocus(lines.get(0));
        addDrawableChild(ButtonWidget.builder(Text.translatable("hudfabric.button.done"), button -> close())
                .dimensions(panelX + 184, panelY + 158, 100, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        int x = (width - 300) / 2;
        int y = (height - 174) / 2;
        context.fill(x, y, x + 300, y + 174, 0xF20A1020);
        context.fill(x, y, x + 300, y + 2, 0xFF7286FF);
        context.drawTextWithShadow(textRenderer, Text.translatable("hudfabric.note.editor_title"), x + 16, y + 14, 0xFFEAF0FF);
        context.drawTextWithShadow(textRenderer, Text.translatable("hudfabric.note.editor_hint"), x + 16, y + 30, 0xFF9DA8C3);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        HudFabricClient.config.noteBoardText = String.join("\n", lines.stream().map(TextFieldWidget::getText).toList()).stripTrailing();
        HudFabricClient.config.noteBoardEnabled = true;
        HudFabricClient.config.save();
        MinecraftClient.getInstance().setScreen(parent);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}

