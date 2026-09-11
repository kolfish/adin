package dev.koifih.client.mixin.render;

import dev.koifih.client.AdinClient;
import dev.koifih.client.event.events.HandRenderEvent;
import dev.koifih.client.render.entity.EntityFills;
import dev.koifih.client.render.entity.EntityOutlines;
import dev.koifih.client.render.entity.HandBounds;
import net.minecraft.client.renderer.ItemInHandRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @Inject(method = "submitHandsWithItems", at = @At("HEAD"))
    private void adin$beginHand(CallbackInfo info) {
        EntityFills.beginHand(AdinClient.EVENTS.post(new HandRenderEvent()).fill());
        if (EntityOutlines.hand() != null) HandBounds.begin();
    }

    @Inject(method = "submitHandsWithItems", at = @At("RETURN"))
    private void adin$endHand(CallbackInfo info) {
        if (EntityOutlines.hand() != null) HandBounds.end(EntityOutlines.hand());
        EntityFills.endHand();
    }
}
