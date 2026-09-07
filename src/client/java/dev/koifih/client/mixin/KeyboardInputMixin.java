package dev.koifih.client.mixin;

import dev.koifih.client.AdinClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends ClientInput {
    @Inject(method = "tick", at = @At("TAIL"))
    private void adin$correctInput(CallbackInfo info) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) moveVector = AdinClient.ROTATIONS.correctInput(moveVector, player.getYRot());
    }
}
