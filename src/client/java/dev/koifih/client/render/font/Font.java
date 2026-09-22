package dev.koifih.client.render.font;

import com.google.gson.Gson;
import dev.koifih.Adin;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import net.minecraft.resources.Identifier;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class Font {
    private static final String ASSET_ROOT = "/assets/adin/";

    private final Identifier texture;
    private final Atlas atlas;
    private final Int2ObjectOpenHashMap<Glyph> glyphs = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectOpenHashMap<Bounds> uvs = new Int2ObjectOpenHashMap<>();
    private final Long2FloatOpenHashMap kerning = new Long2FloatOpenHashMap();

    public record Bounds(float left, float top, float right, float bottom) {}
    public record Glyph(int unicode, float advance, Bounds planeBounds, Bounds atlasBounds) {}
    private record Atlas(String type, float distanceRange, int width, int height, String yOrigin) {}
    private record KerningPair(int unicode1, int unicode2, float advance) {}
    private record Metrics(Atlas atlas, Glyph[] glyphs, KerningPair[] kerning) {}

    private Font(Metrics metrics, Identifier texture, int fallbackCodepoint) {
        this.texture = texture;
        this.atlas = metrics.atlas;
        if (!"msdf".equals(atlas.type) || !"top".equals(atlas.yOrigin)) {
            throw new IllegalStateException("Expected a top-origin MSDF font atlas for " + texture);
        }
        for (Glyph glyph : metrics.glyphs) {
            glyphs.put(glyph.unicode, glyph);
            if (glyph.atlasBounds != null) uvs.put(glyph.unicode, normalized(glyph.atlasBounds));
        }
        if (!glyphs.containsKey(fallbackCodepoint)) {
            throw new IllegalStateException("Missing fallback glyph in " + texture);
        }
        glyphs.defaultReturnValue(glyphs.get(fallbackCodepoint));
        if (metrics.kerning != null) {
            for (KerningPair pair : metrics.kerning) {
                kerning.put(pairKey(pair.unicode1, pair.unicode2), pair.advance);
            }
        }
    }

    public static Font load(String name, int fallbackCodepoint) {
        String path = ASSET_ROOT + name + ".json";
        Identifier texture = Adin.id(name + ".png");
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
        return glyphs.get(codepoint);
    }

    public float kerning(int previous, int current) {
        return kerning.isEmpty() ? 0f : kerning.get(pairKey(previous, current));
    }

    public Bounds uv(Glyph glyph) {
        return uvs.get(glyph.unicode());
    }

    private Bounds normalized(Bounds area) {
        return new Bounds(area.left() / atlas.width, area.top() / atlas.height,
                area.right() / atlas.width, area.bottom() / atlas.height);
    }

    private static long pairKey(int first, int second) {
        return ((long) first << 32) | (second & 0xffffffffL);
    }
}
