package dev.koifih.client.friends;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.koifih.Adin;
import dev.koifih.client.AdinClient;
import dev.koifih.client.module.impl.misc.Friends;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FriendList {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
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
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve(Adin.MOD_ID).resolve("friends.json");
    }

    private static void load() {
        if (loaded) return;
        loaded = true;
        if (!Files.exists(path())) return;
        try (Reader reader = Files.newBufferedReader(path())) {
            String[] names = GSON.fromJson(reader, String[].class);
            if (names != null) for (String name : names) friends.put(key(name), name);
        } catch (IOException | RuntimeException exception) {
            Adin.LOGGER.error("Cannot read friends", exception);
        }
    }

    private static void save() {
        try {
            Files.createDirectories(path().getParent());
            try (Writer writer = Files.newBufferedWriter(path())) {
                GSON.toJson(friends.values().toArray(new String[0]), writer);
            }
        } catch (IOException exception) {
            Adin.LOGGER.error("Cannot save friends", exception);
        }
    }
}
