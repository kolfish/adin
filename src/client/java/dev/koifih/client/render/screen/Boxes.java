package dev.koifih.client.render.screen;

import dev.koifih.client.render.Corners;
import dev.koifih.client.render.Point;
import dev.koifih.client.render.Rect;
import dev.koifih.client.render.Style;
import net.minecraft.world.phys.AABB;

public final class Boxes {
    private Boxes() {}

    public static Point[] project(Projector projector, AABB box) {
        Point[] corners = new Point[Corners.COUNT];
        for (int i = 0; i < corners.length; i++) {
            corners[i] = projector.project(Corners.x(box, i), Corners.y(box, i), Corners.z(box, i));
        }
        return corners;
    }

    public static Rect bounds(Point[] corners) {
        Extent extent = new Extent();
        for (Point corner : corners) {
            if (corner != null) extent.add(corner);
        }
        return extent.toRect();
    }

    public static void collect(ScreenBuffer buffer, Point[] corners, Style style) {
        if (style.hasFill()) {
            for (int[] face : Corners.FACES) {
                Point a = corners[face[0]];
                Point b = corners[face[1]];
                Point c = corners[face[2]];
                Point d = corners[face[3]];
                if (a != null && b != null && c != null && d != null) buffer.quad(a, b, c, d, style.fill());
            }
        }
        if (style.hasOutline()) edges(buffer, corners, style.outline(), style.outlineWidth());
        if (style.hasStroke()) edges(buffer, corners, style.stroke(), style.strokeWidth());
    }

    private static void edges(ScreenBuffer buffer, Point[] corners, int color, float width) {
        for (int i = 0; i < Corners.EDGES.length; i += 2) {
            Point a = corners[Corners.EDGES[i]];
            Point b = corners[Corners.EDGES[i + 1]];
            if (a != null && b != null) buffer.line(a, b, color, width);
        }
    }
}
