package dev.koifih.client.ui.animation;

public final class Transition {
    private final long durationNanos;
    private final Easing easing;
    private float from;
    private float target;
    private long startedAt;

    public Transition(float initial, int durationMillis, Easing easing) {
        this.durationNanos = durationMillis * 1_000_000L;
        this.easing = easing;
        snap(initial);
    }

    public void snap(float value) {
        from = value;
        target = value;
        startedAt = System.nanoTime();
    }

    public void set(float newTarget) {
        if (newTarget == target) return;
        from = value();
        target = newTarget;
        startedAt = System.nanoTime();
    }

    public float target() {
        return target;
    }

    public float value() {
        float progress = progress();
        return progress >= 1f ? target : from + (target - from) * easing.apply(progress);
    }

    private float progress() {
        return Math.clamp((System.nanoTime() - startedAt) / (float) durationNanos, 0f, 1f);
    }
}
