package dev.koifih.client.render.cape;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClothMesh {
    private static final int COLS = ClothSimulation.COLS;
    private static final int ROWS = ClothSimulation.ROWS;

    private static final float[] normalX = new float[ROWS * COLS];
    private static final float[] normalY = new float[ROWS * COLS];
    private static final float[] normalZ = new float[ROWS * COLS];

    static void emit(ClothSimulation cloth, PoseStack.Pose entry, VertexConsumer consumer, int light) {
        computeNormals(cloth);
        for (int row = 0; row + 1 < ROWS; row++) {
            for (int column = 0; column + 1 < COLS; column++) {
                vertex(cloth, entry, consumer, light, ClothSimulation.index(row, column), u(column), v(row));
                vertex(cloth, entry, consumer, light, ClothSimulation.index(row + 1, column), u(column), v(row + 1));
                vertex(cloth, entry, consumer, light, ClothSimulation.index(row + 1, column + 1), u(column + 1), v(row + 1));
                vertex(cloth, entry, consumer, light, ClothSimulation.index(row, column + 1), u(column + 1), v(row));
            }
        }
    }

    private static void computeNormals(ClothSimulation cloth) {
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLS; column++) {
                int i = ClothSimulation.index(row, column);
                int left = ClothSimulation.index(row, Math.max(column - 1, 0));
                int right = ClothSimulation.index(row, Math.min(column + 1, COLS - 1));
                int up = ClothSimulation.index(Math.max(row - 1, 0), column);
                int down = ClothSimulation.index(Math.min(row + 1, ROWS - 1), column);
                float ax = cloth.x(right) - cloth.x(left);
                float ay = cloth.y(right) - cloth.y(left);
                float az = cloth.z(right) - cloth.z(left);
                float bx = cloth.x(down) - cloth.x(up);
                float by = cloth.y(down) - cloth.y(up);
                float bz = cloth.z(down) - cloth.z(up);
                float nx = ay * bz - az * by;
                float ny = az * bx - ax * bz;
                float nz = ax * by - ay * bx;
                float lengthSquared = nx * nx + ny * ny + nz * nz;
                if (lengthSquared > 1e-9f) {
                    float inverse = (float) (1.0 / Math.sqrt(lengthSquared));
                    normalX[i] = nx * inverse;
                    normalY[i] = ny * inverse;
                    normalZ[i] = nz * inverse;
                } else {
                    normalX[i] = 0f;
                    normalY[i] = 0f;
                    normalZ[i] = -1f;
                }
            }
        }
    }

    private static float u(int column) {
        return Mth.lerp(column / (float) (COLS - 1), CapeLayout.FACE_U1, CapeLayout.FACE_U0);
    }

    private static float v(int row) {
        return Mth.lerp(row / (float) (ROWS - 1), CapeLayout.FACE_V0, CapeLayout.FACE_V1);
    }

    private static void vertex(ClothSimulation cloth, PoseStack.Pose entry, VertexConsumer consumer, int light,
                               int i, float u, float v) {
        consumer.addVertex(entry, cloth.x(i), cloth.y(i), cloth.z(i))
                .setColor(-1)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(entry, normalX[i], normalY[i], normalZ[i]);
    }
}
