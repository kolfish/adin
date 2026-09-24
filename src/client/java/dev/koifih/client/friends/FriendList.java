package dev.koifih.client.friends;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import dev.koifih.client.AdinClient;
import dev.koifih.client.config.Storage;
import dev.koifih.client.module.impl.misc.Friends;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FriendList {
    private static final String FILE = "friends";
    private static final Map<String, String> friends = new LinkedHashMap<>();
    private static boolean loaded;

    public static List<String> all() {
        load();
        return List.copyOf(friends.values());
    }

    public static boolean contains(String name) {
        load();
        return name != null && friends.containsKey(key(name));
    }

    public static boolean protects(Entity entity) {
        return entity instanceof Player player && AdinClient.MODULES.isEnabled(Friends.class)
                && contains(player.getGameProfile().name());
    }

    public static void add(String name) {
        load();
        friends.put(key(name), name);
        save();
    }

    public static void remove(String name) {
        load();
        if (friends.remove(key(name)) != null) save();
    }

    private static String key(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }

    private static Path path() {
        return Storage.file(FILE);
    }

    private static void load() {
        if (loaded) return;
        loaded = true;
        String[] names = Storage.read(path(), String[].class);
        if (names == null) return;
        for (String name : names) if (name != null && !name.isBlank()) friends.put(key(name), name);
    }

    private static void save() {
        Storage.write(path(), friends.values().toArray(new String[0]));
    }
}
