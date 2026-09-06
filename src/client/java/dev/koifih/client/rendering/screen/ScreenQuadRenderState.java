package dev.koifih.client.rendering.screen;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.koifih.client.rendering.Pipelines;
import dev.koifih.client.utils.MathHelper;
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
        float minX = MathHelper.min(a.x(), b.x(), c.x(), d.x());
        float minY = MathHelper.min(a.y(), b.y(), c.y(), d.y());
        float maxX = MathHelper.max(a.x(), b.x(), c.x(), d.x());
        float maxY = MathHelper.max(a.y(), b.y(), c.y(), d.y());
        return new ScreenRectangle((int) Math.floor(minX), (int) Math.floor(minY),
                (int) Math.ceil(maxX - minX) + 1, (int) Math.ceil(maxY - minY) + 1).transformMaxBounds(pose);
    }
}
