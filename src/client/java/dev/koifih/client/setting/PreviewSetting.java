package dev.koifih.client.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

public abstract class PreviewSetting extends Setting<Void> {
    protected PreviewSetting(String id) {
        super(id, null);
    }

    @Override
    public JsonElement save() {
        return JsonNull.INSTANCE;
    }

    @Override
    public void load(JsonElement json) {
    }
}
