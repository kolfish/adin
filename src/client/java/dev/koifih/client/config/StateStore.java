package dev.koifih.client.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import dev.koifih.client.AdinClient;
import dev.koifih.client.event.events.ModuleToggleEvent;
import dev.koifih.client.event.events.TickEvent;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import java.nio.file.Path;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StateStore {
    private static final String FILE = "state";
    private static final long FLUSH_INTERVAL_NANOS = 10_000_000_000L;

    private static boolean ready;
    private static boolean dirty;
    private static long savedAt;

    public static void init() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> load());
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> save());
        AdinClient.EVENTS.subscribe(ModuleToggleEvent.class, event -> dirty = true);
        AdinClient.EVENTS.subscribe(TickEvent.class, event -> flush());
    }

    private static Path path() {
        return Storage.file(FILE);
    }

    public static void load() {
        State state = Storage.read(path(), State.class);
        if (state != null) Snapshot.apply(state, true, true);
        ready = true;
        dirty = false;
        savedAt = System.nanoTime();
    }

    public static void save() {
        if (!ready) return;
        State state = new State();
        Snapshot.capture(state, true, true);
        Storage.write(path(), state);
        dirty = false;
        savedAt = System.nanoTime();
    }

    private static void flush() {
        if (dirty && System.nanoTime() - savedAt >= FLUSH_INTERVAL_NANOS) save();
    }
}
