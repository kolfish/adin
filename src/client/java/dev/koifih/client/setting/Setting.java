package dev.koifih.client.setting;

import lombok.Getter;
import lombok.experimental.Accessors;
import com.google.gson.JsonElement;
import dev.koifih.client.util.Lang;
import java.util.function.BooleanSupplier;

@Accessors(fluent = true)
public abstract class Setting<T> {
    @Getter
    private final String id;
    private final T defaultValue;
    private String moduleId = "";
    private BooleanSupplier visible = () -> true;
    private boolean described;
    private final Lang.Localized name = new Lang.Localized(() -> Lang.getOrDefault("setting." + moduleId + "." + id(),
            Lang.getOrDefault("setting." + id(), Lang.capitalize(id()))));
    private final Lang.Localized description = new Lang.Localized(() -> Lang.getOrDefault("setting." + moduleId + "." + id() + ".help", ""));
    protected T value;

    protected Setting(String id, T defaultValue) {
        this.id = id;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public void attach(String moduleId) {
        this.moduleId = moduleId;
        name.invalidate();
        description.invalidate();
    }

    public void visibleWhen(BooleanSupplier condition) {
        visible = condition;
    }

    public boolean isVisible() {
        return visible.getAsBoolean();
    }

    public void describe() {
        described = true;
    }

    public boolean described() {
        return described;
    }

    public String description() {
        return description.get();
    }

    public String name() {
        return name.get();
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
