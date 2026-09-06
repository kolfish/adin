package dev.koifih.client.feature;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.platform.InputConstants;
import dev.koifih.client.config.Config;
import dev.koifih.client.event.EventBus;
import dev.koifih.client.event.FeatureToggleEvent;
import dev.koifih.client.event.TickEvent;
import dev.koifih.client.feature.movement.Sprint;
import dev.koifih.client.feature.render.Esp;
import dev.koifih.client.feature.setting.Setting;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class FeatureManager {
    private static final List<Feature> FEATURES = new ArrayList<>();
    private static final Map<String, Feature> BY_ID = new HashMap<>();
    private static final Map<Class<? extends Feature>, Feature> BY_TYPE = new HashMap<>();
    private static final List<Feature> VIEW = Collections.unmodifiableList(FEATURES);

    private FeatureManager() {}

    public static void init() {
        register(new Sprint());
        register(new Esp());
        EventBus.subscribe(TickEvent.class, event -> {
            for (Feature feature : FEATURES) feature.tickKeybind(event.client());
        });
    }

    public static <F extends Feature> F register(F feature) {
        if (BY_ID.containsKey(feature.id())) {
            throw new IllegalArgumentException("Duplicate feature id: " + feature.id());
        }
        FEATURES.add(feature);
        BY_ID.put(feature.id(), feature);
        BY_TYPE.put(feature.getClass(), feature);
        return feature;
    }

    public static List<Feature> all() {
        return VIEW;
    }

    public static Optional<Feature> find(String id) {
        return Optional.ofNullable(BY_ID.get(id));
    }

    public static Feature get(String id) {
        return find(id).orElseThrow(() -> new IllegalArgumentException("Unknown feature: " + id));
    }

    public static <F extends Feature> F get(Class<F> type) {
        Feature feature = BY_TYPE.get(type);
        if (feature == null) throw new IllegalArgumentException("Unregistered feature: " + type.getSimpleName());
        return type.cast(feature);
    }

    public static List<Feature> in(Category category) {
        List<Feature> features = new ArrayList<>();
        for (Feature feature : FEATURES) if (feature.category() == category) features.add(feature);
        return features;
    }

    public static List<Feature> enabled() {
        List<Feature> features = new ArrayList<>();
        for (Feature feature : FEATURES) if (feature.isEnabled()) features.add(feature);
        return features;
    }

    public static boolean isEnabled(Class<? extends Feature> type) {
        Feature feature = BY_TYPE.get(type);
        return feature != null && feature.isEnabled();
    }

    public static void disableAll() {
        for (Feature feature : FEATURES) feature.setEnabled(false);
    }

    public static Optional<Feature> boundTo(InputConstants.Key key) {
        if (key == InputConstants.UNKNOWN) return Optional.empty();
        for (Feature feature : FEATURES) if (feature.key().equals(key)) return Optional.of(feature);
        return Optional.empty();
    }

    static void toggled(Feature feature) {
        EventBus.post(new FeatureToggleEvent(feature, feature.isEnabled()));
    }

    public static Map<String, Config.FeatureState> snapshot() {
        Map<String, Config.FeatureState> states = new LinkedHashMap<>();
        for (Feature feature : FEATURES) {
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
        for (Feature feature : FEATURES) {
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
