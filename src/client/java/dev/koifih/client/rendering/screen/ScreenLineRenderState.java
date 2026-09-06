package dev.koifih.client.rendering.screen;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.koifih.client.rendering.Pipelines;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Matrix3x2fc;

public record ScreenLineRenderState(Matrix3x2fc pose, OverlayCollector.Line line,
                                    ScreenRectangle scissorArea) implements GuiElementRenderState {
    @Override
    public void buildVertices(VertexConsumer vertices) {
        ScreenGeometry.line(vertices, pose, line.x0(), line.y0(), line.x1(), line.y1(), line.width(), line.color());
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
        ScreenRect outer = ScreenRect.of(line.x0(), line.y0(), line.x1(), line.y1()).expand(line.width() * 0.5f + ScreenGeometry.PADDING);
        return new ScreenRectangle((int) Math.floor(outer.minX()), (int) Math.floor(outer.minY()),
                (int) Math.ceil(outer.width()) + 1, (int) Math.ceil(outer.height()) + 1).transformMaxBounds(pose);
    }
}
