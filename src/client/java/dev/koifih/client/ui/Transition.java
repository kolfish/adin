package dev.koifih.client.ui;

public final class Transition {
    @FunctionalInterface
    public interface Easing {
        Easing EASE_OUT_CUBIC = t -> 1f - (1f - t) * (1f - t) * (1f - t);
        Easing SMOOTHSTEP = t -> t * t * (3f - 2f * t);
        Easing EASE_OUT_BACK = t -> 1f + 2.70158f * (t - 1f) * (t - 1f) * (t - 1f) + 1.70158f * (t - 1f) * (t - 1f);

        float apply(float t);

        default float at(float t) {
            return apply(Math.clamp(t, 0f, 1f));
        }
    }

    private final long durationNanos;
    private final Easing easing;
    private float from;
    private float target;
    private long startedAt;

    public Transition(float initial, int durationMillis) {
        this(initial, durationMillis, Easing.EASE_OUT_CUBIC);
    }

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
        float progress = Math.clamp((System.nanoTime() - startedAt) / (float) durationNanos, 0f, 1f);
        return progress >= 1f ? target : from + (target - from) * easing.apply(progress);
    }
}
