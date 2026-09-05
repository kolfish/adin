package dev.koifih.client.rendering;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import dev.koifih.client.rendering.state.RectRenderState;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2f;

public final class RectRenderer {
    private RectRenderer() {}

    public static void draw(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int radius, int color) {
        draw(graphics, x, y, width, height, radius, color, Pipelines.RECT);
    }

    public static void drawHueBar(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int radius) {
        draw(graphics, x, y, width, height, radius, 0xFFFFFFFF, Pipelines.HUE_BAR);
    }

    public static void drawSaturationValue(GuiGraphicsExtractor graphics, int x, int y, int width, int height,
                                           int radius, int hueColor) {
        draw(graphics, x, y, width, height, radius, hueColor | 0xFF000000, Pipelines.SATURATION_VALUE);
    }

    private static void draw(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int radius, int color,
                             RenderPipeline pipeline) {
        color = Opacity.apply(color);
        if (width <= 0 || height <= 0 || (color >>> 24) == 0) return;
        if (width > Short.MAX_VALUE || height > Short.MAX_VALUE) {
            throw new IllegalArgumentException("Rectangle dimensions exceed the vertex format's range");
        }
        int clampedRadius = Math.clamp(radius, 0, Math.min(width, height) / 2);
        var state = new RectRenderState(new Matrix3x2f(graphics.pose()), x, y, width, height, clampedRadius, color, pipeline);
        GuiRenderQueue.submit(graphics, state);
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
}
