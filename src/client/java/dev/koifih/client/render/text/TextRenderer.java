package dev.koifih.client.render.text;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import dev.koifih.client.mixin.GuiGraphicsExtractorAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2f;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Single-line Comfortaa Bold MSDF rendering. Sizes and baselines use GUI pixels. */
public final class TextRenderer {
    private static final MsdfFont FONT = MsdfFont.COMFORTAA_BOLD;
    private static final Identifier ATLAS = Identifier.fromNamespaceAndPath("adin", "textures/font/comfortaa-bold.png");
    static final RenderPipeline PIPELINE = RenderPipeline.builder()
            .withLocation(Identifier.fromNamespaceAndPath("adin", "pipeline/text"))
            .withVertexShader(Identifier.fromNamespaceAndPath("adin", "core/text"))
            .withFragmentShader(Identifier.fromNamespaceAndPath("adin", "core/text"))
            .withShaderDefine("MSDF_RANGE", FONT.atlas.distanceRange())
            .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
            .withBindGroupLayout(BindGroupLayouts.PROJECTION)
            .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
            .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(Optional.empty())
            .withCull(false)
            .build();

    public record Span(String text, int color) {}

    private TextRenderer() {}

    public static float width(String text, float size) {
        float advance = 0;
        int previous = -1;
        for (int codepoint : text.codePoints().toArray()) {
            var glyph = FONT.glyph(codepoint);
            advance += FONT.kerning(previous, glyph.unicode()) + glyph.advance();
            previous = glyph.unicode();
        }
        return advance * size;
    }

    /** Centers the actual glyph bounds vertically, rather than the font's line box. */
    public static float centeredBaseline(String text, float size, float centerY) {
        float top = Float.POSITIVE_INFINITY;
        float bottom = Float.NEGATIVE_INFINITY;
        for (int codepoint : text.codePoints().toArray()) {
            var bounds = FONT.glyph(codepoint).planeBounds();
            if (bounds != null) {
                top = Math.min(top, bounds.top());
                bottom = Math.max(bottom, bounds.bottom());
            }
        }
        return Float.isFinite(top) ? centerY - (top + bottom) * size * 0.5f : centerY;
    }

    public static void draw(GuiGraphicsExtractor graphics, String text, float x, float baseline,
                            float size, int color) {
        draw(graphics, List.of(new Span(text, color)), x, baseline, size);
    }

    /** Spans share the same cursor and kerning, including across color changes. */
    public static void draw(GuiGraphicsExtractor graphics, List<Span> spans, float x, float baseline, float size) {
        if (!Float.isFinite(size) || size <= 0) return;
        var quads = new ArrayList<TextRenderState.Quad>();
        float cursor = x;
        int previous = -1;
        float left = Float.POSITIVE_INFINITY, top = Float.POSITIVE_INFINITY;
        float right = Float.NEGATIVE_INFINITY, bottom = Float.NEGATIVE_INFINITY;
        for (Span span : spans) {
            for (int codepoint : span.text.codePoints().toArray()) {
                var glyph = FONT.glyph(codepoint);
                cursor += FONT.kerning(previous, glyph.unicode()) * size;
                var p = glyph.planeBounds();
                var a = glyph.atlasBounds();
                if (p != null && a != null && (span.color >>> 24) != 0) {
                    float x0 = cursor + p.left() * size, y0 = baseline + p.top() * size;
                    float x1 = cursor + p.right() * size, y1 = baseline + p.bottom() * size;
                    quads.add(new TextRenderState.Quad(x0, y0, x1, y1,
                            a.left() / FONT.atlas.width(), a.top() / FONT.atlas.height(),
                            a.right() / FONT.atlas.width(), a.bottom() / FONT.atlas.height(), span.color));
                    left = Math.min(left, x0); top = Math.min(top, y0);
                    right = Math.max(right, x1); bottom = Math.max(bottom, y1);
                }
                cursor += glyph.advance() * size;
                previous = glyph.unicode();
            }
        }
        if (quads.isEmpty()) return;
        var pose = new Matrix3x2f(graphics.pose());
        var bounds = new ScreenRectangle((int) Math.floor(left), (int) Math.floor(top),
                (int) Math.ceil(right) - (int) Math.floor(left),
                (int) Math.ceil(bottom) - (int) Math.floor(top)).transformMaxBounds(pose);
        // TextureManager reloads/releases the atlas; SamplerCache owns the linear sampler.
        var texture = Minecraft.getInstance().getTextureManager().getTexture(ATLAS);
        var setup = TextureSetup.singleTexture(texture.getTextureView(),
                RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
        ((GuiGraphicsExtractorAccessor) graphics).adin$getGuiRenderState()
                .addGuiElement(new TextRenderState(pose, setup, List.copyOf(quads), bounds));
    }
}
