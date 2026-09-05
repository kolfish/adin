package dev.koifih.client.event;

import net.minecraft.client.Minecraft;

public record TickEvent(Minecraft client) implements Event {
}
