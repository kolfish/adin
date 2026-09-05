package dev.koifih.client.gui;

import dev.koifih.client.utils.Colors;
import dev.koifih.client.gui.animation.Easing;
import dev.koifih.client.gui.animation.Transition;

public final class Theme {
    public enum Mode { DARK, LIGHT }

    public static final int DEFAULT_ACCENT = 0xB8DDB0;
    private static final int COLOR_COUNT = 19;
    private static final int[] DARK = {
            0xFF101010, 0xFFEAEAEA, 0xFF858585, 0xFF999999, 0xFF161616, 0xFF0F0F0F, 0xFF1C1C1C, 0xFF171717,
            0xFF141414, 0xFF262626, 0xFF3A3A3A, 0xFF343434, 0xFF202020, 0x00000000, 0xFF2B322B, 0xFF444444,
            0xFF171717, 0xFF2B2B2B, 0xFF484848};
    private static final int[] LIGHT = {
            0xFF101010, 0xFF141414, 0xFF666666, 0xFF4E4E4E, 0xFFD6D6D6, 0xFFE3E3E3, 0xFFF1F1F1, 0xFFFFFFFF,
            0xFFE6E6E6, 0xFFDADADA, 0xFFC2C2C2, 0xFFCACACA, 0xFFFFFFFF, 0xFFC8C8C8, 0xFFE3EAE1, 0xFFB0B0B0,
            0xFFD0D0D0, 0xFFB4B4B4, 0xFF7A7A7A};

    public static int ACCENT;
    public static int ON_ACCENT;
    public static int TEXT;
    public static int MUTED;
    public static int DIM;
    public static int SIDEBAR;
    public static int MAIN;
    public static int ROW;
    public static int OVERLAY;
    public static int FIELD;
    public static int CONTROL;
    public static int CONTROL_ACTIVE;
    public static int TRACK;
    public static int POPUP;
    public static int POPUP_BORDER;
    public static int HIGHLIGHT;
    public static int UNCHECKED;
    public static int TOGGLE_OFF;
    public static int TOGGLE_OFF_BORDER;
    public static int THUMB_OFF;

    private static final int[] from = new int[COLOR_COUNT];
    private static final int[] to = new int[COLOR_COUNT];
    private static final Transition blend = new Transition(1f, 220, Easing.EASE_OUT_CUBIC);
    private static final Transition accentBlend = new Transition(1f, 120, Easing.EASE_OUT_CUBIC);
    private static int accentFrom;
    private static int accentTo;
    private static int accentRgb = DEFAULT_ACCENT;
    private static Mode mode = Mode.DARK;

    static {
        System.arraycopy(DARK, 0, to, 0, COLOR_COUNT);
        System.arraycopy(DARK, 0, from, 0, COLOR_COUNT);
        accentFrom = accentTo = 0xFF000000 | DEFAULT_ACCENT;
        update();
    }

    private Theme() {}

    public static Mode mode() {
        return mode;
    }

    public static void setMode(Mode newMode) {
        if (newMode == mode) return;
        mode = newMode;
        for (int i = 0; i < COLOR_COUNT; i++) from[i] = Colors.lerp(from[i], to[i], blend.value());
        System.arraycopy(newMode == Mode.LIGHT ? LIGHT : DARK, 0, to, 0, COLOR_COUNT);
        blend.snap(0f);
        blend.set(1f);
        setAccent(accentRgb);
    }

    public static int accentRgb() {
        return accentRgb;
    }

    public static void setAccent(int rgb) {
        accentRgb = rgb & 0xFFFFFF;
        accentFrom = ACCENT;
        accentTo = 0xFF000000 | (mode == Mode.LIGHT ? Colors.darken(accentRgb, 0.28f) : accentRgb);
        accentBlend.snap(0f);
        accentBlend.set(1f);
    }

    public static void update() {
        float t = blend.value();
        int i = 0;
        ON_ACCENT = Colors.lerp(from[i], to[i++], t);
        TEXT = Colors.lerp(from[i], to[i++], t);
        MUTED = Colors.lerp(from[i], to[i++], t);
        DIM = Colors.lerp(from[i], to[i++], t);
        SIDEBAR = Colors.lerp(from[i], to[i++], t);
        MAIN = Colors.lerp(from[i], to[i++], t);
        ROW = Colors.lerp(from[i], to[i++], t);
        OVERLAY = Colors.lerp(from[i], to[i++], t);
        FIELD = Colors.lerp(from[i], to[i++], t);
        CONTROL = Colors.lerp(from[i], to[i++], t);
        CONTROL_ACTIVE = Colors.lerp(from[i], to[i++], t);
        TRACK = Colors.lerp(from[i], to[i++], t);
        POPUP = Colors.lerp(from[i], to[i++], t);
        POPUP_BORDER = Colors.lerp(from[i], to[i++], t);
        HIGHLIGHT = Colors.lerp(from[i], to[i++], t);
        UNCHECKED = Colors.lerp(from[i], to[i++], t);
        TOGGLE_OFF = Colors.lerp(from[i], to[i++], t);
        TOGGLE_OFF_BORDER = Colors.lerp(from[i], to[i++], t);
        THUMB_OFF = Colors.lerp(from[i], to[i], t);
        ACCENT = Colors.lerp(accentFrom, accentTo, accentBlend.value());
    }

}
