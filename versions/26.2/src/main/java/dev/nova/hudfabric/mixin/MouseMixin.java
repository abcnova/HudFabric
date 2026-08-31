package dev.nova.hudfabric.mixin;

import dev.nova.hudfabric.HudForgeClient;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseMixin {
    @Inject(method = "onButton", at = @At("HEAD"))
    private void hudfabric$recordMouseClick(long window, MouseButtonInfo info, int action, CallbackInfo ci) {
        if (action == GLFW.GLFW_PRESS) HudForgeClient.recordMouseClick(info.button());
    }
}
