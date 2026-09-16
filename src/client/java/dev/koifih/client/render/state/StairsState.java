package dev.koifih.client.render.state;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.koifih.client.render.Scissor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Matrix3x2fc;

public record StairsState(Matrix3x2fc pose, Row row, int color, ScreenRectangle scissorArea) implements GuiElementRenderState {
    public static final int FLUSH_INNER = 1;
    public static final int FLUSH_TOP = 2;
    public static final int NONE = -1;
    private static final int PACK = 4096;
    private static final int BIAS = 2048;
    private static final int ANTIALIAS_PADDING = 1;

    public record Row(int innerX, int y, int width, int height, int above, int below, int radius, int edges,
                      boolean mirrored, boolean first, boolean last) {}

    public StairsState(Matrix3x2fc pose, Row row, int color) {
        this(pose, row, color, Scissor.current());
    }

    @Override
    public void buildVertices(VertexConsumer vertices) {
        float left = (row.edges() & FLUSH_INNER) != 0 ? 0 : -ANTIALIAS_PADDING;
        float right = Math.max(row.width(), Math.max(row.above(), row.below())) + row.radius() + ANTIALIAS_PADDING;
        float top = row.first() ? -ANTIALIAS_PADDING : 0;
        float bottom = row.last() ? row.height() + ANTIALIAS_PADDING : row.height();
        vertex(vertices, left, top);
        vertex(vertices, left, bottom);
        vertex(vertices, right, bottom);
        vertex(vertices, right, top);
    }

    private void vertex(VertexConsumer vertices, float localX, float localY) {
        float screenX = row.mirrored() ? row.innerX() - localX : row.innerX() + localX;
        vertices.addVertexWith2DPose(pose, screenX, row.y() + localY)
                .setColor(color)
                .setUv(localX, localY)
                .setUv1(row.radius(), row.edges())
                .setUv2(row.width(), row.height())
                .setLineWidth((float) (row.above() + BIAS) * PACK + row.below() + BIAS);
    }

    @Override
    public RenderPipeline pipeline() {
        return Pipelines.STAIRS;
    }

    @Override
    public TextureSetup textureSetup() {
        return TextureSetup.noTexture();
    }

    @Override
    public ScreenRectangle bounds() {
        int reach = Math.max(row.width(), Math.max(row.above(), row.below())) + row.radius() + ANTIALIAS_PADDING;
        int x = row.mirrored() ? row.innerX() - reach : row.innerX() - ANTIALIAS_PADDING;
        return new ScreenRectangle(x, row.y() - ANTIALIAS_PADDING, reach + ANTIALIAS_PADDING, row.height() + 2 * ANTIALIAS_PADDING)
                .transformMaxBounds(pose);
    }
}
