package dev.koifih.client.rotation;

import java.util.function.Supplier;

public enum Smoothing {
    EASE_OUT_CUBIC(EaseOutCubic::new),
    WIND_MOUSE(WindMouse::new);

    public static final String[] NAMES = {"Ease out cubic", "WindMouse"};
    private final Supplier<Rotator> factory;

    Smoothing(Supplier<Rotator> factory) {
        this.factory = factory;
    }

    public Rotator create() {
        return factory.get();
    }
}
