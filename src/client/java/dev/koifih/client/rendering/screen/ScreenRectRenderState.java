package dev.koifih.client.rendering.screen;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.koifih.client.rendering.Pipelines;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Matrix3x2fc;

public record ScreenRectRenderState(Matrix3x2fc pose, ScreenRect rect, RectStyle style,
                                    ScreenRectangle scissorArea) implements GuiElementRenderState {
    private static final float CORNER_FRACTION = 0.25f;

    @Override
    public void buildVertices(VertexConsumer vertices) {
        if (style.hasFill()) ScreenGeometry.fill(vertices, pose, rect, style.fill());
        if (style.edges() == EdgeStyle.CORNERED) {
            float reach = Math.min(rect.width(), rect.height()) * CORNER_FRACTION;
            float cap = (style.outlineWidth() - style.strokeWidth()) * 0.5f;
            if (style.hasOutline()) ScreenGeometry.corners(vertices, pose, rect, style.outlineWidth(), reach + cap, style.outline());
            if (style.hasStroke()) ScreenGeometry.corners(vertices, pose, rect, style.strokeWidth(), reach, style.stroke());
            return;
        }
        if (style.hasOutline()) ScreenGeometry.border(vertices, pose, rect, style.outlineWidth(), style.outline());
        if (style.hasStroke()) ScreenGeometry.border(vertices, pose, rect, style.strokeWidth(), style.stroke());
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
        ScreenRect outer = rect.expand(style.widestStroke() * 0.5f + ScreenGeometry.PADDING);
        return new ScreenRectangle((int) Math.floor(outer.minX()), (int) Math.floor(outer.minY()),
                (int) Math.ceil(outer.width()) + 1, (int) Math.ceil(outer.height()) + 1).transformMaxBounds(pose);
    }
}
