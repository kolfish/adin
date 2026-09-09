package dev.koifih.client.render;

import net.minecraft.world.phys.AABB;

public final class Corners {
    public static final int COUNT = 8;
    public static final int[] EDGES = {
            0, 1, 2, 3, 4, 5, 6, 7,
            0, 2, 1, 3, 4, 6, 5, 7,
            0, 4, 1, 5, 2, 6, 3, 7
    };
    public static final int[][] FACES = {
            {0, 1, 3, 2}, {4, 5, 7, 6}, {0, 1, 5, 4}, {2, 3, 7, 6}, {0, 2, 6, 4}, {1, 3, 7, 5}
    };

    private Corners() {}

    public static double x(AABB box, int corner) {
        return (corner & 1) == 0 ? box.minX : box.maxX;
    }

    public static double y(AABB box, int corner) {
        return (corner & 2) == 0 ? box.minY : box.maxY;
    }

    public static double z(AABB box, int corner) {
        return (corner & 4) == 0 ? box.minZ : box.maxZ;
    }
}
