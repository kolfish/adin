package dev.koifih.client.setting;

import dev.koifih.client.gui.Units;

public enum Measure {
    NONE,
    DISTANCE,
    PERCENT,
    DEGREES;

    public String format(int value) {
        return switch (this) {
            case DISTANCE -> Units.current().distance(value);
            case PERCENT -> value + "%";
            case DEGREES -> value + "\u00b0";
            case NONE -> Integer.toString(value);
        };
    }
}
