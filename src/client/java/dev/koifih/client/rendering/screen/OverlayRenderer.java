package dev.koifih.client.rendering.screen;

import dev.koifih.Adin;
import dev.koifih.client.event.EventBus;
import dev.koifih.client.event.ScreenExtractEvent;
import dev.koifih.client.rendering.GuiRenderQueue;
import dev.koifih.client.rendering.Scissor;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;

public final class OverlayRenderer {
    private OverlayRenderer() {}

    public static void init() {
        HudElementRegistry.addLast(Adin.id("overlay"), OverlayRenderer::extract);
    }

    private static void extract(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        W2S w2s = W2S.capture();
        OverlayCollector shapes = new OverlayCollector();
        EventBus.post(new ScreenExtractEvent(minecraft.level, minecraft.gameRenderer.mainCamera(), deltaTracker, w2s, shapes));
        submit(graphics, w2s, shapes);
    }

    private static void submit(GuiGraphicsExtractor graphics, W2S w2s, OverlayCollector shapes) {
        Matrix3x2fc pose = new Matrix3x2f(graphics.pose());
        ScreenRectangle scissor = Scissor.current();
        for (OverlayCollector.Rect rect : shapes.rects()) {
            GuiRenderQueue.submit(graphics, new ScreenRectRenderState(pose, rect.bounds(), rect.style().scaled(w2s.pixel()), scissor));
        }
        for (OverlayCollector.Line line : shapes.lines()) {
            OverlayCollector.Line scaled = new OverlayCollector.Line(line.x0(), line.y0(), line.x1(), line.y1(), line.color(), line.width() * w2s.pixel());
            GuiRenderQueue.submit(graphics, new ScreenLineRenderState(pose, scaled, scissor));
        }
    }
}
