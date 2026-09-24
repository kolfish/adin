package dev.koifih.client.ui;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Accessors(fluent = true)
public enum UiScale {
    PERCENT_100(1f),
    PERCENT_125(1.25f),
    PERCENT_150(1.5f),
    PERCENT_200(2f);

    public static final UiScale[] ALL = values();
    private static UiScale current = PERCENT_100;
    @Getter
    private final float factor;

    public String displayName() {
        return Math.round(factor * 100) + "%";
    }

    public static UiScale current() {
        return current;
    }

    public static void set(UiScale scale) {
        current = scale;
    }
}
