package dev.koifih.client.render.blocks;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;
import java.util.Arrays;

final class BlockMesh {
    record Built(long key, int version, MeshData fill, MeshData lines, ByteBufferBuilder fillBytes, ByteBufferBuilder lineBytes) {
        static Built empty(long key, int version) {
            return new Built(key, version, null, null, null, null);
        }

        void close() {
            if (fill != null) fill.close();
            if (lines != null) lines.close();
            if (fillBytes != null) fillBytes.close();
            if (lineBytes != null) lineBytes.close();
        }
    }

    private static final int WHITE = 0xFFFFFFFF;
    private static final int INITIAL_BYTES = 64 * 1024;
    private static final int SIZE = BlockIndex.SIZE;
    private static final int AXES = 3;
    private static final int[][] FACE_CORNERS = {
            {0, 1, 2, 3}, {4, 5, 6, 7}, {0, 1, 5, 4}, {3, 2, 6, 7}, {0, 3, 7, 4}, {1, 2, 6, 5}};
    private static final int[][] EDGES = {
            {0, 1}, {1, 2}, {2, 3}, {3, 0}, {4, 5}, {5, 6}, {6, 7}, {7, 4}, {0, 4}, {1, 5}, {2, 6}, {3, 7}};
    private static final int[] CORNER_X = {0, 1, 1, 0, 0, 1, 1, 0};
    private static final int[] CORNER_Y = {0, 0, 0, 0, 1, 1, 1, 1};
    private static final int[] CORNER_Z = {0, 0, 1, 1, 0, 0, 1, 1};

    private final BlockIndex.Matches matches;
    private final float originX;
    private final float originY;
    private final float originZ;
    private final BufferBuilder fill;
    private final BufferBuilder lines;
    private final boolean[] mask = new boolean[SIZE * SIZE];

    private BlockMesh(BlockIndex.Matches matches, float originX, float originY, float originZ, BufferBuilder fill, BufferBuilder lines) {
        this.matches = matches;
        this.originX = originX;
        this.originY = originY;
        this.originZ = originZ;
        this.fill = fill;
        this.lines = lines;
    }

    static Built build(long key, int version, BlockIndex.Matches matches, Vec3i anchor) {
        ByteBufferBuilder fillBytes = new ByteBufferBuilder(INITIAL_BYTES);
        ByteBufferBuilder lineBytes = new ByteBufferBuilder(INITIAL_BYTES);
        BufferBuilder fill = new BufferBuilder(fillBytes, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR);
        BufferBuilder lines = new BufferBuilder(lineBytes, PrimitiveTopology.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        BlockMesh mesh = new BlockMesh(matches,
                SectionPos.sectionToBlockCoord(SectionPos.x(key)) - anchor.getX(),
                SectionPos.sectionToBlockCoord(SectionPos.y(key)) - anchor.getY(),
                SectionPos.sectionToBlockCoord(SectionPos.z(key)) - anchor.getZ(), fill, lines);
        mesh.shapes();
        for (int axis = 0; axis < AXES; axis++) {
            mesh.faces(axis, -1);
            mesh.faces(axis, 1);
            mesh.edges(axis);
        }
        return new Built(key, version, fill.build(), lines.build(), fillBytes, lineBytes);
    }

    private void shapes() {
        float[] box = new float[6];
        for (int index = 0; index < BlockIndex.VOLUME; index++) {
            if (!matches.has(index)) continue;
            AABB shape = matches.shape(index);
            if (shape == null) continue;
            int x = index & 15;
            int y = index >>> 8;
            int z = (index >>> 4) & 15;
            box[0] = originX + x + (float) shape.minX;
            box[1] = originY + y + (float) shape.minY;
            box[2] = originZ + z + (float) shape.minZ;
            box[3] = originX + x + (float) shape.maxX;
            box[4] = originY + y + (float) shape.maxY;
            box[5] = originZ + z + (float) shape.maxZ;
            for (int[] face : FACE_CORNERS) {
                for (int corner : face) fill.addVertex(cx(box, corner), cy(box, corner), cz(box, corner)).setColor(WHITE);
            }
            for (int[] edge : EDGES) {
                float x0 = cx(box, edge[0]);
                float y0 = cy(box, edge[0]);
                float z0 = cz(box, edge[0]);
                float x1 = cx(box, edge[1]);
                float y1 = cy(box, edge[1]);
                float z1 = cz(box, edge[1]);
                lines.addVertex(x0, y0, z0).setColor(WHITE).setNormal(Math.signum(x1 - x0), Math.signum(y1 - y0), Math.signum(z1 - z0));
                lines.addVertex(x1, y1, z1).setColor(WHITE).setNormal(Math.signum(x1 - x0), Math.signum(y1 - y0), Math.signum(z1 - z0));
            }
        }
    }

    private void faces(int axis, int sign) {
        for (int slice = 0; slice < SIZE; slice++) {
            boolean any = false;
            for (int b = 0; b < SIZE; b++) {
                for (int c = 0; c < SIZE; c++) {
                    boolean exposed = full(axis, slice, b, c) && !full(axis, slice + sign, b, c);
                    mask[b * SIZE + c] = exposed;
                    any |= exposed;
                }
            }
            if (!any) continue;
            float plane = slice + (sign > 0 ? 1 : 0);
            for (int c = 0; c < SIZE; c++) {
                for (int b = 0; b < SIZE; b++) {
                    if (!mask[b * SIZE + c]) continue;
                    int width = 1;
                    while (b + width < SIZE && mask[(b + width) * SIZE + c]) width++;
                    int height = 1;
                    while (c + height < SIZE && row(b, c + height, width)) height++;
                    for (int i = 0; i < width; i++) Arrays.fill(mask, (b + i) * SIZE + c, (b + i) * SIZE + c + height, false);
                    vertex(fill, axis, plane, b, c).setColor(WHITE);
                    vertex(fill, axis, plane, b + width, c).setColor(WHITE);
                    vertex(fill, axis, plane, b + width, c + height).setColor(WHITE);
                    vertex(fill, axis, plane, b, c + height).setColor(WHITE);
                }
            }
        }
    }

    private boolean row(int b, int c, int width) {
        for (int i = 0; i < width; i++) if (!mask[(b + i) * SIZE + c]) return false;
        return true;
    }

    private void edges(int axis) {
        float nx = axis == 0 ? 1f : 0f;
        float ny = axis == 1 ? 1f : 0f;
        float nz = axis == 2 ? 1f : 0f;
        for (int b = 0; b <= SIZE; b++) {
            for (int c = 0; c <= SIZE; c++) {
                int run = -1;
                for (int a = 0; a <= SIZE; a++) {
                    boolean outline = a < SIZE && outline(axis, a, b, c);
                    if (outline && run < 0) {
                        run = a;
                    } else if (!outline && run >= 0) {
                        vertex(lines, axis, run, b, c).setColor(WHITE).setNormal(nx, ny, nz);
                        vertex(lines, axis, a, b, c).setColor(WHITE).setNormal(nx, ny, nz);
                        run = -1;
                    }
                }
            }
        }
    }

    private boolean outline(int axis, int a, int b, int c) {
        boolean lowLow = full(axis, a, b - 1, c - 1);
        boolean highLow = full(axis, a, b, c - 1);
        boolean lowHigh = full(axis, a, b - 1, c);
        boolean highHigh = full(axis, a, b, c);
        int count = (lowLow ? 1 : 0) + (highLow ? 1 : 0) + (lowHigh ? 1 : 0) + (highHigh ? 1 : 0);
        return count == 1 || count == 3 || (count == 2 && lowLow == highHigh);
    }

    private boolean full(int axis, int a, int b, int c) {
        int x = axis == 0 ? a : b;
        int y = axis == 1 ? a : axis == 0 ? b : c;
        int z = axis == 2 ? a : c;
        if ((x | y | z) < 0 || x >= SIZE || y >= SIZE || z >= SIZE) return false;
        int index = BlockIndex.index(x, y, z);
        return matches.has(index) && matches.shape(index) == null;
    }

    private VertexConsumer vertex(BufferBuilder builder, int axis, float a, float b, float c) {
        float x = axis == 0 ? a : b;
        float y = axis == 1 ? a : axis == 0 ? b : c;
        float z = axis == 2 ? a : c;
        return builder.addVertex(originX + x, originY + y, originZ + z);
    }

    private static float cx(float[] box, int corner) {
        return CORNER_X[corner] == 0 ? box[0] : box[3];
    }

    private static float cy(float[] box, int corner) {
        return CORNER_Y[corner] == 0 ? box[1] : box[4];
    }

    private static float cz(float[] box, int corner) {
        return CORNER_Z[corner] == 0 ? box[2] : box[5];
    }
}
