package dev.koifih.client.feature.setting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.util.LinkedHashSet;
import java.util.Set;

public final class MultiSetting extends Setting<Set<Integer>> {
    private final String[] options;

    public MultiSetting(String id, String[] options, int... defaults) {
        super(id, new LinkedHashSet<>());
        this.options = options;
        for (int index : defaults) value.add(index);
    }

    public String[] options() {
        return options;
    }

    @Override
    public JsonElement save() {
        JsonArray array = new JsonArray();
        for (int index : value) array.add(options[index]);
        return array;
    }

    @Override
    public void load(JsonElement json) {
        if (json == null || !json.isJsonArray()) return;
        value.clear();
        for (JsonElement element : json.getAsJsonArray()) {
            for (int i = 0; i < options.length; i++) if (options[i].equals(element.getAsString())) value.add(i);
        }
    }
}
