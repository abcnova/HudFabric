package dev.nova.hudfabric.test;

import dev.nova.hudfabric.HudFabricClient;
import dev.nova.hudfabric.gui.HudFabricConfigScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.screens.TitleScreen;

@SuppressWarnings("UnstableApiUsage")
public final class HudEditorClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        context.runOnClient(client -> {
            HudFabricClient.config.statsEnabled = true;
            HudFabricClient.config.statsX = 20;
            HudFabricClient.config.statsY = 60;
        });
        context.setScreen(() -> new HudFabricConfigScreen(null));
        context.waitForScreen(HudFabricConfigScreen.class);

        double scale = context.computeOnClient(client -> client.getWindow().getGuiScale());
        context.getInput().setCursorPos(30 * scale, 70 * scale);
        context.getInput().holdMouse(InputConstants.MOUSE_BUTTON_LEFT);
        context.getInput().moveCursor(45 * scale, 30 * scale);
        context.waitTick();
        context.getInput().releaseMouse(InputConstants.MOUSE_BUTTON_LEFT);

        int[] position = context.computeOnClient(client -> new int[]{HudFabricClient.config.statsX, HudFabricClient.config.statsY});
        if (position[0] == 20 && position[1] == 60) {
            throw new AssertionError("The stats HUD did not move after a mouse drag");
        }
        context.setScreen(() -> new HudFabricConfigScreen(null, true));
        context.waitForScreen(HudFabricConfigScreen.class);
        context.takeScreenshot("hudfabric-settings-overview");
        context.setScreen(TitleScreen::new);
    }
}
