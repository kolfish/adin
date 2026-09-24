package dev.koifih.client.render.world;

import dev.koifih.client.render.Style;
import net.minecraft.world.phys.AABB;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class WorldBuffer {
    public record Box(AABB bounds, Style style) {}

    private final List<Box> boxes = new ArrayList<>();

    public void box(AABB bounds, Style style) {
        boxes.add(new Box(bounds, style));
    }

    public List<Box> boxes() {
        return Collections.unmodifiableList(boxes);
    }
}
