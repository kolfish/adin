package dev.koifih.client.render.state;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.koifih.client.render.Edges;
import dev.koifih.client.render.Rect;
import dev.koifih.client.render.Style;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Matrix3x2fc;

public record ShapeState(Matrix3x2fc pose, Rect rect, Style style,
                                    ScreenRectangle scissorArea) implements GuiElementRenderState {
    private static final float CORNER_FRACTION = 0.25f;

    @Override
    public void buildVertices(VertexConsumer vertices) {
        if (style.hasFill()) Geometry.fill(vertices, pose, rect, style.fill());
        if (style.edges() == Edges.CORNERED) {
            float reach = Math.min(rect.width(), rect.height()) * CORNER_FRACTION;
            float cap = (style.outlineWidth() - style.strokeWidth()) * 0.5f;
            if (style.hasOutline()) Geometry.corners(vertices, pose, rect, style.outlineWidth(), reach + cap, style.outline());
            if (style.hasStroke()) Geometry.corners(vertices, pose, rect, style.strokeWidth(), reach, style.stroke());
            return;
        }
        if (style.hasOutline()) Geometry.border(vertices, pose, rect, style.outlineWidth(), style.outline());
        if (style.hasStroke()) Geometry.border(vertices, pose, rect, style.strokeWidth(), style.stroke());
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
        return Geometry.bounds(rect.expand(style.widestStroke() * 0.5f), pose);
    }
}
