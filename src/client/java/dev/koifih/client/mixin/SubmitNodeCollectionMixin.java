package dev.koifih.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import dev.koifih.client.render.entity.EntityFill;
import dev.koifih.client.render.entity.EntityFills;
import dev.koifih.client.render.entity.Filled;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.List;

@Mixin(SubmitNodeCollection.class)
public abstract class SubmitNodeCollectionMixin {
    @Inject(method = "submitModel", at = @At("HEAD"), cancellable = true)
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void adin$fillModel(Model model, Object state, PoseStack pose, RenderType type, int light, int overlay, int color,
                                TextureAtlasSprite sprite, int outline, ModelFeatureRenderer.CrumblingOverlay crumbling, CallbackInfo info) {
        boolean hand = !(state instanceof Filled);
        EntityFill fill = hand ? adin$hand(pose) : ((Filled) state).adin$fill();
        if (fill == null || EntityFills.isFill(type)) return;
        Identifier texture = EntityFills.texture(type);
        if (texture == null) return;
        SubmitNodeCollection collection = (SubmitNodeCollection) (Object) this;
        if (fill.occluded() != 0) {
            collection.submitModel(model, state, pose, EntityFills.occluded(texture, fill.effect()), fill.light(), fill.occludedOverlay(),
                    fill.occluded(), sprite, 0, crumbling);
        }
        RenderType visible = hand ? EntityFills.visibleHand(texture, fill.effect()) : EntityFills.visible(texture, fill.effect());
        collection.submitModel(model, state, pose, visible, fill.light(), fill.visibleOverlay(), fill.visible(), sprite, 0, crumbling);
        info.cancel();
    }

    @Inject(method = "submitItem", at = @At("HEAD"), cancellable = true)
    private void adin$fillItem(PoseStack pose, ItemDisplayContext context, int light, int overlay, int outline, int[] tints,
                               List<BakedQuad> quads, ItemStackRenderState.FoilType foil, CallbackInfo info) {
        EntityFill fill = adin$hand(pose);
        if (fill == null || quads.isEmpty()) return;
        Identifier texture = EntityFills.texture(quads.getFirst().materialInfo().itemRenderType());
        if (texture == null) return;
        QuadInstance instance = new QuadInstance();
        instance.setColor(fill.visible());
        instance.setLightCoords(fill.light());
        instance.setOverlayCoords(fill.visibleOverlay());
        SubmitNodeCollection collection = (SubmitNodeCollection) (Object) this;
        collection.submitCustomGeometry(pose, EntityFills.visibleHand(texture, fill.effect()), (entry, consumer) -> {
            for (BakedQuad quad : quads) consumer.putBakedQuad(entry, quad, instance);
        });
        info.cancel();
    }

    /**
     * The hand has no entity position, so anchor its pattern at the pose that placed it. That is the same matrix the
     * vertices went through, so the origin is in their frame by construction and the pattern follows swings and bob.
     */
    @Unique
    private static EntityFill adin$hand(PoseStack pose) {
        EntityFill fill = EntityFills.hand();
        if (fill == null) return null;
        Vector3f pivot = pose.last().pose().getTranslation(new Vector3f());
        return fill.anchored(new Vec3(pivot));
    }
}
