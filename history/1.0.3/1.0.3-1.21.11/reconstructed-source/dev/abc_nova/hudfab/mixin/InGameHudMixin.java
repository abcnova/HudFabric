/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_266
 *  net.minecraft.class_310
 *  net.minecraft.class_329
 *  net.minecraft.class_332
 *  net.minecraft.class_9779
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.abc_nova.hudfab.mixin;

import dev.abc_nova.hudfab.HudForgeClient;
import net.minecraft.class_266;
import net.minecraft.class_310;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={class_329.class})
public abstract class InGameHudMixin {
    @Inject(method={"method_1757(Lnet/minecraft/class_332;Lnet/minecraft/class_266;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void hudforge$hideVanillaScoreboard(class_332 context, class_266 objective, CallbackInfo ci) {
        if (HudForgeClient.config != null && HudForgeClient.config.scoreboardMode != 1) {
            ci.cancel();
        }
    }

    @Inject(method={"method_1736"}, at={@At(value="HEAD")}, cancellable=true)
    private void hudforge$hideVanillaCrosshair(class_332 context, class_9779 tickCounter, CallbackInfo ci) {
        if (HudForgeClient.config != null && HudForgeClient.config.crosshairMode != 0 && class_310.method_1551().field_1690.method_31044().method_31034()) {
            ci.cancel();
        }
    }

    @Inject(method={"method_1765"}, at={@At(value="HEAD")}, cancellable=true)
    private void hudforge$hideVanillaEffects(class_332 context, class_9779 tickCounter, CallbackInfo ci) {
        if (HudForgeClient.config != null && HudForgeClient.config.effectsEnabled && HudForgeClient.config.hideVanillaEffects) {
            ci.cancel();
        }
    }
}

