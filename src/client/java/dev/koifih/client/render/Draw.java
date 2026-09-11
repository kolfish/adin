package dev.koifih.client.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.koifih.client.render.font.Font;
import dev.koifih.client.render.font.Fonts;
import dev.koifih.client.render.state.Pipelines;
import dev.koifih.client.render.state.RectState;
import dev.koifih.client.render.state.Submit;
import dev.koifih.client.render.state.TextState;
import dev.koifih.client.util.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2f;
import java.util.ArrayList;
import java.util.List;

public final class Draw {
    public static final int TOP_LEFT = 1;
    public static final int TOP_RIGHT = 2;
    public static final int BOTTOM_RIGHT = 4;
    public static final int BOTTOM_LEFT = 8;
    public static final int ALL_CORNERS = 15;

    private static final int FILLET = 16;
    private static final float SMALL_LOGO_PIXELS = 40f;
    private static final int LOGO_STAR = 1;
    private static final int LOGO_BAND_BACK = 2;
    private static final int LOGO_BAND_FRONT = 3;
    private static final int LOGO_BACK_COLOR = 0xFFA3A3A3;
    private static final int LOGO_FRONT_COLOR = 0xFFEAEAEA;

    private Draw() {}

    public static void rect(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int radius, int color) {
        rect(graphics, x, y, width, height, radius, ALL_CORNERS, color, Pipelines.RECT);
    }

    public static void rect(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int radius,
                            int corners, int color) {
        rect(graphics, x, y, width, height, radius, corners, color, Pipelines.RECT);
    }

    public static void rect(GuiGraphicsExtractor graphics, float x, float y, float width, int height,
                            int radius, int color) {
        if (width <= 0 || height <= 0) return;
        graphics.pose().pushMatrix();
        try {
            int vertexWidth = (int) Math.ceil(width);
            graphics.pose().translate(x, y).scale(width / vertexWidth, 1f);
            rect(graphics, 0, 0, vertexWidth, height, radius, color);
        } finally {
            graphics.pose().popMatrix();
        }
    }

    public static void fillet(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int radius,
                              int corner, int color) {
        rect(graphics, x, y, width, height, radius, FILLET | corner, color, Pipelines.RECT);
    }

    public static void gradient(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int radius,
                                int corners, int top, int bottom) {
        rect(graphics, x, y, width, height, radius, corners, top, bottom, Pipelines.RECT);
    }

    public static void bordered(GuiGraphicsExtractor graphics, float x, float y, float width, float height,
                                float radius, int fill, int border) {
        int r = Math.max(0, Math.round(radius));
        int h = Math.max(1, Math.round(height));
        rect(graphics, x - 1, y - 1, width + 2, h + 2, r + 1, border);
        rect(graphics, x, y, width, h, r, fill);
    }

    public static void hueBar(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int radius) {
        rect(graphics, x, y, width, height, radius, ALL_CORNERS, 0xFFFFFFFF, Pipelines.HUE_BAR);
    }

    public static void saturationValue(GuiGraphicsExtractor graphics, int x, int y, int width, int height,
                                       int radius, int hueColor) {
        rect(graphics, x, y, width, height, radius, ALL_CORNERS, Colors.opaque(hueColor), Pipelines.SATURATION_VALUE);
    }

    public static void icon(GuiGraphicsExtractor graphics, int codepoint, float x, float y, float size, int color) {
        color = Opacity.apply(color);
        if (size <= 0 || !Float.isFinite(size) || Colors.transparent(color)) return;
        Font font = Fonts.MATERIAL_ICONS;
        Font.Glyph glyph = font.glyph(codepoint);
        Font.Bounds plane = glyph.planeBounds();
        if (plane == null || glyph.atlasBounds() == null) return;
        float centerX = x + size * 0.5f;
        float centerY = y + size * 0.5f;
        float halfWidth = (plane.right() - plane.left()) * size * 0.5f;
        float halfHeight = (plane.bottom() - plane.top()) * size * 0.5f;
        Font.Bounds uv = font.uv(glyph);
        var quad = new TextState.Quad(
                centerX - halfWidth, centerY - halfHeight, centerX + halfWidth, centerY + halfHeight,
                uv.left(), uv.top(), uv.right(), uv.bottom(), color);
        Submit.submit(graphics, TextState.of(graphics, font.texture(), List.of(quad)));
    }

    public static void logo(GuiGraphicsExtractor graphics, float x, float y, float size, int starColor) {
        if (size <= 0 || !Float.isFinite(size)) return;
        Font font = size * Minecraft.getInstance().getWindow().getGuiScale() <= SMALL_LOGO_PIXELS
                ? Fonts.LOGO_SMALL : Fonts.LOGO;
        List<TextState.Quad> quads = new ArrayList<>();
        logoLayer(quads, font, LOGO_BAND_BACK, x, y, size, LOGO_BACK_COLOR);
        logoLayer(quads, font, LOGO_STAR, x, y, size, starColor);
        logoLayer(quads, font, LOGO_BAND_FRONT, x, y, size, LOGO_FRONT_COLOR);
        Submit.submit(graphics, TextState.of(graphics, font.texture(), quads));
    }

    private static void logoLayer(List<TextState.Quad> quads, Font font, int codepoint, float x, float y,
                                  float size, int color) {
        color = Opacity.apply(color);
        if (Colors.transparent(color)) return;
        Font.Glyph glyph = font.glyph(codepoint);
        Font.Bounds plane = glyph.planeBounds();
        Font.Bounds uv = font.uv(glyph);
        quads.add(new TextState.Quad(x + plane.left() * size, y + plane.top() * size,
                x + plane.right() * size, y + plane.bottom() * size,
                uv.left(), uv.top(), uv.right(), uv.bottom(), color));
    }

    private static void rect(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int radius,
                             int shape, int color, RenderPipeline pipeline) {
        rect(graphics, x, y, width, height, radius, shape, color, color, pipeline);
    }

    private static void rect(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int radius,
                             int shape, int color, int bottomColor, RenderPipeline pipeline) {
        color = Opacity.apply(color);
        bottomColor = Opacity.apply(bottomColor);
        if (width <= 0 || height <= 0 || (Colors.transparent(color) && Colors.transparent(bottomColor))) return;
        if (width > Short.MAX_VALUE || height > Short.MAX_VALUE) {
            throw new IllegalArgumentException("Rectangle dimensions exceed the vertex format's range");
        }
        int limit = (shape & FILLET) != 0 ? Math.max(width, height) : Math.min(width, height) / 2;
        Submit.submit(graphics, new RectState(new Matrix3x2f(graphics.pose()), x, y, width, height,
                Math.clamp(radius, 0, limit), shape, color, bottomColor, pipeline));
    }
}
