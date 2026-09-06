package dev.koifih.client.render.screen;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.koifih.client.render.Pipelines;
import dev.koifih.client.util.MathUtil;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Matrix3x2fc;

public record ScreenQuadRenderState(Matrix3x2fc pose, ScreenPoint a, ScreenPoint b, ScreenPoint c, ScreenPoint d,
                                    int color, ScreenRectangle scissorArea) implements GuiElementRenderState {
    @Override
    public void buildVertices(VertexConsumer vertices) {
        vertices.addVertexWith2DPose(pose, a.x(), a.y()).setColor(color);
        vertices.addVertexWith2DPose(pose, b.x(), b.y()).setColor(color);
        vertices.addVertexWith2DPose(pose, c.x(), c.y()).setColor(color);
        vertices.addVertexWith2DPose(pose, d.x(), d.y()).setColor(color);
    }

    @Override
    public RenderPipeline pipeline() {
        return Pipelines.QUAD;
    }

    @Override
    public TextureSetup textureSetup() {
        return TextureSetup.noTexture();
    }

    @Override
    public ScreenRectangle bounds() {
        ScreenRect rect = new ScreenRect(MathUtil.min(a.x(), b.x(), c.x(), d.x()), MathUtil.min(a.y(), b.y(), c.y(), d.y()),
                MathUtil.max(a.x(), b.x(), c.x(), d.x()), MathUtil.max(a.y(), b.y(), c.y(), d.y()));
        return ScreenGeometry.bounds(rect, pose);
    }
}
