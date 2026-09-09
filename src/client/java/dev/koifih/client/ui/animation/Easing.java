package dev.koifih.client.ui.animation;

@FunctionalInterface
public interface Easing {
    Easing LINEAR = t -> t;
    Easing EASE_OUT_CUBIC = t -> 1f - (1f - t) * (1f - t) * (1f - t);
    Easing SMOOTHSTEP = t -> t * t * (3f - 2f * t);

    float apply(float t);
}
