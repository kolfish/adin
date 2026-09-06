package dev.koifih.client.render.screen;

import dev.koifih.Adin;
import dev.koifih.client.AdinClient;
import dev.koifih.client.event.events.HudRenderEvent;
import dev.koifih.client.event.events.ScreenRenderEvent;
import dev.koifih.client.render.GuiElements;
import dev.koifih.client.render.Opacity;
import dev.koifih.client.render.Scissor;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;

public final class ScreenRenderer {
    private ScreenRenderer() {}

    public static void init() {
        HudElementRegistry.addLast(Adin.id("overlay"), ScreenRenderer::extract);
    }

    public static void submit(GuiGraphicsExtractor graphics, ScreenBuffer buffer) {
        if (buffer.isEmpty()) return;
        Matrix3x2fc pose = new Matrix3x2f(graphics.pose());
        ScreenRectangle scissor = Scissor.current();
        for (ScreenBuffer.Rect rect : buffer.rects()) {
            GuiElements.submit(graphics, new ScreenRectRenderState(pose, rect.bounds(), Opacity.apply(rect.style()), scissor));
        }
        for (ScreenBuffer.Quad quad : buffer.quads()) {
            GuiElements.submit(graphics, new ScreenQuadRenderState(pose, quad.a(), quad.b(), quad.c(), quad.d(),
                    Opacity.apply(quad.color()), scissor));
        }
        for (ScreenBuffer.Line line : buffer.lines()) {
            GuiElements.submit(graphics, new ScreenLineRenderState(pose, line, Opacity.apply(line.color()), scissor));
        }
    }

    private static void extract(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        ScreenBuffer buffer = new ScreenBuffer();
        AdinClient.EVENTS.post(new ScreenRenderEvent(minecraft.level, minecraft.gameRenderer.mainCamera(), deltaTracker,
                Projection.capture(), buffer));
        submit(graphics, buffer);
        AdinClient.EVENTS.post(new HudRenderEvent(graphics, deltaTracker));
    }
}
