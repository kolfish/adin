package dev.koifih.client.render.screen;

import dev.koifih.client.render.Point;
import dev.koifih.client.render.Rect;
final class Extent {
    private float minX = Float.POSITIVE_INFINITY;
    private float minY = Float.POSITIVE_INFINITY;
    private float maxX = Float.NEGATIVE_INFINITY;
    private float maxY = Float.NEGATIVE_INFINITY;

    void add(Point point) {
        minX = Math.min(minX, point.x());
        minY = Math.min(minY, point.y());
        maxX = Math.max(maxX, point.x());
        maxY = Math.max(maxY, point.y());
    }

    Rect toRect() {
        return minX <= maxX ? new Rect(minX, minY, maxX, maxY) : null;
    }
}
