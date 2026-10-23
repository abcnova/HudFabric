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
        int panelX = (width - 360) / 2;
        int panelY = (height - 252) / 2;
        String[] saved = HudFabricClient.config.noteBoardText.split("\\n", -1);
        for (int i = 0; i < 5; i++) {
            TextFieldWidget field = new TextFieldWidget(textRenderer, panelX + 40, panelY + 68 + i * 27, 280, 20, Text.empty());
            field.setMaxLength(64);
            field.setDrawsBackground(false);
            field.setEditableColor(0xFF3D3023);
            if (i < saved.length) field.setText(saved[i]);
            lines.add(addDrawableChild(field));
        }
        setInitialFocus(lines.get(0));
        addDrawableChild(ButtonWidget.builder(Text.translatable("hudfabric.button.done"), button -> close())
                .dimensions(panelX + 246, panelY + 218, 96, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        int x = (width - 360) / 2;
        int y = (height - 252) / 2;
        context.fill(x + 4, y + 5, x + 364, y + 257, 0x88000000);
        context.fill(x, y, x + 360, y + 252, 0xFF382317);
        context.fill(x + 3, y + 3, x + 357, y + 249, 0xFF9A6036);
        context.fill(x + 7, y + 7, x + 353, y + 245, 0xFF52321E);
        for (int row = 13; row < 242; row += 12) context.fill(x + 8, y + row, x + 352, y + row + 1, row % 24 == 1 ? 0x557F4D2D : 0x55613720);
        context.fill(x + 14, y + 14, x + 346, y + 238, 0xFF986A43);
        for (int row = 18; row < 234; row += 9) { int offset = (row / 9 % 2) * 5; for (int col = 19 + offset; col < 341; col += 13) context.fill(x + col, y + row, x + col + 1, y + row + 1, 0x55754B30); }
        context.fill(x + 22, y + 21, x + 338, y + 235, 0xFFFFE9B7);
        context.fill(x + 24, y + 23, x + 336, y + 233, 0xFFFFF3D4);
        context.fill(x + 178, y + 17, x + 182, y + 23, 0xFF7D211B);
        context.fill(x + 179, y + 17, x + 181, y + 20, 0xFFE35D44);
        context.drawTextWithShadow(textRenderer, Text.translatable("hudfabric.note.editor_title"), x + 34, y + 29, 0xFF56351F);
        context.drawTextWithShadow(textRenderer, Text.translatable("hudfabric.note.editor_hint"), x + 34, y + 45, 0xFF806C55);
        for (int i = 0; i < 5; i++) { int rowY = y + 66 + i * 27; context.fill(x + 32, rowY, x + 328, rowY + 23, 0xFFFFF8E1); context.fill(x + 32, rowY + 22, x + 328, rowY + 23, 0x558B7355); }
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

