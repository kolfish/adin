package dev.koifih.client.feature.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class SliderSetting extends Setting<Integer> {
    private final int min;
    private final int max;

    public SliderSetting(String id, int defaultValue, int min, int max) {
        super(id, defaultValue);
        this.min = min;
        this.max = max;
    }

    public int min() {
        return min;
    }

    public int max() {
        return max;
    }

    @Override
    public void set(Integer value) {
        super.set(Math.clamp(value, min, max));
    }

    @Override
    public JsonElement save() {
        return new JsonPrimitive(value);
    }

    @Override
    public void load(JsonElement json) {
        if (json != null && json.isJsonPrimitive()) set(json.getAsInt());
    }
}
