package dev.koifih.client.event.events;

import dev.koifih.client.event.Event;
import dev.koifih.client.module.Module;

public record ModuleToggleEvent(Module module, boolean enabled) implements Event {
}
