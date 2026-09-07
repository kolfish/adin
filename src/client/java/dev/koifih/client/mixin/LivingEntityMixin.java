package dev.koifih.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.koifih.client.AdinClient;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @ModifyExpressionValue(method = "jumpFromGround", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getYRot()F"))
    private float adin$jumpYaw(float yaw) {
        return AdinClient.ROTATIONS.movementYaw((Entity) (Object) this, yaw);
    }
}
