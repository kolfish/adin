package dev.koifih.client.rotation;

import lombok.RequiredArgsConstructor;
import java.util.function.Supplier;

@RequiredArgsConstructor
public enum Smoothing {
    EASE_OUT_CUBIC(EaseOutCubic::new),
    WIND_MOUSE(WindMouse::new);

    public static final String[] NAMES = {"Ease out cubic", "WindMouse"};
    private final Supplier<Rotator> factory;

    public Rotator create() {
        return factory.get();
    }
}
