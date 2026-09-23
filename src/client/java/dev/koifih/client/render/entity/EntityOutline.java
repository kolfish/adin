package dev.koifih.client.render.entity;

public record EntityOutline(int color, float width, float fill, float glow, float radius) {
    public EntityOutline withColor(int color) {
        return new EntityOutline(color, width, fill, glow, radius);
    }

    public float reach() {
        return Math.max(width, glow > 0f ? radius : 0f);
    }
}
