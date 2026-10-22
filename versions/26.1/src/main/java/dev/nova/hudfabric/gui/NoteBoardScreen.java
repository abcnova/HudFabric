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
        int panelX = (width - 300) / 2;
        int panelY = (height - 174) / 2;
        String[] saved = HudFabricClient.config.noteBoardText.split("\\n", -1);
        for (int i = 0; i < 5; i++) {
            EditBox field = new EditBox(font, panelX + 16, panelY + 42 + i * 23, 268, 20, Component.empty());
            field.setMaxLength(64);
            if (i < saved.length) field.setValue(saved[i]);
            lines.add(addRenderableWidget(field));
        }
        setInitialFocus(lines.getFirst());
        addRenderableWidget(Button.builder(Component.translatable("hudfabric.button.done"), button -> onClose())
                .bounds(panelX + 184, panelY + 158, 100, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xA0000000);
        int x = (width - 300) / 2;
        int y = (height - 174) / 2;
        context.fill(x, y, x + 300, y + 174, 0xF20A1020);
        context.fill(x, y, x + 300, y + 2, 0xFF7286FF);
        context.text(font, Component.translatable("hudfabric.note.editor_title").getString(), x + 16, y + 14, 0xFFEAF0FF);
        context.text(font, Component.translatable("hudfabric.note.editor_hint").getString(), x + 16, y + 30, 0xFF9DA8C3);
        super.extractRenderState(context, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        HudFabricClient.config.noteBoardText = String.join("\n", lines.stream().map(EditBox::getValue).toList()).stripTrailing();
        HudFabricClient.config.noteBoardEnabled = true;
        HudFabricClient.config.save();
        net.minecraft.client.Minecraft.getInstance().setScreen(parent);
    }
}

