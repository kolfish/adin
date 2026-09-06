package dev.koifih.client.gui;

public enum Palette {
    ON_ACCENT(0xFF101010, 0xFF101010),
    TEXT(0xFFEAEAEA, 0xFF141414),
    MUTED(0xFF858585, 0xFF666666),
    DIM(0xFF999999, 0xFF4E4E4E),
    SIDEBAR(0xFF161616, 0xFFD6D6D6),
    MAIN(0xFF0F0F0F, 0xFFE3E3E3),
    ROW(0xFF1C1C1C, 0xFFF1F1F1),
    OVERLAY(0xFF171717, 0xFFFFFFFF),
    FIELD(0xFF141414, 0xFFE6E6E6),
    CONTROL(0xFF262626, 0xFFDADADA),
    CONTROL_ACTIVE(0xFF3A3A3A, 0xFFC2C2C2),
    TRACK(0xFF343434, 0xFFCACACA),
    POPUP(0xFF202020, 0xFFFFFFFF),
    POPUP_BORDER(0x00000000, 0xFFC8C8C8),
    HIGHLIGHT(0xFF2B322B, 0xFFE3EAE1),
    UNCHECKED(0xFF444444, 0xFFB0B0B0),
    TOGGLE_OFF(0xFF171717, 0xFFD0D0D0),
    TOGGLE_OFF_BORDER(0xFF2B2B2B, 0xFFB4B4B4),
    THUMB_OFF(0xFF484848, 0xFF7A7A7A);

    static final Palette[] ALL = values();

    private final int dark;
    private final int light;

    Palette(int dark, int light) {
        this.dark = dark;
        this.light = light;
    }

    int in(Theme.Mode mode) {
        return mode == Theme.Mode.LIGHT ? light : dark;
    }
}
