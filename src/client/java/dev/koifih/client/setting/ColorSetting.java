package dev.koifih.client.setting;

import lombok.Getter;
import lombok.experimental.Accessors;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.koifih.client.util.Colors;
import java.util.function.BooleanSupplier;

@Accessors(fluent = true)
public final class ColorSetting extends Setting<Integer> {
    private final int defaultSecondary;
    @Getter
    private int secondary;
    private BooleanSupplier gradient = () -> false;

    public ColorSetting(String id, int defaultRgb) {
        this(id, defaultRgb, defaultRgb);
    }

    public ColorSetting(String id, int defaultRgb, int defaultSecondaryRgb) {
        super(id, Colors.rgb(defaultRgb));
        this.defaultSecondary = Colors.rgb(defaultSecondaryRgb);
        this.secondary = defaultSecondary;
    }

    public void gradientWhen(BooleanSupplier condition) {
        gradient = condition;
    }

    public boolean isGradient() {
        return gradient.getAsBoolean();
    }

    public void setSecondary(int rgb) {
        secondary = Colors.rgb(rgb);
    }

    @Override
    public void reset() {
        super.reset();
        secondary = defaultSecondary;
    }

    @Override
    public JsonElement save() {
        if (secondary == value) return new JsonPrimitive(Colors.hex(value));
        JsonArray array = new JsonArray();
        array.add(Colors.hex(value));
        array.add(Colors.hex(secondary));
        return array;
    }

    @Override
    public void load(JsonElement json) {
        if (json == null) return;
        if (json.isJsonArray() && json.getAsJsonArray().size() == 2) {
            JsonArray array = json.getAsJsonArray();
            value = parse(array.get(0), value);
            secondary = parse(array.get(1), secondary);
        } else if (json.isJsonPrimitive()) {
            value = parse(json, value);
            secondary = value;
        }
    }

    private static int parse(JsonElement json, int fallback) {
        try {
            return Colors.rgb(Integer.parseInt(json.getAsString().replace("#", ""), 16));
        } catch (NumberFormatException | IllegalStateException exception) {
            return fallback;
        }
    }
}
