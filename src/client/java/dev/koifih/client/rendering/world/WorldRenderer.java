package dev.koifih.client.rendering.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.koifih.client.event.EventBus;
import dev.koifih.client.event.WorldExtractEvent;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.List;

public final class WorldRenderer {
    private static volatile List<ShapeCollector.Box> frame = List.of();

    private WorldRenderer() {}

    public static void init() {
        LevelExtractionEvents.END_EXTRACTION.register(WorldRenderer::extract);
        LevelRenderEvents.COLLECT_SUBMITS.register(WorldRenderer::submit);
    }

    private static void extract(LevelExtractionContext context) {
        ShapeCollector shapes = new ShapeCollector();
        EventBus.post(new WorldExtractEvent(context.level(), context.camera(), context.deltaTracker(), shapes));
        frame = shapes.boxes();
    }

    private static void submit(LevelRenderContext context) {
        List<ShapeCollector.Box> boxes = frame;
        if (boxes.isEmpty()) return;
        Vec3 camera = context.levelState().cameraRenderState.pos;
        SubmitNodeCollector collector = context.submitNodeCollector();
        PoseStack poseStack = context.poseStack();
        collector.submitCustomGeometry(poseStack, WorldRenderTypes.FILL,
                (pose, consumer) -> submitFills(pose, consumer, boxes, camera));
        collector.submitCustomGeometry(poseStack, WorldRenderTypes.LINES,
                (pose, consumer) -> submitEdges(pose, consumer, boxes, camera));
    }

    private static void submitFills(PoseStack.Pose pose, VertexConsumer consumer, List<ShapeCollector.Box> boxes, Vec3 camera) {
        for (ShapeCollector.Box box : boxes) {
            if (!box.style().hasFill()) continue;
            AABB b = box.bounds();
            BoxGeometry.fill(pose, consumer,
                    (float) (b.minX - camera.x), (float) (b.minY - camera.y), (float) (b.minZ - camera.z),
                    (float) (b.maxX - camera.x), (float) (b.maxY - camera.y), (float) (b.maxZ - camera.z),
                    box.style().fill());
        }
    }

    private static void submitEdges(PoseStack.Pose pose, VertexConsumer consumer, List<ShapeCollector.Box> boxes, Vec3 camera) {
        for (ShapeCollector.Box box : boxes) {
            if (box.style().hasOutline()) edges(pose, consumer, box.bounds(), camera, box.style().outline(), box.style().outlineWidth());
        }
        for (ShapeCollector.Box box : boxes) {
            if (box.style().hasStroke()) edges(pose, consumer, box.bounds(), camera, box.style().stroke(), box.style().strokeWidth());
        }
    }

    private static void edges(PoseStack.Pose pose, VertexConsumer consumer, AABB b, Vec3 camera, int color, float width) {
        BoxGeometry.edges(pose, consumer,
                (float) (b.minX - camera.x), (float) (b.minY - camera.y), (float) (b.minZ - camera.z),
                (float) (b.maxX - camera.x), (float) (b.maxY - camera.y), (float) (b.maxZ - camera.z),
                color, width);
    }
}
