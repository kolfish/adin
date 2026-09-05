package dev.koifih.client.feature;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.platform.InputConstants;
import dev.koifih.client.config.Config;
import dev.koifih.client.event.EventBus;
import dev.koifih.client.event.TickEvent;
import dev.koifih.client.feature.movement.Sprint;
import dev.koifih.client.feature.setting.Setting;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Features {
    private static final List<Feature> ALL = new ArrayList<>();

    public static final Sprint SPRINT = register(new Sprint());

    private Features() {}

    private static <F extends Feature> F register(F feature) {
        ALL.add(feature);
        return feature;
    }

    public static void init() {
        EventBus.subscribe(TickEvent.class, event -> {
            for (Feature feature : ALL) feature.tickKeybind(event.client());
        });
    }

    public static List<Feature> all() {
        return ALL;
    }

    public static List<Feature> in(Category category) {
        List<Feature> features = new ArrayList<>();
        for (Feature feature : ALL) if (feature.category() == category) features.add(feature);
        return features;
    }

    public static Map<String, Config.FeatureState> snapshot() {
        Map<String, Config.FeatureState> states = new LinkedHashMap<>();
        for (Feature feature : ALL) {
            Config.FeatureState state = new Config.FeatureState();
            state.enabled = feature.isEnabled();
            state.key = feature.key().getName();
            state.hold = feature.hold();
            state.settings = new LinkedHashMap<>();
            for (Setting<?> setting : feature.settings()) state.settings.put(setting.id(), setting.save());
            states.put(feature.id(), state);
        }
        return states;
    }

    public static void apply(Map<String, Config.FeatureState> states) {
        for (Feature feature : ALL) {
            Config.FeatureState state = states.get(feature.id());
            if (state == null) continue;
            feature.setHold(state.hold);
            try {
                feature.setKey(state.key == null ? InputConstants.UNKNOWN : InputConstants.getKey(state.key));
            } catch (IllegalArgumentException exception) {
                feature.setKey(InputConstants.UNKNOWN);
            }
            if (state.settings != null) {
                for (Setting<?> setting : feature.settings()) {
                    JsonElement json = state.settings.get(setting.id());
                    if (json != null) setting.load(json);
                }
            }
            feature.setEnabled(state.enabled);
        }
    }
}
