package dev.koifih.client.setting;

import dev.koifih.client.gui.Theme;
import dev.koifih.client.render.gui.Text;
import dev.koifih.client.util.Colors;
import java.util.List;

public final class TextColorSettings implements SettingGroup {
    private static final String[] MODES = {"Accent", "Solid", "Gradient"};
    private static final int ACCENT = 0;
    private static final int GRADIENT = 2;
    private static final float WAVE_LENGTH = 120f;
    private static final float WAVE_SECONDS = 2f;

    private final EnumSetting mode = new EnumSetting("color", ACCENT, MODES);
    private final ColorSetting color = new ColorSetting("textColor", Theme.DEFAULT_ACCENT, 0xB7CCE8);

    public TextColorSettings() {
        color.visibleWhen(() -> mode.get() != ACCENT);
        color.gradientWhen(() -> mode.get() == GRADIENT);
    }

    @Override
    public List<Setting<?>> settings() {
        return List.of(mode, color);
    }

    public Text.ColorAt colorAt(float scale) {
        if (mode.get() == ACCENT) return x -> Theme.ACCENT;
        int primary = Colors.opaque(color.get());
        if (mode.get() != GRADIENT) return x -> primary;
        int secondary = Colors.opaque(color.secondary());
        float phase = (System.nanoTime() / 1_000_000_000f) / WAVE_SECONDS;
        float length = WAVE_LENGTH * scale;
        return x -> {
            float wave = (x / length - phase) % 1f;
            if (wave < 0f) wave += 1f;
            return Colors.lerp(primary, secondary, wave < 0.5f ? wave * 2f : 2f - wave * 2f);
        };
    }
}
