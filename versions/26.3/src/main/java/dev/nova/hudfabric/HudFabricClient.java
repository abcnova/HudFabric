package dev.nova.hudfabric;

import com.mojang.blaze3d.platform.InputConstants;
import dev.nova.hudfabric.config.HudFabricConfig;
import dev.nova.hudfabric.gui.HudFabricConfigScreen;
import dev.nova.hudfabric.gui.NoteBoardScreen;
import dev.nova.hudfabric.render.HudFabricRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
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
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.sounds.SoundEvents;
import java.util.ArrayDeque;

public final class HudFabricClient implements ClientModInitializer {
    public static final String MOD_ID = "hudfabric";
    private static final KeyMapping.Category HUD_KEY_CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "hud"));
    public static HudFabricConfig config;

    private static KeyMapping openConfigKey;
    private static KeyMapping editNoteBoardKey;
    private static int totalTotems;
    private static ItemStack activeArrow = ItemStack.EMPTY;
    private static int activeArrowCount;
    private static int lowArmorMask;
    private static String activeServerKey = "";
    private static int deathScreenTicks = 0;
    private static final ArrayDeque<Long> LEFT_CLICKS = new ArrayDeque<>();
    private static final ArrayDeque<Long> RIGHT_CLICKS = new ArrayDeque<>();
    private static ServerData lastServer;
    private static Button reconnectButton;
    private static int reconnectTicks;
    private static long jumpResetHitNanos, jumpResetLastJumpNanos;
    private static int jumpResetLastHurtTime, jumpResetDelta = Integer.MIN_VALUE, jumpResetVisibleTicks;
    private static long jumpResetLastPlayerAttackNanos;
    private static Player jumpResetCombatTarget;
    private static boolean jumpResetWindowOpen;

    @Override
    public void onInitializeClient() {
        config = HudFabricConfig.load();
        openConfigKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.hudfabric.open_config",
                InputConstants.Type.KEYBOARD,
                InputConstants.KEY_RSHIFT,
                HUD_KEY_CATEGORY
        ));
        editNoteBoardKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.hudfabric.edit_note_board", InputConstants.Type.KEYBOARD, InputConstants.KEY_N,
                HUD_KEY_CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            updateCps();
            updateJumpReset(client);
            updateReconnect(client);
            updateInventoryCounters(client);
            updateArmorWarning(client);
            if (client.getCurrentServer() != null) lastServer = client.getCurrentServer();
            updateServerProfile(client);
            while (openConfigKey.consumeClick()) {
                openConfigScreen(client.gui.screen());
            }
            while (editNoteBoardKey.consumeClick()) {
                if (client.player != null && client.level != null && client.gui.screen() == null) {
                    client.gui.setScreen(new NoteBoardScreen(null));
                }
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

        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "main"), HudFabricRenderer::render);
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> { if (level.isClientSide() && entity instanceof Player target) { jumpResetLastPlayerAttackNanos = System.nanoTime(); jumpResetCombatTarget = target; } return InteractionResult.PASS; });

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

    private static void updateInventoryCounters(Minecraft client) {
        totalTotems = 0;
        activeArrow = ItemStack.EMPTY;
        activeArrowCount = 0;
        if (client.player == null || config == null || !config.inventoryCountersEnabled) return;
        var inventory = client.player.getInventory();
        var items = inventory.getNonEquipmentItems();
        for (ItemStack stack : items) {
            if (stack.is(Items.TOTEM_OF_UNDYING)) totalTotems += stack.getCount();
        }
        ItemStack offhand = client.player.getOffhandItem();
        if (offhand.is(Items.TOTEM_OF_UNDYING)) totalTotems += offhand.getCount();
        if (offhand.getItem() instanceof ArrowItem) activeArrow = offhand.copy();
        if (activeArrow.isEmpty() && inventory.getSelectedItem().getItem() instanceof ArrowItem) {
            activeArrow = inventory.getSelectedItem().copy();
        }
        if (activeArrow.isEmpty()) for (int slot = 0; slot < Math.min(36, items.size()); slot++) {
            ItemStack stack = items.get(slot);
            if (stack.getItem() instanceof ArrowItem) { activeArrow = stack.copy(); break; }
        }
        if (!activeArrow.isEmpty()) for (ItemStack stack : items) {
            if (stack.is(activeArrow.getItem())) activeArrowCount += stack.getCount();
        }
        if (!activeArrow.isEmpty() && offhand.is(activeArrow.getItem())) activeArrowCount += offhand.getCount();
    }

    private static void updateArmorWarning(Minecraft client) {
        if (client.player == null || config == null || !config.equipmentBreakWarningEnabled) { lowArmorMask = 0; return; }
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        int currentMask = 0;
        for (int i = 0; i < slots.length; i++) {
            ItemStack stack = client.player.getItemBySlot(slots[i]);
            if (!stack.isEmpty() && stack.isDamageableItem()) {
                float remaining = 100.0f * (stack.getMaxDamage() - stack.getDamageValue()) / Math.max(1, stack.getMaxDamage());
                if (remaining <= config.equipmentWarningPercent) currentMask |= 1 << i;
            }
        }
        if (config.equipmentBreakWarningSound && (currentMask & ~lowArmorMask) != 0) {
            client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.12f, 1.7f);
        }
        lowArmorMask = currentMask;
    }

    public static int totalTotems() { return totalTotems; }
    public static ItemStack activeArrow() { return activeArrow; }
    public static int activeArrowCount() { return activeArrowCount; }

    private static void updateCps() {
        long now = System.currentTimeMillis();
        trimClicks(LEFT_CLICKS, now); trimClicks(RIGHT_CLICKS, now);
    }
    public static void recordMouseClick(int button) {
        Minecraft client = Minecraft.getInstance();
        if (client.gui.screen() != null) return;
        long now = System.currentTimeMillis();
        if (button == InputConstants.MOUSE_BUTTON_LEFT) LEFT_CLICKS.addLast(now);
        else if (button == InputConstants.MOUSE_BUTTON_RIGHT) RIGHT_CLICKS.addLast(now);
    }
    private static void trimClicks(ArrayDeque<Long> clicks, long now) { while (!clicks.isEmpty() && now - clicks.peekFirst() > 1000L) clicks.removeFirst(); }
    public static int leftCps() { return LEFT_CLICKS.size(); }
    public static int rightCps() { return RIGHT_CLICKS.size(); }
    private static void updateJumpReset(Minecraft client) {
        if (client.player == null) { jumpResetLastHurtTime = 0; jumpResetHitNanos = 0; jumpResetWindowOpen = false; jumpResetVisibleTicks = 0; jumpResetCombatTarget = null; return; }
        long now = System.nanoTime();
        if (now - jumpResetLastPlayerAttackNanos > 2_500_000_000L) jumpResetCombatTarget = null;
        int hurt = client.player.hurtTime;
        if (config != null && config.jumpResetEnabled && hurt > jumpResetLastHurtTime
                && client.player.getDeltaMovement().horizontalDistanceSqr() > 0.004225D
                && jumpResetCombatTarget != null && jumpResetCombatTarget.isAlive()
                && client.player.distanceToSqr(jumpResetCombatTarget) <= 100.0D
                && now - jumpResetLastPlayerAttackNanos <= 2_500_000_000L) {
            jumpResetHitNanos = now;
            jumpResetWindowOpen = true;
            long earlyMs = Math.floorDiv(jumpResetLastJumpNanos - now, 1_000_000L);
            if (earlyMs >= -200L && earlyMs < 0L) { showJumpResetMs((int) earlyMs); jumpResetWindowOpen = false; }
        }
        jumpResetLastHurtTime = hurt;
        if (jumpResetWindowOpen && now - jumpResetHitNanos > 200_000_000L) { showJumpResetMs(201); jumpResetWindowOpen = false; jumpResetHitNanos = 0; }
        if (jumpResetVisibleTicks > 0) jumpResetVisibleTicks--;
    }
    public static void onLocalJump() { long now = System.nanoTime(); if (now - jumpResetLastJumpNanos < 40_000_000L) return; jumpResetLastJumpNanos = now; if (jumpResetWindowOpen && jumpResetHitNanos > 0) { long ms = (now - jumpResetHitNanos) / 1_000_000L; if (ms >= 0 && ms <= 200) { showJumpResetMs((int)ms); jumpResetWindowOpen = false; jumpResetHitNanos = 0; } } }
    private static void showJumpResetMs(int d) { jumpResetDelta = d; jumpResetVisibleTicks = config == null ? 30 : config.jumpResetDisplayTicks; }
    public static int jumpResetDelta() { return jumpResetDelta; }
    public static boolean jumpResetVisible() { return config != null && config.jumpResetEnabled && jumpResetVisibleTicks > 0; }

    public static void openConfigScreen(Screen parent) {
        Minecraft.getInstance().gui.setScreen(new HudFabricConfigScreen(parent));
    }

    public static void openNoteBoardScreen(Screen parent) {
        Minecraft.getInstance().gui.setScreen(new NoteBoardScreen(parent));
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
