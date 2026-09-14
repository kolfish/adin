package dev.koifih.client.render.font;

import com.mojang.blaze3d.platform.NativeImage;
import dev.koifih.client.util.Colors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.io.InputStream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GlyphImage {
    @FunctionalInterface
    public interface Mask {
        float at(float x, float y);
    }

    public static NativeImage atlas(String path) {
        try (InputStream stream = GlyphImage.class.getResourceAsStream(path)) {
            if (stream == null) throw new IllegalStateException("Missing atlas: " + path);
            return NativeImage.read(stream);
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot load atlas: " + path, exception);
        }
    }

    public static float width(Font font, String text, float emPx) {
        float total = 0;
        int previous = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            total += (font.glyph(c).advance() + font.kerning(previous, c)) * emPx;
            previous = c;
        }
        return total;
    }

    public static void draw(NativeImage target, Font font, NativeImage atlas, Font.Glyph glyph, float penX,
                            float baselineY, float emPx, int color, float alphaMul, float glyphScale, Mask mask) {
        Font.Bounds plane = glyph.planeBounds();
        Font.Bounds area = glyph.atlasBounds();
        if (plane == null || area == null || alphaMul <= 0) return;
        float pxPerEm = (area.right() - area.left()) / (plane.right() - plane.left());
        float rangePx = font.distanceRange() / pxPerEm * emPx * glyphScale;
        int x0 = Math.max(0, (int) Math.floor(penX + plane.left() * emPx));
        int x1 = Math.min(target.getWidth(), (int) Math.ceil(penX + plane.right() * emPx));
        int y0 = Math.max(0, (int) Math.floor(baselineY + plane.top() * emPx));
        int y1 = Math.min(target.getHeight(), (int) Math.ceil(baselineY + plane.bottom() * emPx));
        for (int py = y0; py < y1; py++) {
            for (int px = x0; px < x1; px++) {
                float planeX = (px + 0.5f - penX) / emPx;
                float planeY = (py + 0.5f - baselineY) / emPx;
                float coverage = mask == null ? 1f : mask.at(planeX, planeY);
                if (coverage <= 0) continue;
                float gx = 0.5f + (planeX - 0.5f) / glyphScale;
                float gy = 0.5f + (planeY - 0.5f) / glyphScale;
                if (gx < plane.left() || gx > plane.right() || gy < plane.top() || gy > plane.bottom()) continue;
                float u = area.left() + (gx - plane.left()) / (plane.right() - plane.left()) * (area.right() - area.left());
                float v = area.top() + (gy - plane.top()) / (plane.bottom() - plane.top()) * (area.bottom() - area.top());
                float distance = (distanceAt(atlas, u, v) - 0.5f) * rangePx;
                float alpha = Math.clamp(distance + 0.5f, 0f, 1f) * coverage * alphaMul;
                if (alpha <= 0) continue;
                target.setPixel(px, py, Colors.lerp(target.getPixel(px, py), color, alpha));
            }
        }
    }

    private static float distanceAt(NativeImage atlas, float x, float y) {
        int x0 = Math.clamp((int) Math.floor(x - 0.5f), 0, atlas.getWidth() - 2);
        int y0 = Math.clamp((int) Math.floor(y - 0.5f), 0, atlas.getHeight() - 2);
        float fx = Math.clamp(x - 0.5f - x0, 0f, 1f);
        float fy = Math.clamp(y - 0.5f - y0, 0f, 1f);
        float m00 = median(atlas.getPixel(x0, y0));
        float m10 = median(atlas.getPixel(x0 + 1, y0));
        float m01 = median(atlas.getPixel(x0, y0 + 1));
        float m11 = median(atlas.getPixel(x0 + 1, y0 + 1));
        return (m00 * (1 - fx) + m10 * fx) * (1 - fy) + (m01 * (1 - fx) + m11 * fx) * fy;
    }

    private static float median(int pixel) {
        float r = ((pixel >> 16) & 255) / 255f;
        float g = ((pixel >> 8) & 255) / 255f;
        float b = (pixel & 255) / 255f;
        return Math.max(Math.min(r, g), Math.min(Math.max(r, g), b));
    }
}
