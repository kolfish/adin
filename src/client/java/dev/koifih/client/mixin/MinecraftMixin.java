package dev.koifih.client.mixin;

import dev.koifih.client.AdinClient;
import dev.koifih.client.module.impl.combat.AimAssist;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow public HitResult hitResult;
    @Shadow public MultiPlayerGameMode gameMode;

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void adin$startAttack(CallbackInfoReturnable<Boolean> info) {
        if (adin$blocksBreaking()) info.setReturnValue(false);
    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void adin$continueAttack(boolean leftClick, CallbackInfo info) {
        if (adin$blocksBreaking()) info.cancel();
    }

    @Unique
    private boolean adin$blocksBreaking() {
        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) return false;
        if (!AdinClient.MODULES.get(AimAssist.class).blocksBreaking()) return false;
        if (gameMode != null) gameMode.stopDestroyBlock();
        return true;
    }
}
