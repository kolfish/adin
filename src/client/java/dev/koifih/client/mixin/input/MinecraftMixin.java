package dev.koifih.client.mixin.input;

import dev.koifih.client.AdinClient;
import dev.koifih.client.event.events.PacketProcessEvent;
import dev.koifih.client.util.Clicks;
import dev.koifih.client.util.Players;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketProcessor;processQueuedPackets()V", shift = At.Shift.AFTER))
    private void adin$packetsProcessed(boolean renderLevel, CallbackInfo info) {
        AdinClient.EVENTS.post(new PacketProcessEvent((Minecraft) (Object) this));
    }

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void adin$startAttack(CallbackInfoReturnable<Boolean> info) {
        if (Clicks.blocksBreaking((Minecraft) (Object) this) || Clicks.activates(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
            info.setReturnValue(false);
        }
    }

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void adin$startUseItem(CallbackInfo info) {
        Minecraft client = (Minecraft) (Object) this;
        if (Clicks.interceptsUseItem() && !Players.consuming(client.player)) info.cancel();
    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void adin$continueAttack(boolean leftClick, CallbackInfo info) {
        if (Clicks.blocksBreaking((Minecraft) (Object) this)) info.cancel();
    }
}
