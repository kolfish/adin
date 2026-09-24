package dev.koifih.client.ui.video;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import com.mojang.blaze3d.platform.NativeImage;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AviVideo {
    private record Chunk(int offset, int length) {}

    private static final int RIFF = fourcc("RIFF");
    private static final int LIST = fourcc("LIST");
    private static final int AVIH = fourcc("avih");
    private static final int STRH = fourcc("strh");
    private static final int VIDS = fourcc("vids");
    private static final int VIDEO_FRAME = fourcc("00dc");

    private final ByteBuffer bytes;
    private final List<Chunk> frames = new ArrayList<>();
    private float fps = 15f;

    public static AviVideo read(InputStream stream) throws IOException {
        byte[] data = stream.readAllBytes();
        AviVideo video = new AviVideo(ByteBuffer.allocateDirect(data.length).put(data).flip().order(ByteOrder.LITTLE_ENDIAN));
        video.parse();
        if (video.frames.isEmpty()) throw new IOException("no video frames");
        return video;
    }

    public int frameCount() {
        return frames.size();
    }

    public float fps() {
        return fps;
    }

    public NativeImage decode(int index) throws IOException {
        Chunk chunk = frames.get(index);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);
            ByteBuffer pixels = STBImage.stbi_load_from_memory(bytes.slice(chunk.offset(), chunk.length()), width, height, channels, 4);
            if (pixels == null) throw new IOException(STBImage.stbi_failure_reason());
            return new NativeImage(NativeImage.Format.RGBA, width.get(0), height.get(0), true, MemoryUtil.memAddress(pixels));
        }
    }

    private void parse() throws IOException {
        ByteBuffer buffer = bytes;
        if (buffer.limit() < 12 || buffer.getInt(0) != RIFF) throw new IOException("not a RIFF file");
        walk(buffer, 12, Math.min(buffer.limit(), 8 + buffer.getInt(4)));
    }

    private void walk(ByteBuffer buffer, int start, int end) {
        int position = start;
        while (position + 8 <= end) {
            int id = buffer.getInt(position);
            int size = buffer.getInt(position + 4);
            int data = position + 8;
            if (size < 0 || data + size > end) break;
            if (id == LIST) {
                walk(buffer, data + 4, data + size);
            } else if (id == AVIH) {
                int microsPerFrame = buffer.getInt(data);
                if (microsPerFrame > 0) fps = 1_000_000f / microsPerFrame;
            } else if (id == STRH && buffer.getInt(data) == VIDS) {
                int scale = buffer.getInt(data + 20);
                int rate = buffer.getInt(data + 24);
                if (scale > 0 && rate > 0) fps = (float) rate / scale;
            } else if (id == VIDEO_FRAME && size > 0) {
                frames.add(new Chunk(data, size));
            }
            position = data + size + (size & 1);
        }
    }

    private static int fourcc(String code) {
        return code.charAt(0) | code.charAt(1) << 8 | code.charAt(2) << 16 | code.charAt(3) << 24;
    }
}
