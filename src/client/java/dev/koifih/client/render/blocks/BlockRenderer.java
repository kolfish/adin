package dev.koifih.client.render.blocks;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import dev.koifih.Adin;
import dev.koifih.client.util.Colors;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Util;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class BlockRenderer {
    public record Spec(int color, float fillOpacity, float lineWidth) {}

    private static final class Section {
        final AABB bounds;
        final int blocks;
        boolean visible;

        private Section(AABB bounds, int blocks) {
            this.bounds = bounds;
            this.blocks = blocks;
        }
    }

    private static final double REANCHOR_DISTANCE = 1024.0;
    private static final int LINE_CONFIG_SIZE = 16;
    private static final DepthStencilState SEE_THROUGH = new DepthStencilState(CompareOp.ALWAYS_PASS, false);
    private static final BindGroupLayout LINE_CONFIG = BindGroupLayout.builder()
            .withUniform("BlockLines", UniformType.UNIFORM_BUFFER)
            .build();
    private static final RenderPipeline FILL = RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation(Adin.id("pipeline/blocks_fill"))
            .withDepthStencilState(SEE_THROUGH)
            .withCull(false)
            .build();
    private static final RenderPipeline LINES = RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withLocation(Adin.id("pipeline/blocks_lines"))
            .withVertexShader(Adin.id("world/blocks_lines"))
            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR_NORMAL)
            .withBindGroupLayout(LINE_CONFIG)
            .withDepthStencilState(SEE_THROUGH)
            .build();
    private static final Vector3f NO_OFFSET = new Vector3f();
    private static final Matrix4f IDENTITY = new Matrix4f();
    private static final Matrix4f MODEL_VIEW = new Matrix4f();
    private static final Vector4f MODULATOR = new Vector4f();

    private static final VertexArena fills = new VertexArena("fills", DefaultVertexFormat.POSITION_COLOR.getVertexSize());
    private static final VertexArena lines = new VertexArena("lines", DefaultVertexFormat.POSITION_COLOR_NORMAL.getVertexSize());
    private static final Long2ObjectOpenHashMap<Section> sections = new Long2ObjectOpenHashMap<>();
    private static final Map<Long, Integer> versions = new ConcurrentHashMap<>();
    private static final Queue<BlockMesh.Built> uploads = new ConcurrentLinkedQueue<>();
    private static volatile Spec spec;
    private static volatile Vec3i anchor = Vec3i.ZERO;
    private static GpuBuffer lineConfig;
    private static float uploadedLineWidth = Float.NaN;
    private static volatile int visibleBlocks;

    private BlockRenderer() {}

    public static void init() {
        LevelRenderEvents.END_MAIN.register(BlockRenderer::render);
    }

    public static void configure(Spec next) {
        spec = next;
    }

    public static void anchor(Vec3 position) {
        anchor = new Vec3i(SectionPos.sectionToBlockCoord(SectionPos.blockToSectionCoord(position.x)),
                SectionPos.sectionToBlockCoord(SectionPos.blockToSectionCoord(position.y)),
                SectionPos.sectionToBlockCoord(SectionPos.blockToSectionCoord(position.z)));
    }

    public static int visibleBlocks() {
        return visibleBlocks;
    }

    public static boolean drifted(Vec3 position) {
        return !Vec3.atLowerCornerOf(anchor).closerThan(position, REANCHOR_DISTANCE);
    }

    public static void onSectionChanged(long key) {
        BlockIndex.Matches matches = BlockIndex.matches(key);
        int version = versions.merge(key, 1, Integer::sum);
        if (matches == null || spec == null) {
            uploads.add(BlockMesh.Built.empty(key, version));
            return;
        }
        Vec3i origin = anchor;
        Util.backgroundExecutor().execute(() -> uploads.add(BlockMesh.build(key, version, matches, origin)));
    }

    private static void render(LevelRenderContext context) {
        Spec current = spec;
        BlockIndex.drain();
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        drainUploads(encoder);
        if (current == null) {
            dispose();
            visibleBlocks = 0;
            return;
        }
        CameraRenderState camera = context.levelState().cameraRenderState;
        int blocks = 0;
        for (Section section : sections.values()) {
            section.visible = camera.cullFrustum != null && camera.cullFrustum.isVisible(section.bounds);
            if (section.visible) blocks += section.blocks;
        }
        visibleBlocks = blocks;
        if (blocks == 0) return;
        RenderTarget main = Minecraft.getInstance().gameRenderer.mainRenderTarget();
        GpuTextureView color = RenderSystem.outputColorTextureOverride != null ? RenderSystem.outputColorTextureOverride : main.getColorTextureView();
        GpuTextureView depth = RenderSystem.outputDepthTextureOverride != null ? RenderSystem.outputDepthTextureOverride : main.getDepthTextureView();
        Vec3i origin = anchor;
        MODEL_VIEW.set(camera.viewRotationMatrix)
                .translate((float) (origin.getX() - camera.pos.x), (float) (origin.getY() - camera.pos.y), (float) (origin.getZ() - camera.pos.z));
        int opaque = Colors.opaque(current.color());
        uploadLineWidth(encoder, current.lineWidth());
        try (RenderPass pass = encoder.createRenderPass(() -> "adin block esp", color, Optional.empty(), depth, OptionalDouble.empty())) {
            RenderSystem.bindDefaultUniforms(pass);
            if (current.fillOpacity() > 0f) draw(pass, FILL, PrimitiveTopology.QUADS, fills, Colors.withAlpha(opaque, current.fillOpacity()));
            if (!lines.isEmpty()) {
                pass.setUniform("BlockLines", lineConfig.slice());
                draw(pass, LINES, PrimitiveTopology.LINES, lines, opaque);
            }
        }
    }

    private static void draw(RenderPass pass, RenderPipeline pipeline, PrimitiveTopology topology, VertexArena arena, int color) {
        if (arena.isEmpty()) return;
        boolean started = false;
        long start = 0;
        long length = 0;
        for (VertexArena.Range range : arena.ordered()) {
            Section section = sections.get(range.key);
            if (section == null || !section.visible) continue;
            if (!started) {
                started = true;
                start = range.offset;
                pass.setPipeline(pipeline);
                MODULATOR.set(Colors.red(color) / 255f, Colors.green(color) / 255f, Colors.blue(color) / 255f, Colors.alpha(color) / 255f);
                pass.setUniform("DynamicTransforms", RenderSystem.getDynamicUniforms().writeTransform(MODEL_VIEW, MODULATOR, NO_OFFSET, IDENTITY));
                RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(topology);
                pass.setIndexBuffer(indices.getBuffer(arena.usedIndices()), indices.type());
                pass.setVertexBuffer(0, arena.buffer().slice());
            } else if (range.offset != start + length) {
                pass.drawIndexed(arena.indexCount(length), 1, arena.indexCount(start), 0, 0);
                start = range.offset;
                length = 0;
            }
            length += range.bytes;
        }
        if (started) pass.drawIndexed(arena.indexCount(length), 1, arena.indexCount(start), 0, 0);
    }

    private static void uploadLineWidth(CommandEncoder encoder, float width) {
        if (lineConfig == null) {
            lineConfig = RenderSystem.getDevice().createBuffer(() -> "adin block esp line config",
                    GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, LINE_CONFIG_SIZE);
        }
        if (width == uploadedLineWidth) return;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            encoder.writeToBuffer(lineConfig.slice(), Std140Builder.onStack(stack, LINE_CONFIG_SIZE).putFloat(width).get());
        }
        uploadedLineWidth = width;
    }

    private static void drainUploads(CommandEncoder encoder) {
        BlockMesh.Built built;
        while ((built = uploads.poll()) != null) {
            Integer version = versions.get(built.key());
            if (version != null && version == built.version()) store(encoder, built);
            built.close();
        }
    }

    private static void store(CommandEncoder encoder, BlockMesh.Built built) {
        long key = built.key();
        VertexArena.Range fill = put(encoder, fills, key, built.fill());
        VertexArena.Range line = put(encoder, lines, key, built.lines());
        if (fill == null && line == null) {
            sections.remove(key);
            return;
        }
        double x = SectionPos.sectionToBlockCoord(SectionPos.x(key));
        double y = SectionPos.sectionToBlockCoord(SectionPos.y(key));
        double z = SectionPos.sectionToBlockCoord(SectionPos.z(key));
        BlockIndex.Matches matches = BlockIndex.matches(key);
        sections.put(key, new Section(new AABB(x, y, z, x + BlockIndex.SIZE, y + BlockIndex.SIZE, z + BlockIndex.SIZE), matches == null ? 0 : matches.count()));
    }

    private static VertexArena.Range put(CommandEncoder encoder, VertexArena arena, long key, MeshData mesh) {
        if (mesh == null) {
            arena.remove(key);
            return null;
        }
        return arena.put(encoder, key, mesh.vertexBuffer());
    }

    private static void dispose() {
        if (sections.isEmpty()) return;
        sections.clear();
        versions.clear();
        fills.close();
        lines.close();
    }
}
