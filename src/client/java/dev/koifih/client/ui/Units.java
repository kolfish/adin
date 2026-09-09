package dev.koifih.client.ui;

import dev.koifih.client.util.Lang;

public enum Units {
    METRIC("units.metric") {
        @Override
        public String distance(int blocks) {
            return blocks + "m";
        }
    },
    IMPERIAL("units.imperial") {
        @Override
        public String distance(int blocks) {
            return Math.round(blocks * FEET_PER_BLOCK) + "ft";
        }
    };

    public static final Units[] ALL = values();
    private static final float FEET_PER_BLOCK = 3.28084f;
    private static Units current = METRIC;
    private final String key;

    Units(String key) {
        this.key = key;
    }

    public abstract String distance(int blocks);

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
