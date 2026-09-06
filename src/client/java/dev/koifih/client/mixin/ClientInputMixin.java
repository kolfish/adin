package dev.koifih.client.mixin;

import dev.koifih.client.feature.FeatureManager;
import dev.koifih.client.feature.movement.Sprint;
import net.minecraft.client.player.ClientInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientInput.class)
public abstract class ClientInputMixin {
    @Inject(method = "hasForwardImpulse", at = @At("HEAD"), cancellable = true)
    private void adin$omniSprint(CallbackInfoReturnable<Boolean> info) {
        if (!FeatureManager.get(Sprint.class).omniActive()) return;
        if (((ClientInput) (Object) this).getMoveVector().lengthSquared() > 1.0E-10f) info.setReturnValue(true);
    }
}
