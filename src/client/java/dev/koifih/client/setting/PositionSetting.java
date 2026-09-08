package dev.koifih.client.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Locale;

public final class PositionSetting extends Setting<PositionSetting.Position> {
    public enum Anchor {
        START,
        CENTER,
        END
    }

    public record Position(Anchor horizontal, Anchor vertical, float offsetX, float offsetY) {
        public float x(float width, float screenWidth) {
            return along(horizontal, offsetX, width, screenWidth);
        }

        public float y(float height, float screenHeight) {
            return along(vertical, offsetY, height, screenHeight);
        }

        public static Position of(float x, float y, float width, float height, float screenWidth, float screenHeight) {
            Anchor horizontal = anchor(x + width * 0.5f, screenWidth);
            Anchor vertical = anchor(y + height * 0.5f, screenHeight);
            return new Position(horizontal, vertical, offset(horizontal, x, width, screenWidth), offset(vertical, y, height, screenHeight));
        }

        private static float along(Anchor anchor, float offset, float size, float screen) {
            return switch (anchor) {
                case START -> offset;
                case CENTER -> (screen - size) * 0.5f + offset;
                case END -> screen - size - offset;
            };
        }

        private static Anchor anchor(float center, float screen) {
            if (center < screen / 3f) return Anchor.START;
            return center < screen * 2f / 3f ? Anchor.CENTER : Anchor.END;
        }

        private static float offset(Anchor anchor, float start, float size, float screen) {
            return switch (anchor) {
                case START -> start;
                case CENTER -> start - (screen - size) * 0.5f;
                case END -> screen - start - size;
            };
        }
    }

    public PositionSetting(String id, Position defaultPosition) {
        super(id, defaultPosition);
    }

    @Override
    public JsonElement save() {
        JsonObject json = new JsonObject();
        json.addProperty("horizontal", value.horizontal().name().toLowerCase(Locale.ROOT));
        json.addProperty("vertical", value.vertical().name().toLowerCase(Locale.ROOT));
        json.addProperty("x", value.offsetX());
        json.addProperty("y", value.offsetY());
        return json;
    }

    @Override
    public void load(JsonElement json) {
        if (json == null || !json.isJsonObject()) return;
        JsonObject object = json.getAsJsonObject();
        try {
            Anchor horizontal = Anchor.valueOf(object.get("horizontal").getAsString().toUpperCase(Locale.ROOT));
            Anchor vertical = Anchor.valueOf(object.get("vertical").getAsString().toUpperCase(Locale.ROOT));
            value = new Position(horizontal, vertical, object.get("x").getAsFloat(), object.get("y").getAsFloat());
        } catch (RuntimeException ignored) {
            reset();
        }
    }
}
