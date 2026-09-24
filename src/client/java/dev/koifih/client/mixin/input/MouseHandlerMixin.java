package dev.koifih.client.mixin.input;

import dev.koifih.client.AdinClient;
import dev.koifih.client.event.events.MouseMoveEvent;
import dev.koifih.client.event.events.TurnEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    @Inject(method = "turnPlayer", at = @At("TAIL"))
    private void adin$turned(double seconds, CallbackInfo info) {
        AdinClient.EVENTS.post(new TurnEvent(Minecraft.getInstance(), (float) seconds * 20f));
    }

    @Inject(method = "handleAccumulatedMovement", at = @At("TAIL"))
    private void adin$moved(CallbackInfo info) {
        Minecraft client = Minecraft.getInstance();
        if (client.gui.screen() == null) return;
        MouseHandler handler = (MouseHandler) (Object) this;
        AdinClient.EVENTS.post(new MouseMoveEvent(client,
                handler.getScaledXPos(client.getWindow()), handler.getScaledYPos(client.getWindow())));
    }
}
