package dev.koifih.client.render.state;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.koifih.client.render.Point;
import dev.koifih.client.render.Rect;
import dev.koifih.client.util.Maths;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Matrix3x2fc;

public record QuadState(Matrix3x2fc pose, Point a, Point b, Point c, Point d,
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
        Rect rect = new Rect(Maths.min(a.x(), b.x(), c.x(), d.x()), Maths.min(a.y(), b.y(), c.y(), d.y()),
                Maths.max(a.x(), b.x(), c.x(), d.x()), Maths.max(a.y(), b.y(), c.y(), d.y()));
        return Geometry.bounds(rect, pose);
    }
}
