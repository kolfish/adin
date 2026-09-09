package dev.koifih.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.koifih.client.render.Corners;
import dev.koifih.client.util.Maths;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class BoxGeometry {
    private BoxGeometry() {}

    static void fill(PoseStack.Pose pose, VertexConsumer consumer, AABB box, Vec3 camera, int color) {
        for (int[] face : Corners.FACES) {
            for (int corner : face) vertex(pose, consumer, box, camera, corner).setColor(color);
        }
    }

    static void edges(PoseStack.Pose pose, VertexConsumer consumer, AABB box, Vec3 camera, int color, float width) {
        for (int i = 0; i < Corners.EDGES.length; i += 2) {
            int from = Corners.EDGES[i];
            int to = Corners.EDGES[i + 1];
            float dx = (float) (Corners.x(box, to) - Corners.x(box, from));
            float dy = (float) (Corners.y(box, to) - Corners.y(box, from));
            float dz = (float) (Corners.z(box, to) - Corners.z(box, from));
            float length = Maths.length(dx, dy, dz);
            if (Maths.nearlyZero(length)) continue;
            float nx = dx / length;
            float ny = dy / length;
            float nz = dz / length;
            vertex(pose, consumer, box, camera, from).setNormal(pose, nx, ny, nz).setColor(color).setLineWidth(width);
            vertex(pose, consumer, box, camera, to).setNormal(pose, nx, ny, nz).setColor(color).setLineWidth(width);
        }
    }

    private static VertexConsumer vertex(PoseStack.Pose pose, VertexConsumer consumer, AABB box, Vec3 camera, int corner) {
        return consumer.addVertex(pose, (float) (Corners.x(box, corner) - camera.x),
                (float) (Corners.y(box, corner) - camera.y), (float) (Corners.z(box, corner) - camera.z));
    }
}
