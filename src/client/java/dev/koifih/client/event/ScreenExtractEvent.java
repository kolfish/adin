package dev.koifih.client.event;

import dev.koifih.client.rendering.screen.OverlayCollector;
import dev.koifih.client.rendering.screen.W2S;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;

public record ScreenExtractEvent(ClientLevel level, Camera camera, DeltaTracker deltaTracker, W2S w2s,
                                 OverlayCollector shapes) implements LevelFrameEvent {
}
