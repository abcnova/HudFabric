package dev.nova.hudfabric.mixin;

import dev.nova.hudfabric.HudFabricClient;
import dev.nova.hudfabric.config.HudFabricConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.scores.Objective;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class InGameHudMixin {
    @Inject(method = "displayScoreboardSidebar(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/scores/Objective;)V", at = @At("HEAD"), cancellable = true)
    private void hudfabric$hideVanillaScoreboard(GuiGraphicsExtractor context, Objective objective, CallbackInfo ci) {
        if (HudFabricClient.config != null && HudFabricClient.config.scoreboardMode != HudFabricConfig.SCOREBOARD_VANILLA) {
            ci.cancel();
        }
    }

    @Inject(method = "extractCrosshair", at = @At("HEAD"), cancellable = true)
    private void hudfabric$hideVanillaCrosshair(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (HudFabricClient.config != null
                && HudFabricClient.config.crosshairMode != HudFabricConfig.CROSSHAIR_VANILLA
                && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            ci.cancel();
        }
    }

    @Inject(method = "extractCrosshair", at = @At("TAIL"))
    private void hudfabric$renderCrosshairIndicator(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (HudFabricClient.config != null
                && HudFabricClient.config.crosshairMode == HudFabricConfig.CROSSHAIR_VANILLA
                && HudFabricClient.config.crosshairIndicator
                && client.gui.screen() == null
                && client.options.getCameraType().isFirstPerson()
                && client.crosshairPickEntity instanceof Player) {
            context.blitSprite(RenderPipelines.CROSSHAIR, Identifier.fromNamespaceAndPath("hudfabric", "crosshair_indicator"),
                    (context.guiWidth() - 15) / 2, (context.guiHeight() - 15) / 2, 15, 15);
        }
    }

    @Inject(method = "extractEffects", at = @At("HEAD"), cancellable = true)
    private void hudfabric$hideVanillaEffects(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (HudFabricClient.config != null && HudFabricClient.config.effectsEnabled && HudFabricClient.config.hideVanillaEffects) {
            ci.cancel();
        }
    }
}
