package dev.nova.hudfabric.mixin;

import dev.nova.hudfabric.HudForgeClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.input.MouseInput;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void hudfabric$recordMouseClick(long window, MouseInput info, int action, CallbackInfo ci) {
        if (action == GLFW.GLFW_PRESS) HudForgeClient.recordMouseClick(info.button());
    }
}
