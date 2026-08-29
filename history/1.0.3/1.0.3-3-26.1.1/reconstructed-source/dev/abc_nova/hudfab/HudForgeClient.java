/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants$Type
 *  net.fabricmc.api.ClientModInitializer
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
 *  net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
 *  net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
 *  net.fabricmc.fabric.api.client.screen.v1.Screens
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.KeyMapping$Category
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.components.AbstractWidget
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.screens.DeathScreen
 *  net.minecraft.client.gui.screens.PauseScreen
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.multiplayer.ServerData
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.Identifier
 */
package dev.abc_nova.hudfab;

import com.mojang.blaze3d.platform.InputConstants;
import dev.abc_nova.hudfab.config.HudForgeConfig;
import dev.abc_nova.hudfab.gui.HudForgeConfigScreen;
import dev.abc_nova.hudfab.render.HudForgeRenderer;
import java.util.Locale;
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
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class HudForgeClient
implements ClientModInitializer {
    public static final String MOD_ID = "hudfabric";
    public static HudForgeConfig config;
    private static KeyMapping openConfigKey;
    private static String activeServerKey;
    private static int deathScreenTicks;
    private static Object lastLevel;
    private static Object lastPlayer;

    public void onInitializeClient() {
        config = HudForgeConfig.load();
        openConfigKey = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("key.hudfabric.open_config", InputConstants.Type.KEYSYM, 344, KeyMapping.Category.register((Identifier)Identifier.fromNamespaceAndPath((String)MOD_ID, (String)"hud"))));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            HudForgeClient.clearEffectsAfterWorldChange(client);
            HudForgeClient.updateServerProfile(client);
            while (openConfigKey.consumeClick()) {
                HudForgeClient.openConfigScreen(client.screen);
            }
            if (config != null && HudForgeClient.config.autoRespawn && client.screen instanceof DeathScreen && client.player != null) {
                if (++deathScreenTicks >= 20) {
                    client.player.respawn();
                    deathScreenTicks = 0;
                }
            } else {
                deathScreenTicks = 0;
            }
        });
        HudElementRegistry.addLast((Identifier)Identifier.fromNamespaceAndPath((String)MOD_ID, (String)"main"), HudForgeRenderer::render);
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof PauseScreen) {
                int buttonWidth = Math.min(112, Math.max(88, scaledWidth / 5));
                int buttonX = scaledWidth - buttonWidth - 10;
                int buttonY = scaledHeight - 32;
                for (AbstractWidget widget : Screens.getWidgets((Screen)screen)) {
                    if (!HudForgeClient.overlaps(buttonX, buttonY, buttonWidth, 20, widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight())) continue;
                    buttonY = Math.max(8, widget.getY() - 24);
                }
                Screens.getWidgets((Screen)screen).add(Button.builder((Component)Component.translatable((String)"hudfabric.button.hud_editor"), button -> HudForgeClient.openConfigScreen(screen)).bounds(buttonX, buttonY, buttonWidth, 20).build());
            }
        });
    }

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
        Minecraft.getInstance().setScreen((Screen)new HudForgeConfigScreen(parent));
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
        String key = HudForgeClient.serverKey(client);
        if (!key.equals(activeServerKey)) {
            if (!activeServerKey.isBlank()) {
                config.saveActiveProfile(activeServerKey);
                config.save();
            }
            if (!(activeServerKey = key).isBlank()) {
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
            return "server:" + info.ip.trim().toLowerCase(Locale.ROOT);
        }
        if (client.isLocalServer()) {
            return "singleplayer";
        }
        return "unknown";
    }

    private static boolean overlaps(int ax, int ay, int aw, int ah, int bx, int by, int bw, int bh) {
        return ax < bx + bw && ax + aw > bx && ay < by + bh && ay + ah > by;
    }

    static {
        activeServerKey = "";
        deathScreenTicks = 0;
    }
}

