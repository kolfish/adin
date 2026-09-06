package dev.koifih.client.render.screen;

final class ScreenExtent {
    private float minX = Float.POSITIVE_INFINITY;
    private float minY = Float.POSITIVE_INFINITY;
    private float maxX = Float.NEGATIVE_INFINITY;
    private float maxY = Float.NEGATIVE_INFINITY;

    void add(ScreenPoint point) {
        minX = Math.min(minX, point.x());
        minY = Math.min(minY, point.y());
        maxX = Math.max(maxX, point.x());
        maxY = Math.max(maxY, point.y());
    }

    ScreenRect toRect() {
        return minX <= maxX ? new ScreenRect(minX, minY, maxX, maxY) : null;
    }
}
