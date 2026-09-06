package dev.koifih.client.event.events;

import dev.koifih.client.render.world.WorldBuffer;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;

public record WorldRenderEvent(ClientLevel level, Camera camera, DeltaTracker deltaTracker,
                               WorldBuffer buffer) implements FrameEvent {
}
