package dev.koifih.client.gui;

public enum Units {
    METRIC("units.metric"),
    IMPERIAL("units.imperial");

    public static final Units[] ALL = values();
    private static Units current = METRIC;
    private final String key;

    Units(String key) {
        this.key = key;
    }

    public String displayName() {
        return Lang.get(key);
    }

    public static Units current() {
        return current;
    }

    public static void set(Units units) {
        current = units;
    }
}
