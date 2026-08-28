package dev.nova.hudfabric;

import dev.nova.hudfabric.config.HudForgeConfig;
import dev.nova.hudfabric.gui.HudForgeConfigScreen;
import dev.nova.hudfabric.render.HudForgeRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayDeque;

public final class HudForgeClient implements ClientModInitializer {
    public static final String MOD_ID = "hudfabric";
    public static HudForgeConfig config;

    private static KeyBinding openConfigKey;
    private static String activeServerKey = "";
    private static int deathScreenTicks = 0;
    private static final ArrayDeque<Long> LEFT_CLICKS = new ArrayDeque<>();
    private static final ArrayDeque<Long> RIGHT_CLICKS = new ArrayDeque<>();
    private static boolean leftDown;
    private static boolean rightDown;
    private static ServerInfo lastServer;
    private static ButtonWidget reconnectButton;
    private static int reconnectTicks;

    @Override
    public void onInitializeClient() {
        config = HudForgeConfig.load();
        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hudfabric.open_config",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyBinding.Category.create(Identifier.of(MOD_ID, "hud"))
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            updateCps(client);
            updateReconnect(client);
            if (client.getCurrentServerEntry() != null) lastServer = client.getCurrentServerEntry();
            updateServerProfile(client);
            while (openConfigKey.wasPressed()) {
                openConfigScreen(client.currentScreen);
            }
            if (config != null && config.autoRespawn && client.currentScreen instanceof DeathScreen && client.player != null) {
                deathScreenTicks++;
                if (deathScreenTicks >= config.autoRespawnDelayTicks) {
                    client.player.requestRespawn();
                    deathScreenTicks = 0;
                }
            } else {
                deathScreenTicks = 0;
            }
        });

        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((client, world) -> {
            if (client.player != null) {
                client.player.clearStatusEffects();
            }
        });

        HudRenderCallback.EVENT.register(HudForgeRenderer::render);

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof GameMenuScreen) {
                int buttonWidth = Math.min(112, Math.max(88, scaledWidth / 5));
                int buttonX = scaledWidth - buttonWidth - 10;
                int buttonY = scaledHeight - 32;
                for (ClickableWidget widget : Screens.getButtons(screen)) {
                    if (overlaps(buttonX, buttonY, buttonWidth, 20, widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight())) {
                        buttonY = Math.max(8, widget.getY() - 24);
                    }
                }
                Screens.getButtons(screen).add(ButtonWidget.builder(Text.translatable("hudfabric.button.hud_editor"), button -> openConfigScreen(screen))
                        .dimensions(buttonX, buttonY, buttonWidth, 20)
                        .build());
            }
            if (screen instanceof DisconnectedScreen && lastServer != null) {
                reconnectTicks = Math.max(20, config.autoReconnectDelaySeconds * 20);
                reconnectButton = ButtonWidget.builder(reconnectText(), button -> reconnect(client))
                        .dimensions(scaledWidth / 2 - 100, scaledHeight - 52, 200, 20).build();
                Screens.getButtons(screen).add(reconnectButton);
            }
        });
    }

    private static void updateReconnect(MinecraftClient client) {
        if (!(client.currentScreen instanceof DisconnectedScreen) || reconnectButton == null) return;
        if (config.autoReconnect && reconnectTicks > 0) reconnectTicks--;
        reconnectButton.setMessage(reconnectText());
        if (config.autoReconnect && reconnectTicks == 0) {
            reconnectTicks = -1;
            reconnect(client);
        }
    }

    private static Text reconnectText() {
        if (config != null && config.autoReconnect && reconnectTicks > 0) return Text.literal("Reconnect (" + ((reconnectTicks + 19) / 20) + "s)");
        return Text.literal("Reconnect now");
    }

    private static void reconnect(MinecraftClient client) {
        if (lastServer == null) return;
        ConnectScreen.connect(new MultiplayerScreen(new TitleScreen()), client, ServerAddress.parse(lastServer.address), lastServer, false, null);
    }

    private static void updateCps(MinecraftClient client) {
        long now = System.currentTimeMillis();
        long window = client.getWindow().getHandle();
        boolean left = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean right = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        if (client.currentScreen == null) {
            if (left && !leftDown) LEFT_CLICKS.addLast(now);
            if (right && !rightDown) RIGHT_CLICKS.addLast(now);
        }
        leftDown = left;
        rightDown = right;
        trimClicks(LEFT_CLICKS, now);
        trimClicks(RIGHT_CLICKS, now);
    }

    private static void trimClicks(ArrayDeque<Long> clicks, long now) {
        while (!clicks.isEmpty() && now - clicks.peekFirst() > 1000L) clicks.removeFirst();
    }

    public static int leftCps() { return LEFT_CLICKS.size(); }
    public static int rightCps() { return RIGHT_CLICKS.size(); }

    public static void openConfigScreen(Screen parent) {
        MinecraftClient.getInstance().setScreen(new HudForgeConfigScreen(parent));
    }

    public static void saveActiveServerProfile() {
        if (config != null && activeServerKey != null && !activeServerKey.isBlank()) {
            config.saveActiveProfile(activeServerKey);
            config.save();
        }
    }

    private static void updateServerProfile(MinecraftClient client) {
        if (config == null) {
            return;
        }
        String key = serverKey(client);
        if (!key.equals(activeServerKey)) {
            if (!activeServerKey.isBlank()) {
                config.saveActiveProfile(activeServerKey);
                config.save();
            }
            activeServerKey = key;
            if (!activeServerKey.isBlank()) {
                config.applyProfile(activeServerKey);
                config.saveActiveProfile(activeServerKey);
                config.save();
            }
        }
    }

    private static String serverKey(MinecraftClient client) {
        if (client.world == null) {
            return "";
        }
        ServerInfo info = client.getCurrentServerEntry();
        if (info != null && info.address != null && !info.address.isBlank()) {
            return "server:" + info.address.trim().toLowerCase(java.util.Locale.ROOT);
        }
        if (client.isInSingleplayer()) {
            return "singleplayer";
        }
        return "unknown";
    }

    private static boolean overlaps(int ax, int ay, int aw, int ah, int bx, int by, int bw, int bh) {
        return ax < bx + bw && ax + aw > bx && ay < by + bh && ay + ah > by;
    }
}
