package dev.koifih.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.koifih.client.render.BoxCorners;
import dev.koifih.client.util.MathUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class BoxGeometry {
    private BoxGeometry() {}

    static void fill(PoseStack.Pose pose, VertexConsumer consumer, AABB box, Vec3 camera, int color) {
        for (int[] face : BoxCorners.FACES) {
            for (int corner : face) vertex(pose, consumer, box, camera, corner).setColor(color);
        }
    }

    static void edges(PoseStack.Pose pose, VertexConsumer consumer, AABB box, Vec3 camera, int color, float width) {
        for (int i = 0; i < BoxCorners.EDGES.length; i += 2) {
            int from = BoxCorners.EDGES[i];
            int to = BoxCorners.EDGES[i + 1];
            float dx = (float) (BoxCorners.x(box, to) - BoxCorners.x(box, from));
            float dy = (float) (BoxCorners.y(box, to) - BoxCorners.y(box, from));
            float dz = (float) (BoxCorners.z(box, to) - BoxCorners.z(box, from));
            float length = MathUtil.length(dx, dy, dz);
            if (MathUtil.nearlyZero(length)) continue;
            float nx = dx / length;
            float ny = dy / length;
            float nz = dz / length;
            vertex(pose, consumer, box, camera, from).setNormal(pose, nx, ny, nz).setColor(color).setLineWidth(width);
            vertex(pose, consumer, box, camera, to).setNormal(pose, nx, ny, nz).setColor(color).setLineWidth(width);
        }
    }

    private static VertexConsumer vertex(PoseStack.Pose pose, VertexConsumer consumer, AABB box, Vec3 camera, int corner) {
        return consumer.addVertex(pose, (float) (BoxCorners.x(box, corner) - camera.x),
                (float) (BoxCorners.y(box, corner) - camera.y), (float) (BoxCorners.z(box, corner) - camera.z));
    }
}
