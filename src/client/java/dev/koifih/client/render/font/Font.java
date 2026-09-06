package dev.koifih.client.render.font;

import com.google.gson.Gson;
import net.minecraft.resources.Identifier;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class Font {
    private static final String METRICS_DIRECTORY = "/assets/adin/font/";

    private final Identifier texture;
    private final int fallbackCodepoint;
    private final Atlas atlas;
    private final Map<Integer, Glyph> glyphs = new HashMap<>();
    private final Map<Long, Float> kerning = new HashMap<>();

    public record Bounds(float left, float top, float right, float bottom) {}
    public record Glyph(int unicode, float advance, Bounds planeBounds, Bounds atlasBounds) {}
    private record Atlas(String type, float distanceRange, int width, int height, String yOrigin) {}
    private record KerningPair(int unicode1, int unicode2, float advance) {}
    private record Metrics(Atlas atlas, Glyph[] glyphs, KerningPair[] kerning) {}

    private Font(Metrics metrics, Identifier texture, int fallbackCodepoint) {
        this.texture = texture;
        this.fallbackCodepoint = fallbackCodepoint;
        this.atlas = metrics.atlas;
        if (!"msdf".equals(atlas.type) || !"top".equals(atlas.yOrigin)) {
            throw new IllegalStateException("Expected a top-origin MSDF font atlas for " + texture);
        }
        for (Glyph glyph : metrics.glyphs) glyphs.put(glyph.unicode, glyph);
        if (!glyphs.containsKey(fallbackCodepoint)) {
            throw new IllegalStateException("Missing fallback glyph in " + texture);
        }
        if (metrics.kerning != null) {
            for (KerningPair pair : metrics.kerning) {
                kerning.put(pairKey(pair.unicode1, pair.unicode2), pair.advance);
            }
        }
    }

    public static Font load(String name, Identifier texture, int fallbackCodepoint) {
        String path = METRICS_DIRECTORY + name + ".json";
        try (var stream = Font.class.getResourceAsStream(path)) {
            if (stream == null) throw new IllegalStateException("Missing font metrics: " + path);
            try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                return new Font(new Gson().fromJson(reader, Metrics.class), texture, fallbackCodepoint);
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot load font metrics: " + path, exception);
        }
    }

    public Identifier texture() {
        return texture;
    }

    public float distanceRange() {
        return atlas.distanceRange;
    }

    public Glyph glyph(int codepoint) {
        return glyphs.getOrDefault(codepoint, glyphs.get(fallbackCodepoint));
    }

    public float kerning(int previous, int current) {
        return kerning.getOrDefault(pairKey(previous, current), 0f);
    }

    public Bounds uv(Glyph glyph) {
        Bounds area = glyph.atlasBounds();
        return new Bounds(area.left() / atlas.width, area.top() / atlas.height,
                area.right() / atlas.width, area.bottom() / atlas.height);
    }

    private static long pairKey(int first, int second) {
        return ((long) first << 32) | (second & 0xffffffffL);
    }
}
