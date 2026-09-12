package dev.koifih.client.setting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.world.entity.player.Inventory;
import java.util.LinkedHashSet;
import java.util.Set;

public final class HotbarSetting extends Setting<Set<Integer>> {
    public static final int SLOTS = Inventory.SELECTION_SIZE;

    public HotbarSetting(String id, int... defaults) {
        super(id, new LinkedHashSet<>());
        for (int slot : defaults) value.add(slot);
    }

    public boolean has(int slot) {
        return value.contains(slot);
    }

    public void toggle(int slot) {
        if (slot < 0 || slot >= SLOTS) return;
        if (!value.remove(slot)) value.add(slot);
    }

    @Override
    public JsonElement save() {
        JsonArray array = new JsonArray();
        for (int slot = 0; slot < SLOTS; slot++) if (value.contains(slot)) array.add(slot + 1);
        return array;
    }

    @Override
    public void load(JsonElement json) {
        if (json == null || !json.isJsonArray()) return;
        value.clear();
        for (JsonElement element : json.getAsJsonArray()) {
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) continue;
            int slot = element.getAsInt() - 1;
            if (slot >= 0 && slot < SLOTS) value.add(slot);
        }
    }
}
