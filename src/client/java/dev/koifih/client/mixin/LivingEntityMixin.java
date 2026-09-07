package dev.koifih.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.koifih.client.AdinClient;
import dev.koifih.client.module.impl.combat.Triggerbot;
import dev.koifih.client.util.Entities;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @ModifyExpressionValue(method = "jumpFromGround", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getYRot()F"))
    private float adin$jumpYaw(float yaw) {
        return AdinClient.ROTATIONS.movementYaw((Entity) (Object) this, yaw);
    }

    @Inject(method = "setSprinting", at = @At("HEAD"), cancellable = true)
    private void adin$holdSprint(boolean sprinting, CallbackInfo info) {
        if (!sprinting || !Entities.isLocal((Entity) (Object) this)) return;
        if (AdinClient.MODULES.get(Triggerbot.class).holdsSprint()) info.cancel();
    }
}
