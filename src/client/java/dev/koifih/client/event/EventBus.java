package dev.koifih.client.event;

import dev.koifih.Adin;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class EventBus {
    private record Handler<E extends Event>(Class<E> type, int priority, Listener<E> listener) {
        void accept(Event event) {
            listener.on(type.cast(event));
        }
    }

    private final Map<Class<? extends Event>, List<Handler<?>>> handlers = new ConcurrentHashMap<>();
    private volatile Map<Class<? extends Event>, Handler<?>[]> dispatch = new ConcurrentHashMap<>();

    public <E extends Event> Subscription subscribe(Class<E> type, Listener<E> listener) {
        return subscribe(type, Priority.NORMAL, listener);
    }

    public <E extends Event> Subscription subscribe(Class<E> type, int priority, Listener<E> listener) {
        Handler<E> handler = new Handler<>(type, priority, listener);
        handlers.computeIfAbsent(type, key -> new CopyOnWriteArrayList<>()).add(handler);
        invalidate();
        return () -> {
            handlers.getOrDefault(type, List.of()).remove(handler);
            invalidate();
        };
    }

    public <E extends Event> E post(E event) {
        for (Handler<?> handler : handlersFor(event.getClass())) {
            if (event instanceof CancellableEvent cancellable && cancellable.isCancelled()) break;
            try {
                handler.accept(event);
            } catch (RuntimeException exception) {
                Adin.LOGGER.error("Listener for {} failed", handler.type().getSimpleName(), exception);
            }
        }
        return event;
    }

    private Handler<?>[] handlersFor(Class<? extends Event> type) {
        return dispatch.computeIfAbsent(type, this::flatten);
    }

    private Handler<?>[] flatten(Class<? extends Event> type) {
        List<Handler<?>> found = new ArrayList<>();
        handlers.forEach((registered, listeners) -> {
            if (registered.isAssignableFrom(type)) found.addAll(listeners);
        });
        found.sort(Comparator.comparingInt((Handler<?> handler) -> handler.priority()).reversed());
        return found.toArray(new Handler<?>[0]);
    }

    private void invalidate() {
        dispatch = new ConcurrentHashMap<>();
    }
}
