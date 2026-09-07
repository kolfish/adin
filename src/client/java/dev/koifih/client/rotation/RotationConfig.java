package dev.koifih.client.rotation;

public record RotationConfig(float smoothness, Smoothing smoothing, boolean silent, boolean moveFix) {
    public static RotationConfig visible(float smoothness, Smoothing smoothing) {
        return new RotationConfig(smoothness, smoothing, false, false);
    }

    public float scaled(float slow, float fast) {
        float eager = (1f - smoothness) * (1f - smoothness);
        return slow + (fast - slow) * eager;
    }
}
