package dev.nova.hudfabric.mixin;

import dev.nova.hudfabric.HudForgeClient;
import dev.nova.hudfabric.config.HudForgeConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Inject(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V", at = @At("HEAD"), cancellable = true)
    private void hudforge$hideVanillaScoreboard(DrawContext context, ScoreboardObjective objective, CallbackInfo ci) {
        if (HudForgeClient.config != null && HudForgeClient.config.scoreboardMode != HudForgeConfig.SCOREBOARD_VANILLA) {
            ci.cancel();
        }
    }

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void hudforge$hideVanillaCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (HudForgeClient.config != null
                && HudForgeClient.config.crosshairMode != HudForgeConfig.CROSSHAIR_VANILLA
                && MinecraftClient.getInstance().options.getPerspective().isFirstPerson()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderCrosshair", at = @At("TAIL"))
    private void hudforge$renderCrosshairIndicator(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (HudForgeClient.config != null
                && HudForgeClient.config.crosshairMode == HudForgeConfig.CROSSHAIR_VANILLA
                && HudForgeClient.config.crosshairIndicator
                && client.currentScreen == null
                && client.options.getPerspective().isFirstPerson()
                && client.targetedEntity instanceof PlayerEntity) {
            context.drawGuiTexture(RenderPipelines.CROSSHAIR, Identifier.of("hudfabric", "crosshair_indicator"),
                    (context.getScaledWindowWidth() - 15) / 2, (context.getScaledWindowHeight() - 15) / 2, 15, 15);
        }
    }

    @Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
    private void hudforge$hideVanillaEffects(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (HudForgeClient.config != null && HudForgeClient.config.effectsEnabled && HudForgeClient.config.hideVanillaEffects) {
            ci.cancel();
        }
    }
}
