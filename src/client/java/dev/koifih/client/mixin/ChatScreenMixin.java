package dev.koifih.client.mixin;

import dev.koifih.client.ui.hud.HudEditor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void adin$dragHud(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> info) {
        if (event.button() == 0 && HudEditor.press(event.x(), event.y())) info.setReturnValue(true);
    }
}
