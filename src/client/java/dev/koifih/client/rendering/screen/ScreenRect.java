package dev.koifih.client.rendering.screen;

public record ScreenRect(float minX, float minY, float maxX, float maxY) {
    public static ScreenRect of(float x0, float y0, float x1, float y1) {
        return new ScreenRect(Math.min(x0, x1), Math.min(y0, y1), Math.max(x0, x1), Math.max(y0, y1));
    }

    public float width() {
        return maxX - minX;
    }

    public float height() {
        return maxY - minY;
    }

    public float centerX() {
        return (minX + maxX) * 0.5f;
    }

    public float centerY() {
        return (minY + maxY) * 0.5f;
    }

    public ScreenRect expand(float amount) {
        return new ScreenRect(minX - amount, minY - amount, maxX + amount, maxY + amount);
    }

    public boolean intersects(float x0, float y0, float x1, float y1) {
        return maxX >= x0 && minX <= x1 && maxY >= y0 && minY <= y1;
    }

    public boolean contains(float x, float y) {
        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }
}
