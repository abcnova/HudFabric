package dev.nova.hudfabric.mixin;

import dev.nova.hudfabric.HudForgeClient;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    @ModifyVariable(method = "drawBlockOutline", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int hudfabric$customBlockOutlineColor(int color) {
        if (HudForgeClient.config != null && HudForgeClient.config.customBlockOutline) {
            return HudForgeClient.config.blockOutlineColor;
        }
        return color;
    }
}
