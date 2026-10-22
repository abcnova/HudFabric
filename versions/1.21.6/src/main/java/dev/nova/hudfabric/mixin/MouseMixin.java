package dev.nova.hudfabric.mixin;

import dev.nova.hudfabric.HudFabricClient;
import net.minecraft.client.Mouse;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void hudfabric$recordMouseClick(long window, int button, int action, CallbackInfo ci) {
        if (action == GLFW.GLFW_PRESS) HudFabricClient.recordMouseClick(button);
    }
    @Inject(method = "onCursorPos", at = @At("HEAD"))
    private void hudfabric$dragLegacyHudButton(long window, double x, double y, CallbackInfo ci) { HudFabricClient.dragLegacyHudButton(x, y); }
    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void hudfabric$releaseLegacyHudButton(long window, int button, int action, CallbackInfo ci) { if (action == GLFW.GLFW_RELEASE && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) HudFabricClient.finishLegacyHudButtonDrag(); }
}
