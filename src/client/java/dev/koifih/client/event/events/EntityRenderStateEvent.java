package dev.koifih.client.event.events;

import dev.koifih.client.event.Event;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public record EntityRenderStateEvent(Entity entity, EntityRenderState state, float partialTicks) implements Event {
    public Vec3 renderPosition() {
        return new Vec3(Mth.lerp(partialTicks, entity.xOld, entity.getX()),
                Mth.lerp(partialTicks, entity.yOld, entity.getY()),
                Mth.lerp(partialTicks, entity.zOld, entity.getZ()));
    }
}
