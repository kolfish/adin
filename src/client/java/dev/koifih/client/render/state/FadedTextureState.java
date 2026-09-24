package dev.koifih.client.render.state;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.koifih.client.render.Rect;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Matrix3x2fc;

public record FadedTextureState(Matrix3x2fc pose, TextureSetup textureSetup, float u0, float fade, int color,
                                ScreenRectangle scissorArea) implements GuiElementRenderState {
    @Override
    public void buildVertices(VertexConsumer vertices) {
        int clear = color & 0x00FFFFFF;
        quad(vertices, 0f, fade, clear, color);
        quad(vertices, fade, 1f, color, color);
    }

    private void quad(VertexConsumer vertices, float x0, float x1, int left, int right) {
        float left0 = u(x0);
        float right0 = u(x1);
        vertices.addVertexWith2DPose(pose, x0, 0f).setUv(left0, 0f).setColor(left);
        vertices.addVertexWith2DPose(pose, x0, 1f).setUv(left0, 1f).setColor(left);
        vertices.addVertexWith2DPose(pose, x1, 1f).setUv(right0, 1f).setColor(right);
        vertices.addVertexWith2DPose(pose, x1, 0f).setUv(right0, 0f).setColor(right);
    }

    private float u(float x) {
        return u0 + x * (1f - u0);
    }

    @Override
    public RenderPipeline pipeline() {
        return RenderPipelines.GUI_TEXTURED;
    }

    @Override
    public ScreenRectangle bounds() {
        return Geometry.bounds(new Rect(0f, 0f, 1f, 1f), pose);
    }
}
