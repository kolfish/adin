package dev.koifih.client;

import dev.koifih.client.event.EventBus;
import dev.koifih.client.event.events.TickEvent;
import dev.koifih.client.input.Keybinds;
import dev.koifih.client.module.ModuleManager;
import dev.koifih.client.module.impl.combat.AimAssist;
import dev.koifih.client.module.impl.hud.Notifications;
import dev.koifih.client.module.impl.movement.Sprint;
import dev.koifih.client.module.impl.render.esp.Esp;
import dev.koifih.client.render.preview.EntityPreview;
import dev.koifih.client.rotation.RotationManager;
import dev.koifih.client.render.screen.ScreenRenderer;
import dev.koifih.client.render.world.WorldRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public final class AdinClient implements ClientModInitializer {
    public static final EventBus EVENTS = new EventBus();
    public static final ModuleManager MODULES = new ModuleManager();
    public static final RotationManager ROTATIONS = new RotationManager();

    @Override
    public void onInitializeClient() {
        MODULES.register(new AimAssist());
        MODULES.register(new Sprint());
        MODULES.register(new Esp());
        MODULES.register(new Notifications());
        Keybinds.register();
        ROTATIONS.init();
        WorldRenderer.init();
        ScreenRenderer.init();
        EVENTS.subscribe(TickEvent.class, event -> MODULES.tickKeybinds(event.client()));
        ClientTickEvents.END_CLIENT_TICK.register(client -> EVENTS.post(new TickEvent(client)));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> EntityPreview.clear());
    }
}
