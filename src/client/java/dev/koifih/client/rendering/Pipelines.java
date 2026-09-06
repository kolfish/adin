package dev.koifih.client.rendering;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.koifih.Adin;
import dev.koifih.client.rendering.font.Fonts;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import java.util.Optional;

public final class Pipelines {
    public static final VertexFormat RECT_FORMAT = VertexFormat.builder(0)
            .addAttribute("Position", GpuFormat.RGB32_FLOAT)
            .addAttribute("Color", GpuFormat.RGBA8_UNORM)
            .addAttribute("UV0", GpuFormat.RG32_FLOAT)
            .addAttribute("UV1", GpuFormat.RG16_SINT)
            .addAttribute("UV2", GpuFormat.RG16_SINT)
            .build();

    public static final RenderPipeline RECT = rect("rect").build();
    public static final RenderPipeline HUE_BAR = rect("hue_bar").build();
    public static final RenderPipeline SATURATION_VALUE = rect("saturation_value").build();
    public static final RenderPipeline QUAD = RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
            .withLocation(Adin.id("pipeline/quad"))
            .withCull(false)
            .build();
    public static final RenderPipeline SHAPE = gui("shape")
            .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
            .withBindGroupLayout(BindGroupLayouts.PROJECTION)
            .withVertexBinding(0, RECT_FORMAT)
            .build();

    public static final RenderPipeline TEXT = gui("text")
            .withShaderDefine("MSDF_RANGE", Fonts.DISTANCE_RANGE)
            .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
            .withBindGroupLayout(BindGroupLayouts.PROJECTION)
            .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
            .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
            .build();

    private Pipelines() {}

    private static RenderPipeline.Builder rect(String fragmentShader) {
        return gui("rect")
                .withLocation(Adin.id("pipeline/" + fragmentShader))
                .withFragmentShader(Adin.id("core/" + fragmentShader))
                .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
                .withBindGroupLayout(BindGroupLayouts.PROJECTION)
                .withVertexBinding(0, RECT_FORMAT);
    }

    private static RenderPipeline.Builder gui(String shader) {
        return RenderPipeline.builder()
                .withLocation(Adin.id("pipeline/" + shader))
                .withVertexShader(Adin.id("core/" + shader))
                .withFragmentShader(Adin.id("core/" + shader))
                .withPrimitiveTopology(PrimitiveTopology.QUADS)
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withDepthStencilState(Optional.empty())
                .withCull(false);
    }
}
