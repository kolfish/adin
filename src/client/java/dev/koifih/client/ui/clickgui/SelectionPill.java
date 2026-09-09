package dev.koifih.client.ui.clickgui;

import dev.koifih.client.ui.animation.Easing;
import dev.koifih.client.ui.animation.Transition;

final class SelectionPill {
    private static final int DURATION_MILLIS = 150;

    private final Transition y = new Transition(0, DURATION_MILLIS, Easing.EASE_OUT_CUBIC);
    private final Transition width = new Transition(0, DURATION_MILLIS, Easing.EASE_OUT_CUBIC);
    private boolean initialized;

    record Position(float y, float width) {}

    void reset() {
        initialized = false;
    }

    Position update(float targetY, float targetWidth) {
        if (!initialized) {
            y.snap(targetY);
            width.snap(targetWidth);
            initialized = true;
        }
        y.set(targetY);
        width.set(targetWidth);
        return new Position(y.value(), width.value());
    }
}
