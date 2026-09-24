package dev.koifih.client.event;

public abstract class CancellableEvent implements Event {
    private boolean cancelled;

    public void cancel() {
        cancelled = true;
    }

    public boolean isCancelled() {
        return cancelled;
    }
}
