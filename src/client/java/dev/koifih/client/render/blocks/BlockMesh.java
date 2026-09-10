package dev.koifih.client.render.blocks;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;

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
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final int[][] FACE_CORNERS = {
            {0, 1, 2, 3}, {4, 5, 6, 7}, {0, 1, 5, 4}, {3, 2, 6, 7}, {0, 3, 7, 4}, {1, 2, 6, 5}};
    private static final int[][] EDGES = {
            {0, 1, 0, 2}, {1, 2, 0, 5}, {2, 3, 0, 3}, {3, 0, 0, 4},
            {4, 5, 1, 2}, {5, 6, 1, 5}, {6, 7, 1, 3}, {7, 4, 1, 4},
            {0, 4, 4, 2}, {1, 5, 5, 2}, {2, 6, 5, 3}, {3, 7, 4, 3}};
    private static final int[] CORNER_X = {0, 1, 1, 0, 0, 1, 1, 0};
    private static final int[] CORNER_Y = {0, 0, 0, 0, 1, 1, 1, 1};
    private static final int[] CORNER_Z = {0, 0, 1, 1, 0, 0, 1, 1};

    private BlockMesh() {}

    static Built build(long key, int version, BlockIndex.Matches matches, Vec3i anchor) {
        float originX = SectionPos.sectionToBlockCoord(SectionPos.x(key)) - anchor.getX();
        float originY = SectionPos.sectionToBlockCoord(SectionPos.y(key)) - anchor.getY();
        float originZ = SectionPos.sectionToBlockCoord(SectionPos.z(key)) - anchor.getZ();
        ByteBufferBuilder fillBytes = new ByteBufferBuilder(INITIAL_BYTES);
        ByteBufferBuilder lineBytes = new ByteBufferBuilder(INITIAL_BYTES);
        BufferBuilder fill = new BufferBuilder(fillBytes, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR);
        BufferBuilder lines = new BufferBuilder(lineBytes, PrimitiveTopology.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        float[] box = new float[6];
        for (int index = 0; index < BlockIndex.VOLUME; index++) {
            if (!matches.has(index)) continue;
            int x = index & 15;
            int y = index >>> 8;
            int z = (index >>> 4) & 15;
            AABB shape = matches.shape(index);
            boolean full = shape == null;
            box[0] = originX + x + (full ? 0f : (float) shape.minX);
            box[1] = originY + y + (full ? 0f : (float) shape.minY);
            box[2] = originZ + z + (full ? 0f : (float) shape.minZ);
            box[3] = originX + x + (full ? 1f : (float) shape.maxX);
            box[4] = originY + y + (full ? 1f : (float) shape.maxY);
            box[5] = originZ + z + (full ? 1f : (float) shape.maxZ);
            for (int face = 0; face < 6; face++) {
                Direction direction = DIRECTIONS[face];
                if (!full || !cube(matches, x + direction.getStepX(), y + direction.getStepY(), z + direction.getStepZ())) quad(fill, box, face);
            }
            for (int[] edge : EDGES) {
                if (!full || outline(matches, x, y, z, edge[2], edge[3])) line(lines, box, edge[0], edge[1]);
            }
        }
        return new Built(key, version, fill.build(), lines.build(), fillBytes, lineBytes);
    }

    private static boolean outline(BlockIndex.Matches matches, int x, int y, int z, int faceA, int faceB) {
        Direction a = DIRECTIONS[faceA];
        Direction b = DIRECTIONS[faceB];
        boolean acrossA = cube(matches, x + a.getStepX(), y + a.getStepY(), z + a.getStepZ());
        boolean acrossB = cube(matches, x + b.getStepX(), y + b.getStepY(), z + b.getStepZ());
        if (acrossA != acrossB) return false;
        if (!acrossA) return true;
        return !cube(matches, x + a.getStepX() + b.getStepX(), y + a.getStepY() + b.getStepY(), z + a.getStepZ() + b.getStepZ());
    }

    private static boolean cube(BlockIndex.Matches matches, int x, int y, int z) {
        if ((x | y | z) < 0 || x >= BlockIndex.SIZE || y >= BlockIndex.SIZE || z >= BlockIndex.SIZE) return false;
        int index = BlockIndex.index(x, y, z);
        return matches.has(index) && matches.shape(index) == null;
    }

    private static void quad(BufferBuilder fill, float[] box, int face) {
        for (int corner : FACE_CORNERS[face]) fill.addVertex(cx(box, corner), cy(box, corner), cz(box, corner)).setColor(WHITE);
    }

    private static void line(BufferBuilder lines, float[] box, int from, int to) {
        float nx = cx(box, to) - cx(box, from);
        float ny = cy(box, to) - cy(box, from);
        float nz = cz(box, to) - cz(box, from);
        lines.addVertex(cx(box, from), cy(box, from), cz(box, from)).setColor(WHITE).setNormal(nx, ny, nz);
        lines.addVertex(cx(box, to), cy(box, to), cz(box, to)).setColor(WHITE).setNormal(nx, ny, nz);
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
