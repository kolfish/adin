package dev.koifih.client.event;

@FunctionalInterface
public interface Subscription {
    void cancel();
}
