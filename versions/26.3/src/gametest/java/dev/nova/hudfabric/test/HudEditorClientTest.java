package dev.nova.hudfabric.test;

import dev.nova.hudfabric.HudForgeClient;
import dev.nova.hudfabric.gui.HudForgeConfigScreen;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.screens.TitleScreen;

@SuppressWarnings("UnstableApiUsage")
public final class HudEditorClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        context.runOnClient(client -> {
            HudForgeClient.config.statsEnabled = true;
            HudForgeClient.config.statsX = 20;
            HudForgeClient.config.statsY = 60;
        });
        context.setScreen(() -> new HudForgeConfigScreen(null));
        context.waitForScreen(HudForgeConfigScreen.class);

        double scale = context.computeOnClient(client -> client.getWindow().getGuiScale());
        context.getInput().setCursorPos(30 * scale, 70 * scale);
        context.getInput().holdMouse(0);
        context.getInput().moveCursor(45 * scale, 30 * scale);
        context.waitTick();
        context.getInput().releaseMouse(0);

        int[] position = context.computeOnClient(client -> new int[]{HudForgeClient.config.statsX, HudForgeClient.config.statsY});
        if (position[0] == 20 && position[1] == 60) {
            throw new AssertionError("The stats HUD did not move after a mouse drag");
        }
        context.setScreen(() -> new HudForgeConfigScreen(null, true));
        context.waitForScreen(HudForgeConfigScreen.class);
        context.takeScreenshot("hudfabric-settings-overview");
        context.setScreen(TitleScreen::new);
    }
}
