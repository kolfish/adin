package dev.koifih.client.setting;

import com.google.gson.JsonElement;
import dev.koifih.client.util.Lang;
import java.util.function.BooleanSupplier;

public abstract class Setting<T> {
    private final String id;
    private final T defaultValue;
    private String moduleId = "";
    private BooleanSupplier visible = () -> true;
    protected T value;

    protected Setting(String id, T defaultValue) {
        this.id = id;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public void attach(String moduleId) {
        this.moduleId = moduleId;
    }

    public String id() {
        return id;
    }

    public void visibleWhen(BooleanSupplier condition) {
        visible = condition;
    }

    public boolean isVisible() {
        return visible.getAsBoolean();
    }

    public String name() {
        return Lang.getOrDefault("setting." + moduleId + "." + id, Character.toUpperCase(id.charAt(0)) + id.substring(1));
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
