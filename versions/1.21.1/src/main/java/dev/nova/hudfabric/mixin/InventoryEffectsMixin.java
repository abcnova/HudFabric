package dev.nova.hudfabric.mixin;

import dev.nova.hudfabric.HudFabricClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractInventoryScreen.class)
public abstract class InventoryEffectsMixin {
    @Inject(method = "drawStatusEffects", at = @At("HEAD"), cancellable = true)
    private void hudfabric$hideInventoryEffects(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        if (HudFabricClient.config != null && HudFabricClient.config.effectsEnabled && HudFabricClient.config.hideVanillaEffects) {
            ci.cancel();
        }
    }
}
