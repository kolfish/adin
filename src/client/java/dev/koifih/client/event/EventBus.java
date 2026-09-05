package dev.koifih.client.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EventBus {
    private static final Map<Class<? extends Event>, List<Listener<?>>> LISTENERS = new HashMap<>();

    private EventBus() {}

    public static <E extends Event> Subscription subscribe(Class<E> type, Listener<E> listener) {
        LISTENERS.computeIfAbsent(type, key -> new ArrayList<>()).add(listener);
        return () -> {
            List<Listener<?>> listeners = LISTENERS.get(type);
            if (listeners != null) listeners.remove(listener);
        };
    }

    @SuppressWarnings("unchecked")
    public static <E extends Event> void post(E event) {
        List<Listener<?>> listeners = LISTENERS.get(event.getClass());
        if (listeners == null || listeners.isEmpty()) return;
        for (Listener<?> listener : List.copyOf(listeners)) ((Listener<E>) listener).on(event);
    }
}
