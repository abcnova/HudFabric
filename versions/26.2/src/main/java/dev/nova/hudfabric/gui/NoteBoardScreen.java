package dev.nova.hudfabric.gui;

import dev.nova.hudfabric.HudFabricClient;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Local-only editor for the movable in-game note board. */
public final class NoteBoardScreen extends Screen {
    private final Screen parent;
    private final List<EditBox> lines = new ArrayList<>();

    public NoteBoardScreen(Screen parent) {
        super(Component.translatable("hudfabric.note.editor_title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        lines.clear();
        int panelX = (width - 360) / 2;
        int panelY = (height - 252) / 2;
        String[] saved = HudFabricClient.config.noteBoardText.split("\\n", -1);
        for (int i = 0; i < 5; i++) {
            EditBox field = new EditBox(font, panelX + 40, panelY + 68 + i * 27, 280, 20, Component.empty());
            field.setMaxLength(64);
            field.setBordered(false);
            field.setTextColor(0xFF3D3023);
            if (i < saved.length) field.setValue(saved[i]);
            lines.add(addRenderableWidget(field));
        }
        setInitialFocus(lines.getFirst());
        addRenderableWidget(Button.builder(Component.translatable("hudfabric.button.done"), button -> onClose())
                .bounds(panelX + 246, panelY + 218, 96, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xA0000000);
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
        context.text(font, Component.translatable("hudfabric.note.editor_title").getString(), x + 34, y + 29, 0xFF56351F);
        context.text(font, Component.translatable("hudfabric.note.editor_hint").getString(), x + 34, y + 45, 0xFF806C55);
        for (int i = 0; i < 5; i++) {
            int rowY = y + 66 + i * 27;
            context.fill(x + 32, rowY, x + 328, rowY + 23, 0xFFFFF8E1);
            context.fill(x + 32, rowY + 22, x + 328, rowY + 23, 0x558B7355);
        }
        super.extractRenderState(context, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        HudFabricClient.config.noteBoardText = String.join("\n", lines.stream().map(EditBox::getValue).toList()).stripTrailing();
        HudFabricClient.config.noteBoardEnabled = true;
        HudFabricClient.config.save();
        net.minecraft.client.Minecraft.getInstance().gui.setScreen(parent);
    }
}
