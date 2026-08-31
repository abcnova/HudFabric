package dev.nova.hudfabric.mixin;

import dev.nova.hudfabric.HudForgeClient;
import dev.nova.hudfabric.config.HudForgeConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.scores.Objective;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
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

    @Inject(method = "extractCrosshair", at = @At("TAIL"))
    private void hudforge$renderCrosshairIndicator(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (HudForgeClient.config != null
                && HudForgeClient.config.crosshairMode == HudForgeConfig.CROSSHAIR_VANILLA
                && HudForgeClient.config.crosshairIndicator
                && client.screen == null
                && client.options.getCameraType().isFirstPerson()
                && client.crosshairPickEntity instanceof Player) {
            context.blitSprite(RenderPipelines.CROSSHAIR, Identifier.fromNamespaceAndPath("hudfabric", "crosshair_indicator"),
                    (context.guiWidth() - 15) / 2, (context.guiHeight() - 15) / 2, 15, 15);
        }
    }

    @Inject(method = "extractEffects", at = @At("HEAD"), cancellable = true)
    private void hudforge$hideVanillaEffects(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (HudForgeClient.config != null && HudForgeClient.config.effectsEnabled && HudForgeClient.config.hideVanillaEffects) {
            ci.cancel();
        }
    }
}
