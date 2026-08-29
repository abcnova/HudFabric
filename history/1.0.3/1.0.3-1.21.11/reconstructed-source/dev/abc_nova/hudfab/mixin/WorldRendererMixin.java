/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_761
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyVariable
 */
package dev.abc_nova.hudfab.mixin;

import dev.abc_nova.hudfab.HudForgeClient;
import net.minecraft.class_761;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value={class_761.class})
public abstract class WorldRendererMixin {
    @ModifyVariable(method={"method_22712"}, at=@At(value="HEAD"), argsOnly=true, ordinal=0)
    private int hudfabric$customBlockOutlineColor(int color) {
        if (HudForgeClient.config != null && HudForgeClient.config.customBlockOutline) {
            return HudForgeClient.config.blockOutlineColor;
        }
        return color;
    }
}

