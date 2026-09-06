package dev.koifih.client.event;

import dev.koifih.client.rendering.world.ShapeCollector;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;

public record WorldExtractEvent(ClientLevel level, Camera camera, DeltaTracker deltaTracker,
                                ShapeCollector shapes) implements LevelFrameEvent {
}
