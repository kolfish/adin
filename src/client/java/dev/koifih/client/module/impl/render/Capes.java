package dev.koifih.client.module.impl.render;

import dev.koifih.client.event.events.EntityRenderStateEvent;
import dev.koifih.client.module.Module;
import dev.koifih.client.render.cape.Cape;
import dev.koifih.client.render.cape.CapeCatalog;
import dev.koifih.client.setting.CapeSetting;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;

public final class Capes extends Module {
    private final CapeSetting cape = add(new CapeSetting());

    public Capes() {
        super("capes");
    }

    public Cape selected() {
        return CapeCatalog.get(cape.get());
    }

    @Override
    protected void onEnable() {
        listen(EntityRenderStateEvent.class, this::onRenderState);
    }

    private void onRenderState(EntityRenderStateEvent event) {
        if (!(event.state() instanceof AvatarRenderState avatar) || event.entity() != mc.player) return;
        Cape selected = selected();
        selected.update();
        avatar.skin = selected.apply(avatar.skin);
        avatar.showCape = true;
    }
}
