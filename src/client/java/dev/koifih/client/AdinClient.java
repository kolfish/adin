package dev.koifih.client;

import dev.koifih.client.event.EventBus;
import dev.koifih.client.event.TickEvent;
import dev.koifih.client.feature.FeatureManager;
import dev.koifih.client.keybind.Keybinds;
import dev.koifih.client.rendering.screen.OverlayRenderer;
import dev.koifih.client.rendering.world.WorldRenderer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.api.ClientModInitializer;

public final class AdinClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Keybinds.register();
        FeatureManager.init();
        WorldRenderer.init();
        OverlayRenderer.init();
        ClientTickEvents.END_CLIENT_TICK.register(client -> EventBus.post(new TickEvent(client)));
    }
}
