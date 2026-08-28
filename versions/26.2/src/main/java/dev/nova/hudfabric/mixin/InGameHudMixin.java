package dev.nova.hudfabric.mixin;

import dev.nova.hudfabric.HudForgeClient;
import dev.nova.hudfabric.config.HudForgeConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.scores.Objective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class InGameHudMixin {
    @Inject(method = "displayScoreboardSidebar(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/scores/Objective;)V", at = @At("HEAD"), cancellable = true)
    private void hudforge$hideVanillaScoreboard(GuiGraphicsExtractor context, Objective objective, CallbackInfo ci) {
        if (HudForgeClient.config != null && HudForgeClient.config.scoreboardMode != HudForgeConfig.SCOREBOARD_VANILLA) {
            ci.cancel();
        }
    }

    @Inject(method = "extractCrosshair", at = @At("HEAD"), cancellable = true)
    private void hudforge$hideVanillaCrosshair(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (HudForgeClient.config != null
                && HudForgeClient.config.crosshairMode != HudForgeConfig.CROSSHAIR_VANILLA
                && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            ci.cancel();
        }
    }

    @Inject(method = "extractEffects", at = @At("HEAD"), cancellable = true)
    private void hudforge$hideVanillaEffects(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (HudForgeClient.config != null && HudForgeClient.config.effectsEnabled && HudForgeClient.config.hideVanillaEffects) {
            ci.cancel();
        }
    }
}
