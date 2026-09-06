package dev.koifih.client.render;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.koifih.Adin;
import dev.koifih.client.render.font.Fonts;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import java.util.Optional;

public final class Pipelines {
    public static final VertexFormat SHAPE_FORMAT = VertexFormat.builder(0)
            .addAttribute("Position", GpuFormat.RGB32_FLOAT)
            .addAttribute("Color", GpuFormat.RGBA8_UNORM)
            .addAttribute("UV0", GpuFormat.RG32_FLOAT)
            .addAttribute("UV1", GpuFormat.RG16_SINT)
            .addAttribute("UV2", GpuFormat.RG16_SINT)
            .build();

    public static final RenderPipeline RECT = shape("gui/rect", "gui/rect");
    public static final RenderPipeline HUE_BAR = shape("gui/rect", "gui/hue_bar");
    public static final RenderPipeline SATURATION_VALUE = shape("gui/rect", "gui/saturation_value");
    public static final RenderPipeline SHAPE = shape("screen/shape", "screen/shape");

    public static final RenderPipeline TEXT = builder("gui/text", "gui/text")
            .withShaderDefine("MSDF_RANGE", Fonts.DISTANCE_RANGE)
            .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
            .withBindGroupLayout(BindGroupLayouts.PROJECTION)
            .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
            .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
            .build();

    public static final RenderPipeline QUAD = RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
            .withLocation(Adin.id("pipeline/quad"))
            .withCull(false)
            .build();

    private Pipelines() {}

    private static RenderPipeline shape(String vertexShader, String fragmentShader) {
        return builder(vertexShader, fragmentShader)
                .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
                .withBindGroupLayout(BindGroupLayouts.PROJECTION)
                .withVertexBinding(0, SHAPE_FORMAT)
                .build();
    }

    private static RenderPipeline.Builder builder(String vertexShader, String fragmentShader) {
        return RenderPipeline.builder()
                .withLocation(Adin.id("pipeline/" + fragmentShader))
                .withVertexShader(Adin.id(vertexShader))
                .withFragmentShader(Adin.id(fragmentShader))
                .withPrimitiveTopology(PrimitiveTopology.QUADS)
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withDepthStencilState(Optional.empty())
                .withCull(false);
    }
}
