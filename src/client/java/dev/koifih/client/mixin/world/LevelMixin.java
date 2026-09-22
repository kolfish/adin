package dev.koifih.client.mixin.world;

import dev.koifih.client.module.impl.render.world.WorldModifier;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class LevelMixin {
    @Inject(method = "getRainLevel", at = @At("RETURN"), cancellable = true)
    private void adin$rainLevel(float partialTicks, CallbackInfoReturnable<Float> info) {
        if (!((Object) this instanceof ClientLevel) || WorldModifier.get().atmosphere() == null) return;
        info.setReturnValue(WorldModifier.get().rainLevel(partialTicks, info.getReturnValueF()));
    }

    @Inject(method = "getThunderLevel", at = @At("RETURN"), cancellable = true)
    private void adin$thunderLevel(float partialTicks, CallbackInfoReturnable<Float> info) {
        if (!((Object) this instanceof ClientLevel) || WorldModifier.get().atmosphere() == null) return;
        info.setReturnValue(WorldModifier.get().thunderLevel(partialTicks, info.getReturnValueF()));
    }
}
