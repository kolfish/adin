package dev.koifih.client.event.events;

import dev.koifih.client.event.Event;
import net.minecraft.client.Minecraft;

public record TickEvent(Minecraft client) implements Event {
}
