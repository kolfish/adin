package dev.koifih.client.mixin;

import dev.koifih.client.render.entity.EntityFill;
import dev.koifih.client.render.entity.Filled;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public abstract class EntityRenderStateMixin implements Filled {
    @Unique
    private EntityFill adin$fill;

    @Override
    public EntityFill adin$fill() {
        return adin$fill;
    }

    @Override
    public void adin$setFill(EntityFill fill) {
        adin$fill = fill;
    }
}
