package dev.koifih.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.koifih.client.AdinClient;
import dev.koifih.client.rotation.Rotation;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    @Shadow private float yRotLast;
    @Shadow private float xRotLast;

    @ModifyExpressionValue(method = "sendPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getYRot()F"))
    private float adin$sentYaw(float yaw) {
        return AdinClient.ROTATIONS.sentYaw(yaw);
    }

    @ModifyExpressionValue(method = "sendPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getXRot()F"))
    private float adin$sentPitch(float pitch) {
        return AdinClient.ROTATIONS.sentPitch(pitch);
    }

    @Inject(method = "sendPosition", at = @At("TAIL"))
    private void adin$sent(CallbackInfo info) {
        AdinClient.ROTATIONS.sent(new Rotation(yRotLast, xRotLast));
    }
}
