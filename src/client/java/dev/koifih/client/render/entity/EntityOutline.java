package dev.koifih.client.render.entity;

public record EntityOutline(int color, float width, float fill, int layers, float glow, float radius) {
    public EntityOutline withColor(int color) {
        return new EntityOutline(color, width, fill, layers, glow, radius);
    }

    public float reach() {
        return Math.max(width * (2 * layers + 1), glow > 0f ? radius : 0f);
    }
}
