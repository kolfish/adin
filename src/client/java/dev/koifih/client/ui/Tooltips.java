package dev.koifih.client.ui;

import dev.koifih.client.util.Lang;

public enum Tooltips {
    DESCRIPTION("tooltips.description"),
    VIDEO("tooltips.video");

    public static final Tooltips[] ALL = values();
    private static Tooltips current = DESCRIPTION;
    private final String key;

    Tooltips(String key) {
        this.key = key;
    }

    public String displayName() {
        return Lang.get(key);
    }

    public static Tooltips current() {
        return current;
    }

    public static void set(Tooltips mode) {
        current = mode;
    }
}
