/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.DeltaTracker
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Gui
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.world.scores.Objective
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package dev.abc_nova.hudfab.mixin;

import dev.abc_nova.hudfab.HudForgeClient;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.scores.Objective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Gui.class})
public abstract class InGameHudMixin {
    @Inject(method={"displayScoreboardSidebar(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/scores/Objective;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void hudforge$hideVanillaScoreboard(GuiGraphicsExtractor context, Objective objective, CallbackInfo ci) {
        if (HudForgeClient.config != null && HudForgeClient.config.scoreboardMode != 1) {
            ci.cancel();
        }
    }

    @Inject(method={"extractCrosshair"}, at={@At(value="HEAD")}, cancellable=true)
    private void hudforge$hideVanillaCrosshair(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (HudForgeClient.config != null && HudForgeClient.config.crosshairMode != 0 && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            ci.cancel();
        }
    }
}

