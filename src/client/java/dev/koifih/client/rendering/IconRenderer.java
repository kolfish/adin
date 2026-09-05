package dev.koifih.client.rendering;

import dev.koifih.client.rendering.font.Fonts;
import dev.koifih.client.rendering.font.MsdfFont;
import dev.koifih.client.rendering.state.TextRenderState;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import java.util.List;

public final class IconRenderer {
    private static final MsdfFont FONT = Fonts.MATERIAL_ICONS;

    private IconRenderer() {}

    public static void draw(GuiGraphicsExtractor graphics, int codepoint, float x, float y, float size, int color) {
        color = Opacity.apply(color);
        if (size <= 0 || !Float.isFinite(size) || (color >>> 24) == 0) return;
        MsdfFont.Glyph glyph = FONT.glyph(codepoint);
        MsdfFont.Bounds plane = glyph.planeBounds();
        if (plane == null || glyph.atlasBounds() == null) return;
        float centerX = x + size * 0.5f;
        float centerY = y + size * 0.5f;
        float halfWidth = (plane.right() - plane.left()) * size * 0.5f;
        float halfHeight = (plane.bottom() - plane.top()) * size * 0.5f;
        MsdfFont.Bounds uv = FONT.uv(glyph);
        var quad = new TextRenderState.Quad(
                centerX - halfWidth, centerY - halfHeight, centerX + halfWidth, centerY + halfHeight,
                uv.left(), uv.top(), uv.right(), uv.bottom(), color);
        GuiRenderQueue.submit(graphics, TextRenderState.of(graphics, FONT.texture(), List.of(quad)));
    }
}
