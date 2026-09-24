package dev.koifih.client.event.events;

import dev.koifih.client.render.screen.Projection;
import dev.koifih.client.render.screen.ScreenBuffer;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;

public record ScreenRenderEvent(ClientLevel level, Camera camera, DeltaTracker deltaTracker, Projection projection,
                                ScreenBuffer buffer) implements FrameEvent {
}
