package dev.koifih.client.render.glass;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import dev.koifih.Adin;
import dev.koifih.client.render.post.KawaseBlur;
import dev.koifih.client.util.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.lwjgl.system.MemoryStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GlassRenderer {
    public static final int REGULAR = 0;
    public static final int CLEAR = 1;

    private static final int CONFIG_SIZE = 64;
    private static final float BLUR = 1.5f;
    private static final float BEZEL = 14f;
    private static final float SHADOW_REACH = 0.8f;
    private static final float SHADOW_DROP = 1.5f;
    private static final BindGroupLayout SAMPLERS = BindGroupLayout.builder()
            .withSampler("SharpSampler")
            .withSampler("BlurSampler")
            .build();
    private static final BindGroupLayout CONFIG = BindGroupLayout.builder()
            .withUniform("GlassConfig", UniformType.UNIFORM_BUFFER)
            .build();
    private static final RenderPipeline PIPELINE = RenderPipeline.builder()
            .withLocation(Adin.id("pipeline/post/glass"))
            .withVertexShader(Adin.id("post/glass"))
            .withFragmentShader(Adin.id("post/glass"))
            .withBindGroupLayout(SAMPLERS)
            .withBindGroupLayout(CONFIG)
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT_PREMULTIPLIED_ALPHA))
            .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
            .build();
    private static final KawaseBlur BLUR_PASS = new KawaseBlur("glass");
    private static final List<Pane> PANES = new ArrayList<>();
    private static final List<GpuBuffer> CONFIGS = new ArrayList<>();

    private static GpuTexture sharp;
    private static GpuTextureView sharpView;

    public record Pane(float x, float y, float width, float height, float radius, int variant, int tint, float lightX,
                       float lightY, float energy, float opacity, ScreenRectangle clip) {
        float lift() {
            return Math.min(Math.min(width, height) * 0.5f, BEZEL);
        }
    }

    private record Region(int x0, int y0, int x1, int y1) {
        static Region of(Pane pane, float scale, int width, int height) {
            float pad = pane.lift() * SHADOW_REACH + SHADOW_DROP + 1f;
            float left = pane.x() - pad;
            float top = pane.y() - pad;
            float right = pane.x() + pane.width() + pad;
            float bottom = pane.y() + pane.height() + pad + SHADOW_DROP;
            ScreenRectangle clip = pane.clip();
            if (clip != null) {
                left = Math.max(left, clip.left());
                top = Math.max(top, clip.top());
                right = Math.min(right, clip.left() + clip.width());
                bottom = Math.min(bottom, clip.top() + clip.height());
            }
            int x0 = Math.clamp((int) Math.floor(left * scale), 0, width);
            int x1 = Math.clamp((int) Math.ceil(right * scale), 0, width);
            int y0 = Math.clamp(height - (int) Math.ceil(bottom * scale), 0, height);
            int y1 = Math.clamp(height - (int) Math.floor(top * scale), 0, height);
            return new Region(x0, y0, x1, y1);
        }

        boolean isEmpty() {
            return x1 <= x0 || y1 <= y0;
        }

        Region union(Region other) {
            return new Region(Math.min(x0, other.x0), Math.min(y0, other.y0), Math.max(x1, other.x1), Math.max(y1, other.y1));
        }

        ScreenRectangle halfPadded(int pad, int width, int height) {
            int halfWidth = (width + 1) / 2;
            int halfHeight = (height + 1) / 2;
            int left = Math.max(0, (x0 - pad) / 2);
            int bottom = Math.max(0, (y0 - pad) / 2);
            int right = Math.min(halfWidth, (x1 + pad + 1) / 2);
            int top = Math.min(halfHeight, (y1 + pad + 1) / 2);
            return new ScreenRectangle(left, bottom, right - left, top - bottom);
        }
    }

    public static void submit(Pane pane) {
        if (pane.width() > 0f && pane.height() > 0f && pane.opacity() > 0f) PANES.add(pane);
    }

    public static void render() {
        if (PANES.isEmpty()) return;
        RenderSystem.assertOnRenderThread();
        try {
            draw();
        } finally {
            PANES.clear();
        }
    }

    private static void draw() {
        Minecraft minecraft = Minecraft.getInstance();
        RenderTarget main = minecraft.gameRenderer.mainRenderTarget();
        GpuTexture screen = main.getColorTexture();
        int width = screen.getWidth(0);
        int height = screen.getHeight(0);
        float scale = minecraft.getWindow().getGuiScale();
        List<Region> regions = new ArrayList<>(PANES.size());
        Region union = null;
        boolean frosted = false;
        for (Pane pane : PANES) {
            Region region = Region.of(pane, scale, width, height);
            regions.add(region);
            if (region.isEmpty()) continue;
            union = union == null ? region : union.union(region);
            frosted |= pane.variant() != CLEAR;
        }
        if (union == null) return;
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        capture(encoder, screen, width, height);
        float blur = BLUR * scale;
        GpuTextureView blurred = frosted
                ? BLUR_PASS.blur(encoder, sharpView, width, height, blur, union.halfPadded(KawaseBlur.reach(blur), width, height))
                : sharpView;
        GpuSampler sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
        for (int i = 0; i < PANES.size(); i++) {
            Region region = regions.get(i);
            if (region.isEmpty()) continue;
            GpuBuffer config = config(encoder, i, PANES.get(i), scale);
            try (RenderPass pass = encoder.createRenderPass(() -> "adin glass", main.getColorTextureView(),
                    Optional.empty(), null, OptionalDouble.empty())) {
                pass.setPipeline(PIPELINE);
                pass.enableScissor(region.x0(), region.y0(), region.x1() - region.x0(), region.y1() - region.y0());
                RenderSystem.bindDefaultUniforms(pass);
                pass.bindTexture("SharpSampler", sharpView, sampler);
                pass.bindTexture("BlurSampler", blurred, sampler);
                pass.setUniform("GlassConfig", config.slice());
                pass.draw(3, 1, 0, 0);
            }
        }
    }

    private static void capture(CommandEncoder encoder, GpuTexture screen, int width, int height) {
        if (sharp == null || sharp.getWidth(0) != width || sharp.getHeight(0) != height || sharp.getFormat() != screen.getFormat()) {
            if (sharpView != null) sharpView.close();
            if (sharp != null) sharp.close();
            sharp = RenderSystem.getDevice().createTexture("adin glass backdrop",
                    GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING, screen.getFormat(), width, height, 1, 1);
            sharpView = RenderSystem.getDevice().createTextureView(sharp);
        }
        encoder.copyTextureToTexture(screen, sharp, 0, 0, 0, 0, 0, width, height);
    }

    private static GpuBuffer config(CommandEncoder encoder, int index, Pane pane, float scale) {
        while (CONFIGS.size() <= index) {
            int slot = CONFIGS.size();
            CONFIGS.add(RenderSystem.getDevice().createBuffer(() -> "adin glass config " + slot,
                    GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, CONFIG_SIZE));
        }
        GpuBuffer buffer = CONFIGS.get(index);
        int tint = pane.tint();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            encoder.writeToBuffer(buffer.slice(), Std140Builder.onStack(stack, CONFIG_SIZE)
                    .putVec4(pane.x() * scale, pane.y() * scale, pane.width() * scale, pane.height() * scale)
                    .putVec4(pane.lightX(), pane.lightY(), pane.energy(), scale)
                    .putVec4(Colors.red(tint) / 255f, Colors.green(tint) / 255f, Colors.blue(tint) / 255f, Colors.alpha(tint) / 255f)
                    .putVec4(pane.variant(), pane.opacity(), pane.radius(), 0f)
                    .get());
        }
        return buffer;
    }
}
