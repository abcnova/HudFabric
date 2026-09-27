package dev.nova.hudfabric.mixin;

import dev.nova.hudfabric.HudForgeClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "jump", at = @At("HEAD"))
    private void hudfabric$recordRealLocalJump(CallbackInfo ci) {
        if ((Object) this == MinecraftClient.getInstance().player) {
            HudForgeClient.onLocalJump();
        }
    }
}
