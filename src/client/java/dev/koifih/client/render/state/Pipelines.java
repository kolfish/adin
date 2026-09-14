package dev.koifih.client.render.state;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
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

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Pipelines {
    public static final VertexFormat SHAPE_FORMAT = VertexFormat.builder(0)
            .addAttribute("Position", GpuFormat.RGB32_FLOAT)
            .addAttribute("Color", GpuFormat.RGBA8_UNORM)
            .addAttribute("UV0", GpuFormat.RG32_FLOAT)
            .addAttribute("UV1", GpuFormat.RG16_SINT)
            .addAttribute("UV2", GpuFormat.RG16_SINT)
            .build();

    private static final int MODE_SOLID = 0;
    private static final int MODE_HUE_BAR = 1;
    private static final int MODE_SATURATION_VALUE = 2;

    public static final RenderPipeline RECT = rect("rect", MODE_SOLID);
    public static final RenderPipeline HUE_BAR = rect("hue_bar", MODE_HUE_BAR);
    public static final RenderPipeline SATURATION_VALUE = rect("saturation_value", MODE_SATURATION_VALUE);
    public static final RenderPipeline SHAPE = shape("screen/shape", "screen/shape", "screen/shape").build();

    public static final RenderPipeline TEXT = builder("gui/text", "gui/text", "gui/text")
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

    private static RenderPipeline rect(String name, int mode) {
        return shape("gui/" + name, "gui/rect", "gui/rect")
                .withShaderDefine("RECT_MODE", mode)
                .build();
    }

    private static RenderPipeline.Builder shape(String name, String vertexShader, String fragmentShader) {
        return builder(name, vertexShader, fragmentShader)
                .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
                .withBindGroupLayout(BindGroupLayouts.PROJECTION)
                .withVertexBinding(0, SHAPE_FORMAT);
    }

    private static RenderPipeline.Builder builder(String name, String vertexShader, String fragmentShader) {
        return RenderPipeline.builder()
                .withLocation(Adin.id("pipeline/" + name))
                .withVertexShader(Adin.id(vertexShader))
                .withFragmentShader(Adin.id(fragmentShader))
                .withPrimitiveTopology(PrimitiveTopology.QUADS)
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withDepthStencilState(Optional.empty())
                .withCull(false);
    }
}
