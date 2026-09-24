package dev.koifih.client.mixin.input;

<<<<<<< HEAD
import dev.koifih.client.AdinClient;
import dev.koifih.client.event.events.PacketProcessEvent;
import dev.koifih.client.util.Clicks;
import dev.koifih.client.util.Players;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
=======
import com.mojang.blaze3d.platform.InputConstants;
import dev.koifih.client.AdinClient;
import dev.koifih.client.event.events.PacketProcessEvent;
import dev.koifih.client.module.impl.combat.AimAssist;
import dev.koifih.client.util.Clicks;
import dev.koifih.client.util.Players;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.HitResult;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
>>>>>>> 630b1b46c1def750e98fc9ad571b50ab0e4f226d
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
<<<<<<< HEAD
=======
    @Shadow public HitResult hitResult;
    @Shadow public MultiPlayerGameMode gameMode;
    @Shadow public LocalPlayer player;

>>>>>>> 630b1b46c1def750e98fc9ad571b50ab0e4f226d
    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketProcessor;processQueuedPackets()V", shift = At.Shift.AFTER))
    private void adin$packetsProcessed(boolean renderLevel, CallbackInfo info) {
        AdinClient.EVENTS.post(new PacketProcessEvent((Minecraft) (Object) this));
    }

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void adin$startAttack(CallbackInfoReturnable<Boolean> info) {
<<<<<<< HEAD
        if (Clicks.blocksBreaking((Minecraft) (Object) this) || Clicks.activates(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
            info.setReturnValue(false);
        }
=======
        if (adin$blocksBreaking() || adin$activates(GLFW.GLFW_MOUSE_BUTTON_LEFT)) info.setReturnValue(false);
>>>>>>> 630b1b46c1def750e98fc9ad571b50ab0e4f226d
    }

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void adin$startUseItem(CallbackInfo info) {
<<<<<<< HEAD
        Minecraft client = (Minecraft) (Object) this;
        if (Clicks.interceptsUseItem() && !Players.consuming(client.player)) info.cancel();
=======
        if (adin$activates(GLFW.GLFW_MOUSE_BUTTON_RIGHT) && !Players.consuming(player)) info.cancel();
    }

    @Unique
    private static boolean adin$activates(int button) {
        return !Clicks.simulating(button) && AdinClient.MODULES.activates(InputConstants.Type.MOUSE.getOrCreate(button));
>>>>>>> 630b1b46c1def750e98fc9ad571b50ab0e4f226d
    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void adin$continueAttack(boolean leftClick, CallbackInfo info) {
<<<<<<< HEAD
        if (Clicks.blocksBreaking((Minecraft) (Object) this)) info.cancel();
=======
        if (adin$blocksBreaking()) info.cancel();
    }

    @Unique
    private boolean adin$blocksBreaking() {
        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) return false;
        if (!AdinClient.MODULES.get(AimAssist.class).blocksBreaking()) return false;
        if (gameMode != null) gameMode.stopDestroyBlock();
        return true;
>>>>>>> 630b1b46c1def750e98fc9ad571b50ab0e4f226d
    }
}
