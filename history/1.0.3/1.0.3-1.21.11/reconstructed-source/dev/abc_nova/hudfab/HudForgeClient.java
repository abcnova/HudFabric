/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.ClientModInitializer
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents
 *  net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
 *  net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
 *  net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
 *  net.fabricmc.fabric.api.client.screen.v1.Screens
 *  net.minecraft.class_2561
 *  net.minecraft.class_2960
 *  net.minecraft.class_304
 *  net.minecraft.class_304$class_11900
 *  net.minecraft.class_310
 *  net.minecraft.class_339
 *  net.minecraft.class_3675$class_307
 *  net.minecraft.class_418
 *  net.minecraft.class_4185
 *  net.minecraft.class_433
 *  net.minecraft.class_437
 *  net.minecraft.class_642
 */
package dev.abc_nova.hudfab;

import dev.abc_nova.hudfab.config.HudForgeConfig;
import dev.abc_nova.hudfab.gui.HudForgeConfigScreen;
import dev.abc_nova.hudfab.render.HudForgeRenderer;
import java.util.Locale;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_339;
import net.minecraft.class_3675;
import net.minecraft.class_418;
import net.minecraft.class_4185;
import net.minecraft.class_433;
import net.minecraft.class_437;
import net.minecraft.class_642;

public final class HudForgeClient
implements ClientModInitializer {
    public static final String MOD_ID = "hudfabric";
    public static HudForgeConfig config;
    private static class_304 openConfigKey;
    private static String activeServerKey;
    private static int deathScreenTicks;

    public void onInitializeClient() {
        config = HudForgeConfig.load();
        openConfigKey = KeyBindingHelper.registerKeyBinding((class_304)new class_304("key.hudfabric.open_config", class_3675.class_307.field_1668, 344, class_304.class_11900.method_74698((class_2960)class_2960.method_60655((String)MOD_ID, (String)"hud"))));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            HudForgeClient.updateServerProfile(client);
            while (openConfigKey.method_1436()) {
                HudForgeClient.openConfigScreen(client.field_1755);
            }
            if (config != null && HudForgeClient.config.autoRespawn && client.field_1755 instanceof class_418 && client.field_1724 != null) {
                if (++deathScreenTicks >= 20) {
                    client.field_1724.method_7331();
                    deathScreenTicks = 0;
                }
            } else {
                deathScreenTicks = 0;
            }
        });
        ClientWorldEvents.AFTER_CLIENT_WORLD_CHANGE.register((client, world) -> {
            if (client.field_1724 != null) {
                client.field_1724.method_6012();
            }
        });
        HudRenderCallback.EVENT.register(HudForgeRenderer::render);
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof class_433) {
                int buttonWidth = Math.min(112, Math.max(88, scaledWidth / 5));
                int buttonX = scaledWidth - buttonWidth - 10;
                int buttonY = scaledHeight - 32;
                for (class_339 widget : Screens.getButtons((class_437)screen)) {
                    if (!HudForgeClient.overlaps(buttonX, buttonY, buttonWidth, 20, widget.method_46426(), widget.method_46427(), widget.method_25368(), widget.method_25364())) continue;
                    buttonY = Math.max(8, widget.method_46427() - 24);
                }
                Screens.getButtons((class_437)screen).add(class_4185.method_46430((class_2561)class_2561.method_43471((String)"hudfabric.button.hud_editor"), button -> HudForgeClient.openConfigScreen(screen)).method_46434(buttonX, buttonY, buttonWidth, 20).method_46431());
            }
        });
    }

    public static void openConfigScreen(class_437 parent) {
        class_310.method_1551().method_1507((class_437)new HudForgeConfigScreen(parent));
    }

    public static void saveActiveServerProfile() {
        if (config != null && activeServerKey != null && !activeServerKey.isBlank()) {
            config.saveActiveProfile(activeServerKey);
            config.save();
        }
    }

    private static void updateServerProfile(class_310 client) {
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

    private static String serverKey(class_310 client) {
        if (client.field_1687 == null) {
            return "";
        }
        class_642 info = client.method_1558();
        if (info != null && info.field_3761 != null && !info.field_3761.isBlank()) {
            return "server:" + info.field_3761.trim().toLowerCase(Locale.ROOT);
        }
        if (client.method_1542()) {
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

