package dev.koifih.client.event;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public interface LevelFrameEvent extends Event {
    ClientLevel level();

    Camera camera();

    DeltaTracker deltaTracker();

    default float partialTicks(Entity entity) {
        return deltaTracker().getGameTimeDeltaPartialTick(!level().tickRateManager().isEntityFrozen(entity));
    }

    default Vec3 renderPosition(Entity entity) {
        float partial = partialTicks(entity);
        return new Vec3(Mth.lerp(partial, entity.xOld, entity.getX()),
                Mth.lerp(partial, entity.yOld, entity.getY()),
                Mth.lerp(partial, entity.zOld, entity.getZ()));
    }

    default AABB interpolatedBounds(Entity entity) {
        return entity.getBoundingBox().move(renderPosition(entity).subtract(entity.position()));
    }
}
