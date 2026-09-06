package dev.koifih.client.module;

import dev.koifih.client.util.Lang;

public enum Category {
    COMBAT("combat", 0xe1b3),
    MOVEMENT("movement", 0xe566),
    RENDER("render", 0xe8f4),
    PLAYER("player", 0xe7fd),
    MISC("misc", 0xe1bd);

    private final String id;
    private final int icon;

    Category(String id, int icon) {
        this.id = id;
        this.icon = icon;
    }

    public String id() {
        return id;
    }

    public int icon() {
        return icon;
    }

    public String label() {
        return Lang.get("category." + id);
    }
}
