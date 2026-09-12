package dev.koifih.client.setting;

import lombok.Getter;
import lombok.experimental.Accessors;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

@Accessors(fluent = true)
public final class EnumSetting extends Setting<Integer> {
    @Getter
    private final String[] options;

    public EnumSetting(String id, int defaultIndex, String... options) {
        super(id, defaultIndex);
        this.options = options;
    }

    public String selected() {
        return options[value];
    }

    @Override
    public void set(Integer value) {
        super.set(Math.clamp(value, 0, options.length - 1));
    }

    @Override
    public JsonElement save() {
        return new JsonPrimitive(options[value]);
    }

    @Override
    public void load(JsonElement json) {
        if (json == null || !json.isJsonPrimitive()) return;
        String name = json.getAsString();
        for (int i = 0; i < options.length; i++) if (options[i].equals(name)) set(i);
    }
}
