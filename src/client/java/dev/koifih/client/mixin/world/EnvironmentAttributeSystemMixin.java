package dev.koifih.client.mixin.world;

import dev.koifih.client.module.impl.render.world.WorldModifier;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.attribute.SpatialAttributeInterpolator;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnvironmentAttributeSystem.class)
public abstract class EnvironmentAttributeSystemMixin {
    @Inject(method = "getDimensionValue", at = @At("RETURN"), cancellable = true)
    private void adin$dimensionValue(EnvironmentAttribute<Object> attribute, CallbackInfoReturnable<Object> info) {
        WorldModifier.applyEnvironmentAttribute(this, attribute, info);
    }

    @Inject(method = "getValue", at = @At("RETURN"), cancellable = true)
    private void adin$value(EnvironmentAttribute<Object> attribute, Vec3 pos, SpatialAttributeInterpolator biomes,
                            CallbackInfoReturnable<Object> info) {
        WorldModifier.applyEnvironmentAttribute(this, attribute, info);
    }
}
