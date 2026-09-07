package dev.koifih.client.rotation;

import java.util.function.Supplier;

public enum Smoothing {
    EASE_OUT_CUBIC(EaseOutCubic::new);

    public static final String[] NAMES = {"Ease out cubic"};
    private final Supplier<Rotator> factory;

    Smoothing(Supplier<Rotator> factory) {
        this.factory = factory;
    }

    public Rotator create() {
        return factory.get();
    }
}
