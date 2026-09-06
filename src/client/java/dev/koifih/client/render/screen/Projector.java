package dev.koifih.client.render.screen;

import net.minecraft.world.phys.Vec3;

@FunctionalInterface
public interface Projector {
    ScreenPoint project(double x, double y, double z);

    default ScreenPoint project(Vec3 position) {
        return project(position.x, position.y, position.z);
    }
}
