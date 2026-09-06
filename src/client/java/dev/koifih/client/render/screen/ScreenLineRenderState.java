package dev.koifih.client.render.screen;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.koifih.client.render.Pipelines;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Matrix3x2fc;

public record ScreenLineRenderState(Matrix3x2fc pose, ScreenBuffer.Line line, int color,
                                    ScreenRectangle scissorArea) implements GuiElementRenderState {
    @Override
    public void buildVertices(VertexConsumer vertices) {
        ScreenGeometry.line(vertices, pose, line.x0(), line.y0(), line.x1(), line.y1(), line.width(), color);
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
        ScreenRect rect = ScreenRect.of(line.x0(), line.y0(), line.x1(), line.y1());
        return ScreenGeometry.bounds(rect.expand(line.width() * 0.5f), pose);
    }
}
