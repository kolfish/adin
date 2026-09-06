package dev.koifih.client.setting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class IdSetSetting extends Setting<Set<String>> {
    protected IdSetSetting(String id) {
        super(id, new LinkedHashSet<>());
    }

    @Override
    public JsonElement save() {
        JsonArray array = new JsonArray();
        for (String id : value) array.add(id);
        return array;
    }

    @Override
    public void load(JsonElement json) {
        if (json == null || !json.isJsonArray()) return;
        value.clear();
        for (JsonElement element : json.getAsJsonArray()) value.add(element.getAsString());
    }
}
