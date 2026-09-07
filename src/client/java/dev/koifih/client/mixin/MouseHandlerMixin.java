package dev.koifih.client.mixin;

import dev.koifih.client.AdinClient;
import dev.koifih.client.event.events.TurnEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    @Unique
    private static final float TICKS_PER_SECOND = 20f;

    @Inject(method = "turnPlayer", at = @At("TAIL"))
    private void adin$turned(double seconds, CallbackInfo info) {
        AdinClient.EVENTS.post(new TurnEvent(Minecraft.getInstance(), (float) seconds * TICKS_PER_SECOND));
    }
}
