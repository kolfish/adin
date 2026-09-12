package dev.koifih.client.mixin.render;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.koifih.client.render.entity.EntityOutlines;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.PostChain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @ModifyExpressionValue(method = "render(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/renderer/state/level/CameraRenderState;Lorg/joml/Matrix4fc;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;Z)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ShaderManager;getPostChain(Lnet/minecraft/resources/Identifier;Ljava/util/Set;)Lnet/minecraft/client/renderer/PostChain;"))
    private PostChain adin$skipGlow(PostChain chain) {
        return EntityOutlines.active() ? null : chain;
    }

    @Inject(method = "doEntityOutline", at = @At("HEAD"), cancellable = true)
    private void adin$skipBlit(CallbackInfo info) {
        if (EntityOutlines.active()) info.cancel();
    }
}
