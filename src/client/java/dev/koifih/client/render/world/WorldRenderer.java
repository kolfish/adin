package dev.koifih.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.koifih.client.AdinClient;
import dev.koifih.client.event.events.WorldRenderEvent;
import dev.koifih.client.render.Style;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.phys.Vec3;
import java.util.List;

public final class WorldRenderer {
    private static final float MIN_LINE_WIDTH = 1f;
    private static final float MIN_OUTLINE_BORDER = 2f;

    private static volatile List<WorldBuffer.Box> frame = List.of();

    private WorldRenderer() {}

    public static void init() {
        LevelExtractionEvents.END_EXTRACTION.register(WorldRenderer::extract);
        LevelRenderEvents.COLLECT_SUBMITS.register(WorldRenderer::submit);
    }

    private static void extract(LevelExtractionContext context) {
        WorldBuffer buffer = new WorldBuffer();
        AdinClient.EVENTS.post(new WorldRenderEvent(context.level(), context.camera(), context.deltaTracker(), buffer));
        frame = buffer.boxes();
    }

    private static void submit(LevelRenderContext context) {
        List<WorldBuffer.Box> boxes = frame;
        if (boxes.isEmpty()) return;
        Vec3 camera = context.levelState().cameraRenderState.pos;
        SubmitNodeCollector collector = context.submitNodeCollector();
        PoseStack poseStack = context.poseStack();
        collector.submitCustomGeometry(poseStack, WorldRenderTypes.FILL,
                (pose, consumer) -> submitFills(pose, consumer, boxes, camera));
        collector.submitCustomGeometry(poseStack, WorldRenderTypes.LINES,
                (pose, consumer) -> submitEdges(pose, consumer, boxes, camera));
    }

    private static void submitFills(PoseStack.Pose pose, VertexConsumer consumer, List<WorldBuffer.Box> boxes, Vec3 camera) {
        for (WorldBuffer.Box box : boxes) {
            if (box.style().hasFill()) BoxGeometry.fill(pose, consumer, box.bounds(), camera, box.style().fill());
        }
    }

    private static void submitEdges(PoseStack.Pose pose, VertexConsumer consumer, List<WorldBuffer.Box> boxes, Vec3 camera) {
        for (WorldBuffer.Box box : boxes) {
            Style style = box.style();
            float border = style.outlineWidth() - style.strokeWidth();
            if (!style.hasOutline() || border < MIN_OUTLINE_BORDER) continue;
            BoxGeometry.edges(pose, consumer, box.bounds(), camera, style.outline(), lineWidth(style) + border);
        }
        for (WorldBuffer.Box box : boxes) {
            Style style = box.style();
            if (style.hasStroke()) BoxGeometry.edges(pose, consumer, box.bounds(), camera, style.stroke(), lineWidth(style));
        }
    }

    private static float lineWidth(Style style) {
        return Math.max(MIN_LINE_WIDTH, style.strokeWidth());
    }
}
