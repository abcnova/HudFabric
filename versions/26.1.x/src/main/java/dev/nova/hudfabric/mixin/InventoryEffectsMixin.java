package dev.nova.hudfabric.mixin;

import dev.nova.hudfabric.HudFabricClient;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EffectsInInventory.class)
public abstract class InventoryEffectsMixin {
    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void hudfabric$hideInventoryEffects(GuiGraphicsExtractor context, int mouseX, int mouseY, CallbackInfo ci) {
        if (HudFabricClient.config != null && HudFabricClient.config.effectsEnabled && HudFabricClient.config.hideVanillaEffects) {
            ci.cancel();
        }
    }
}
