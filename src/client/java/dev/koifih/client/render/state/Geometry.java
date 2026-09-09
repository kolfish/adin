package dev.koifih.client.render.screen;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.koifih.client.util.MathUtil;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;

final class ScreenGeometry {
    static final float PADDING = 1f;
    private static final float FIXED_POINT = 8f;

    private ScreenGeometry() {}

    static ScreenRectangle bounds(ScreenRect rect, Matrix3x2fc pose) {
        ScreenRect outer = rect.expand(PADDING);
        return new ScreenRectangle((int) Math.floor(outer.minX()), (int) Math.floor(outer.minY()),
                (int) Math.ceil(outer.width()) + 1, (int) Math.ceil(outer.height()) + 1).transformMaxBounds(pose);
    }

    static void fill(VertexConsumer consumer, Matrix3x2fc pose, ScreenRect rect, int color) {
        shape(consumer, pose, rect.minX(), rect.minY(), rect.width(), rect.height(), 0f, color);
    }

    static void border(VertexConsumer consumer, Matrix3x2fc pose, ScreenRect rect, float width, int color) {
        ScreenRect outer = rect.expand(width * 0.5f);
        shape(consumer, pose, outer.minX(), outer.minY(), outer.width(), outer.height(), width, color);
    }

    static void corners(VertexConsumer consumer, Matrix3x2fc pose, ScreenRect rect, float width, float length, int color) {
        ScreenRect outer = rect.expand(width * 0.5f);
        float reach = Math.min(length, Math.min(outer.width(), outer.height()) * 0.5f);
        float x0 = outer.minX();
        float y0 = outer.minY();
        float x1 = outer.maxX();
        float y1 = outer.maxY();
        shape(consumer, pose, x0, y0, reach, width, 0f, color);
        shape(consumer, pose, x0, y0, width, reach, 0f, color);
        shape(consumer, pose, x1 - reach, y0, reach, width, 0f, color);
        shape(consumer, pose, x1 - width, y0, width, reach, 0f, color);
        shape(consumer, pose, x0, y1 - width, reach, width, 0f, color);
        shape(consumer, pose, x0, y1 - reach, width, reach, 0f, color);
        shape(consumer, pose, x1 - reach, y1 - width, reach, width, 0f, color);
        shape(consumer, pose, x1 - width, y1 - reach, width, reach, 0f, color);
    }

    static void line(VertexConsumer consumer, Matrix3x2fc pose, float x0, float y0, float x1, float y1, float width, int color) {
        float dx = x1 - x0;
        float dy = y1 - y0;
        float length = MathUtil.length(dx, dy);
        if (MathUtil.nearlyZero(length)) return;
        Matrix3x2f along = new Matrix3x2f(pose).translate(x0, y0).rotate((float) Math.atan2(dy, dx));
        shape(consumer, along, 0f, -width * 0.5f, length, width, 0f, color);
    }

    private static void shape(VertexConsumer consumer, Matrix3x2fc pose, float x, float y, float width, float height,
                              float border, int color) {
        if (width <= 0f || height <= 0f) return;
        int sizeX = fixed(width);
        int sizeY = fixed(height);
        int ring = fixed(border);
        vertex(consumer, pose, x, y, -PADDING, -PADDING, sizeX, sizeY, ring, color);
        vertex(consumer, pose, x, y, -PADDING, height + PADDING, sizeX, sizeY, ring, color);
        vertex(consumer, pose, x, y, width + PADDING, height + PADDING, sizeX, sizeY, ring, color);
        vertex(consumer, pose, x, y, width + PADDING, -PADDING, sizeX, sizeY, ring, color);
    }

    private static void vertex(VertexConsumer consumer, Matrix3x2fc pose, float x, float y, float localX, float localY,
                               int sizeX, int sizeY, int ring, int color) {
        consumer.addVertexWith2DPose(pose, x + localX, y + localY)
                .setColor(color)
                .setUv(localX, localY)
                .setUv1(ring, 0)
                .setUv2(sizeX, sizeY);
    }

    private static int fixed(float value) {
        return Math.clamp(Math.round(value * FIXED_POINT), 0, Short.MAX_VALUE);
    }
}
