package dev.koifih.client.render.blocks;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

final class VertexArena {
    record Range(long offset, int bytes) {}

    private static final long INITIAL_CAPACITY = 1 << 20;
    private static final int VERTICES_PER_PRIMITIVE = 4;
    private static final int INDICES_PER_PRIMITIVE = 6;
    private static final int USAGE = GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_COPY_SRC;

    private final String label;
    private final int vertexSize;
    private final Map<Long, Range> ranges = new HashMap<>();
    private GpuBuffer buffer;
    private long capacity;
    private long used;
    private long live;

    VertexArena(String label, int vertexSize) {
        this.label = label;
        this.vertexSize = vertexSize;
    }

    Range put(CommandEncoder encoder, long key, ByteBuffer data) {
        remove(key);
        int bytes = data.remaining();
        if (bytes == 0) return null;
        if (buffer == null || used + bytes > capacity) repack(encoder, bytes);
        Range range = new Range(used, bytes);
        encoder.writeToBuffer(buffer.slice(used, bytes), data);
        used += bytes;
        live += bytes;
        ranges.put(key, range);
        return range;
    }

    void remove(long key) {
        Range old = ranges.remove(key);
        if (old != null) live -= old.bytes();
    }

    Range range(long key) {
        return ranges.get(key);
    }

    GpuBuffer buffer() {
        return buffer;
    }

    boolean isEmpty() {
        return live == 0;
    }

    int indexCount(long bytes) {
        return (int) (bytes / vertexSize / VERTICES_PER_PRIMITIVE * INDICES_PER_PRIMITIVE);
    }

    int usedIndices() {
        return indexCount(used);
    }

    void close() {
        if (buffer != null) buffer.close();
        buffer = null;
        ranges.clear();
        capacity = 0;
        used = 0;
        live = 0;
    }

    private void repack(CommandEncoder encoder, int incoming) {
        long needed = live + incoming;
        long next = Math.max(capacity, INITIAL_CAPACITY);
        while (needed > next / 2) next *= 2;
        GpuBuffer target = RenderSystem.getDevice().createBuffer(() -> "adin block esp " + label, USAGE, next);
        long cursor = 0;
        for (Map.Entry<Long, Range> entry : ranges.entrySet()) {
            Range range = entry.getValue();
            encoder.copyToBuffer(buffer.slice(range.offset(), range.bytes()), target.slice(cursor, range.bytes()));
            entry.setValue(new Range(cursor, range.bytes()));
            cursor += range.bytes();
        }
        if (buffer != null) buffer.close();
        buffer = target;
        capacity = next;
        used = cursor;
    }
}
