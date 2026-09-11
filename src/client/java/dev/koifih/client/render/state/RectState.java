package dev.koifih.client.render.state;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.koifih.client.render.Scissor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Matrix3x2fc;

public record RectState(Matrix3x2fc pose, int x, int y, int width, int height, int radius, int shape, int color,
                        int bottomColor, RenderPipeline pipeline, ScreenRectangle scissorArea) implements GuiElementRenderState {
    private static final int ANTIALIAS_PADDING = 1;

    public RectState(Matrix3x2fc pose, int x, int y, int width, int height, int radius, int shape, int color,
                     int bottomColor, RenderPipeline pipeline) {
        this(pose, x, y, width, height, radius, shape, color, bottomColor, pipeline, Scissor.current());
    }

    @Override
    public void buildVertices(VertexConsumer vertices) {
        vertex(vertices, -ANTIALIAS_PADDING, -ANTIALIAS_PADDING, color);
        vertex(vertices, -ANTIALIAS_PADDING, height + ANTIALIAS_PADDING, bottomColor);
        vertex(vertices, width + ANTIALIAS_PADDING, height + ANTIALIAS_PADDING, bottomColor);
        vertex(vertices, width + ANTIALIAS_PADDING, -ANTIALIAS_PADDING, color);
    }

    private void vertex(VertexConsumer vertices, float localX, float localY, int vertexColor) {
        vertices.addVertexWith2DPose(pose, x + localX, y + localY)
                .setColor(vertexColor)
                .setUv(localX, localY)
                .setUv1(radius, shape)
                .setUv2(width, height);
    }

    @Override
    public TextureSetup textureSetup() {
        return TextureSetup.noTexture();
    }

    @Override
    public ScreenRectangle bounds() {
        return new ScreenRectangle(x - ANTIALIAS_PADDING, y - ANTIALIAS_PADDING,
                width + 2 * ANTIALIAS_PADDING, height + 2 * ANTIALIAS_PADDING).transformMaxBounds(pose);
    }
}
