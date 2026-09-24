package dev.koifih.client.render.screen;

import dev.koifih.client.render.Point;
import net.minecraft.world.phys.Vec3;

@FunctionalInterface
public interface Projector {
    Point project(double x, double y, double z);

    default Point project(Vec3 position) {
        return project(position.x, position.y, position.z);
    }
}
