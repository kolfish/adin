package dev.koifih.client.render.gui;

import dev.koifih.client.render.GuiElements;
import dev.koifih.client.render.Opacity;
import dev.koifih.client.render.font.Font;
import dev.koifih.client.render.font.Fonts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import java.util.ArrayList;
import java.util.List;

public final class Logo {
    private static final float SMALL_PIXELS = 40f;
    private static final int STAR = 1;
    private static final int BAND_BACK = 2;
    private static final int BAND_FRONT = 3;
    private static final int BACK_COLOR = 0xFFA3A3A3;
    private static final int FRONT_COLOR = 0xFFEAEAEA;

    private Logo() {}

    public static void draw(GuiGraphicsExtractor graphics, float x, float y, float size, int starColor) {
        if (size <= 0 || !Float.isFinite(size)) return;
        Font font = size * Minecraft.getInstance().getWindow().getGuiScale() <= SMALL_PIXELS ? Fonts.LOGO_SMALL : Fonts.LOGO;
        List<TextRenderState.Quad> quads = new ArrayList<>();
        layer(quads, font, BAND_BACK, x, y, size, BACK_COLOR);
        layer(quads, font, STAR, x, y, size, starColor);
        layer(quads, font, BAND_FRONT, x, y, size, FRONT_COLOR);
        GuiElements.submit(graphics, TextRenderState.of(graphics, font.texture(), quads));
    }

    private static void layer(List<TextRenderState.Quad> quads, Font font, int codepoint, float x, float y, float size, int color) {
        color = Opacity.apply(color);
        if ((color >>> 24) == 0) return;
        Font.Glyph glyph = font.glyph(codepoint);
        Font.Bounds plane = glyph.planeBounds();
        Font.Bounds uv = font.uv(glyph);
        quads.add(new TextRenderState.Quad(x + plane.left() * size, y + plane.top() * size, x + plane.right() * size, y + plane.bottom() * size,
                uv.left(), uv.top(), uv.right(), uv.bottom(), color));
    }
}
