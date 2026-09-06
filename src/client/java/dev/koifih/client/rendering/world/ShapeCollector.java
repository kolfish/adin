package dev.koifih.client.rendering.world;

import net.minecraft.world.phys.AABB;
import java.util.ArrayList;
import java.util.List;

public final class ShapeCollector {
    public record Box(AABB bounds, BoxStyle style) {}

    private final List<Box> boxes = new ArrayList<>();

    public void box(AABB bounds, BoxStyle style) {
        boxes.add(new Box(bounds, style));
    }

    public List<Box> boxes() {
        return List.copyOf(boxes);
    }
}
