package dev.koifih.client.rotation;

public record RotationConfig(float smoothness, Smoothing smoothing, boolean silent, boolean moveFix) {
    public static RotationConfig visible(float smoothness, Smoothing smoothing) {
        return new RotationConfig(smoothness, smoothing, false, false);
    }
}
