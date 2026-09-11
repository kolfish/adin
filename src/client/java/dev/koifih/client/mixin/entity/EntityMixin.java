package dev.koifih.client.mixin.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.koifih.client.AdinClient;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "getViewVector", at = @At("HEAD"), cancellable = true)
    private void adin$viewVector(float partialTick, CallbackInfoReturnable<Vec3> info) {
        Vec3 view = AdinClient.ROTATIONS.viewVector((Entity) (Object) this);
        if (view != null) info.setReturnValue(view);
    }

    @ModifyExpressionValue(method = "moveRelative", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getYRot()F"))
    private float adin$movementYaw(float yaw) {
        return AdinClient.ROTATIONS.movementYaw((Entity) (Object) this, yaw);
    }
}
