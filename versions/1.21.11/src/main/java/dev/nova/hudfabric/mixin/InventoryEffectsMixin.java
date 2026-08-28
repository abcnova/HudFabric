package dev.nova.hudfabric.mixin;

import dev.nova.hudfabric.HudForgeClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.StatusEffectsDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StatusEffectsDisplay.class)
public abstract class InventoryEffectsMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void hudforge$hideInventoryEffects(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        if (HudForgeClient.config != null && HudForgeClient.config.effectsEnabled && HudForgeClient.config.hideVanillaEffects) {
            ci.cancel();
        }
    }
}
