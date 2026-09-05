package dev.koifih.client.feature.setting;

import com.google.gson.JsonElement;
import dev.koifih.client.gui.Lang;

public abstract class Setting<T> {
    private final String id;
    private final T defaultValue;
    private String featureId = "";
    protected T value;

    protected Setting(String id, T defaultValue) {
        this.id = id;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public void attach(String featureId) {
        this.featureId = featureId;
    }

    public String id() {
        return id;
    }

    public String name() {
        return Lang.getOrDefault("setting." + featureId + "." + id, Character.toUpperCase(id.charAt(0)) + id.substring(1));
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }

    public void reset() {
        value = defaultValue;
    }

    public abstract JsonElement save();

    public abstract void load(JsonElement json);
}
