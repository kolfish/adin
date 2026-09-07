package dev.koifih.client.render.gui;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.koifih.client.render.GuiElements;
import dev.koifih.client.render.Opacity;
import dev.koifih.client.render.Pipelines;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2f;

public final class Rects {
    public static final int TOP_LEFT = 1;
    public static final int TOP_RIGHT = 2;
    public static final int BOTTOM_RIGHT = 4;
    public static final int BOTTOM_LEFT = 8;
    public static final int ALL_CORNERS = 15;
    private static final int FILLET = 16;

    private Rects() {}

    public static void draw(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int radius, int color) {
        draw(graphics, x, y, width, height, radius, ALL_CORNERS, color, Pipelines.RECT);
    }

    public static void draw(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int radius, int corners, int color) {
        draw(graphics, x, y, width, height, radius, corners, color, Pipelines.RECT);
    }

    public static void fillet(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int radius, int corner, int color) {
        draw(graphics, x, y, width, height, radius, FILLET | corner, color, Pipelines.RECT);
    }

    public static void draw(GuiGraphicsExtractor graphics, float x, float y, float width, int height,
                            int radius, int color) {
        if (width <= 0 || height <= 0) return;
        graphics.pose().pushMatrix();
        try {
            int vertexWidth = (int) Math.ceil(width);
            graphics.pose().translate(x, y).scale(width / vertexWidth, 1f);
            draw(graphics, 0, 0, vertexWidth, height, radius, color);
        } finally {
            graphics.pose().popMatrix();
        }
    }

    public static void bordered(GuiGraphicsExtractor graphics, float x, float y, float width, float height,
                                float radius, int fill, int border) {
        int r = Math.max(0, Math.round(radius));
        int h = Math.max(1, Math.round(height));
        draw(graphics, x - 1, y - 1, width + 2, h + 2, r + 1, border);
        draw(graphics, x, y, width, h, r, fill);
    }

    public static void drawHueBar(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int radius) {
        draw(graphics, x, y, width, height, radius, ALL_CORNERS, 0xFFFFFFFF, Pipelines.HUE_BAR);
    }

    public static void drawSaturationValue(GuiGraphicsExtractor graphics, int x, int y, int width, int height,
                                           int radius, int hueColor) {
        draw(graphics, x, y, width, height, radius, ALL_CORNERS, hueColor | 0xFF000000, Pipelines.SATURATION_VALUE);
    }

    private static void draw(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int radius, int shape, int color,
                             RenderPipeline pipeline) {
        color = Opacity.apply(color);
        if (width <= 0 || height <= 0 || (color >>> 24) == 0) return;
        if (width > Short.MAX_VALUE || height > Short.MAX_VALUE) {
            throw new IllegalArgumentException("Rectangle dimensions exceed the vertex format's range");
        }
        int limit = (shape & FILLET) != 0 ? Math.max(width, height) : Math.min(width, height) / 2;
        var state = new RectRenderState(new Matrix3x2f(graphics.pose()), x, y, width, height, Math.clamp(radius, 0, limit), shape, color, pipeline);
        GuiElements.submit(graphics, state);
    }
}
