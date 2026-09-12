package dev.koifih.client.event.events;

import dev.koifih.client.event.Event;
import net.minecraft.client.Minecraft;

public record MouseMoveEvent(Minecraft client, double x, double y) implements Event {
}
