package dev.koifih.client.render.screen;

import dev.koifih.client.render.BoxCorners;
import dev.koifih.client.render.Style;
import net.minecraft.world.phys.AABB;

public final class ScreenBoxes {
    private ScreenBoxes() {}

    public static ScreenPoint[] project(Projector projector, AABB box) {
        ScreenPoint[] corners = new ScreenPoint[BoxCorners.COUNT];
        for (int i = 0; i < corners.length; i++) {
            corners[i] = projector.project(BoxCorners.x(box, i), BoxCorners.y(box, i), BoxCorners.z(box, i));
        }
        return corners;
    }

    public static ScreenRect bounds(ScreenPoint[] corners) {
        ScreenExtent extent = new ScreenExtent();
        for (ScreenPoint corner : corners) {
            if (corner != null) extent.add(corner);
        }
        return extent.toRect();
    }

    public static void collect(ScreenBuffer buffer, ScreenPoint[] corners, Style style) {
        if (style.hasFill()) {
            for (int[] face : BoxCorners.FACES) {
                ScreenPoint a = corners[face[0]];
                ScreenPoint b = corners[face[1]];
                ScreenPoint c = corners[face[2]];
                ScreenPoint d = corners[face[3]];
                if (a != null && b != null && c != null && d != null) buffer.quad(a, b, c, d, style.fill());
            }
        }
        if (style.hasOutline()) edges(buffer, corners, style.outline(), style.outlineWidth());
        if (style.hasStroke()) edges(buffer, corners, style.stroke(), style.strokeWidth());
    }

    private static void edges(ScreenBuffer buffer, ScreenPoint[] corners, int color, float width) {
        for (int i = 0; i < BoxCorners.EDGES.length; i += 2) {
            ScreenPoint a = corners[BoxCorners.EDGES[i]];
            ScreenPoint b = corners[BoxCorners.EDGES[i + 1]];
            if (a != null && b != null) buffer.line(a, b, color, width);
        }
    }
}
