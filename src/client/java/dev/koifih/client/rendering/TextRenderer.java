package dev.koifih.client.rendering;

import dev.koifih.client.rendering.font.Fonts;
import dev.koifih.client.utils.Colors;
import dev.koifih.client.rendering.font.MsdfFont;
import dev.koifih.client.rendering.state.TextRenderState;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import java.util.ArrayList;
import java.util.List;

public final class TextRenderer {
    private static final MsdfFont FONT = Fonts.COMFORTAA_BOLD;

    public record Span(String text, int color) {}

    @FunctionalInterface
    private interface AlphaAt {
        float at(float x);
    }

    private TextRenderer() {}

    public static float width(String text, float size) {
        float advance = 0;
        int previous = -1;
        for (int codepoint : text.codePoints().toArray()) {
            MsdfFont.Glyph glyph = FONT.glyph(codepoint);
            advance += FONT.kerning(previous, glyph.unicode()) + glyph.advance();
            previous = glyph.unicode();
        }
        return advance * size;
    }

    private static final MsdfFont.Bounds CAP_BOUNDS = FONT.glyph('H').planeBounds();

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
                glyphX -> Math.clamp((glyphX - transparentX) / width, 0f, 1f));
    }

    public static void draw(GuiGraphicsExtractor graphics, List<Span> spans, float x, float baseline, float size) {
        draw(graphics, spans, x, baseline, size, null);
    }

    private static void draw(GuiGraphicsExtractor graphics, List<Span> spans, float x, float baseline, float size,
                             AlphaAt alpha) {
        if (!Float.isFinite(size) || size <= 0) return;
        List<TextRenderState.Quad> quads = new ArrayList<>();
        float cursor = x;
        int previous = -1;
        for (Span span : spans) {
            int color = Opacity.apply(span.color());
            for (int codepoint : span.text().codePoints().toArray()) {
                MsdfFont.Glyph glyph = FONT.glyph(codepoint);
                cursor += FONT.kerning(previous, glyph.unicode()) * size;
                MsdfFont.Bounds plane = glyph.planeBounds();
                if (plane != null && glyph.atlasBounds() != null && (color >>> 24) != 0) {
                    MsdfFont.Bounds uv = FONT.uv(glyph);
                    float left = cursor + plane.left() * size;
                    float right = cursor + plane.right() * size;
                    int leftColor = alpha == null ? color : Colors.withAlpha(color, alpha.at(left));
                    int rightColor = alpha == null ? color : Colors.withAlpha(color, alpha.at(right));
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
        GuiRenderQueue.submit(graphics, TextRenderState.of(graphics, FONT.texture(), quads));
    }
}
