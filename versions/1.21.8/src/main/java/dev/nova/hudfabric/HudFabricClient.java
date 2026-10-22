package dev.nova.hudfabric;

import dev.nova.hudfabric.config.HudFabricConfig;
import dev.nova.hudfabric.gui.HudFabricShareScreen;
import dev.nova.hudfabric.config.HudFabricShareCode;
import dev.nova.hudfabric.gui.HudFabricConfigScreen;
import dev.nova.hudfabric.render.HudFabricRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
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
import dev.nova.hudfabric.gui.NoteBoardScreen;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.Map;

public final class HudFabricClient implements ClientModInitializer {
    public static final String MOD_ID = "hudfabric";
    public static HudFabricConfig config;

    private static KeyBinding openConfigKey;
    private static KeyBinding editNoteBoardKey;
    private static KeyBinding moveHudEditorKey;
    private static boolean repositionHudEditor;
    private static boolean draggingHudEditor;
    private static int dragOffsetX;
    private static int dragOffsetY;
    private static ButtonWidget draggedEditorButton;
    private static ButtonWidget draggedEditButton;
    private static int dragScreenWidth;
    private static int dragScreenHeight;
    private static int totalTotems;
    private static ItemStack activeArrow = ItemStack.EMPTY;
    private static int activeArrowCount;
    private static int lowArmorMask;
    private static String activeServerKey = "";
    private static int deathScreenTicks = 0;
    private static final ArrayDeque<Long> LEFT_CLICKS = new ArrayDeque<>();
    private static final ArrayDeque<Long> RIGHT_CLICKS = new ArrayDeque<>();
    private static ServerInfo lastServer;
    private static ButtonWidget reconnectButton;
    private static int reconnectTicks;
    private static long jumpResetHitNanos;
    private static long jumpResetLastJumpNanos;
    private static int jumpResetLastHurtTime;
    private static int jumpResetDelta = Integer.MIN_VALUE;
    private static int jumpResetVisibleTicks;
    private static long jumpResetLastPlayerAttackNanos;
    private static PlayerEntity jumpResetCombatTarget;
    private static boolean jumpResetWindowOpen;

    @Override
    public void onInitializeClient() {
        config = HudFabricConfig.load();
        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hudfabric.open_config",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.hudfabric"
        ));
        editNoteBoardKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hudfabric.edit_note_board",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                "category.hudfabric"
        ));
        moveHudEditorKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hudfabric.move_hud_editor", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_ALT,
                "category.hudfabric"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            updateHudEditorButtonDrag(client);
            updateCps();
            updateInventoryCounters(client);
            updateArmorWarning(client);
            updateJumpReset(client);
            updateReconnect(client);
            if (client.getCurrentServerEntry() != null) lastServer = client.getCurrentServerEntry();
            updateServerProfile(client);
            while (openConfigKey.wasPressed()) {
                openConfigScreen(client.currentScreen);
            }
            while (editNoteBoardKey.wasPressed()) { if (client.player != null && client.world != null && client.currentScreen == null) client.setScreen(new NoteBoardScreen(null)); }
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

        HudRenderCallback.EVENT.register(HudFabricRenderer::render);
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient() && entity instanceof PlayerEntity target) {
                jumpResetLastPlayerAttackNanos = System.nanoTime();
                jumpResetCombatTarget = target;
            }
            return ActionResult.PASS;
        });

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof GameMenuScreen) {
                int buttonWidth = Math.min(112, Math.max(88, scaledWidth / 5));
                int buttonX = config.hudEditorButtonX < 0 ? scaledWidth - buttonWidth - 32 : Math.max(0, Math.min(config.hudEditorButtonX, scaledWidth - buttonWidth - 22));
                int buttonY = config.hudEditorButtonY < 0 ? scaledHeight - 32 : Math.max(0, Math.min(config.hudEditorButtonY, scaledHeight - 20));
                if (config.hudEditorButtonX < 0 && config.hudEditorButtonY < 0) for (ClickableWidget widget : Screens.getButtons(screen)) if (overlaps(buttonX, buttonY, buttonWidth + 22, 20, widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight())) buttonY = Math.max(8, widget.getY() - 24);
                repositionHudEditor = false;
                ButtonWidget editorButton = ButtonWidget.builder(Text.translatable("hudfabric.button.hud_editor"), button -> openConfigScreen(screen)).dimensions(buttonX, buttonY, buttonWidth, 20).build();
                ButtonWidget editButton = ButtonWidget.builder(Text.literal("✎"), button -> { repositionHudEditor = !repositionHudEditor; button.setMessage(Text.literal(repositionHudEditor ? "✓" : "✎")); editorButton.active = !repositionHudEditor; }).dimensions(buttonX + buttonWidth + 2, buttonY, 20, 20).build();
                Screens.getButtons(screen).add(editorButton); Screens.getButtons(screen).add(editButton);
                draggedEditorButton = editorButton;
                draggedEditButton = editButton;
                dragScreenWidth = scaledWidth;
                dragScreenHeight = scaledHeight;
            }

            if (screen instanceof DisconnectedScreen && lastServer != null) {
                reconnectTicks = Math.max(20, config.autoReconnectDelaySeconds * 20);
                reconnectButton = ButtonWidget.builder(reconnectText(), button -> reconnect(client))
                        .dimensions(scaledWidth / 2 - 100, scaledHeight - 52, 200, 20).build();
                Screens.getButtons(screen).add(reconnectButton);
            }
        });
    }

    private static void updateHudEditorButtonDrag(MinecraftClient client) {
        if (draggedEditorButton == null || !(client.currentScreen instanceof GameMenuScreen)) return;
        boolean editMode = repositionHudEditor || moveHudEditorKey.isPressed();
        boolean leftDown = GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        if (editMode && leftDown) {
            double mouseX = client.mouse.getX();
            double mouseY = client.mouse.getY();
            int nextX = Math.max(0, Math.min((int) mouseX - draggedEditorButton.getWidth() / 2, dragScreenWidth - draggedEditorButton.getWidth() - 22));
            int nextY = Math.max(0, Math.min((int) mouseY - draggedEditorButton.getHeight() / 2, dragScreenHeight - draggedEditorButton.getHeight()));
            draggedEditorButton.setPosition(nextX, nextY);
            if (draggedEditButton != null) draggedEditButton.setPosition(nextX + draggedEditorButton.getWidth() + 2, nextY);
            config.hudEditorButtonX = nextX;
            config.hudEditorButtonY = nextY;
            draggingHudEditor = true;
        } else if (draggingHudEditor) {
            draggingHudEditor = false;
            config.save();
        }
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

    private static void updateInventoryCounters(MinecraftClient client) {
        totalTotems = 0;
        activeArrow = ItemStack.EMPTY;
        activeArrowCount = 0;
        if (client.player == null || config == null || !config.inventoryCountersEnabled) return;

        var inventory = client.player.getInventory();
        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (stack.isOf(Items.TOTEM_OF_UNDYING)) totalTotems += stack.getCount();
        }

        ItemStack offhand = client.player.getOffHandStack();
        if (offhand.getItem() instanceof ArrowItem) {
            activeArrow = offhand.copy();
        }
        if (activeArrow.isEmpty()) {
            ItemStack selected = inventory.getStack(inventory.getSelectedSlot());
            if (selected.getItem() instanceof ArrowItem) activeArrow = selected.copy();
        }
        if (activeArrow.isEmpty()) {
            for (int slot = 0; slot < Math.min(36, inventory.size()); slot++) {
                ItemStack stack = inventory.getStack(slot);
                if (stack.getItem() instanceof ArrowItem) {
                    activeArrow = stack.copy();
                    break;
                }
            }
        }
        if (!activeArrow.isEmpty()) {
            for (int slot = 0; slot < inventory.size(); slot++) {
                ItemStack stack = inventory.getStack(slot);
                if (stack.getItem() == activeArrow.getItem()) activeArrowCount += stack.getCount();
            }
        }
    }

    private static void updateArmorWarning(MinecraftClient client) {
        if (client.player == null || config == null || !config.equipmentBreakWarningEnabled) {
            lowArmorMask = 0;
            return;
        }
        net.minecraft.entity.EquipmentSlot[] slots = {
                net.minecraft.entity.EquipmentSlot.HEAD,
                net.minecraft.entity.EquipmentSlot.CHEST,
                net.minecraft.entity.EquipmentSlot.LEGS,
                net.minecraft.entity.EquipmentSlot.FEET
        };
        int currentMask = 0;
        for (int i = 0; i < slots.length; i++) {
            ItemStack stack = client.player.getEquippedStack(slots[i]);
            if (!stack.isEmpty() && stack.isDamageable()) {
                float remaining = 100.0f * (stack.getMaxDamage() - stack.getDamage()) / Math.max(1, stack.getMaxDamage());
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
        trimClicks(LEFT_CLICKS, now);
        trimClicks(RIGHT_CLICKS, now);
    }

    public static void recordMouseClick(int button) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen != null) return;
        long now = System.currentTimeMillis();
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) LEFT_CLICKS.addLast(now);
        else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) RIGHT_CLICKS.addLast(now);
    }

    private static void trimClicks(ArrayDeque<Long> clicks, long now) {
        while (!clicks.isEmpty() && now - clicks.peekFirst() > 1000L) clicks.removeFirst();
    }

    public static int leftCps() { return LEFT_CLICKS.size(); }
    public static int rightCps() { return RIGHT_CLICKS.size(); }

    private static void updateJumpReset(MinecraftClient client) {
        if (client.player == null) { jumpResetLastHurtTime = 0; jumpResetHitNanos = 0; jumpResetWindowOpen = false; jumpResetVisibleTicks = 0; jumpResetCombatTarget = null; return; }
        long now = System.nanoTime();
        if (now - jumpResetLastPlayerAttackNanos > 2_500_000_000L) jumpResetCombatTarget = null;
        int hurt = client.player.hurtTime;
        double horizontalKnockback = client.player.getVelocity().horizontalLengthSquared();
        if (config != null && config.jumpResetEnabled
                && hurt > jumpResetLastHurtTime
                && horizontalKnockback > 0.004225D
                && jumpResetCombatTarget != null
                && jumpResetCombatTarget.isAlive()
                && client.player.squaredDistanceTo(jumpResetCombatTarget) <= 100.0D
                && now - jumpResetLastPlayerAttackNanos <= 2_500_000_000L) {
            jumpResetHitNanos = now;
            jumpResetWindowOpen = true;
            long earlyMs = Math.floorDiv(jumpResetLastJumpNanos - now, 1_000_000L);
            if (earlyMs >= -200L && earlyMs < 0L) {
                showJumpResetMs((int) earlyMs);
                jumpResetWindowOpen = false;
            }
        }
        jumpResetLastHurtTime = hurt;
        if (jumpResetWindowOpen && now - jumpResetHitNanos > 200_000_000L) {
            showJumpResetMs(201);
            jumpResetWindowOpen = false;
            jumpResetHitNanos = 0;
        }
        if (jumpResetVisibleTicks > 0) jumpResetVisibleTicks--;
    }
    public static void onLocalJump() {
        long now = System.nanoTime();
        if (now - jumpResetLastJumpNanos < 40_000_000L) return;
        jumpResetLastJumpNanos = now;
        if (jumpResetWindowOpen && jumpResetHitNanos > 0) {
            long elapsedMs = (now - jumpResetHitNanos) / 1_000_000L;
            if (elapsedMs >= 0 && elapsedMs <= 200) {
                showJumpResetMs((int) elapsedMs);
                jumpResetWindowOpen = false;
                jumpResetHitNanos = 0;
            }
        }
    }
    private static void showJumpResetMs(int milliseconds) { jumpResetDelta = milliseconds; jumpResetVisibleTicks = config == null ? 30 : config.jumpResetDisplayTicks; }
    public static int jumpResetDelta() { return jumpResetDelta; }
    public static boolean jumpResetVisible() { return config != null && config.jumpResetEnabled && jumpResetVisibleTicks > 0; }

    public static void openConfigScreen(Screen parent) {
        MinecraftClient.getInstance().setScreen(new HudFabricConfigScreen(parent));
    }

    public static String exportSettingsShareCode() { Map<String,String> keys=new LinkedHashMap<>(); keys.put("openEditor",openConfigKey.getBoundKeyTranslationKey()); keys.put("moveEditorButton",moveHudEditorKey.getBoundKeyTranslationKey()); keys.put("editNoteBoard",editNoteBoardKey.getBoundKeyTranslationKey()); return HudFabricShareCode.encode(config,keys); }
    public static void importSettingsShareCode(String code) { Map<String,String> keys=HudFabricShareCode.decodeAndApply(code,config); applyKey(openConfigKey,keys.get("openEditor")); applyKey(moveHudEditorKey,keys.get("moveEditorButton")); applyKey(editNoteBoardKey,keys.get("editNoteBoard")); MinecraftClient.getInstance().options.write(); saveActiveServerProfile(); }
    private static void applyKey(KeyBinding mapping,String value) { if(value!=null&&!value.isBlank()) mapping.setBoundKey(InputUtil.fromTranslationKey(value)); }

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

