package dev.koifih.client.event;

@FunctionalInterface
public interface Listener<E extends Event> {
    void on(E event);
}
