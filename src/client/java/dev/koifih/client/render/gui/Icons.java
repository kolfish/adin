package dev.koifih.client.render.gui;

import dev.koifih.client.render.GuiElements;
import dev.koifih.client.render.Opacity;
import dev.koifih.client.render.font.Font;
import dev.koifih.client.render.font.Fonts;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import java.util.List;

public final class Icons {
    private static final Font FONT = Fonts.MATERIAL_ICONS;

    private Icons() {}

    public static void draw(GuiGraphicsExtractor graphics, int codepoint, float x, float y, float size, int color) {
        color = Opacity.apply(color);
        if (size <= 0 || !Float.isFinite(size) || (color >>> 24) == 0) return;
        Font.Glyph glyph = FONT.glyph(codepoint);
        Font.Bounds plane = glyph.planeBounds();
        if (plane == null || glyph.atlasBounds() == null) return;
        float centerX = x + size * 0.5f;
        float centerY = y + size * 0.5f;
        float halfWidth = (plane.right() - plane.left()) * size * 0.5f;
        float halfHeight = (plane.bottom() - plane.top()) * size * 0.5f;
        Font.Bounds uv = FONT.uv(glyph);
        var quad = new TextRenderState.Quad(
                centerX - halfWidth, centerY - halfHeight, centerX + halfWidth, centerY + halfHeight,
                uv.left(), uv.top(), uv.right(), uv.bottom(), color);
        GuiElements.submit(graphics, TextRenderState.of(graphics, FONT.texture(), List.of(quad)));
    }
}
