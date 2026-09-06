package dev.koifih.client.rendering.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

final class BoxGeometry {
    private BoxGeometry() {}

    static void fill(PoseStack.Pose pose, VertexConsumer consumer, float x0, float y0, float z0,
                     float x1, float y1, float z1, int color) {
        quad(pose, consumer, color, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1);
        quad(pose, consumer, color, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0);
        quad(pose, consumer, color, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0);
        quad(pose, consumer, color, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1);
        quad(pose, consumer, color, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0);
        quad(pose, consumer, color, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1);
    }

    static void edges(PoseStack.Pose pose, VertexConsumer consumer, float x0, float y0, float z0,
                      float x1, float y1, float z1, int color, float width) {
        line(pose, consumer, color, width, x0, y0, z0, x1, y0, z0);
        line(pose, consumer, color, width, x0, y0, z1, x1, y0, z1);
        line(pose, consumer, color, width, x0, y1, z0, x1, y1, z0);
        line(pose, consumer, color, width, x0, y1, z1, x1, y1, z1);
        line(pose, consumer, color, width, x0, y0, z0, x0, y1, z0);
        line(pose, consumer, color, width, x1, y0, z0, x1, y1, z0);
        line(pose, consumer, color, width, x0, y0, z1, x0, y1, z1);
        line(pose, consumer, color, width, x1, y0, z1, x1, y1, z1);
        line(pose, consumer, color, width, x0, y0, z0, x0, y0, z1);
        line(pose, consumer, color, width, x1, y0, z0, x1, y0, z1);
        line(pose, consumer, color, width, x0, y1, z0, x0, y1, z1);
        line(pose, consumer, color, width, x1, y1, z0, x1, y1, z1);
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer consumer, int color,
                             float ax, float ay, float az, float bx, float by, float bz,
                             float cx, float cy, float cz, float dx, float dy, float dz) {
        consumer.addVertex(pose, ax, ay, az).setColor(color);
        consumer.addVertex(pose, bx, by, bz).setColor(color);
        consumer.addVertex(pose, cx, cy, cz).setColor(color);
        consumer.addVertex(pose, dx, dy, dz).setColor(color);
    }

    private static void line(PoseStack.Pose pose, VertexConsumer consumer, int color, float width,
                             float ax, float ay, float az, float bx, float by, float bz) {
        float dx = bx - ax;
        float dy = by - ay;
        float dz = bz - az;
        float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length <= 0f) return;
        float nx = dx / length;
        float ny = dy / length;
        float nz = dz / length;
        consumer.addVertex(pose, ax, ay, az).setNormal(pose, nx, ny, nz).setColor(color).setLineWidth(width);
        consumer.addVertex(pose, bx, by, bz).setNormal(pose, nx, ny, nz).setColor(color).setLineWidth(width);
    }
}
