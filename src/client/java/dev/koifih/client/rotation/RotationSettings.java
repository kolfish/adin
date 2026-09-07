package dev.koifih.client.rotation;

import dev.koifih.client.setting.BoolSetting;
import dev.koifih.client.setting.EnumSetting;
import dev.koifih.client.setting.Measure;
import dev.koifih.client.setting.Setting;
import dev.koifih.client.setting.SettingGroup;
import dev.koifih.client.setting.SliderSetting;
import java.util.List;

public final class RotationSettings implements SettingGroup {
    private final SliderSetting smoothness = new SliderSetting("smoothness", 50, 0, 100, Measure.PERCENT);
    private final EnumSetting smoothing = new EnumSetting("smoothing", 0, Smoothing.NAMES);
    private final BoolSetting silent = new BoolSetting("silent", true);
    private final BoolSetting moveFix = new BoolSetting("moveFix", true);

    public RotationSettings() {
        moveFix.visibleWhen(silent::get);
    }

    @Override
    public List<Setting<?>> settings() {
        return List.of(smoothness, smoothing, silent, moveFix);
    }

    public RotationConfig config() {
        return new RotationConfig(smoothness.get() / 100f, Smoothing.values()[smoothing.get()], silent.get(), moveFix.get());
    }
}
