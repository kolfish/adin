package dev.koifih.client.module;

import dev.koifih.client.util.Lang;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Accessors(fluent = true)
public enum Category {
    COMBAT("combat", 0xe1b3),
    MOVEMENT("movement", 0xe566),
    RENDER("render", 0xe8f4),
    HUD("hud", 0xe41c),
    PLAYER("player", 0xe7fd),
    MISC("misc", 0xe1bd);

    @Getter
    private final String id;
    @Getter
    private final int icon;

    public String label() {
        return Lang.get("category." + id);
    }
}
