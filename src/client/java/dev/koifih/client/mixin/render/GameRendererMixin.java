package dev.koifih.client.mixin.render;

import dev.koifih.client.mixin.accessor.LevelRendererAccessor;
import dev.koifih.client.render.entity.EntityOutlines;
<<<<<<< HEAD
import dev.koifih.client.render.glass.GlassRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
=======
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
>>>>>>> 630b1b46c1def750e98fc9ad571b50ab0e4f226d
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
<<<<<<< HEAD
=======
    @Shadow
    @Final
    private Minecraft minecraft;

>>>>>>> 630b1b46c1def750e98fc9ad571b50ab0e4f226d
    @Inject(method = "render(Lnet/minecraft/client/DeltaTracker;Z)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;doEntityOutline()V"))
    private void adin$inkOutline(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo info) {
        if (!EntityOutlines.active()) return;
        GameRenderer renderer = (GameRenderer) (Object) this;
        if (!renderer.gameRenderState().levelRenderState.shouldShowEntityOutlines) return;
<<<<<<< HEAD
        Minecraft mc = Minecraft.getInstance();
        EntityOutlines.render(((LevelRendererAccessor) mc.levelRenderer).adin$getEntityOutlineTarget(), renderer.mainRenderTarget());
    }

    @Inject(method = "render(Lnet/minecraft/client/DeltaTracker;Z)V", at = @At("TAIL"))
    private void adin$renderGlass(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo info) {
        GlassRenderer.render();
=======
        EntityOutlines.render(((LevelRendererAccessor) minecraft.levelRenderer).adin$getEntityOutlineTarget(), renderer.mainRenderTarget());
>>>>>>> 630b1b46c1def750e98fc9ad571b50ab0e4f226d
    }
}
