package dev.koifih.client.render.gui;

import dev.koifih.client.render.GuiElements;
import dev.koifih.client.render.Opacity;
import dev.koifih.client.render.font.Font;
import dev.koifih.client.render.font.Fonts;
import dev.koifih.client.util.Colors;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import java.util.ArrayList;
import java.util.List;

public final class Text {
    private static final Font FONT = Fonts.COMFORTAA_BOLD;

    public record Span(String text, int color) {}

    public record Ink(float left, float right) {
        public float width() {
            return right - left;
        }
    }

    @FunctionalInterface
    public interface ColorAt {
        int at(float x);
    }

    private Text() {}

    public static float width(String text, float size) {
        float advance = 0;
        int previous = -1;
        for (int codepoint : text.codePoints().toArray()) {
            Font.Glyph glyph = FONT.glyph(codepoint);
            advance += FONT.kerning(previous, glyph.unicode()) + glyph.advance();
            previous = glyph.unicode();
        }
        return advance * size;
    }

    public static Ink ink(String text, float size) {
        float advance = 0;
        int previous = -1;
        float left = Float.MAX_VALUE;
        float right = -Float.MAX_VALUE;
        for (int codepoint : text.codePoints().toArray()) {
            Font.Glyph glyph = FONT.glyph(codepoint);
            advance += FONT.kerning(previous, glyph.unicode());
            Font.Bounds plane = glyph.planeBounds();
            if (plane != null) {
                left = Math.min(left, advance + plane.left());
                right = Math.max(right, advance + plane.right());
            }
            advance += glyph.advance();
            previous = glyph.unicode();
        }
        return left > right ? new Ink(0f, 0f) : new Ink(left * size, right * size);
    }

    private static final Font.Bounds CAP_BOUNDS = FONT.glyph('H').planeBounds();

    public static float centeredBaseline(String text, float size, float centerY) {
        return centerY - (CAP_BOUNDS.top() + CAP_BOUNDS.bottom()) * size * 0.5f;
    }

    public static void draw(GuiGraphicsExtractor graphics, String text, float x, float baseline, float size, int color) {
        draw(graphics, List.of(new Span(text, color)), x, baseline, size);
    }

    public static void drawCentered(GuiGraphicsExtractor graphics, String text, float x, float centerY,
                                    float size, int color) {
        draw(graphics, text, x, centeredBaseline(text, size, centerY), size, color);
    }

    public static void drawCentered(GuiGraphicsExtractor graphics, String text, float x, float centerY,
                                    float size, ColorAt color) {
        draw(graphics, List.of(new Span(text, 0xFFFFFFFF)), x, centeredBaseline(text, size, centerY), size, color);
    }

    public static void drawCenteredX(GuiGraphicsExtractor graphics, String text, float centerX, float centerY,
                                     float size, int color) {
        drawCentered(graphics, text, centerX - width(text, size) / 2, centerY, size, color);
    }

    public static String fit(String text, float width, float size) {
        if (width(text, size) <= width) return text;
        while (!text.isEmpty() && width(text + "...", size) > width) {
            text = text.substring(0, text.offsetByCodePoints(text.length(), -1));
        }
        return text + "...";
    }

    public static void drawFaded(GuiGraphicsExtractor graphics, String text, float x, float baseline, float size,
                                 int color, float transparentX, float opaqueX) {
        float fadeWidth = opaqueX - transparentX;
        if (fadeWidth == 0f) fadeWidth = 0.001f;
        float width = fadeWidth;
        draw(graphics, List.of(new Span(text, color)), x, baseline, size,
                glyphX -> Colors.withAlpha(color, Math.clamp((glyphX - transparentX) / width, 0f, 1f)));
    }

    public static void draw(GuiGraphicsExtractor graphics, List<Span> spans, float x, float baseline, float size) {
        draw(graphics, spans, x, baseline, size, null);
    }

    private static void draw(GuiGraphicsExtractor graphics, List<Span> spans, float x, float baseline, float size,
                             ColorAt colorAt) {
        if (!Float.isFinite(size) || size <= 0) return;
        List<TextRenderState.Quad> quads = new ArrayList<>();
        float cursor = x;
        int previous = -1;
        for (Span span : spans) {
            int color = Opacity.apply(span.color());
            for (int codepoint : span.text().codePoints().toArray()) {
                Font.Glyph glyph = FONT.glyph(codepoint);
                cursor += FONT.kerning(previous, glyph.unicode()) * size;
                Font.Bounds plane = glyph.planeBounds();
                if (plane != null && glyph.atlasBounds() != null && (color >>> 24) != 0) {
                    Font.Bounds uv = FONT.uv(glyph);
                    float left = cursor + plane.left() * size;
                    float right = cursor + plane.right() * size;
                    int leftColor = colorAt == null ? color : Opacity.apply(colorAt.at(left));
                    int rightColor = colorAt == null ? color : Opacity.apply(colorAt.at(right));
                    if ((leftColor >>> 24) != 0 || (rightColor >>> 24) != 0) {
                        quads.add(new TextRenderState.Quad(
                                left, baseline + plane.top() * size, right, baseline + plane.bottom() * size,
                                uv.left(), uv.top(), uv.right(), uv.bottom(), leftColor, rightColor));
                    }
                }
                cursor += glyph.advance() * size;
                previous = glyph.unicode();
            }
        }
        if (quads.isEmpty()) return;
        GuiElements.submit(graphics, TextRenderState.of(graphics, FONT.texture(), quads));
    }
}
