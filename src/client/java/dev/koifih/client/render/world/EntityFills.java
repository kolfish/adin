package dev.koifih.client.render.world;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import dev.koifih.Adin;
import dev.koifih.client.render.EntityFill;
import dev.koifih.client.mixin.RenderSetupAccessor;
import dev.koifih.client.mixin.RenderTypeAccessor;
import dev.koifih.client.mixin.TextureBindingAccessor;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class EntityFills {
    public static final int EFFECTS = 6;

    private static final String TEXTURE = "Sampler0";
    private static final RenderPipeline[] OCCLUDED = pipelines("occluded", ColorTargetState.DEFAULT,
            new DepthStencilState(CompareOp.LESS_THAN, false));
    private static final RenderPipeline[] VISIBLE = pipelines("visible", new ColorTargetState(BlendFunction.TRANSLUCENT),
            DepthStencilState.DEFAULT);
    private static final Set<RenderPipeline> ALL = new HashSet<>();
    private static final Map<Key, RenderType> VISIBLE_TYPES = new HashMap<>();
    private static final Map<Key, RenderType> OCCLUDED_TYPES = new HashMap<>();

    private static EntityFill hand;

    private record Key(Identifier texture, int effect) {}

    static {
        for (RenderPipeline pipeline : OCCLUDED) ALL.add(pipeline);
        for (RenderPipeline pipeline : VISIBLE) ALL.add(pipeline);
    }

    private EntityFills() {}

    public static void beginHand(EntityFill fill) {
        hand = fill;
    }

    public static void endHand() {
        hand = null;
    }

    public static EntityFill hand() {
        return hand;
    }

    public static RenderType visible(Identifier texture, int effect) {
        return VISIBLE_TYPES.computeIfAbsent(new Key(texture, effect), key -> create("visible", VISIBLE[key.effect()], key));
    }

    public static RenderType occluded(Identifier texture, int effect) {
        return OCCLUDED_TYPES.computeIfAbsent(new Key(texture, effect), key -> create("occluded", OCCLUDED[key.effect()], key));
    }

    public static boolean isFill(RenderType type) {
        return ALL.contains(type.pipeline());
    }

    public static Identifier texture(RenderType type) {
        RenderSetup setup = ((RenderTypeAccessor) type).adin$getSetup();
        Object binding = ((RenderSetupAccessor) (Object) setup).adin$getTextures().get(TEXTURE);
        return binding == null ? null : ((TextureBindingAccessor) binding).adin$getLocation();
    }

    private static RenderType create(String name, RenderPipeline pipeline, Key key) {
        return RenderType.create(Adin.MOD_ID + ":entity_fill_" + name + key.effect() + "/" + key.texture(),
                RenderSetup.builder(pipeline).withTexture(TEXTURE, key.texture()).createRenderSetup());
    }

    private static RenderPipeline[] pipelines(String name, ColorTargetState color, DepthStencilState depth) {
        RenderPipeline[] pipelines = new RenderPipeline[EFFECTS + 1];
        for (int effect = 0; effect < pipelines.length; effect++) {
            pipelines[effect] = RenderPipeline.builder()
                    .withLocation(Adin.id("pipeline/entity_fill_" + name + effect))
                    .withVertexShader(Adin.id("world/entity_fill"))
                    .withFragmentShader(Adin.id("world/entity_fill"))
                    .withShaderDefine("EFFECT", effect)
                    .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
                    .withBindGroupLayout(BindGroupLayouts.PROJECTION)
                    .withBindGroupLayout(BindGroupLayouts.GLOBALS)
                    .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
                    .withVertexBinding(0, DefaultVertexFormat.ENTITY)
                    .withPrimitiveTopology(PrimitiveTopology.QUADS)
                    .withColorTargetState(color)
                    .withDepthStencilState(depth)
                    .withCull(false)
                    .build();
        }
        return pipelines;
    }
}
