package dev.koifih.client.mixin.render;

import dev.koifih.client.render.glass.GlassRenderer;
import net.minecraft.client.gui.render.GuiRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiRenderer.class)
public abstract class GuiRendererMixin {
    @Inject(method = "render()V", at = @At("TAIL"))
    private void adin$renderGlass(CallbackInfo info) {
        GlassRenderer.render();
    }
}
