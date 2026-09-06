package dev.koifih.client.feature.setting;

import dev.koifih.client.gui.Units;

public enum Measure {
    NONE,
    DISTANCE;

    public String format(int value) {
        return this == DISTANCE ? Units.current().distance(value) : Integer.toString(value);
    }
}
