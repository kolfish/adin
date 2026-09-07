package dev.koifih.client.event.events;

import dev.koifih.client.event.Event;
import net.minecraft.client.Minecraft;

public record TurnEvent(Minecraft client, float deltaTicks) implements Event {
}
