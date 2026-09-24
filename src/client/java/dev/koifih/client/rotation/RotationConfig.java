package dev.koifih.client.rotation;

public record RotationConfig(float smoothness, Smoothing smoothing, boolean silent) {
    public static RotationConfig visible(float smoothness, Smoothing smoothing) {
        return new RotationConfig(smoothness, smoothing, false);
    }

    public static RotationConfig silent(float smoothness, Smoothing smoothing) {
        return new RotationConfig(smoothness, smoothing, true);
    }

    public float scaled(float slow, float fast) {
        float eager = (1f - smoothness) * (1f - smoothness);
        return slow + (fast - slow) * eager;
    }
}
