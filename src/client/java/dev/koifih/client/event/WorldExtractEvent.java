package dev.koifih.client.event;

import dev.koifih.client.rendering.world.ShapeCollector;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public record WorldExtractEvent(ClientLevel level, Camera camera, DeltaTracker deltaTracker,
                                ShapeCollector shapes) implements Event {
    public float partialTicks(Entity entity) {
        return deltaTracker.getGameTimeDeltaPartialTick(!level.tickRateManager().isEntityFrozen(entity));
    }

    public AABB interpolatedBounds(Entity entity) {
        Vec3 offset = entity.getPosition(partialTicks(entity)).subtract(entity.position());
        return entity.getBoundingBox().move(offset);
    }
}
