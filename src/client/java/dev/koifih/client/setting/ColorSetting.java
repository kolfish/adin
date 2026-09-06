package dev.koifih.client.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class ColorSetting extends Setting<Integer> {
    public ColorSetting(String id, int defaultRgb) {
        super(id, defaultRgb & 0xFFFFFF);
    }

    @Override
    public JsonElement save() {
        return new JsonPrimitive(String.format("#%06X", value));
    }

    @Override
    public void load(JsonElement json) {
        if (json == null || !json.isJsonPrimitive()) return;
        try {
            value = Integer.parseInt(json.getAsString().replace("#", ""), 16) & 0xFFFFFF;
        } catch (NumberFormatException ignored) {
        }
    }
}
