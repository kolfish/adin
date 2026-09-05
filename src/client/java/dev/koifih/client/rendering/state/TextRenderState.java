package dev.koifih.client.rendering.state;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.koifih.client.rendering.Pipelines;
import dev.koifih.client.rendering.Scissor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import java.util.List;

public record TextRenderState(Matrix3x2fc pose, TextureSetup textureSetup, List<Quad> quads,
                              ScreenRectangle bounds, ScreenRectangle scissorArea) implements GuiElementRenderState {
    public record Quad(float left, float top, float right, float bottom,
                       float u0, float v0, float u1, float v1, int leftColor, int rightColor) {
        public Quad(float left, float top, float right, float bottom,
                    float u0, float v0, float u1, float v1, int color) {
            this(left, top, right, bottom, u0, v0, u1, v1, color, color);
        }
    }

    public static TextRenderState of(GuiGraphicsExtractor graphics, Identifier atlas, List<Quad> quads) {
        float left = Float.POSITIVE_INFINITY, top = Float.POSITIVE_INFINITY;
        float right = Float.NEGATIVE_INFINITY, bottom = Float.NEGATIVE_INFINITY;
        for (Quad quad : quads) {
            left = Math.min(left, quad.left);
            top = Math.min(top, quad.top);
            right = Math.max(right, quad.right);
            bottom = Math.max(bottom, quad.bottom);
        }
        var pose = new Matrix3x2f(graphics.pose());
        var bounds = new ScreenRectangle((int) Math.floor(left), (int) Math.floor(top),
                (int) Math.ceil(right) - (int) Math.floor(left),
                (int) Math.ceil(bottom) - (int) Math.floor(top)).transformMaxBounds(pose);
        var texture = Minecraft.getInstance().getTextureManager().getTexture(atlas);
        var textureSetup = TextureSetup.singleTexture(texture.getTextureView(),
                RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
        return new TextRenderState(pose, textureSetup, List.copyOf(quads), bounds, Scissor.current());
    }

    @Override
    public void buildVertices(VertexConsumer vertices) {
        for (Quad quad : quads) {
            vertex(vertices, quad.left, quad.top, quad.u0, quad.v0, quad.leftColor);
            vertex(vertices, quad.left, quad.bottom, quad.u0, quad.v1, quad.leftColor);
            vertex(vertices, quad.right, quad.bottom, quad.u1, quad.v1, quad.rightColor);
            vertex(vertices, quad.right, quad.top, quad.u1, quad.v0, quad.rightColor);
        }
    }

    private void vertex(VertexConsumer vertices, float x, float y, float u, float v, int color) {
        vertices.addVertexWith2DPose(pose, x, y).setUv(u, v).setColor(color);
    }

    @Override
    public RenderPipeline pipeline() {
        return Pipelines.TEXT;
    }
}
