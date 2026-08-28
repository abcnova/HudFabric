package dev.nova.hudfabric;

import com.mojang.blaze3d.platform.InputConstants;
import dev.nova.hudfabric.config.HudForgeConfig;
import dev.nova.hudfabric.gui.HudForgeConfigScreen;
import dev.nova.hudfabric.render.HudForgeRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayDeque;

public final class HudForgeClient implements ClientModInitializer {
    public static final String MOD_ID = "hudfabric";
    public static HudForgeConfig config;

    private static KeyMapping openConfigKey;
    private static String activeServerKey = "";
    private static int deathScreenTicks = 0;
    private static Object lastLevel;
    private static Object lastPlayer;
    private static final ArrayDeque<Long> LEFT_CLICKS = new ArrayDeque<>();
    private static final ArrayDeque<Long> RIGHT_CLICKS = new ArrayDeque<>();
    private static boolean leftDown, rightDown;
    private static ServerData lastServer;
    private static Button reconnectButton;
    private static int reconnectTicks;

    @Override
    public void onInitializeClient() {
        config = HudForgeConfig.load();
        openConfigKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.hudfabric.open_config",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "hud"))
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            updateCps(client);
            updateReconnect(client);
            if (client.getCurrentServer() != null) lastServer = client.getCurrentServer();
            clearEffectsAfterWorldChange(client);
            updateServerProfile(client);
            while (openConfigKey.consumeClick()) {
                openConfigScreen(client.gui.screen());
            }
            if (config != null && config.autoRespawn && client.gui.screen() instanceof DeathScreen && client.player != null) {
                deathScreenTicks++;
                if (deathScreenTicks >= config.autoRespawnDelayTicks) {
                    client.player.respawn();
                    deathScreenTicks = 0;
                }
            } else {
                deathScreenTicks = 0;
            }
        });

        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "main"), HudForgeRenderer::render);

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof PauseScreen) {
                int buttonWidth = Math.min(112, Math.max(88, scaledWidth / 5));
                int buttonX = scaledWidth - buttonWidth - 10;
                int buttonY = scaledHeight - 32;
                for (AbstractWidget widget : Screens.getWidgets(screen)) {
                    if (overlaps(buttonX, buttonY, buttonWidth, 20, widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight())) {
                        buttonY = Math.max(8, widget.getY() - 24);
                    }
                }
                Screens.getWidgets(screen).add(Button.builder(Component.translatable("hudfabric.button.hud_editor"), button -> openConfigScreen(screen))
                        .bounds(buttonX, buttonY, buttonWidth, 20)
                        .build());
            }
            if (screen instanceof DisconnectedScreen && lastServer != null) {
                reconnectTicks = Math.max(20, config.autoReconnectDelaySeconds * 20);
                reconnectButton = Button.builder(reconnectText(), button -> reconnect(client)).bounds(scaledWidth / 2 - 100, scaledHeight - 52, 200, 20).build();
                Screens.getWidgets(screen).add(reconnectButton);
            }
        });
    }

    private static void updateReconnect(Minecraft client) {
        if (!(client.gui.screen() instanceof DisconnectedScreen) || reconnectButton == null) return;
        if (config.autoReconnect && reconnectTicks > 0) reconnectTicks--;
        reconnectButton.setMessage(reconnectText());
        if (config.autoReconnect && reconnectTicks == 0) { reconnectTicks = -1; reconnect(client); }
    }
    private static Component reconnectText() { return config != null && config.autoReconnect && reconnectTicks > 0 ? Component.literal("Reconnect (" + ((reconnectTicks + 19) / 20) + "s)") : Component.literal("Reconnect now"); }
    private static void reconnect(Minecraft client) {
        if (lastServer != null) ConnectScreen.startConnecting(new JoinMultiplayerScreen(new TitleScreen()), client, ServerAddress.parseString(lastServer.ip), lastServer, false, null);
    }

    private static void updateCps(Minecraft client) {
        long now = System.currentTimeMillis();
        long window = client.getWindow().handle();
        boolean left = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean right = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        if (client.gui.screen() == null) {
            if (left && !leftDown) LEFT_CLICKS.addLast(now);
            if (right && !rightDown) RIGHT_CLICKS.addLast(now);
        }
        leftDown = left; rightDown = right;
        trimClicks(LEFT_CLICKS, now); trimClicks(RIGHT_CLICKS, now);
    }
    private static void trimClicks(ArrayDeque<Long> clicks, long now) { while (!clicks.isEmpty() && now - clicks.peekFirst() > 1000L) clicks.removeFirst(); }
    public static int leftCps() { return LEFT_CLICKS.size(); }
    public static int rightCps() { return RIGHT_CLICKS.size(); }

    private static void clearEffectsAfterWorldChange(Minecraft client) {
        if (client.level != lastLevel || client.player != lastPlayer) {
            lastLevel = client.level;
            lastPlayer = client.player;
            if (client.player != null) {
                client.player.removeAllEffects();
            }
        }
    }

    public static void openConfigScreen(Screen parent) {
        Minecraft.getInstance().gui.setScreen(new HudForgeConfigScreen(parent));
    }

    public static void saveActiveServerProfile() {
        if (config != null && activeServerKey != null && !activeServerKey.isBlank()) {
            config.saveActiveProfile(activeServerKey);
            config.save();
        }
    }

    private static void updateServerProfile(Minecraft client) {
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

    private static String serverKey(Minecraft client) {
        if (client.level == null) {
            return "";
        }
        ServerData info = client.getCurrentServer();
        if (info != null && info.ip != null && !info.ip.isBlank()) {
            return "server:" + info.ip.trim().toLowerCase(java.util.Locale.ROOT);
        }
        if (client.isLocalServer()) {
            return "singleplayer";
        }
        return "unknown";
    }

    private static boolean overlaps(int ax, int ay, int aw, int ah, int bx, int by, int bw, int bh) {
        return ax < bx + bw && ax + aw > bx && ay < by + bh && ay + ah > by;
    }
}
