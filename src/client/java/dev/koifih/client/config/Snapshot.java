package dev.koifih.client.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import dev.koifih.client.AdinClient;
import dev.koifih.client.ui.Theme;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Snapshot {
    public static void capture(State state, boolean colors, boolean settings) {
        if (colors) {
            state.colors = new State.Colors();
            state.colors.theme = Theme.mode().name();
            state.colors.accent = Theme.accentRgb();
        }
        if (settings) state.modules = AdinClient.MODULES.snapshot();
    }

    public static void apply(State state, boolean colors, boolean settings) {
        if (colors && state.colors != null) {
            Theme.setMode(mode(state.colors.theme));
            Theme.setAccent(state.colors.accent);
        }
        if (settings && state.modules != null) AdinClient.MODULES.apply(state.modules);
    }

    private static Theme.Mode mode(String name) {
        if (name == null) return Theme.mode();
        try {
            return Theme.Mode.valueOf(name);
        } catch (IllegalArgumentException exception) {
            return Theme.mode();
        }
    }
}
