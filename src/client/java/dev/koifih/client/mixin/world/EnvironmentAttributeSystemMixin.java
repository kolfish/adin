package dev.koifih.client.mixin.world;

import dev.koifih.client.module.impl.render.world.Atmosphere;
import dev.koifih.client.module.impl.render.world.WorldModifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.attribute.SpatialAttributeInterpolator;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnvironmentAttributeSystem.class)
public abstract class EnvironmentAttributeSystemMixin {
    @Inject(method = "getDimensionValue", at = @At("RETURN"), cancellable = true)
    private void adin$dimensionValue(EnvironmentAttribute<Object> attribute, CallbackInfoReturnable<Object> info) {
        adin$apply(attribute, info);
    }

    @Inject(method = "getValue", at = @At("RETURN"), cancellable = true)
    private void adin$value(EnvironmentAttribute<Object> attribute, Vec3 pos, SpatialAttributeInterpolator biomes,
                            CallbackInfoReturnable<Object> info) {
        adin$apply(attribute, info);
    }

    @Unique
    private void adin$apply(EnvironmentAttribute<Object> attribute, CallbackInfoReturnable<Object> info) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || level.environmentAttributes() != (Object) this) return;
        Atmosphere atmosphere = WorldModifier.get().atmosphere();
        if (atmosphere != null) info.setReturnValue(atmosphere.apply(attribute, info.getReturnValue()));
    }
}
