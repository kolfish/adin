package dev.koifih.client.render.state;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.koifih.client.render.Rect;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Matrix3x2fc;

public record LineState(Matrix3x2fc pose, float x0, float y0, float x1, float y1, float width, int color,
                        ScreenRectangle scissorArea) implements GuiElementRenderState {
    @Override
    public void buildVertices(VertexConsumer vertices) {
        Geometry.line(vertices, pose, x0, y0, x1, y1, width, color);
    }

    @Override
    public RenderPipeline pipeline() {
        return Pipelines.SHAPE;
    }

    @Override
    public TextureSetup textureSetup() {
        return TextureSetup.noTexture();
    }

    @Override
    public ScreenRectangle bounds() {
        return Geometry.bounds(Rect.of(x0, y0, x1, y1).expand(width * 0.5f), pose);
    }
}
