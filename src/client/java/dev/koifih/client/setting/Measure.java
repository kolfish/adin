package dev.koifih.client.setting;

import dev.koifih.client.ui.Units;
import java.util.Locale;

public enum Measure {
    NONE,
    DISTANCE,
    PERCENT,
    DEGREES,
    FRACTION,
    MILLIS,
    TICKS;

    public String format(int value) {
        return switch (this) {
            case DISTANCE -> Units.current().distance(value);
            case PERCENT -> value + "%";
            case DEGREES -> value + "\u00b0";
            case FRACTION -> String.format(Locale.ROOT, "%.2f", value / 100.0);
            case MILLIS -> value + "ms";
            case TICKS -> value + "t";
            case NONE -> Integer.toString(value);
        };
    }
}
